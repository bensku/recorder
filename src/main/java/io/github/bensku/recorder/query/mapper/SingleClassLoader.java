package io.github.bensku.recorder.query.mapper;

/**
 * Loads a single class. Temporary until JDK 15 anonymous class support.
 *
 */
class SingleClassLoader extends ClassLoader {
	
	private final byte[] code;
	
	public SingleClassLoader(byte[] code) {
		this.code = code;
	}

	@Override
	protected Class<?> findClass(String name) {
		return defineClass(name, code, 0, code.length);
	}
}
