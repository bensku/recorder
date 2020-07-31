package io.github.bensku.recorder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import javax.sql.DataSource;

import io.github.bensku.recorder.cache.Cache;
import io.github.bensku.recorder.cache.GlobalCache;
import io.github.bensku.recorder.cache.QueryCache;
import io.github.bensku.recorder.cache.QueryCaches;
import io.github.bensku.recorder.query.QueryHelper;
import io.github.bensku.recorder.query.SelectBuilder;
import io.github.bensku.recorder.query.mapper.MapperGenerator;
import io.github.bensku.recorder.query.mapper.RecordMapper;
import io.github.bensku.recorder.query.mapper.TableSource;
import io.github.bensku.recorder.sql.JavaType;
import io.github.bensku.recorder.sql.Table;
import io.github.bensku.recorder.sql.adapter.SqlAdapter;

public class Recorder {
	
	/**
	 * Data source (connection provider) for current database.
	 */
	private final DataSource dataSource;
	
	/**
	 * SQL adapter for currently used database.
	 */
	private final SqlAdapter sqlAdapter;
	
	/**
	 * Creates table definitions from Java records.
	 */
	private final TableSource tableSource;
	
	/**
	 * {@link QueryCache Query caches} for different types of query builders.
	 */
	private final ThreadLocal<QueryCaches> queryCaches;
	
	/**
	 * Cache of tables resolved with {@link #tableSource}.
	 */
	private final GlobalCache<JavaType, Table> tableCache;
	
	/**
	 * Record mapper code generator.
	 */
	private final MapperGenerator mapperGenerator;
	
	/**
	 * Cached record mappers.
	 */
	private final GlobalCache<JavaType, RecordMapper<?>> mapperCache;
	
	public Recorder(DataSource dataSource, SqlAdapter sqlAdapter) {
		this.dataSource = dataSource;
		this.sqlAdapter = sqlAdapter;
		this.tableSource = null; // TODO implementations :)
		this.queryCaches = new ThreadLocal<>() {
			protected QueryCaches initialValue() {
				return new QueryCaches(30, 10);
			}
		};
		this.tableCache = new GlobalCache<>(tableSource::get);
		
		// Creating table definitions is expensive, so we're using cache as
		// source, even though we have direct access to source
		this.mapperGenerator = new MapperGenerator(tableCache::get);
		// And because generating and loading bytecode is not cheap either...
		this.mapperCache = new GlobalCache<>(mapperGenerator::create);
	}
	
	@SuppressWarnings("unchecked") // Generic magic
	private <T, R extends Record> QueryHelper<T, R> newHelper(QueryCache<?> queryCache, Class<R> record) throws SQLException {
		Cache<JavaType, Table> tables = tableCache.threadLocalView();
		RecordMapper<R> mapper = (RecordMapper<R>) mapperCache.get(JavaType.of(record));
		return new QueryHelper<T, R>(sqlAdapter, dataSource.getConnection(), (QueryCache<T>) queryCache, tables::get, mapper);
	}
	
	public <R extends Record> SelectBuilder<R> select(Class<R> record) throws SQLException {
		QueryHelper<SelectBuilder<R>, R> helper = newHelper(queryCaches.get().select(), record);
		return new SelectBuilder<>(helper, record);
	}
}
