package quaz.compiler.compiler.values;

import quaz.compiler.compiler.Context;

public class Function {
	
	private String name;
	private String owner;
	private String descriptor;
	private String returnType;
	private boolean isInterface;
	private Context localContext;
	
	public Function(String name, String owner, String descriptor, boolean isInterface, Context context, String returnType) {
		this.name = name;
		this.owner = owner;
		this.descriptor = descriptor;
		this.isInterface = isInterface;
		this.localContext = context;
		this.returnType = returnType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	public Context getLocalContext() {
		return localContext;
	}

	public void setLocalContext(Context localContext) {
		this.localContext = localContext;
	}

	public String getReturnTypeDescriptor() {
		return returnType;
	}

	public void setReturnTypeDescriptor(String returnType) {
		this.returnType = returnType;
	}
	
}
