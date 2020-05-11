module io.github.bensku.recorder {
	requires java.sql;
	requires org.objectweb.asm;
	
	exports io.github.bensku.recorder;
	exports io.github.bensku.recorder.query;
	exports io.github.bensku.recorder.record;
}