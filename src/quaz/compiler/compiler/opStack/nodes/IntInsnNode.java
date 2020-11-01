package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.MethodVisitor;

public class IntInsnNode extends OpNode {
	
	private int value;
	
	public IntInsnNode(int opcode, int value) {
		super(opcode);
		this.value = value;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitIntInsn(opcode, value);
	}
	
	public Object getValue() {
		return value;
	}
	
}
