package quaz.compiler.compiler.preprocessors;

import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
import quaz.compiler.compiler.values.Function;
import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.function.FunctionDefinitionNode;
import quaz.compiler.parser.nodes.function.ParameterListNode;
import quaz.compiler.standardLibrary.Pair;

public class FunctionVisitor {
	
	public void visitFunctionDefinitionNode(Node node, Context context) {
		
		FunctionDefinitionNode fdn = (FunctionDefinitionNode) node;
		
		String name = fdn.getId().getValue();
		
		ParameterListNode pln = (ParameterListNode) fdn.getArguments();
		
		//TODO
		String givenDescriptor = name.equals("main") ? "[Ljava/lang/String;" : "";
		
		for(Pair<Token, String> var : pln.getVars()) {
			String typeGiven = var.getSecond();
			String desc = "";
			
			if(Descriptors.typeIsPrimative(typeGiven)) {
				desc = Descriptors.typeToMethodDescriptor(typeGiven);
			}
			else {
				String type = typeGiven.contains(".") ? typeGiven : context.getTypeReferences().get(typeGiven);
				
				desc = Descriptors.typeToMethodDescriptor(type);
			}
			
			givenDescriptor += desc;
		}
		
		String returnType = "V";
		
		if(fdn.getReturnType() != null) {
			
			String typeGiven = fdn.getReturnType().getValue();
			
			if(Descriptors.typeIsPrimative(typeGiven)) {
				returnType = Descriptors.typeToMethodDescriptor(typeGiven);
			}
			else {
				String type = typeGiven.contains(".") ? typeGiven : context.getTypeReferences().get(typeGiven);
				
				returnType = Descriptors.typeToMethodDescriptor(type);
			}
		}
		
		String descriptor = "(" + givenDescriptor + ")" + returnType;
		
		context.getFunctions().put(name, givenDescriptor, new Function(name, context.getName(), descriptor, false, context.copy(), returnType));
	}
	
	public void visitFunctionCallNode(Node node, Context context) {}
	
	public void visitReturnNode(Node node, Context context) {}
	
}
