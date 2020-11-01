package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class JumpNode extends OpNode {
	
	private Label label;
	
	public JumpNode(int opcode, Label label) {
		super(opcode);
		this.label = label;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitJumpInsn(opcode, label);
	}
	
	
	
}
