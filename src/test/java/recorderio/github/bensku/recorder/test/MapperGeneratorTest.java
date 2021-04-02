package recorderio.github.bensku.recorder.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.bensku.recorder.codegen.MapperGenerator;
import io.github.bensku.recorder.codegen.RecordMapper;
import io.github.bensku.recorder.table.Column;
import io.github.bensku.recorder.table.JavaType;
import io.github.bensku.recorder.table.Table;
import io.github.bensku.recorder.table.TableSource;
import io.github.bensku.recorder.table.constraint.Constraint;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class MapperGeneratorTest {

	public static class FakeTableSource implements TableSource {

		public Table table;
		
		@Override
		public Table get(JavaType type) {
			return table;
		}
		
	}
	
	private FakeTableSource tableSource;
	private MapperGenerator generator;
	
	@Mock
	private ResultSet results;
	
	@Mock
	private PreparedStatement statement;
	
	@BeforeAll
	public void initGenerator() {
		tableSource = new FakeTableSource();
		generator = new MapperGenerator(tableSource);
	}
	
	public record EmptyRecord() {}
		
	@Test
	public void emptyTable() {
		tableSource.table = new Table(JavaType.of(EmptyRecord.class), "empty_records", new Column[0], null);
		@SuppressWarnings("unchecked")
		RecordMapper<EmptyRecord> mapper = (RecordMapper<EmptyRecord>) generator.create(JavaType.of(EmptyRecord.class));
		mapper.read(results);
		mapper.write(statement, new EmptyRecord());
		Mockito.verifyNoInteractions(results, statement);
	}
	
	public record AllTypes(boolean bool, byte b, short s, int i, long l, float f, double d, Object o, String str) {}
	
	private Column column(String name, Class<?> type) {
		return new Column(name, JavaType.of(type), new Constraint[0]);
	}
	
	@Test
	public void basicTable() throws SQLException {
		tableSource.table = new Table(JavaType.of(AllTypes.class), "all_types", new Column[] {
				column("bool", boolean.class), column("b", byte.class), column("s", short.class), column("i", int.class), column("l", long.class),
				column("f", float.class), column("d", double.class), column("o", Object.class), column("str", String.class)
		}, null);
		@SuppressWarnings("unchecked")
		RecordMapper<AllTypes> mapper = (RecordMapper<AllTypes>) generator.create(JavaType.of(AllTypes.class));
		
		// Mock reading from ResultSet
		Mockito.when(results.getBoolean(0)).thenReturn(true);
		Mockito.when(results.getByte(1)).thenReturn((byte) 42);
		Mockito.when(results.getShort(2)).thenReturn((short) 1337);
		Mockito.when(results.getInt(3)).thenReturn(0xf00f);
		Mockito.when(results.getLong(4)).thenReturn(0xdeadbeefL);
		Mockito.when(results.getFloat(5)).thenReturn(Float.NaN);
		Mockito.when(results.getDouble(6)).thenReturn(Double.MIN_VALUE);
		Object obj = new Object();
		Mockito.when(results.getObject(7, Object.class)).thenReturn(obj);
		Mockito.when(results.getString(8)).thenReturn("hello world");
		
		// Read and verify
		assertEquals(new AllTypes(true, (byte) 42, (short) 1337, 0xf00f, 0xdeadbeefl, Float.NaN, Double.MIN_VALUE, obj, "hello world"), mapper.read(results));
	}
	
}
