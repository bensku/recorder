package recorderio.github.bensku.recorder.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StringLiteralTest {

	private int identityHash() {
		return System.identityHashCode("lorem ipsum");
	}
	
	@Test
	public void stringIdentity() {
		assertEquals(identityHash(), identityHash());
	}
}
