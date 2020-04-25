package io.github.bensku.recorder;

public record ComponentReference<C>(
		Class<?> record,
		String name,
		Class<C> type
) {}