package quaz.compiler.compiler.visitors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import quaz.compiler.compiler.Compiler;
import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.opStack.nodes.FieldNode;
import quaz.compiler.compiler.opStack.nodes.InsnNode;
import quaz.compiler.compiler.opStack.nodes.LabelNode;
import quaz.compiler.compiler.opStack.nodes.LineNumberNode;
import quaz.compiler.compiler.opStack.nodes.MethodNode;
import quaz.compiler.compiler.opStack.nodes.TypeNode;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.classes.CastNode;
import quaz.compiler.parser.nodes.classes.InstanceNode;
import quaz.compiler.parser.nodes.classes.MemberAccessNode;
import quaz.compiler.parser.nodes.function.FunctionCallNode;
import quaz.compiler.parser.nodes.variable.VariableAccessNode;

public class ClassesVisitor {
	
	public void visitInstanceNode(Node node, Context context) throws CompilerLogicException {

		InstanceNode in = (InstanceNode) node;
		
		OperationStack stack = context.getOpStack();

		Compiler ci = context.getCompilerInstance();

		String typeGiven = in.getType();

		String type = typeGiven.contains("/") ? typeGiven : context.getTypeReferences().get(typeGiven);

		if(type == null) {
			throw new CompilerLogicException("Unknown type " + typeGiven + " used in current scope", in.getStart(),
					in.getEnd());
		}

		//mv.visitTypeInsn(Opcodes.NEW, type);
		stack.push(new TypeNode(Opcodes.NEW, type));
		stack.push(new InsnNode(Opcodes.DUP));

		String descriptor = "";

		for(Node n : (Node[]) in.getArguments().getValue()) {
			ci.visit(n, context);
			descriptor += context.getLastDescriptor();
		}
		
		Class<?> parent;
		
		try {
			parent = Class.forName(Descriptors.descriptorToType("L" + type + ";"));
		} catch(ClassNotFoundException e) {
			throw new CompilerLogicException("Unknown type " + typeGiven + " used in current scope", in.getStart(),
					in.getEnd());
		}
		
		Class<?>[] givenParams = Descriptors.descriptorToClasses("(" + descriptor + ")V");
		
		try {
			Constructor<?> c = parent.getDeclaredConstructor(givenParams);
			if(!Modifier.isPublic(c.getModifiers()))
				throw new CompilerLogicException("Constructor for type " + Descriptors.descriptorToType("L" + type + ";") + " is not accessible.\nArgument types given: " + String.join(", ", Descriptors.descriptorToTypes("(" + descriptor + ")V")), in.getStart(), in.getEnd());
		} catch(NoSuchMethodException e) {
			throw new CompilerLogicException("Constructor for type " + Descriptors.descriptorToType("L" + type + ";") + " does not exist.\nArgument types given: " + String.join(", ", Descriptors.descriptorToTypes("(" + descriptor + ")V")), in.getStart(), in.getEnd());
		} catch(SecurityException e) {
			e.printStackTrace();
		}

		//mv.visitMethodInsn(Opcodes.INVOKESPECIAL, type, "<init>", "(" + descriptor + ")V", false);
		stack.push(new MethodNode(Opcodes.INVOKESPECIAL, type, "<init>", "(" + descriptor + ")V", false));
		
		context.setLastDescriptor("L" + type + ";");
		context.setLastWasConstant(false);
	}

	public void visitMemberAccessNode(Node node, Context context, boolean root) throws CompilerLogicException {

		MemberAccessNode man = (MemberAccessNode) node;
		
		Compiler ci = context.getCompilerInstance();
		
		String leftDesc = null;
		String leftDescType = null;
		
		if(man.getLeft() instanceof VariableAccessNode && context.getTypeReferences().containsKey(man.getLeft().getValue())) {
			
			String desc = context.getTypeReferences().get(man.getLeft().getValue());
			
			leftDesc = Descriptors.cropTypeDescriptor(desc);
			leftDescType = desc;
			
		} else {
			ci.visit(man.getLeft(), context);

			leftDesc = Descriptors.cropTypeDescriptor(context.getLastDescriptor());
			
			leftDescType = context.getLastDescriptor();
		}
		
		Node right = man.getRight();
		
		OperationStack stack = context.getOpStack();
		
		if(right instanceof FunctionCallNode) {
			memberFunctionCall(right, leftDesc, leftDescType, ci, stack, man, context, root);
		}

		else if(right instanceof VariableAccessNode) {
			memberAccess(right, leftDesc, leftDescType, stack, man, context, false);
		}
		
		else { // Member Access Node
			memberAccessNode(right, leftDesc, leftDescType, ci, stack, man, context, false, root);
		}
		context.setLastWasConstant(false);
		
	}
	
