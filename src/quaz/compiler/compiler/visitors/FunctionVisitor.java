package quaz.compiler.compiler.visitors;

import java.util.HashMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Compiler;
import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
import quaz.compiler.compiler.values.Function;
import quaz.compiler.compiler.values.LocalVariable;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.function.FunctionCallNode;
import quaz.compiler.parser.nodes.function.FunctionDefinitionNode;
import quaz.compiler.parser.nodes.function.ParameterListNode;
import quaz.compiler.standardLibrary.Pair;

public class FunctionVisitor implements Opcodes {
	
	public void visitFunctionDefinitionNode(Node node, Context context) throws CompilerLogicException {
		
		FunctionDefinitionNode fdn = (FunctionDefinitionNode) node;
		
		String name = fdn.getId().getValue();
		
		ParameterListNode pln = (ParameterListNode) fdn.getArguments();
		
		//TODO Fix this shambles
		String descriptor = name.equals("main") ? "[Ljava/lang/String;" : "";
		
		Context copy = context.copy();
		
		int index = 0;
		
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
			
			descriptor += desc;
			
			copy.getLocalVariables().put(var.getFirst().getValue(), new LocalVariable(var.getFirst().getValue(), desc, index, Descriptors.isPrimative(desc)));
			index++;
		}
		
		Function function = context.getFunctions().get(name, descriptor);
		
		MethodVisitor visitor = context.getWriter()
				.visitMethod(ACC_PUBLIC | ACC_STATIC, 
						function.getName(), 
						function.getDescriptor(), 
						null, null);
		
		context.setVisitor(visitor);
		copy.setVisitor(visitor);
		
		String returnType = "void";
		
		if(fdn.getReturnType() != null) {
			
			String typeGiven = fdn.getReturnType().getValue();
			
			if(Descriptors.typeIsPrimative(typeGiven)) {
				returnType = typeGiven;
			}
			else {
				String type = typeGiven.contains(".") ? typeGiven : context.getTypeReferences().get(typeGiven);
				
				returnType = type;
			}
		}
		
		copy.setMethodReturnType(returnType);
		
		visitor.visitCode();
		
		context.getCompilerInstance().visit(fdn.getStatement(), copy);
		
		if(!copy.hasReturnedLast())
			getDefaultReturn(visitor, copy);
			//visitor.visitInsn(RETURN);
		
		visitor.visitMaxs(0, 0);
		
		visitor.visitEnd();
		
