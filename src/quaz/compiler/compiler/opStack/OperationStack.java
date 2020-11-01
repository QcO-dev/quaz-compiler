package quaz.compiler.compiler.opStack;

import java.util.Stack;

import org.objectweb.asm.MethodVisitor;

import quaz.compiler.compiler.opStack.nodes.OpNode;

public class OperationStack extends Stack<OpNode> {
	private static final long serialVersionUID = -2512905100431077931L;
	
	
	
	public void fillMethodVisitor(final MethodVisitor mv) {
		for(OpNode node : this) {
			node.generateBytecode(mv);
		}
		
	}
	
}
