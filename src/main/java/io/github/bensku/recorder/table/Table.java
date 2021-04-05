package io.github.bensku.recorder.table;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;

import io.github.bensku.recorder.codegen.JavaType;
import io.github.bensku.recorder.record.PrimaryKey;
import io.github.bensku.recorder.record.RecordFormatException;
import io.github.bensku.recorder.record.TableName;

/**
 * A database table that represents a collection of records.
 *
 */
public record Table(
		
		/**
		 * Record that this table represents.
		 */
		JavaType record,
		
		/**
		 * Name of the table.
		 */
		String name,
		
		/**
		 * Table columns.
		 */
		List<Column> columns,
		
		/**
		 * Primary key column. It is also contained in {@link #columns()}.
		 * Can be null if the table doesn't have a primary key.
		 */
		Column primaryKey
) {
	
	private static final JavaType PRIMARY_KEY = JavaType.fromClass(PrimaryKey.class);
	
	public Table {
		// Validate that the table is not totally nonsensical
		long primaryKeys = columns.stream()
				.filter(column -> column.type().equals(PRIMARY_KEY))
				.count();
		if (primaryKeys > 1) {
			throw new RecordFormatException("only one primary key allowed");
		}
	}
	
	/**
	 * Creates a table by using reflection to look at already loaded class.
	 * @param type Class of the record type.
	 * @return Table definition.
	 */
	public static Table fromClass(Class<? extends Record> type) {
		TableName annotation = type.getAnnotation(TableName.class);
		String tableName = annotation != null ? annotation.value() : type.getSimpleName();
		
		// Create columns and figure out primary key (if it exists)
		RecordComponent[] components = type.getRecordComponents();
		List<Column> columns = new ArrayList<>(components.length);
		Column primaryKey = null;
		for (RecordComponent component : components) {
			Column column = Column.fromComponent(component);
			columns.add(column);
			if (component.getType().equals(PrimaryKey.class)) {
				primaryKey = column;
			}
		}
		
		return new Table(JavaType.fromClass(type), tableName, columns, primaryKey);
	}
	
	/**
	 * Creates a table by parsing a class bytecode.
	 * @param internalName Full internal name of the class.
	 * @param code Full bytecode of the class.
	 * @return Table definition.
	 */
	public static Table fromBytecode(String internalName, byte[] code) {
		List<Column> columns = new ArrayList<>();
		// TableName is the simple name by default
		String className = internalName.substring(internalName.lastIndexOf('/') + 1);
		AtomicReference<String> tableName = new AtomicReference<>(className
				.substring(className.lastIndexOf('$') + 1)); // Strip parent class
		
		AnnotationVisitor nameVisitor = new AnnotationVisitor(Opcodes.ASM9) {
			@Override
			public void visit(String name, Object value) {
				// TableName has only one value, which is always a string
				tableName.setPlain((String) value);
			}
		};
		ClassVisitor cv = new ClassVisitor(Opcodes.ASM9) {
			@Override
			public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
				if (Type.getType(descriptor).equals(Type.getType(TableName.class))) {
					return nameVisitor;
				}
				return null; // Not interested about this annotation
			}
			
			@Override
			public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
				// Use signature for generic types and descriptor for others
				JavaType type = signature != null ? JavaType.fromSignature(signature)
						: JavaType.fromDescriptor(descriptor);
				columns.add(new Column(name, type));
				return null; // Not interested in annotations or other attributes
			}
		};
		new ClassReader(code).accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
		
		// Search for a primary key
		Column primaryKey = columns.stream()
				.filter(column -> column.type().equals(PRIMARY_KEY))
				.findAny().orElse(null);
		return new Table(JavaType.fromName(internalName.replace('/', '.')),
				tableName.getPlain(), columns, primaryKey);
	}
}