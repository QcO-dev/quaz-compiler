package quaz.compiler.compiler.preprocessors;

import quaz.compiler.compiler.Context;
import quaz.compiler.parser.nodes.Node;

public class ValueVisitor {
	
	public void visitStringNode(Node node, Context context) {}
	
	public void visitIntNode(Node node, Context context) {}
	
	public void visitDoubleNode(Node node, Context context) {}

	public void visitFloatNode(Node node, Context context) {}
	
	public void visitBooleanNode(Node node, Context context) {}
	
	public void visitCharNode(Node node, Context context) {}
	
	public void visitByteNode(Node node, Context context) {}
	
	public void visitLongNode(Node node, Context context) {}
	
	public void visitShortNode(Node node, Context context) {}
	
	public void visitExprStringNode(Node node, Context context) {}
	
}
