package quaz.compiler.compiler.visitors;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.opStack.nodes.InsnNode;
import quaz.compiler.compiler.opStack.nodes.IntInsnNode;
import quaz.compiler.compiler.opStack.nodes.InvokeDynamicNode;
import quaz.compiler.compiler.opStack.nodes.JumpNode;
import quaz.compiler.compiler.opStack.nodes.LabelNode;
import quaz.compiler.compiler.opStack.nodes.LdcNode;
import quaz.compiler.compiler.opStack.nodes.MethodNode;
import quaz.compiler.compiler.opStack.nodes.OpNode;
import quaz.compiler.compiler.opStack.nodes.VarNode;
import quaz.compiler.compiler.values.LocalVariable;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.lexer.TokenType;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.classes.MemberAccessNode;
import quaz.compiler.parser.nodes.operation.BinaryOperationNode;
import quaz.compiler.parser.nodes.variable.VariableAccessNode;

public class OperationVisitor {

	public void visitBinaryOperationNode(Node node, Context context) throws CompilerLogicException {

		BinaryOperationNode bon = (BinaryOperationNode) node;

		if(bon.getLeft() instanceof MemberAccessNode) {

			// context.getVisitor().visitFieldInsn(opcode, owner, name, descriptor);

			new ClassesVisitor().putMemberAccessNode(bon.getLeft(), context, bon.getRight());

			return;
		}
		
		String leftDesc = null;
		
		int addedElements = 0;
		
		if(bon.getLeft() instanceof VariableAccessNode && bon.getType() == TokenType.EQUALS) {
			LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());

			if(var == null) {
				throw new CompilerLogicException(
						"Variable \'" + bon.getLeft().getValue() + "\' does not exist in the current scope.",
						bon.getLeft().getStart(), bon.getLeft().getEnd());
			}
			
			leftDesc = var.getDescriptor();
		}
		else {
			
			int length = context.getOpStack().size();
			
			context.getCompilerInstance().visit(bon.getLeft(), context);
			
			int newLength = context.getOpStack().size();
			
			addedElements = newLength - length;
			
			leftDesc = context.getLastDescriptor();
		}
		
