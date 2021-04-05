package io.github.bensku.recorder.test;

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

import io.github.bensku.recorder.codegen.MapperLoader;
import io.github.bensku.recorder.codegen.RecordMapper;
import io.github.bensku.recorder.test.TestRecords.AllTypes;
import io.github.bensku.recorder.test.TestRecords.EmptyRecord;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class MapperGeneratorTest {
	
	private MapperLoader loader;
	
	@Mock
	private ResultSet results;
	
	@Mock
	private PreparedStatement statement;
	
	@BeforeAll
	public void initGenerator() {
		loader = MapperLoader.create(true);
	}
			
	@Test
	public void emptyTable() {
		RecordMapper<EmptyRecord> mapper = loader.loadMapper(EmptyRecord.class);
		mapper.read(results);
		mapper.write(statement, new EmptyRecord());
		Mockito.verifyNoInteractions(results, statement);
	}
	
	@Test
	public void basicTable() throws SQLException {
		RecordMapper<AllTypes> mapper = loader.loadMapper(AllTypes.class);
		
		// Mock reading from ResultSet
		Mockito.when(results.getBoolean(0)).thenReturn(true);
		Mockito.when(results.getByte(1)).thenReturn((byte) 42);
		Mockito.when(results.getShort(2)).thenReturn((short) 1337);
		Mockito.when(results.getInt(3)).thenReturn(0xf00f);
		Mockito.when(results.getLong(4)).thenReturn(0xdeadbeefL);
		Mockito.when(results.getFloat(5)).thenReturn(Float.NaN);
		Mockito.when(results.getDouble(6)).thenReturn(Double.MIN_VALUE);
		Object obj = new Object();
		Mockito.when(results.getString(7)).thenReturn("hello world");
		Mockito.when(results.getObject(8, Object.class)).thenReturn(obj);
		
		// Read and verify
		assertEquals(new AllTypes(true, (byte) 42, (short) 1337, 0xf00f, 0xdeadbeefl, Float.NaN, Double.MIN_VALUE, "hello world", obj), mapper.read(results));
	}
	
}