	public void putMemberAccessNode(Node node, Context context, Node val) throws CompilerLogicException {
		
		MemberAccessNode man = (MemberAccessNode) node;
		
		Compiler ci = context.getCompilerInstance();
		
		ci.visit(man.getLeft(), context);

		String leftDesc = Descriptors.cropTypeDescriptor(context.getLastDescriptor());
		
		String leftDescType = context.getLastDescriptor();
		
		ci.visit(val, context);
		
		Node right = man.getRight();
		
		OperationStack stack = context.getOpStack();
		
		if(right instanceof FunctionCallNode) {
			throw new CompilerLogicException("Cannot set a function", man.getStart(), man.getEnd());
		}

		else if(right instanceof VariableAccessNode) {
			memberAccess(right, leftDesc, leftDescType, stack, man, context, true);
		}
		
		else { // Member Access Node
			memberAccessNode(right, leftDesc, leftDescType, ci, stack, man, context, true, false);
		}
		context.setLastWasConstant(false);
		
	}
	
	private void memberAccessNode(Node right, String leftDesc, String leftDescType, Compiler ci, OperationStack stack, MemberAccessNode man, Context context, boolean put, boolean root) throws CompilerLogicException {
		Node left = ((MemberAccessNode) right).getLeft();
		
		//System.out.println(left);
		
		if(left instanceof FunctionCallNode) {
			memberFunctionCall(left, leftDesc, leftDescType, ci, stack, man, context, root);
			
			String newLeftDesc = Descriptors.cropTypeDescriptor(context.getLastDescriptor());
			
			String newLeftDescType = context.getLastDescriptor();
			
			Node newRight = ((MemberAccessNode) right).getRight();
			
			if(newRight instanceof FunctionCallNode) {
				memberFunctionCall(newRight, newLeftDesc, newLeftDescType, ci, stack, (MemberAccessNode) right, context, root);
			}

			else if(newRight instanceof VariableAccessNode) {
				memberAccess(newRight, newLeftDesc, newLeftDescType, stack, (MemberAccessNode) right, context, put);
			}
			else {
				memberAccessNode(newRight, newLeftDesc, newLeftDescType, ci, stack, man, context, put, root);
			}
			
		}
		else if(left instanceof VariableAccessNode) {
			
			memberAccess(left, leftDesc, leftDescType, stack, man, context, put);
			
			String newLeftDesc = Descriptors.cropTypeDescriptor(context.getLastDescriptor());
			
			String newLeftDescType = context.getLastDescriptor();
			
			Node newRight = ((MemberAccessNode) right).getRight();
			
			if(newRight instanceof FunctionCallNode) {
				
				if(put) {
					throw new CompilerLogicException("Cannot set a function", man.getStart(), man.getEnd());
				}
				
				memberFunctionCall(newRight, newLeftDesc, newLeftDescType, ci, stack, (MemberAccessNode) right, context, root);
			}

			else if(newRight instanceof VariableAccessNode) {
				memberAccess(newRight, newLeftDesc, newLeftDescType, stack, (MemberAccessNode) right, context, put);
			}
			
			else {
				memberAccessNode(newRight, newLeftDesc, newLeftDescType, ci, stack, man, context, put, root);
			}
			
		}
		
	}
	
