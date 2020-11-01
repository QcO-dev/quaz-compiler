package quaz.compiler.compiler.visitors;

import java.util.ArrayList;
import java.util.Collections;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Compiler;
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
				intOperation(bon, bon.getType(), bon.getRight(), context);
				break;
			case "D":
				doubleOperation(bon, bon.getType(), bon.getRight(), context);
				break;
			case "F":
				floatOperation(bon, bon.getType(), bon.getRight(), context);
				break;
			case "Z":
				booleanOperation(bon, bon.getType(), bon.getRight(), context);
				break;
			default:
				refOperation(leftDesc, bon, bon.getType(), bon.getRight(), context, addedElements);
				break;
		}

	}

	private void refOperation(String leftDesc, BinaryOperationNode bon, TokenType type, Node right, Context context, int addedElements)
			throws CompilerLogicException {
		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();
		
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
		
		//context.setLastDescriptor(leftDesc);
	}
	
	private void concatStrings(BinaryOperationNode bon, OperationStack stack, int addedElements, Context context) throws CompilerLogicException {
		if(addedElements != 0)
			for(int i = 0; i <= addedElements; i++) {
				stack.pop();
			}
		
		Node rightAdd = bon;
		
		ArrayList<Object> descriptorList = new ArrayList<>();
		ArrayList<Object> recipeList = new ArrayList<>();
		
		Compiler ci = context.getCompilerInstance();
		
		while(rightAdd instanceof BinaryOperationNode && ((BinaryOperationNode) rightAdd).getType() == TokenType.PLUS) {
			
			
			ci.visit(((BinaryOperationNode) rightAdd).getRight(), context);
			
			if(context.getLastWasConstant()) {
				
				OpNode oNode = stack.pop();
				
				if(oNode instanceof InsnNode) {
					recipeList.add(((InsnNode) oNode).getValue());
				}
				else if(oNode instanceof IntInsnNode) {
					recipeList.add(((IntInsnNode) oNode).getValue());
				}
				else if(oNode instanceof LdcNode) {
					recipeList.add(((LdcNode) oNode).getValue());
				}
				else {
					// Shouldn't happen, handle just in case something went very wrong
					throw new CompilerLogicException("Unexpected constant value.", ((BinaryOperationNode) rightAdd).getLeft().getStart(), ((BinaryOperationNode) rightAdd).getLeft().getEnd());
				}
				
			}
			else {
				descriptorList.add(context.getLastDescriptor());
				recipeList.add("\u0001");
			}
			
			rightAdd = ((BinaryOperationNode) rightAdd).getLeft();
			
		}
		
		ci.visit(rightAdd, context);
		
		if(context.getLastWasConstant()) {
			
			OpNode oNode = stack.pop();
			
			if(oNode instanceof InsnNode) {
				recipeList.add(((InsnNode) oNode).getValue());
			}
			else if(oNode instanceof IntInsnNode) {
				recipeList.add(((IntInsnNode) oNode).getValue());
			}
			else if(oNode instanceof LdcNode) {
				recipeList.add(((LdcNode) oNode).getValue());
			}
			else {
				// Shouldn't happen, handle just in case something went very wrong
				throw new CompilerLogicException("Unexpected constant value.", rightAdd.getStart(), rightAdd.getEnd());
			}
			
		}
		else {
			descriptorList.add(context.getLastDescriptor());
			recipeList.add("\u0001");
		}
		
		//descriptorList.add(")Ljava/lang/String;");
		
		Collections.reverse(descriptorList);
		Collections.reverse(recipeList);
		
		StringBuilder descriptor = new StringBuilder("(");
		StringBuilder recipe = new StringBuilder();
		
		descriptorList.forEach(descriptor::append);
		recipeList.forEach(recipe::append);
		
		descriptor.append(")Ljava/lang/String;");
		
		
		stack.push(new InvokeDynamicNode("makeConcatWithConstants", descriptor.toString(), new Handle(Opcodes.H_INVOKESTATIC, 
					"java/lang/invoke/StringConcatFactory", 
					"makeConcatWithConstants", 
					"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
					false), 
				new Object[] {recipe.toString()}));
		
		context.setLastDescriptor("Ljava/lang/String;");
	}
	
	private void intOperation(BinaryOperationNode bon, TokenType type, Node right, Context context)
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
					break;

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

	private void doubleOperation(BinaryOperationNode bon, TokenType type, Node right, Context context)
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

	private void floatOperation(BinaryOperationNode bon, TokenType type, Node right, Context context)
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

	private void booleanOperation(BinaryOperationNode bon, TokenType type, Node right, Context context)
			throws CompilerLogicException {

		OperationStack stack = context.getOpStack();

		switch(type) {

			case AND: {

				Label notTrue = new Label();
				Label endIf = new Label();

				stack.push(new JumpNode(Opcodes.IFEQ, notTrue));

				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();

				out: if(!rightDesc.equals("Z")) {

					if(rightDesc.equals("I")) {
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
					if(rightDesc.equals("I")) {
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

					if(rightDesc.equals("I")) {
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

					if(rightDesc.equals("I")) {
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

					if(rightDesc.equals("I")) {
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

}
