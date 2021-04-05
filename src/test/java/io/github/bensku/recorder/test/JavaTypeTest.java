package io.github.bensku.recorder.test;

import org.junit.jupiter.api.Test;

import io.github.bensku.recorder.codegen.JavaType;
import io.github.bensku.recorder.record.PrimaryKey;

public class JavaTypeTest {

	record SignatureTest(PrimaryKey<Integer> key) {}
	
	@Test
	public void parseSignature() {
		String signature = SignatureTest.class.getRecordComponents()[0].getGenericSignature();
		JavaType.fromSignature(signature);
	}
}
