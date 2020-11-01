package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.MethodVisitor;

public class VarNode extends OpNode {

	int index;
	
	public VarNode(int opcode, int index) {
		super(opcode);
		this.index = index;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitVarInsn(opcode, index);
	}
	
	
	
}