	private void memberFunctionCall(Node right, String leftDesc, String leftDescType, Compiler ci, OperationStack stack, MemberAccessNode man, Context context, boolean root) throws CompilerLogicException {
		String funcName = ((FunctionCallNode) right).getId().getValue();
		
		Class<?> parent = Descriptors.descriptorToClass(leftDesc);
		
		Node[] args = (Node[]) ((FunctionCallNode) right).getArguments().getValue();
		
		String descriptor = "";
		
		for(Node n : args) {
			ci.visit(n, context);
			descriptor += context.getLastDescriptor();
		}
		
		Class<?>[] givenParams = Descriptors.descriptorToClasses("(" + descriptor + ")V");
		
		boolean isStatic = false;
		
		String returnTypeDesc = "";
		
		// Check if method exists.
		try {
			Method m = parent.getDeclaredMethod(funcName, givenParams);
			
			isStatic = Modifier.isStatic(m.getModifiers());
			
			returnTypeDesc = Type.getDescriptor(m.getReturnType());
			
			if(!Modifier.isPublic(m.getModifiers()))
				throw new CompilerLogicException("Function \'" + funcName + "\' for type " + Descriptors.descriptorToType(leftDescType) + " is not accessible.\nArgument types given: " + String.join(", ", Descriptors.descriptorToTypes("(" + descriptor + ")V")), right.getStart(), right.getEnd());
		} catch(NoSuchMethodException e) {
			throw new CompilerLogicException("Function \'" + funcName + "\' does not exist in type " + Descriptors.descriptorToType(leftDescType) + ".\nArgument types given: " + String.join(", ", Descriptors.descriptorToTypes("(" + descriptor + ")V")), right.getStart(), right.getEnd());
		} catch(SecurityException e) {
			e.printStackTrace();
		}
		
		Label l0 = new Label();
		
		stack.push(new LabelNode(l0));
		stack.push(new LineNumberNode(man.getStart().getLine(), l0));
		stack.push(new MethodNode(isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, leftDesc, funcName, "(" + descriptor + ")" + returnTypeDesc, false));
		
		if(!returnTypeDesc.equals("V") && root) {
			if(returnTypeDesc.equals("D") || returnTypeDesc.equals("J")) {
				stack.push(new InsnNode(Opcodes.POP2));
			} else {
				stack.push(new InsnNode(Opcodes.POP));
			}
		}
		
		context.setLastDescriptor(returnTypeDesc);
	}
	
	private void memberAccess(Node right, String leftDesc, String leftDescType, OperationStack stack, MemberAccessNode man, Context context, boolean put) throws CompilerLogicException {
		Class<?> parent = Descriptors.descriptorToClass(leftDesc);
		
		String variableName = ((VariableAccessNode) right).getName();
		
		boolean isStatic = false;
		
		String desc = "";
		
		try {
			Field f = parent.getDeclaredField(variableName);
			isStatic = Modifier.isStatic(f.getModifiers());
			
			Class<?> type = f.getType();
			
			desc = Type.getDescriptor(type);
			
			if(!Modifier.isPublic(f.getModifiers()))
				throw new CompilerLogicException("Field \'" + variableName + "\' in type " + Descriptors.descriptorToType(leftDescType) + " is not accessible.", right.getStart(), right.getEnd());
		} catch(NoSuchFieldException e) {
			throw new CompilerLogicException("Field \'" + variableName + "\' in type " + Descriptors.descriptorToType(leftDescType) + " does not exist.", right.getStart(), right.getEnd());
		} catch(SecurityException e) {
			e.printStackTrace();
		}
		
		Label l0 = new Label();

		stack.push(new LabelNode(l0));
		stack.push(new LineNumberNode(man.getStart().getLine(), l0));
		
		int op = put ? (isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD) : (isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD);
		
		stack.push(new FieldNode(op, leftDesc, variableName, desc));
		
		context.setLastDescriptor(desc);
	}
	
	/*
	 * The implementation of this method is inside the preprocessor
	 */
	public void visitImportNode(Node node, Context context) {}

	public void visitPackageNode(Node node, Context context) {}
	
