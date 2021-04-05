package io.github.bensku.recorder.test;

import java.io.IOException;

public class TestRecords {

	public static byte[] getBytecode(Class<?> type) {
		try {
			return type.getResourceAsStream("TestRecords$" + type.getSimpleName() + ".class").readAllBytes();
		} catch (IOException e) {
			throw new AssertionError("failed to read bytecode", e);
		}
	}
	
	public record EmptyRecord() {}
	public record AllTypes(boolean bool, byte b, short s, int i, long l, float f, double d, String str, Object o) {}
}
