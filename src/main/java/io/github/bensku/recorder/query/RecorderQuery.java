package io.github.bensku.recorder.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.github.bensku.recorder.query.cache.QueryCache;
import io.github.bensku.recorder.query.mapper.RecordMapper;
import io.github.bensku.recorder.sql.SqlGenerator;
import io.github.bensku.recorder.sql.adapter.SqlAdapter;

public class RecorderQuery<T, R extends Record> {

	/**
	 * SQL adapter for current database.
	 */
	private final SqlAdapter adapter;
	
	/**
	 * Connection this query is going to be executed on.
	 */
	private final Connection conn;
	
	/**
	 * Query cache.
	 */
	private final QueryCache<T> cache;
	
	/**
	 * Record mapper to use for this query.
	 */
	private final RecordMapper<R> mapper;
	
	RecorderQuery(SqlAdapter adapter, Connection conn, QueryCache<T> cache, RecordMapper<R> mapper) {
		this.adapter = adapter;
		this.conn = conn;
		this.cache = cache;
		this.mapper = mapper;
	}
	
	public String computeSql(T query, SqlGenerator<T> generator) {
		String sql = cache.get(query);
		if (sql == null) { // Put to cache
			sql = generator.generate(adapter, query);
			cache.put(query, sql);
		}
		return sql;
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}
	
	public PreparedStatement prepareStatement(T query, SqlGenerator<T> generator) throws SQLException {
		return prepareStatement(computeSql(query, generator));
	}
	
	public RecordMapper<R> mapper() {
		return mapper;
	}

	public void close() throws SQLException {
		conn.close();
	}
}
