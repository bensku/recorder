package recorderio.github.bensku.recorder.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.github.bensku.recorder.query.mapper.MapperGenerator;
import io.github.bensku.recorder.query.mapper.RecordMapper;
import io.github.bensku.recorder.query.mapper.TableSource;
import io.github.bensku.recorder.sql.Column;
import io.github.bensku.recorder.sql.JavaType;
import io.github.bensku.recorder.sql.Table;

@TestInstance(Lifecycle.PER_CLASS)
public class MapperGeneratorTest {

	public static class TestTableSource implements TableSource {

		public Table table;
		
		@Override
		public Table get(JavaType type) {
			return table;
		}
		
	}
	
	private TestTableSource tableSource;
	private MapperGenerator generator;
	
	@BeforeAll
	public void initGenerator() {
		tableSource = new TestTableSource();
		generator = new MapperGenerator(tableSource);
	}
	
	public record EmptyRecord() {}
	
	@Test
	public void emptyTable() {
		tableSource.table = new Table(JavaType.of(EmptyRecord.class), "empty_records", new Column[0], null);
		@SuppressWarnings("unchecked")
		RecordMapper<EmptyRecord> mapper = (RecordMapper<EmptyRecord>) generator.create(JavaType.of(EmptyRecord.class));
	}
	
}
