package quaz.compiler.compiler.preprocessors;

import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.classes.ImportNode;

public class ClassesVisitor {
	
	public void visitInstanceNode(Node node, Context context) {}
	
	public void visitMemberAccessNode(Node node, Context context) {}
	
	public void visitImportNode(Node node, Context context) throws CompilerLogicException {
		
		String type = ((ImportNode) node).getTypeName();
		
		String name = type.substring(type.lastIndexOf(".")+1);
		
		String descriptor = Descriptors.typeToDescriptor(type);
		
		try {
			Class.forName(type);
		} catch(ClassNotFoundException e) {
			throw new CompilerLogicException("Unknown import " + type, node.getStart(), node.getEnd());
		}
		
		context.getTypeReferences().put(name, descriptor);
	}
	
	public void visitPackageNode(Node node, Context context) {}
	
	public void visitCastNode(Node node, Context context) {}
	
}
