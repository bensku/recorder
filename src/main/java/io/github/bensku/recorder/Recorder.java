package io.github.bensku.recorder;

import java.sql.Connection;
import java.util.function.Supplier;

import io.github.bensku.recorder.query.SelectBuilder;

public class Recorder {

	private final Supplier<Connection> connectionProvider;
	
	public Recorder(Supplier<Connection> connectionProvider) {
		this.connectionProvider = connectionProvider;
	}
	
	public <R extends Record> SelectBuilder<R> select(Class<R> type) {
		
	}
}
