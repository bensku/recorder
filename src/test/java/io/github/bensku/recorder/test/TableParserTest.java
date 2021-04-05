package io.github.bensku.recorder.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import io.github.bensku.recorder.codegen.JavaType;
import io.github.bensku.recorder.table.Column;
import io.github.bensku.recorder.table.Table;

import static io.github.bensku.recorder.test.TestRecords.*;

/**
 * Tests for parsing {@link Table tables} from records.
 *
 */
public class TableParserTest {
	
	@Test
	public void emptyRecord() {
		byte[] code = getBytecode(EmptyRecord.class);
		Table correct = new Table(JavaType.fromClass(EmptyRecord.class), "EmptyRecord", List.of(), null);
		assertEquals(correct, Table.fromClass(EmptyRecord.class));
		assertEquals(correct, Table.fromBytecode(Type.getInternalName(EmptyRecord.class), code));
	}
	
	private Column column(String name, Class<?> type) {
		return new Column(name, JavaType.fromClass(type));
	}
	
	@Test
	public void allTypes() {
		byte[] code = getBytecode(AllTypes.class);
		List<Column> columns = List.of(column("bool", boolean.class), column("b", byte.class),
				column("s", short.class), column("i", int.class), column("l", long.class),
				column("f", float.class), column("d", double.class),
				column("str", String.class), column("o", Object.class));
		Table correct = new Table(JavaType.fromClass(AllTypes.class), "AllTypes", columns, null);
		assertEquals(correct, Table.fromClass(AllTypes.class));
		assertEquals(correct, Table.fromBytecode(Type.getInternalName(AllTypes.class), code));
	}
}
