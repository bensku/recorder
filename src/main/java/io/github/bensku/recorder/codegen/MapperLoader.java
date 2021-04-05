package io.github.bensku.recorder.codegen;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

import io.github.bensku.recorder.table.Table;

public class MapperLoader {
	
	public static MapperLoader create(boolean codegen) {
		return new MapperLoader(codegen ? new MapperGenerator() : null);
	}

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
	
	/**
	 * Record mapper generator.
	 */
	private final MapperGenerator generator;
	
	private MapperLoader(MapperGenerator generator) {
		this.generator = generator;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Record> RecordMapper<T> loadMapper(Class<T> recordType) {
		// Load or generate the mapper class
		Class<?> mapperClass;
		try {
			// Try to load the mapper with same class loader the record was loaded with
			mapperClass = recordType.getClassLoader().loadClass(recordType.getName() + "$RecordMapper");
		} catch (ClassNotFoundException e) {
			if (generator == null) {
				throw new AssertionError("missing record mapper for " + recordType.getName()
						+ "; runtime code generation is disabled", e);
			}
			// If mapper doesn't exist, generate it as hidden class
			byte[] code = generator.generate(Table.fromClass(recordType));
			try {
				MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(recordType, LOOKUP);
				mapperClass = lookup.defineHiddenClass(code, true).lookupClass();
			} catch (IllegalAccessException e1) {
				throw new AssertionError("hidden class loading failed", e1);
			}
		}
		
		// Instantiate it, which should always succeed for correctly generated mappers
		try {
			return (RecordMapper<T>) mapperClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new AssertionError("record mapper instantiation failed", e);
		}
	}
}