		context.setLastWasConstant(false);
		
	}
	
	public void visitFunctionCallNode(Node node, Context context, boolean root) throws CompilerLogicException {
		FunctionCallNode fcn = (FunctionCallNode) node;
		
		String funcName = fcn.getId().getValue();
		MethodVisitor mv = context.getVisitor();
		
		Compiler ci = context.getCompilerInstance();
		
		HashMap<String, Function> functions = context.getFunctions().get(funcName);
		
		String descriptor = "";
		
		for(Node n : (Node[]) fcn.getArguments().getValue()) {
			ci.visit(n, context);
			descriptor += context.getLastDescriptor();
		}
		
		if(functions == null) {
			throw new CompilerLogicException("Function " + funcName + " does not exist in the current scope.", fcn.getId().getStart(), fcn.getId().getEnd());
		}
		
		Function func = functions.get(descriptor);
		
		if(func != null) {
			Label l0 = new Label();
			
			mv.visitLabel(l0);
			mv.visitLineNumber(fcn.getStart().getLine(), l0);
			mv.visitMethodInsn(INVOKESTATIC, func.getOwner(), funcName, func.getDescriptor(), func.isInterface());
			context.setLastDescriptor(func.getReturnTypeDescriptor());
			
			if(!func.getReturnTypeDescriptor().equals("V") && root) {
				if(func.getReturnTypeDescriptor().equals("D") ||func.getReturnTypeDescriptor().equals("J")) {
					mv.visitInsn(Opcodes.POP2);
				} else {
					mv.visitInsn(Opcodes.POP);
				}
			}
			context.setLastWasConstant(false);
			return;
		}
		
		Class<?>[] givenParams = Descriptors.descriptorToClasses("(" + descriptor + ")V");
		
		// Checks all other functions with this name in the scope, and if it finds one that matches, call it. This includes the checking for super classes.
		// This means a function of println:(Ljava/lang/Object;)V will be called if provided function is println(Ljava/lang/String;)V is passed.
		
		outer : for(Function f : functions.values()) {
			Class<?>[] classParameters = Descriptors.descriptorToClasses(f.getDescriptor());
			
			if(classParameters.length != givenParams.length) {
				continue;
			}
			
			for(int i = 0; i < classParameters.length; i++) {
				Class<?> klass = classParameters[i];
				if(!klass.isAssignableFrom(givenParams[i])) {
					//System.out.println(klass);
					//System.out.println(givenParams[i]);
					//throw new CompilerLogicException("Function \'" + funcName + "\' does not exist in current scope.\nArgument types given: " + String.join(", ", Descriptors.descriptorToTypes("(" + descriptor + ")V")), fcn.getStart(), fcn.getEnd());
					continue outer;
				}
			}
			
			Label l0 = new Label();
			
			mv.visitLabel(l0);
			mv.visitLineNumber(fcn.getStart().getLine(), l0);
			mv.visitMethodInsn(INVOKESTATIC, f.getOwner(), funcName, f.getDescriptor(), f.isInterface());
			
			if(!f.getReturnTypeDescriptor().equals("V") && root) {
				if(f.getReturnTypeDescriptor().equals("D") || f.getReturnTypeDescriptor().equals("J")) {
					mv.visitInsn(Opcodes.POP2);
				} else {
					mv.visitInsn(Opcodes.POP);
				}
			}
			
			context.setLastWasConstant(false);
			context.setLastDescriptor(f.getReturnTypeDescriptor());
			return;
		}
		
		throw new CompilerLogicException("Function \'" + funcName + "\' does not exist in current scope.\nArgument types given: " + String.join(", ", Descriptors.descriptorToTypes("(" + descriptor + ")V")), fcn.getStart(), fcn.getEnd());
	}
	
	public void visitReturnNode(Node node, Context context) throws CompilerLogicException {
		
		MethodVisitor mv = context.getVisitor();
		
		Node value = (Node) node.getValue();
		
		if(value == null) {
			getDefaultReturn(mv, context);
			return;
		}
		
		context.getCompilerInstance().visit(value, context);
		
		String desc = context.getLastDescriptor();
		
		
		String methodDesc = Descriptors.typeToMethodDescriptor(context.getMethodReturnType());
		
		if(!desc.equals(methodDesc)) {
			throw new CompilerLogicException("Incorrect return type for function: " + Descriptors.descriptorToType(desc) + "\nExpected: " + context.getMethodReturnType(), value.getStart(), value.getEnd());
		}
		
		switch(desc) {
			case "I":
				mv.visitInsn(IRETURN);
				context.setHasReturnedLast(true);
				break;
				
			case "D":
				mv.visitInsn(DRETURN);
				context.setHasReturnedLast(true);
				break;
				
			case "F":
				mv.visitInsn(FRETURN);
				context.setHasReturnedLast(true);
				break;
				
			case "Z":
				mv.visitInsn(IRETURN);
				context.setHasReturnedLast(true);
				break;
			default:
				mv.visitInsn(ARETURN);
				context.setHasReturnedLast(true);
				break;
		}
		
		context.setLastWasConstant(false);
		
	}
	
	private void getDefaultReturn(MethodVisitor mv, Context context) {
		
		if(context.getMethodReturnType().equals("void")) {
			mv.visitInsn(RETURN);
			context.setHasReturnedLast(true);
			return;
		}
		
		if(Descriptors.typeIsPrimative(context.getMethodReturnType())) {
			switch(context.getMethodReturnType()) {
				case "int":
					mv.visitInsn(ICONST_0);
					mv.visitInsn(IRETURN);
					context.setHasReturnedLast(true);
					break;
				case "double":
					mv.visitInsn(DCONST_0);
					mv.visitInsn(DRETURN);
					context.setHasReturnedLast(true);
					break;
				case "float":
					mv.visitInsn(FCONST_0);
					mv.visitInsn(FRETURN);
					context.setHasReturnedLast(true);
					break;
				case "boolean":
					mv.visitInsn(ICONST_0);
					mv.visitInsn(IRETURN);
					context.setHasReturnedLast(true);
					break;
			}
			return;
		}
		
		mv.visitInsn(ACONST_NULL);
		mv.visitInsn(ARETURN);
		context.setHasReturnedLast(true);
		
	}
	
}