	public void visitCastNode(Node node, Context context) throws CompilerLogicException {
		
		CastNode cn = (CastNode) node;
		
		context.getCompilerInstance().visit(cn.getOrginal(), context);

		String leftDesc = context.getLastDescriptor();
		
		String typeGiven = cn.getCastType();
		
		String desc = "";
		
		if(Descriptors.typeIsPrimative(typeGiven)) {
			desc = Descriptors.typeToMethodDescriptor(typeGiven);			
		}
		else {
			String type = typeGiven.contains(".") ? typeGiven : context.getTypeReferences().get(typeGiven);
			
			desc = Descriptors.typeToMethodDescriptor(type);
		}
		
		//MethodVisitor mv = context.getVisitor();
		
		OperationStack stack = context.getOpStack();
		
		if(Descriptors.isPrimative(leftDesc)) {
			
			switch(leftDesc) {
				case "I":
					switch(desc) {
						case "I":
							break;
						case "D":
							stack.push(new InsnNode(Opcodes.I2D));
							context.setLastDescriptor("D");
							break;
						case "F":
							stack.push(new InsnNode(Opcodes.I2F));
							context.setLastDescriptor("F");
							break;
						case "Z":
							context.setLastDescriptor("Z");
							break;
						
						case "Ljava/lang/String;": {
							stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
							context.setLastDescriptor("Ljava/lang/String;");
							break;
						}
						default:
							throw new CompilerLogicException("Cannot cast from int to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
					}
					break;
					
				case "C":
					switch(desc) {
						case "C":
							break;
						case "I":
							context.setLastDescriptor("I");
							break;
						case "D":
							stack.push(new InsnNode(Opcodes.I2D));
							context.setLastDescriptor("D");
							break;
						case "F":
							stack.push(new InsnNode(Opcodes.I2F));
							context.setLastDescriptor("F");
							break;
						case "Z":
							context.setLastDescriptor("Z");
							break;
						
						case "Ljava/lang/String;": {
							stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Character", "toString", "(C)Ljava/lang/String;", false));
							context.setLastDescriptor("Ljava/lang/String;");
							break;
						}
						default:
							throw new CompilerLogicException("Cannot cast from char to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
					}
					break;	
				
				case "D":
					switch(desc) {
						case "D":
							break;
						case "I":
							stack.push(new InsnNode(Opcodes.D2I));
							context.setLastDescriptor("I");
							break;
						case "F":
							stack.push(new InsnNode(Opcodes.D2F));
							context.setLastDescriptor("F");
							break;
							
						case "Ljava/lang/String;": {
							stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
							context.setLastDescriptor("Ljava/lang/String;");
							break;
						}
							
						default:
							throw new CompilerLogicException("Cannot cast from double to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
					}
					break;
				case "F":
					switch(desc) {
						case "F":
							break;
						case "I":
							stack.push(new InsnNode(Opcodes.F2I));
							context.setLastDescriptor("I");
							break;
						case "D":
							stack.push(new InsnNode(Opcodes.F2D));
							context.setLastDescriptor("D");
							break;
							
						case "Ljava/lang/String;": {
							stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
							context.setLastDescriptor("Ljava/lang/String;");
							break;
						}
							
						default:
							throw new CompilerLogicException("Cannot cast from float to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
					}
					break;
				case "Z":
					switch(desc) {
						case "Z":
							break;
						case "I":
							context.setLastDescriptor("I");
							break;
							
						case "Ljava/lang/String;": {
							stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;", false));
							context.setLastDescriptor("Ljava/lang/String;");
							break;
						}
							
						default:
							throw new CompilerLogicException("Cannot cast from boolean to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
					}
			}
			
			context.setLastWasConstant(false);
			
		}
		else {
			Class<?> leftClass =  Descriptors.descriptorToClass(Descriptors.descriptorToType(leftDesc));
			
			if(leftClass.isAssignableFrom(String.class)) {
				switch(desc) {
					case "I": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false));
						context.setLastDescriptor("I");
						context.setLastWasConstant(false);
						return;
					}
					case "C": {
						stack.push(new InsnNode(Opcodes.ICONST_0));
						stack.push(new MethodNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false));
						context.setLastDescriptor("C");
						context.setLastWasConstant(false);
						return;
					}
					case "D": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Double", "parseDouble", "(Ljava/lang/String;)D", false));
						context.setLastDescriptor("D");
						context.setLastWasConstant(false);
						return;
					}
					
					case "F": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false));
						context.setLastDescriptor("F");
						context.setLastWasConstant(false);
						return;
					}
					
					case "Z": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false));
						context.setLastDescriptor("Z");
						context.setLastWasConstant(false);
						return;
					}
						
				}
				
			}
			
			if(Descriptors.isPrimative(desc)) {
				throw new CompilerLogicException("Cannot cast from " + Descriptors.descriptorToType(leftDesc) + " to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
			}
			
			Class<?> rightClass = Descriptors.descriptorToClass(Descriptors.descriptorToType(desc));
			
			if(!leftClass.isAssignableFrom(rightClass)) {
				throw new CompilerLogicException("Cannot cast from " + Descriptors.descriptorToType(leftDesc) + " to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
			}
			
			stack.push(new TypeNode(Opcodes.CHECKCAST, Descriptors.cropTypeDescriptor(desc)));
			context.setLastDescriptor(desc);
			
		}
		
		context.setLastWasConstant(false);
		
	}

}
