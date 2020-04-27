package io.github.bensku.recorder;

import java.sql.Connection;
import java.util.function.Supplier;

import io.github.bensku.recorder.query.SelectBuilder;
import io.github.bensku.recorder.query.cache.RecorderCaches;

public class Recorder {

	private final Supplier<Connection> connectionProvider;
	
	/**
	 * Thread-local caches that make our fancy statement builders
	 * reasonably fast.
	 */
	private final ThreadLocal<RecorderCaches> caches;
	
	public Recorder(Supplier<Connection> connectionProvider) {
		this.connectionProvider = connectionProvider;
		this.caches = new ThreadLocal<>();
	}
	
	public <R extends Record> SelectBuilder<R> select(Class<R> type) {
		
	}
}