		switch(leftDesc) {
			case "I":
				intOperation(bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
			case "D":
				doubleOperation(bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
			case "F":
				floatOperation(bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
			case "Z":
				booleanOperation(bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
			case "C":
				charOperation(bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
			case "B":
				byteOperation(bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
			case "J":
				longOperation(bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
			default:
				refOperation(leftDesc, bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
		}

	}

	private void refOperation(String leftDesc, BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {
		//MethodVisitor mv = context.getVisitor();
		
		OperationStack stack = context.getOpStack();
		
		if(leftDesc.equals("Ljava/lang/String;")) {
			switch(type) {
				case PLUS:
					concatStrings(bon, stack, addedElements, context);
					return;
				default:
					break;
			}
		}
		
		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();
		
		if(!rightDesc.equals(leftDesc)) {
			throw new CompilerLogicException("Expected " + Descriptors.descriptorToType(leftDesc) + ", got "
					+ Descriptors.descriptorToType(rightDesc), right.getStart(), right.getEnd());
		}

		switch(type) {
			case EQUALS:
				stack.push(new InsnNode(Opcodes.ASTORE));
				context.setLastDescriptor(leftDesc);
				break;
				
			case BOOL_TRI_EQ: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ACMPNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_TRI_NE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ACMPEQ, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_EQ: {
				stack.push(new MethodNode(Opcodes.INVOKEVIRTUAL, Descriptors.cropTypeDescriptor(leftDesc), "equals", "(Ljava/lang/Object;)Z", false));
				context.setLastDescriptor("Z");
				break;
			}
			
			case BOOL_NE: {
				stack.push(new MethodNode(Opcodes.INVOKEVIRTUAL, Descriptors.cropTypeDescriptor(leftDesc), "equals", "(Ljava/lang/Object;)Z", false));
				
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IFNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
				
			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}
		
		context.setLastWasConstant(false);
		
	}
	
	private void concatStrings(BinaryOperationNode bon, OperationStack stack, int addedElements, Context context) throws CompilerLogicException {
		if(addedElements != 0) {
			for(int i = 0; i < addedElements; i++) {
				stack.pop();
			}
		}
		
		StringBuilder descriptor = new StringBuilder("(");
		StringBuilder recipe = new StringBuilder();
		
		genStringConcatSingle(bon, context, stack, recipe, descriptor);
		
		descriptor.append(")Ljava/lang/String;");
		
		stack.push(new InvokeDynamicNode("makeConcatWithConstants", descriptor.toString(), new Handle(Opcodes.H_INVOKESTATIC, 
				"java/lang/invoke/StringConcatFactory", 
				"makeConcatWithConstants", 
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
				false), 
			new Object[] {recipe.toString()}));
	
		context.setLastDescriptor("Ljava/lang/String;");
	}
	
	private void genStringConcatSingle(BinaryOperationNode bon, Context context, OperationStack stack, StringBuilder recipe, StringBuilder descriptor) throws CompilerLogicException {
		Node left = bon.getLeft();
		if(left instanceof BinaryOperationNode && ((BinaryOperationNode) left).getType() == TokenType.PLUS) {
			genStringConcatSingle((BinaryOperationNode) left, context, stack, recipe, descriptor);
		}
		
		else {
			
			context.getCompilerInstance().visit(left, context);
			
			if(context.getLastWasConstant()) {
				ifLastWasConstantString(stack, recipe, descriptor, context, left);
			}
			else {
				descriptor.append(context.getLastDescriptor());
				recipe.append("\u0001");
			}
			
		}
		
		context.getCompilerInstance().visit(bon.getRight(), context);
		
		if(context.getLastWasConstant()) {
			ifLastWasConstantString(stack, recipe, descriptor, context, left);
		}
		else {
			descriptor.append(context.getLastDescriptor());
			recipe.append("\u0001");
		}
		
	}
	
	private void ifLastWasConstantString(OperationStack stack, StringBuilder recipe, StringBuilder descriptor, Context context, Node left) throws CompilerLogicException {
		//System.out.println(stack.peek());
		OpNode oNode = stack.pop();
		
		//System.out.println(context.getLastDescriptor());
		
		if(oNode instanceof InsnNode) {
			
			if(context.getLastDescriptor().equals("Z")) {
				
				if(oNode.getOpcode() == Opcodes.ICONST_0) {
					recipe.append("false");
				}
				else {
					recipe.append("true");
				}
				
			}
			
			else if(context.getLastDescriptor().equals("C")) {
				
				switch(oNode.getOpcode()) {
					
					case Opcodes.ICONST_0:
						recipe.append((char) 0);
						break;
					
					case Opcodes.ICONST_1:
						recipe.append((char) 1);
						break;
						
					case Opcodes.ICONST_2:
						recipe.append((char) 2);
						break;
						
					case Opcodes.ICONST_3:
						recipe.append((char) 3);
						break;
						
					case Opcodes.ICONST_4:
						recipe.append((char) 4);
						break;
						
					case Opcodes.ICONST_5:
						recipe.append((char) 5);
						break;
					
				}
				
			}
			
			else {
				recipe.append(((InsnNode) oNode).getValue());
			}
		}
		else if(oNode instanceof IntInsnNode) {
			
			if(context.getLastDescriptor().equals("C")) {
				// Since oNode.getValue() returns an Object, directly casting to char doesn't work because the Object is an Integer which cannot be cast to Character. The primative
				// types, however, can be casted.
				recipe.append( (char) (int) ((IntInsnNode) oNode).getValue());
			} else {
				recipe.append(((IntInsnNode) oNode).getValue());
			}
		}
		else if(oNode instanceof LdcNode) {
			//System.out.println("LDC");
			if(context.getLastDescriptor().equals("C")) {
				recipe.append((char) (int) ((LdcNode) oNode).getValue());
			} else {
				recipe.append(((LdcNode) oNode).getValue());
			}
		}
		else {
			// Shouldn't happen, handle just in case something went very wrong
			throw new CompilerLogicException("Unexpected constant value.", left.getStart(), left.getEnd());
		}
	}
	
	private void intOperation(BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {

		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();
		
		OperationStack stack = context.getOpStack();
		
		if(!rightDesc.equals("I")) {

			switch(rightDesc) {
				case "D":
					stack.push(new InsnNode(Opcodes.D2I));
					break;
				case "F":
					stack.push(new InsnNode(Opcodes.F2I));
					break;
				case "Z":
				case "C":
				case "B":
					break;
					
				case "Ljava/lang/String;": {
					if(type == TokenType.PLUS) {
						concatStrings(bon, stack, addedElements, context);
						return;
					}
				}
					

				default:
					throw new CompilerLogicException("Expected int, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}

		}
		
		switch(type) {
			case PLUS:
				stack.push(new InsnNode(Opcodes.IADD));
				context.setLastDescriptor("I");
				break;

			case MINUS:
				stack.push(new InsnNode(Opcodes.ISUB));
				context.setLastDescriptor("I");
				break;

			case MULTIPLY:
				stack.push(new InsnNode(Opcodes.IMUL));
				context.setLastDescriptor("I");
				break;
			case DIVIDE:
				stack.push(new InsnNode(Opcodes.IDIV));
				context.setLastDescriptor("I");
				break;

			case MODULUS:
				stack.push(new InsnNode(Opcodes.IREM));
				context.setLastDescriptor("I");
				break;

			case BIT_OR:
				stack.push(new InsnNode(Opcodes.IOR));
				context.setLastDescriptor("I");
				break;
			case BIT_AND:
				stack.push(new InsnNode(Opcodes.IAND));
				context.setLastDescriptor("I");
				break;
				
			case BIT_XOR:
				stack.push(new InsnNode(Opcodes.IXOR));
				context.setLastDescriptor("I");
				break;
				
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPEQ, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPGE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPGT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPLE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPLT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
				
			case EQUALS: {

				if(bon.getLeft() instanceof VariableAccessNode) {
					LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());

					if(var == null) {
						throw new CompilerLogicException(
								"Variable \'" + bon.getLeft().getValue() + "\' does not exist in the current scope.",
								bon.getLeft().getStart(), bon.getLeft().getEnd());
					}

					stack.push(new VarNode(Opcodes.ISTORE, var.getIndex()));
				}

				else {
					throw new CompilerLogicException("Can only assign to variables", bon.getLeft().getStart(),
							bon.getLeft().getEnd());
				}

				context.setLastDescriptor("I");
				break;
			}

			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}
		context.setLastWasConstant(false);
	}
	
	private void charOperation(BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {

		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();
		
		OperationStack stack = context.getOpStack();
		
		if(!rightDesc.equals("C")) {

			switch(rightDesc) {
				case "D":
					stack.push(new InsnNode(Opcodes.D2I));
					break;
				case "F":
					stack.push(new InsnNode(Opcodes.F2I));
					break;
				case "Z":
				case "I":
					break;
					
				case "Ljava/lang/String;": {
					if(type == TokenType.PLUS) {
						concatStrings(bon, stack, addedElements, context);
						return;
					}
				}
					

				default:
					throw new CompilerLogicException("Expected char, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}

		}
		
		switch(type) {
			case PLUS:
				stack.push(new InsnNode(Opcodes.IADD));
				context.setLastDescriptor("C");
				break;

			case MINUS:
				stack.push(new InsnNode(Opcodes.ISUB));
				context.setLastDescriptor("C");
				break;

			case MULTIPLY:
				stack.push(new InsnNode(Opcodes.IMUL));
				context.setLastDescriptor("C");
				break;
			case DIVIDE:
				stack.push(new InsnNode(Opcodes.IDIV));
				context.setLastDescriptor("C");
				break;

			case MODULUS:
				stack.push(new InsnNode(Opcodes.IREM));
				context.setLastDescriptor("C");
				break;

			case BIT_OR:
				stack.push(new InsnNode(Opcodes.IOR));
				context.setLastDescriptor("C");
				break;
			case BIT_AND:
				stack.push(new InsnNode(Opcodes.IAND));
				context.setLastDescriptor("C");
				break;
				
			case BIT_XOR:
				stack.push(new InsnNode(Opcodes.IXOR));
				context.setLastDescriptor("C");
				break;
				
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPEQ, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPGE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPGT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPLE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPLT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
				
			case EQUALS: {

				if(bon.getLeft() instanceof VariableAccessNode) {
					LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());

					if(var == null) {
						throw new CompilerLogicException(
								"Variable \'" + bon.getLeft().getValue() + "\' does not exist in the current scope.",
								bon.getLeft().getStart(), bon.getLeft().getEnd());
					}

					stack.push(new VarNode(Opcodes.ISTORE, var.getIndex()));
				}

				else {
					throw new CompilerLogicException("Can only assign to variables", bon.getLeft().getStart(),
							bon.getLeft().getEnd());
				}

				context.setLastDescriptor("C");
				break;
			}

			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}
		context.setLastWasConstant(false);
	}

	private void doubleOperation(BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {

		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();
		
		OperationStack stack = context.getOpStack();
		
		if(!rightDesc.equals("D")) {
			switch(rightDesc) {
				case "I":
					stack.push(new InsnNode(Opcodes.I2D));
					break;
				case "F":
					stack.push(new InsnNode(Opcodes.F2D));
					break;
					
				case "Ljava/lang/String;": {
					if(type == TokenType.PLUS) {
						concatStrings(bon, stack, addedElements, context);
						return;
					}
				}
					
				default:
					throw new CompilerLogicException("Expected double, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}
		}

		switch(type) {
			case PLUS:
				stack.push(new InsnNode(Opcodes.DADD));
				context.setLastDescriptor("D");
				break;

			case MINUS:
				stack.push(new InsnNode(Opcodes.DSUB));
				context.setLastDescriptor("D");
				break;

			case MULTIPLY:
				stack.push(new InsnNode(Opcodes.DMUL));
				context.setLastDescriptor("D");
				break;
			case DIVIDE:
				stack.push(new InsnNode(Opcodes.DDIV));
				context.setLastDescriptor("D");
				break;

			case MODULUS:
				stack.push(new InsnNode(Opcodes.DREM));
				context.setLastDescriptor("D");
				break;
			
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
			
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.DCMPL));
				
				stack.push(new JumpNode(Opcodes.IFNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.DCMPL));
				
				stack.push(new JumpNode(Opcodes.IFEQ, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.DCMPL));
				
				stack.push(new JumpNode(Opcodes.IFGE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.DCMPL));
				
				stack.push(new JumpNode(Opcodes.IFGT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.DCMPL));
				
				stack.push(new JumpNode(Opcodes.IFLE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.DCMPL));
				
				stack.push(new JumpNode(Opcodes.IFLT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
				
			case EQUALS: {

				LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());

				stack.push(new VarNode(Opcodes.DSTORE, var.getIndex()));
				context.setLastDescriptor("D");
				break;
			}

			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}

		context.setLastWasConstant(false);

	}

	private void floatOperation(BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {

		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();

		OperationStack stack = context.getOpStack();
		
		if(!rightDesc.equals("F")) {
			switch(rightDesc) {
				case "D":
					stack.push(new InsnNode(Opcodes.D2F));
					break;
				case "I":
					stack.push(new InsnNode(Opcodes.I2F));
					break;
				
				case "Ljava/lang/String;": {
					if(type == TokenType.PLUS) {
						concatStrings(bon, stack, addedElements, context);
						return;
					}
				}
					
				default:
					throw new CompilerLogicException("Expected float, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}
		}

		switch(type) {
			case PLUS:
				stack.push(new InsnNode(Opcodes.FADD));
				context.setLastDescriptor("F");
				break;

			case MINUS:
				stack.push(new InsnNode(Opcodes.FSUB));
				context.setLastDescriptor("F");
				break;

			case MULTIPLY:
				stack.push(new InsnNode(Opcodes.FMUL));
				context.setLastDescriptor("F");
				break;
			case DIVIDE:
				stack.push(new InsnNode(Opcodes.FDIV));
				context.setLastDescriptor("F");
				break;

			case MODULUS:
				stack.push(new InsnNode(Opcodes.FREM));
				context.setLastDescriptor("F");
				break;
				
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
				
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.FCMPL));
				
				stack.push(new JumpNode(Opcodes.IFNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.FCMPL));
				
				stack.push(new JumpNode(Opcodes.IFEQ, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.FCMPL));
				
				stack.push(new JumpNode(Opcodes.IFGE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.FCMPL));
				
				stack.push(new JumpNode(Opcodes.IFGT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.FCMPL));
				
				stack.push(new JumpNode(Opcodes.IFLE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.FCMPL));
				
				stack.push(new JumpNode(Opcodes.IFLT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}

			case EQUALS: {

				LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());
				
				context.setLastDescriptor("F");
				stack.push(new VarNode(Opcodes.FSTORE, var.getIndex()));
				break;
			}

			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}
		
		context.setLastWasConstant(false);

	}

	private void booleanOperation(BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {

		OperationStack stack = context.getOpStack();

		switch(type) {
			
			case PLUS: {
				
				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();
				
				if(rightDesc.equals("Ljava/lang/String;")) {
					if(type == TokenType.PLUS) {
						concatStrings(bon, stack, addedElements, context);
						return;
					}
				}
				
				throw new CompilerLogicException("Invalid Operation" + Descriptors.descriptorToType(rightDesc),
						right.getStart(), right.getEnd());
			}
			
			case AND: {

				Label notTrue = new Label();
				Label endIf = new Label();

				stack.push(new JumpNode(Opcodes.IFEQ, notTrue));

				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();

				out: if(!rightDesc.equals("Z")) {

					if(rightDesc.equals("I") || rightDesc.equals("C")) {
						break out;
					}

					throw new CompilerLogicException("Expected boolean, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
				}

				stack.push(new JumpNode(Opcodes.IFEQ, notTrue));

				stack.push(new InsnNode(Opcodes.ICONST_1));

				stack.push(new JumpNode(Opcodes.GOTO, endIf));

				stack.push(new LabelNode(notTrue));

				stack.push(new InsnNode(Opcodes.ICONST_0));

				stack.push(new LabelNode(endIf));

				break;
			}

			case OR: {

				Label isTrue = new Label();
				Label notTrue = new Label();
				Label endIf = new Label();

				stack.push(new JumpNode(Opcodes.IFNE, isTrue));

				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();

				out: if(!rightDesc.equals("Z")) {
					if(rightDesc.equals("I") || rightDesc.equals("C")) {
						break out;
					}
					throw new CompilerLogicException(rightDesc, right.getStart(), right.getEnd());
				}

				stack.push(new JumpNode(Opcodes.IFEQ, notTrue));

				stack.push(new LabelNode(isTrue));

				stack.push(new InsnNode(Opcodes.ICONST_1));

				stack.push(new JumpNode(Opcodes.GOTO, endIf));

				stack.push(new LabelNode(notTrue));

				stack.push(new InsnNode(Opcodes.ICONST_0));

				stack.push(new LabelNode(endIf));

				break;
			}
			
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
				Label falseL = new Label();
				Label end = new Label();
				
				
				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();

				out: if(!rightDesc.equals("Z")) {

					if(rightDesc.equals("I") || rightDesc.equals("C")) {
						break out;
					}

					throw new CompilerLogicException("Expected boolean, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
				}
				
				stack.push(new JumpNode(Opcodes.IF_ICMPNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				Label falseL = new Label();
				Label end = new Label();
				
				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();

				out: if(!rightDesc.equals("Z")) {

					if(rightDesc.equals("I") || rightDesc.equals("C")) {
						break out;
					}

					throw new CompilerLogicException("Expected boolean, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
				}
				
				stack.push(new JumpNode(Opcodes.IF_ICMPEQ, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}

			case EQUALS: {

				LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());
				
				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();

				out: if(!rightDesc.equals("Z")) {

					if(rightDesc.equals("I") || rightDesc.equals("C")) {
						break out;
					}

					throw new CompilerLogicException("Expected boolean, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
				}
				
				stack.push(new VarNode(Opcodes.ISTORE, var.getIndex()));
				break;
			}
			
			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}

		context.setLastDescriptor("Z");
		context.setLastWasConstant(false);
	}
	
	private void byteOperation(BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {

		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();
		
		OperationStack stack = context.getOpStack();
		
		if(!rightDesc.equals("B")) {

			switch(rightDesc) {
				case "Z":
				case "I":
				case "C":
					stack.push(new InsnNode(Opcodes.I2B));
					break;
					
				case "Ljava/lang/String;": {
					if(type == TokenType.PLUS) {
						concatStrings(bon, stack, addedElements, context);
						return;
					}
				}
					

				default:
					throw new CompilerLogicException("Expected byte, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}

		}
		
		switch(type) {
			case PLUS:
				stack.push(new InsnNode(Opcodes.IADD));
				context.setLastDescriptor("B");
				break;

			case MINUS:
				stack.push(new InsnNode(Opcodes.ISUB));
				context.setLastDescriptor("B");
				break;

			case MULTIPLY:
				stack.push(new InsnNode(Opcodes.IMUL));
				context.setLastDescriptor("B");
				break;
			case DIVIDE:
				stack.push(new InsnNode(Opcodes.IDIV));
				context.setLastDescriptor("B");
				break;

			case MODULUS:
				stack.push(new InsnNode(Opcodes.IREM));
				context.setLastDescriptor("B");
				break;

			case BIT_OR:
				stack.push(new InsnNode(Opcodes.IOR));
				context.setLastDescriptor("B");
				break;
			case BIT_AND:
				stack.push(new InsnNode(Opcodes.IAND));
				context.setLastDescriptor("B");
				break;
				
			case BIT_XOR:
				stack.push(new InsnNode(Opcodes.IXOR));
				context.setLastDescriptor("B");
				break;
				
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPEQ, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPGE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPGT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPLE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new JumpNode(Opcodes.IF_ICMPLT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
				
			case EQUALS: {

				if(bon.getLeft() instanceof VariableAccessNode) {
					LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());

					if(var == null) {
						throw new CompilerLogicException(
								"Variable \'" + bon.getLeft().getValue() + "\' does not exist in the current scope.",
								bon.getLeft().getStart(), bon.getLeft().getEnd());
					}

					stack.push(new VarNode(Opcodes.ISTORE, var.getIndex()));
				}

				else {
					throw new CompilerLogicException("Can only assign to variables", bon.getLeft().getStart(),
							bon.getLeft().getEnd());
				}

				context.setLastDescriptor("B");
				break;
			}

			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}
		context.setLastWasConstant(false);
	}
	
	private void longOperation(BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {

		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();
		
		OperationStack stack = context.getOpStack();
		
		if(!rightDesc.equals("J")) {

			switch(rightDesc) {
				case "D":
					stack.push(new InsnNode(Opcodes.D2L));
					break;
				case "F":
					stack.push(new InsnNode(Opcodes.F2L));
					break;
				case "Z":
				case "C":
				case "B":
				case "I":
					stack.push(new InsnNode(Opcodes.I2L));
					break;
					
				case "Ljava/lang/String;": {
					if(type == TokenType.PLUS) {
						concatStrings(bon, stack, addedElements, context);
						return;
					}
				}
					

				default:
					throw new CompilerLogicException("Expected int, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}

		}
		
		switch(type) {
			case PLUS:
				stack.push(new InsnNode(Opcodes.LADD));
				context.setLastDescriptor("J");
				break;

			case MINUS:
				stack.push(new InsnNode(Opcodes.LSUB));
				context.setLastDescriptor("J");
				break;

			case MULTIPLY:
				stack.push(new InsnNode(Opcodes.LMUL));
				context.setLastDescriptor("J");
				break;
			case DIVIDE:
				stack.push(new InsnNode(Opcodes.LDIV));
				context.setLastDescriptor("J");
				break;

			case MODULUS:
				stack.push(new InsnNode(Opcodes.LREM));
				context.setLastDescriptor("J");
				break;

			case BIT_OR:
				stack.push(new InsnNode(Opcodes.LOR));
				context.setLastDescriptor("J");
				break;
			case BIT_AND:
				stack.push(new InsnNode(Opcodes.LAND));
				context.setLastDescriptor("J");
				break;
				
			case BIT_XOR:
				stack.push(new InsnNode(Opcodes.LXOR));
				context.setLastDescriptor("J");
				break;
				
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
			
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.LCMP));
				
				stack.push(new JumpNode(Opcodes.IFNE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.LCMP));
				
				stack.push(new JumpNode(Opcodes.IFEQ, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.LCMP));
				
				stack.push(new JumpNode(Opcodes.IFGE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.LCMP));
				
				stack.push(new JumpNode(Opcodes.IFGT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.LCMP));
				
				stack.push(new JumpNode(Opcodes.IFLE, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				stack.push(new InsnNode(Opcodes.LCMP));
				
				stack.push(new JumpNode(Opcodes.IFLT, falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_1));
				
				stack.push(new JumpNode(Opcodes.GOTO, end));
				
				stack.push(new LabelNode(falseL));
				
				stack.push(new InsnNode(Opcodes.ICONST_0));
				
				stack.push(new LabelNode(end));
				
				context.setLastDescriptor("Z");
				
				break;
			}
				
			case EQUALS: {

				if(bon.getLeft() instanceof VariableAccessNode) {
					LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());

					if(var == null) {
						throw new CompilerLogicException(
								"Variable \'" + bon.getLeft().getValue() + "\' does not exist in the current scope.",
								bon.getLeft().getStart(), bon.getLeft().getEnd());
					}

					stack.push(new VarNode(Opcodes.LSTORE, var.getIndex()));
				}

				else {
					throw new CompilerLogicException("Can only assign to variables", bon.getLeft().getStart(),
							bon.getLeft().getEnd());
				}

				context.setLastDescriptor("J");
				break;
			}

			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}
		context.setLastWasConstant(false);
	}
	
}
