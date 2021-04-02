package io.github.bensku.recorder.query;

import io.github.bensku.recorder.ComponentLambda;
import io.github.bensku.recorder.table.Condition.Type;

public class ConditionBuilder<R extends Record, C> {
	
	/**
	 * Query that this condition is part of.
	 */
	private final SelectBuilder<R> query;
	
	/**
	 * Left hand side of the condition.
	 */
	private final ComponentLambda<?, C> lhs;
	
	ConditionBuilder(SelectBuilder<R> query, ComponentLambda<?, C> lhs) {
		this.query = query;
		this.lhs = lhs;
	}

	public SelectBuilder<R> eq(C rhs) {
		query.addCondition(lhs, Type.EQUAL, rhs);
		return query;
	}
	
	public SelectBuilder<R> eq(ComponentLambda<?, C> rhs) {
		query.addCondition(lhs, Type.EQUAL, rhs);
		return query;
	}
	
	// TODO rest of conditions
}
