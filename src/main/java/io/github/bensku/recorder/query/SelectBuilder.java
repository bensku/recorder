package io.github.bensku.recorder.query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.bensku.recorder.ComponentLambda;
import io.github.bensku.recorder.sql.Condition;
import io.github.bensku.recorder.sql.adapter.SqlAdapter;

public class SelectBuilder<R extends Record> {
	
	private final RecorderQuery<SelectBuilder<R>, R> query;
	
	/**
	 * A singular table name, table type, or an array of these.
	 */
	private Object tables;
	
	/**
	 * If {@link #tables} has its default value.
	 */
	private boolean defaultTables;
	
	/**
	 * Conditions in array.
	 */
	private Object[] conditions;
	
	/**
	 * How many {@link #conditions} there are.
	 */
	private int conditionCount;
	
	/**
	 * Reference to column that this is ordered by.
	 */
	private ComponentLambda<?, ?> orderColumn;
	
	/**
	 * How this is ordered.
	 */
	private Order order;
	
	/**
	 * Maximum number of rows that should be returned.
	 */
	private int limit;
	
	/**
	 * Current (mutable) hash code.
	 */
	private int cachedHash;
	
	SelectBuilder(RecorderQuery<SelectBuilder<R>, R> query, Class<? extends Record> table) {
		this.query = query;
		this.tables = table;
		this.defaultTables = true;
		this.conditions = new Object[6];
		this.conditionCount = 0;
		this.limit = -1;
		this.cachedHash = 1;
	}
	
	private void doTablesAccess() {
		if (!defaultTables) {
			throw new IllegalStateException("source tables already specified (FROM is used twice)");
		}
		defaultTables = false; // Table is now explicitly set
	}
	
	public SelectBuilder<R> from(String table) {
		doTablesAccess();
		this.tables = table;
		cachedHash = 31 * cachedHash + System.identityHashCode(table);
		return this;
	}
	
	public SelectBuilder<R> from(String... tables) {
		doTablesAccess();
		this.tables = tables;
		int hash = cachedHash;
		for (String table : tables) {
			hash = 31 * hash + System.identityHashCode(table);
		}
		cachedHash = hash;
		return this;
	}
	
	public SelectBuilder<R> from(Class<? extends Record> type) {
		doTablesAccess();
		this.tables = type;
		cachedHash = 31 * cachedHash + System.identityHashCode(type);
		return this;
	}
	
	public SelectBuilder<R> from(@SuppressWarnings("unchecked") Class<? extends Record>... types) {
		doTablesAccess();
		this.tables = types;
		int hash = cachedHash;
		for (Class<? extends Record> type : types) {
			hash = 31 * hash + System.identityHashCode(type);
		}
		cachedHash = hash;
		return this;
	}
	
	public <C> ConditionBuilder<R, C> where(ComponentLambda<?, C> lhs) {
		return new ConditionBuilder<>(this, lhs);
	}
	
	<C> void addCondition(ComponentLambda<?, C> lhs, Condition.Type type, Object rhs) {
		Object[] cond = conditions;
		int count = conditionCount;
		int hash = cachedHash;
		
		// If needed, allocate more space for conditions
		if (count > cond.length - 3) {
			cond = new Object[cond.length + 9];
			System.arraycopy(conditions, 0, cond, 0, count);
		}
		
		// Append to conditions and cached hash code
		cond[count++] = lhs;
		hash = 31 * hash + System.identityHashCode(lhs);
		cond[count++] = type;
		if (rhs instanceof ComponentLambda) { // RHS can also be a value set to PreparedStatement
			// So we'll only add it to hash code if it changes SQL string
			hash = 31 * hash + System.identityHashCode(type);
		}
		cond[count++] = rhs;
		hash = 31 * hash + System.identityHashCode(rhs);
		
		conditions = cond;
		conditionCount = count;
		cachedHash = hash;
	}
	
	public SelectBuilder<R> orderBy(ComponentLambda<?, ?> column, Order order) {
		if (orderColumn != null) {
			throw new IllegalStateException("order already specified (ORDER BY used twice)");
		}
		orderColumn = column;
		cachedHash = 31 * cachedHash + System.identityHashCode(column);
		cachedHash = 31 * cachedHash + System.identityHashCode(order);
		return this;
	}
	
	public SelectBuilder<R> limit(int limit) {
		if (this.limit != -1) {
			throw new IllegalStateException("limit already set to " + this.limit);
		}
		this.limit = limit;
		cachedHash = 31 * cachedHash + limit;
		return this;
	}
	
	private String makeSql(SqlAdapter adapter, SelectBuilder<R> unused) {
		
	}
	
	private void setParams(PreparedStatement stmt) {
		
	}
	
	private R mapRow(ResultSet row) {
		return query.mapper().read(row);
	}
	
	public Optional<R> first() throws SQLException {
		if (limit == -1) { // No limit requested...
			limit(1); // Fetch only one row, this might improve performance
		}
		try (PreparedStatement stmt = query.prepareStatement(this, this::makeSql)) {
			setParams(stmt);
			
			try (ResultSet results = stmt.executeQuery()) {
				if (results.next()) {
					return Optional.of(mapRow(results));
				} else {
					return Optional.empty();
				}
			}
		} finally {
			query.close();
		}
	}
	
	public List<R> all() {
		
	}
	
	public Stream<R> stream() {
		
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SelectBuilder<?> b) {
			// Compare FROM tables
			if (tables instanceof Object[] ourTable) {
				if (b.tables instanceof Object[] theirTable) {
					if (ourTable.length != theirTable.length) {
						return false; // Different number of FROM entries
					} else { // Compare identities of things in the tables
						for (int i = 0; i < ourTable.length;) {
							if (ourTable[i] != theirTable[i]) {
								return false; // LHS mismatch
							}
							i++;
							if (ourTable[i] != theirTable[i]) {
								return false; // Condition type mismatch
							}
							i++;
							// For RHS, we ignore mismatch if both are literals
							// (literals do not require SQL changes)
							if (ourTable[i] != theirTable[i]
									&& (ourTable[i] instanceof ComponentLambda || theirTable[i] instanceof ComponentLambda)) {
								return false; // RHS mismatch
							}
							i++;
						}

					}
				} else {
					return false; // We have one FROM, they have many
				}
			} else { // Not tables, just compare identity
				if (tables != b.tables) {
					return false;
				}
			}
			
			// Compare conditions
			if (conditionCount != b.conditionCount) {
				return false; // Different number of them
			}
			int count = conditionCount;
			Object[] our = conditions;
			Object[] their = b.conditions;
			for (int i = 0; i < count; i++) {
				if (our[i] != their[i]) {
					return false; // Conditions might not be the same
				}
			}
			
			// Order should be same, too
			if (orderColumn != b.orderColumn || order != b.order) {
				return false; // Different order column or order
			}
			
			// And limit, too
			if (limit != b.limit) {
				return false;
			}
			
			// It seems that all checks passed
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return cachedHash;
	}
}
