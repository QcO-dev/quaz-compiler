package quaz.compiler.compiler.visitors;

import java.util.HashMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Compiler;
import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.opStack.nodes.InsnNode;
import quaz.compiler.compiler.opStack.nodes.LabelNode;
import quaz.compiler.compiler.opStack.nodes.LineNumberNode;
import quaz.compiler.compiler.opStack.nodes.MethodNode;
import quaz.compiler.compiler.values.Function;
import quaz.compiler.compiler.values.LocalVariable;
import quaz.compiler.compiler.values.LocalVariables;
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
		
		LocalVariables lvs = context.getLocalVariables();
		
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
			
			if(Descriptors.isWide(desc)) {
				lvs.incrementIndex();
			}
			
			copy.getLocalVariables().put(var.getFirst().getValue(), new LocalVariable(var.getFirst().getValue(), desc, lvs.getNextIndex(), Descriptors.isPrimative(desc)));
			lvs.incrementIndex();
		}
		
		Function function = context.getFunctions().get(name, descriptor);
		
		MethodVisitor visitor = context.getWriter()
				.visitMethod(ACC_PUBLIC | ACC_STATIC, 
						function.getName(), 
						function.getDescriptor(), 
						null, null);
		
		OperationStack stack = new OperationStack();
		
		context.setOpStack(stack);
		copy.setOpStack(stack);
		
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
			getDefaultReturn(stack, copy);
			//visitor.visitInsn(RETURN);
		
		stack.fillMethodVisitor(visitor);
		
		visitor.visitMaxs(0, 0);
		
		visitor.visitEnd();
		
		context.setLastWasConstant(false);
		
	}
	
	public void visitFunctionCallNode(Node node, Context context, boolean root) throws CompilerLogicException {
		FunctionCallNode fcn = (FunctionCallNode) node;
		
		String funcName = fcn.getId().getValue();
		
		OperationStack stack = context.getOpStack();
		
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
			
			stack.push(new LabelNode(l0));
			stack.push(new LineNumberNode(fcn.getStart().getLine(), l0));
			stack.push(new MethodNode(INVOKESTATIC, func.getOwner(), funcName, func.getDescriptor(), func.isInterface()));
			context.setLastDescriptor(func.getReturnTypeDescriptor());
			
			if(!func.getReturnTypeDescriptor().equals("V") && root) {
				if(func.getReturnTypeDescriptor().equals("D") ||func.getReturnTypeDescriptor().equals("J")) {
					stack.push(new InsnNode(Opcodes.POP2));
				} else {
					stack.push(new InsnNode(Opcodes.POP));
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
					continue outer;
				}
			}
			
			Label l0 = new Label();
			
			stack.push(new LabelNode(l0));
			stack.push(new LineNumberNode(fcn.getStart().getLine(), l0));
			stack.push(new MethodNode(INVOKESTATIC, f.getOwner(), funcName, f.getDescriptor(), f.isInterface()));
			
			if(!f.getReturnTypeDescriptor().equals("V") && root) {
				if(f.getReturnTypeDescriptor().equals("D") || f.getReturnTypeDescriptor().equals("J")) {
					stack.push(new InsnNode(Opcodes.POP2));
				} else {
					stack.push(new InsnNode(Opcodes.POP));
				}
			}
			
			context.setLastWasConstant(false);
			context.setLastDescriptor(f.getReturnTypeDescriptor());
			return;
		}
		
		throw new CompilerLogicException("Function \'" + funcName + "\' does not exist in current scope.\nArgument types given: " + String.join(", ", Descriptors.descriptorToTypes("(" + descriptor + ")V")), fcn.getStart(), fcn.getEnd());
	}
	
	public void visitReturnNode(Node node, Context context) throws CompilerLogicException {
		
		OperationStack stack = context.getOpStack();
		
		Node value = (Node) node.getValue();
		
		if(value == null) {
			getDefaultReturn(stack, context);
			return;
		}
		
		context.getCompilerInstance().visit(value, context);
		
		String desc = context.getLastDescriptor();
		
		
		String methodDesc = Descriptors.typeToMethodDescriptor(context.getMethodReturnType());
		
		if(!desc.equals(methodDesc)) {
			throw new CompilerLogicException("Incorrect return type for function: " + Descriptors.descriptorToType(desc) + "\nExpected: " + context.getMethodReturnType(), value.getStart(), value.getEnd());
		}
		
		switch(desc) {
			case "C":
			case "B":
			case "I":
			case "S":
				stack.push(new InsnNode(IRETURN));
				context.setHasReturnedLast(true);
				break;
				
			case "J":
				stack.push(new InsnNode(LRETURN));
				context.setHasReturnedLast(true);
				break;
				
			case "D":
				stack.push(new InsnNode(DRETURN));
				context.setHasReturnedLast(true);
				break;
				
			case "F":
				stack.push(new InsnNode(FRETURN));
				context.setHasReturnedLast(true);
				break;
				
			case "Z":
				stack.push(new InsnNode(IRETURN));
				context.setHasReturnedLast(true);
				break;
			default:
				stack.push(new InsnNode(ARETURN));
				context.setHasReturnedLast(true);
				break;
		}
		
		context.setLastWasConstant(false);
		
	}
	
	private void getDefaultReturn(OperationStack stack, Context context) {
		
		if(context.getMethodReturnType().equals("void")) {
			stack.push(new InsnNode(RETURN));
			context.setHasReturnedLast(true);
			return;
		}
		
		if(Descriptors.typeIsPrimative(context.getMethodReturnType())) {
			switch(context.getMethodReturnType()) {
				case "char":
				case "byte":
				case "int":
				case "short":
					stack.push(new InsnNode(ICONST_0));
					stack.push(new InsnNode(IRETURN));
					context.setHasReturnedLast(true);
					break;
					
				case "long":
					stack.push(new InsnNode(LCONST_0));
					stack.push(new InsnNode(LRETURN));
					context.setHasReturnedLast(true);
					break;
					
				case "double":
					stack.push(new InsnNode(DCONST_0));
					stack.push(new InsnNode(DRETURN));
					context.setHasReturnedLast(true);
					break;
				case "float":
					stack.push(new InsnNode(FCONST_0));
					stack.push(new InsnNode(FRETURN));
					context.setHasReturnedLast(true);
					break;
				case "boolean":
					stack.push(new InsnNode(ICONST_0));
					stack.push(new InsnNode(IRETURN));
					context.setHasReturnedLast(true);
					break;
			}
			return;
		}
		
		stack.push(new InsnNode(ACONST_NULL));
		stack.push(new InsnNode(ARETURN));
		context.setHasReturnedLast(true);
		
	}
	
}
