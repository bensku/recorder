package io.github.bensku.recorder.codegen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Record mappers convert JDBC types to records and back. They are generatd
 * automatically by using ASM.
 * @param <R> Record type.
 */
public interface RecordMapper<R extends Record> {

	/**
	 * Reads a record from a single row. Caller must ensure that
	 * {@link ResultSet#next()} is called after each row.
	 * @param results Result set from JDBC.
	 * @return Next record.
	 */
	R read(ResultSet results);
	
	/**
	 * Writes a record to given statement.
	 * @param statement Prepared statement.
	 * @param record Record to write.
	 */
	void write(PreparedStatement statement, R record);
}
