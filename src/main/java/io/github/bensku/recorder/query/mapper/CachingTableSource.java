package io.github.bensku.recorder.query.mapper;

import java.util.HashMap;
import java.util.Map;
import io.github.bensku.recorder.sql.JavaType;
import io.github.bensku.recorder.sql.Table;

public class CachingTableSource implements TableSource {
	
	private final Map<JavaType, Table> definitions;
	private final TableSource source;
	
	public CachingTableSource(TableSource source) {
		this.definitions = new HashMap<>();
		this.source = source;
	}
	
	public Table get(JavaType type) {
		return definitions.computeIfAbsent(type, (t) -> source.get(t));
	}
}
