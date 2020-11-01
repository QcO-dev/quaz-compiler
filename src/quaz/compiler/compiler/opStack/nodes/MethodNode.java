package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.MethodVisitor;

public class MethodNode extends OpNode {

	String owner;
	String name;
	String descriptor;
	boolean isInterface;
	
	public MethodNode(int opcode, String owner, String name, String descriptor, boolean isInterface) {
		super(opcode);
		
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		this.isInterface = isInterface;
		
	}
	
	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
	}

}
