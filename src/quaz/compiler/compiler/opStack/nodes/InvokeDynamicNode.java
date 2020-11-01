package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

public class InvokeDynamicNode extends OpNode {

	String name;
	String descriptor;
	Handle bootstrapMethod;
	Object[] bootstrapArguments;
	
	public InvokeDynamicNode(String name, String descriptor, Handle bootstrapMethod, Object... bootstrapArguments) {
		super(0);
		this.name = name;
		this.descriptor = descriptor;
		this.bootstrapMethod = bootstrapMethod;
		this.bootstrapArguments = bootstrapArguments;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitInvokeDynamicInsn(name, descriptor, bootstrapMethod, bootstrapArguments);
	}
	
	
	
}
