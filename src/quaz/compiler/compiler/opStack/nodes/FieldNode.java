package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.MethodVisitor;

public class FieldNode extends OpNode {

	String owner;
	String name;
	String descriptor;
	
	public FieldNode(int opcode, String owner, String name, String descriptor) {
		super(opcode);
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitFieldInsn(opcode, owner, name, descriptor);
	}
	
	
	
	
	
}
