package io.github.bensku.recorder.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.github.bensku.recorder.cache.CachedQuery;
import io.github.bensku.recorder.cache.QueryCache;
import io.github.bensku.recorder.codegen.RecordMapper;
import io.github.bensku.recorder.sql.adapter.SqlAdapter;
import io.github.bensku.recorder.table.JavaType;
import io.github.bensku.recorder.table.QueryGenerator;
import io.github.bensku.recorder.table.Table;
import io.github.bensku.recorder.table.TableSource;

/**
 * Provides access to data needed by all types of queries.
 *
 * @param <T> Type of query builder.
 * @param <R> Type of query input or output.
 */
public class QueryHelper<T, R extends Record> {

	/**
	 * SQL adapter of current database.
	 */
	private final SqlAdapter adapter;
	
	/**
	 * Connection this query is going to be executed on.
	 */
	private final Connection conn;
	
	/**
	 * Query cache of thread that initiated the query.
	 */
	private final QueryCache<T> cache;
	
	/**
	 * Table source that this query can use to resolve table references.
	 */
	private final TableSource tableSource;
	
	/**
	 * Record mapper to use for this query.
	 */
	private final RecordMapper<R> mapper;
	
	public QueryHelper(SqlAdapter adapter, Connection conn,
			QueryCache<T> cache, TableSource tableSource, RecordMapper<R> mapper) {
		this.adapter = adapter;
		this.conn = conn;
		this.cache = cache;
		this.tableSource = tableSource;
		this.mapper = mapper;
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}
	
	/**
	 * Gets a query for given builder from cache or computes it.
	 * @param builder Query builder 
	 * @param generator Function that generates queries from builders.
	 * Called when query for given builder cannot be found from ache.
	 * @return Cached SQL query.
	 */
	public CachedQuery getQuery(T builder, QueryGenerator<T> generator) {
		CachedQuery query = cache.get(builder);
		if (query == null) { // Put to cache
			query = generator.generate(adapter, builder);
			cache.put(builder, query);
		}
		return query;
	}
	
	public Table getTable(Class<? extends Record> record) {
		return tableSource.get(JavaType.of(record));
	}
	
	public RecordMapper<R> mapper() {
		return mapper;
	}

	public void close() throws SQLException {
		conn.close();
	}
}
