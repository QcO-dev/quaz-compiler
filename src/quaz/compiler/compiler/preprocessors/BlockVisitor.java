package quaz.compiler.compiler.preprocessors;

import quaz.compiler.compiler.Compiler;
import quaz.compiler.compiler.Context;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.block.ForNode;
import quaz.compiler.parser.nodes.block.IfNode;
import quaz.compiler.parser.nodes.block.WhileNode;

public class BlockVisitor {
	
	public void visitStatementsNode(Node node, Context context) throws CompilerLogicException {
		
		Node[] nodes = (Node[]) node.getValue();
		
		Compiler compiler = context.getCompilerInstance();
		
		for(Node n : nodes) {
			compiler.visitPreprocessors(n, context);
		}
	}
	
	public void visitIfNode(Node node, Context context) throws CompilerLogicException {
		Node[] nodes = (Node[]) ((IfNode) node).getBody().getValue();
		
		Compiler compiler = context.getCompilerInstance();
		
		for(Node n : nodes) {
			compiler.visitPreprocessors(n, context);
		}
	}
	
	public void visitWhileNode(Node node, Context context) throws CompilerLogicException {
		Node[] nodes = (Node[]) ((WhileNode) node).getBody().getValue();
		
		Compiler compiler = context.getCompilerInstance();
		
		for(Node n : nodes) {
			compiler.visitPreprocessors(n, context);
		}
	}
	
	public void visitForNode(Node node, Context context) throws CompilerLogicException {
		Node[] nodes = (Node[]) ((ForNode) node).getBody().getValue();
		
		Compiler compiler = context.getCompilerInstance();
		
		for(Node n : nodes) {
			compiler.visitPreprocessors(n, context);
		}
	}
	
	public void visitPassNode(Node node, Context context) {}
	
	public void visitBreakNode(Node node, Context context) {}

	public void visitContinueNode(Node node, Context context) {}
	
}
