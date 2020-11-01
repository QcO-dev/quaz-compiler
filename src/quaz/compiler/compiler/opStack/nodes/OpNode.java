package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.MethodVisitor;

public abstract class OpNode {
	
	protected int opcode;
	
	public OpNode(int opcode) {
		this.opcode = opcode;
	}
	
	public abstract void generateBytecode(MethodVisitor mv);
	
	public int getOpcode() {
		return opcode;
	}

	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}
	
}
