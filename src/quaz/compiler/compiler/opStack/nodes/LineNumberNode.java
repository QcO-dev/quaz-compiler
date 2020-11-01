package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class LineNumberNode extends OpNode {
	
	int lineNumber;
	Label label;
	
	public LineNumberNode(int lineNumber, Label label) {
		super(0);
		this.lineNumber = lineNumber;
		this.label = label;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitLineNumber(lineNumber, label);
	}
	
	
	
}
