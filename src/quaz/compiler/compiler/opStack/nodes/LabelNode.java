package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class LabelNode extends OpNode {

	private Label label;
	
	public LabelNode(Label label) {
		super(0);
		this.label = label;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitLabel(label);
	}
	
	
	
}
