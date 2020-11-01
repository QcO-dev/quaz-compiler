package quaz.compiler.compiler.values;

public class LocalVariable {
	
	private final String name;
	private final String descriptor;
	private final int index;
	private final boolean isPrimitive;
	
	public LocalVariable(String name, String descriptor, int index, boolean isPrim) {
		this.name = name;
		this.descriptor = descriptor;
		this.index = index;
		this.isPrimitive = isPrim;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescriptor() {
		return descriptor;
	}

	public int getIndex() {
		return index;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}
	
}
