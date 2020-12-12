package quaz.compiler.compiler.preprocessors;

import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
import quaz.compiler.compiler.values.Function;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.function.FunctionDefinitionNode;
import quaz.compiler.parser.nodes.function.ParameterListNode;
import quaz.compiler.standardLibrary.Pair;

public class FunctionVisitor {
	
	public void visitFunctionDefinitionNode(Node node, Context context) throws CompilerLogicException {
		
		FunctionDefinitionNode fdn = (FunctionDefinitionNode) node;
		
		String name = fdn.getId().getValue();
		
		ParameterListNode pln = (ParameterListNode) fdn.getArguments();
		
		String givenDescriptor = "";
		
		for(Pair<Token, String> var : pln.getVars()) {
			String typeGiven = var.getSecond();
			String desc = "";
			
			if(Descriptors.typeIsPrimative(typeGiven)) {
				desc = Descriptors.typeToMethodDescriptor(typeGiven);
			}
			else {
				
				String descriptor = Descriptors.typeToDescriptor(typeGiven);
				
				int arrayCount = 0;
				
				if(Descriptors.descriptorIsArray(descriptor)) {
					typeGiven = Descriptors.removeArrayFromType(typeGiven);
					
					arrayCount = Descriptors.getArrayLengthFromDescriptor(descriptor);
				}
				
				String type = typeGiven.contains("/") ? typeGiven : context.getTypeReferences().get(typeGiven);
				
				if(type == null) {
					throw new CompilerLogicException("Unknown type " + typeGiven + " used in current scope", var.getFirst().getStart(),
							var.getFirst().getEnd());
				}
				
				desc = "[".repeat(arrayCount) + Descriptors.typeToMethodDescriptor(type);
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
				String descriptor = Descriptors.typeToDescriptor(typeGiven);
				
				int arrayCount = 0;
				
				if(Descriptors.descriptorIsArray(descriptor)) {
					typeGiven = Descriptors.removeArrayFromType(typeGiven);
					
					arrayCount = Descriptors.getArrayLengthFromDescriptor(descriptor);
				}
				
				String type = typeGiven.contains("/") ? typeGiven : context.getTypeReferences().get(typeGiven);
				
				if(type == null) {
					throw new CompilerLogicException("Unknown type " + typeGiven + " used in current scope", fdn.getReturnType().getStart(),
							fdn.getReturnType().getEnd());
				}
				
				returnType = "[".repeat(arrayCount) + Descriptors.typeToMethodDescriptor(type);
			}
		}
		
		String descriptor = "(" + givenDescriptor + ")" + returnType;
		
		context.getFunctions().put(name, givenDescriptor, new Function(name, context.getName(), descriptor, false, context.copy(), returnType));
	}
	
	public void visitFunctionCallNode(Node node, Context context) {}
	
	public void visitReturnNode(Node node, Context context) {}
	
}
