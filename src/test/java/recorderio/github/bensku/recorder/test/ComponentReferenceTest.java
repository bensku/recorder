package recorderio.github.bensku.recorder.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.bensku.recorder.ComponentLambda;
import io.github.bensku.recorder.ComponentReference;

public class ComponentReferenceTest {

	private record TestRecord(String foo) {}
	
	private int methodRefIdentity() {
		ComponentLambda<TestRecord, String> lambda = TestRecord::foo;
		return System.identityHashCode(lambda);
	}
	
	@Test
	public void stableIdentity() {
		assertEquals(methodRefIdentity(), methodRefIdentity());
	}
	
	@Test
	public void referenceLookup() {
		ComponentLambda<TestRecord, String> lambda = TestRecord::foo;
		ComponentReference<String> ref = lambda.lookupComponent();
		assertEquals(TestRecord.class, ref.record());
		assertEquals("foo", ref.name());
		assertEquals(String.class, ref.type());
	}
}
