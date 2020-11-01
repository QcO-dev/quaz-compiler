package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.MethodVisitor;

public class TypeNode extends OpNode {

	String type;
	
	public TypeNode(int opcode, String type) {
		super(opcode);
		this.type = type;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitTypeInsn(opcode, type);
	}
		
	
	
}
