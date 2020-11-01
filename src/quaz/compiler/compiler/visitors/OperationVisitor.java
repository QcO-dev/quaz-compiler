package quaz.compiler.compiler.visitors;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
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
			context.getCompilerInstance().visit(bon.getLeft(), context);
			
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
				refOperation(leftDesc, bon, bon.getType(), bon.getRight(), context);
				break;
		}

	}

	private void refOperation(String leftDesc, BinaryOperationNode bon, TokenType type, Node right, Context context)
			throws CompilerLogicException {
		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();
		
		MethodVisitor mv = context.getVisitor();
		
		if(leftDesc.equals("Ljava/lang/String;")) {
			switch(type) {
				case PLUS:
					
					/*
					mv.visitInvokeDynamicInsn("makeConcatWithConstants", 
							"(Ljava/lang/String;II)Ljava/lang/String;", 
							new Handle(Opcodes.H_INVOKESTATIC, 
									"java/lang/invoke/StringConcatFactory", 
									"makeConcatWithConstants", 
									"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
									false), 
							// \u0001 = object on stack -> characters = string constant
							new Object[]{"WHAT\u0001\u0001!\u0001"});
					*/
					
					// Go through until no longer adding - then make above
					
					Node rightAdd = bon.getRight();
					
					String descriptor = "";
					String recipe = "";
					
					while(rightAdd instanceof BinaryOperationNode && ((BinaryOperationNode) rightAdd).getType() == TokenType.PLUS) {
						
						
						
						
					}
					
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
				mv.visitInsn(Opcodes.ASTORE);
				context.setLastDescriptor(leftDesc);
				break;
				
			case BOOL_TRI_EQ: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IF_ACMPNE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_TRI_NE: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IF_ACMPEQ, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_EQ: {
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Descriptors.cropTypeDescriptor(leftDesc), "equals", "(Ljava/lang/Object;)Z", false);
				context.setLastDescriptor("Z");
				break;
			}
			
			case BOOL_NE: {
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Descriptors.cropTypeDescriptor(leftDesc), "equals", "(Ljava/lang/Object;)Z", false);
				
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IFNE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				break;
			}
				
			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}
		
		context.setLastWasConstant(false);
		
		//context.setLastDescriptor(leftDesc);
	}

	private void intOperation(BinaryOperationNode bon, TokenType type, Node right, Context context)
			throws CompilerLogicException {

		context.getCompilerInstance().visit(bon.getRight(), context);

		String rightDesc = context.getLastDescriptor();

		if(!rightDesc.equals("I")) {

			switch(rightDesc) {
				case "D":
					context.getVisitor().visitInsn(Opcodes.D2I);
					break;
				case "F":
					context.getVisitor().visitInsn(Opcodes.F2I);
					break;
				case "Z":
					break;

				default:
					throw new CompilerLogicException("Expected int, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}

		}
		
		MethodVisitor mv = context.getVisitor();
		
		switch(type) {
			case PLUS:
				mv.visitInsn(Opcodes.IADD);
				context.setLastDescriptor("I");
				break;

			case MINUS:
				mv.visitInsn(Opcodes.ISUB);
				context.setLastDescriptor("I");
				break;

			case MULTIPLY:
				mv.visitInsn(Opcodes.IMUL);
				context.setLastDescriptor("I");
				break;
			case DIVIDE:
				mv.visitInsn(Opcodes.IDIV);
				context.setLastDescriptor("I");
				break;

			case MODULUS:
				mv.visitInsn(Opcodes.IREM);
				context.setLastDescriptor("I");
				break;

			case BIT_OR:
				mv.visitInsn(Opcodes.IOR);
				context.setLastDescriptor("I");
				break;
			case BIT_AND:
				mv.visitInsn(Opcodes.IAND);
				context.setLastDescriptor("I");
				break;
				
			case BIT_XOR:
				mv.visitInsn(Opcodes.IXOR);
				context.setLastDescriptor("I");
				break;
				
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IF_ICMPNE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IF_ICMPEQ, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IF_ICMPGE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IF_ICMPGT, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IF_ICMPLE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitJumpInsn(Opcodes.IF_ICMPLT, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
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

					mv.visitVarInsn(Opcodes.ISTORE, var.getIndex());
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
		
		MethodVisitor mv = context.getVisitor();
		
		if(!rightDesc.equals("D")) {
			switch(rightDesc) {
				case "I":
					mv.visitInsn(Opcodes.I2D);
					break;
				case "F":
					mv.visitInsn(Opcodes.F2D);
					break;
				default:
					throw new CompilerLogicException("Expected double, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}
		}

		switch(type) {
			case PLUS:
				mv.visitInsn(Opcodes.DADD);
				context.setLastDescriptor("D");
				break;

			case MINUS:
				mv.visitInsn(Opcodes.DSUB);
				context.setLastDescriptor("D");
				break;

			case MULTIPLY:
				mv.visitInsn(Opcodes.DMUL);
				context.setLastDescriptor("D");
				break;
			case DIVIDE:
				mv.visitInsn(Opcodes.DDIV);
				context.setLastDescriptor("D");
				break;

			case MODULUS:
				mv.visitInsn(Opcodes.DREM);
				context.setLastDescriptor("D");
				break;
			
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
				
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.DCMPL);
				
				mv.visitJumpInsn(Opcodes.IFNE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.DCMPL);
				
				mv.visitJumpInsn(Opcodes.IFEQ, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.DCMPL);
				
				mv.visitJumpInsn(Opcodes.IFGE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.DCMPL);
				
				mv.visitJumpInsn(Opcodes.IFGT, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.DCMPL);
				
				mv.visitJumpInsn(Opcodes.IFLE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.DCMPL);
				
				mv.visitJumpInsn(Opcodes.IFLT, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
				
			case EQUALS: {

				LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());

				mv.visitVarInsn(Opcodes.DSTORE, var.getIndex());
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

		MethodVisitor mv = context.getVisitor();
		
		if(!rightDesc.equals("F")) {
			switch(rightDesc) {
				case "D":
					mv.visitInsn(Opcodes.D2F);
					break;
				case "I":
					mv.visitInsn(Opcodes.I2F);
					break;
				default:
					throw new CompilerLogicException("Expected float, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
			}
		}

		switch(type) {
			case PLUS:
				mv.visitInsn(Opcodes.FADD);
				context.setLastDescriptor("F");
				break;

			case MINUS:
				mv.visitInsn(Opcodes.FSUB);
				context.setLastDescriptor("F");
				break;

			case MULTIPLY:
				mv.visitInsn(Opcodes.FMUL);
				context.setLastDescriptor("F");
				break;
			case DIVIDE:
				mv.visitInsn(Opcodes.FDIV);
				context.setLastDescriptor("F");
				break;

			case MODULUS:
				mv.visitInsn(Opcodes.FREM);
				context.setLastDescriptor("F");
				break;
				
			case BOOL_TRI_EQ:
			case BOOL_EQ: {
				
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.FCMPL);
				
				mv.visitJumpInsn(Opcodes.IFNE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_TRI_NE:
			case BOOL_NE: {
				
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.FCMPL);
				
				mv.visitJumpInsn(Opcodes.IFEQ, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
				
			}
			
			case BOOL_LT: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.FCMPL);
				
				mv.visitJumpInsn(Opcodes.IFGE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_LE: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.FCMPL);
				
				mv.visitJumpInsn(Opcodes.IFGT, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GT: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.FCMPL);
				
				mv.visitJumpInsn(Opcodes.IFLE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}
			
			case BOOL_GE: {
				Label falseL = new Label();
				Label end = new Label();
				
				mv.visitInsn(Opcodes.FCMPL);
				
				mv.visitJumpInsn(Opcodes.IFLT, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
				context.setLastDescriptor("Z");
				
				break;
			}

			case EQUALS: {

				LocalVariable var = context.getLocalVariables().get(bon.getLeft().getValue());
				
				context.setLastDescriptor("F");
				mv.visitVarInsn(Opcodes.FSTORE, var.getIndex());
				break;
			}

			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}
		
		context.setLastWasConstant(false);

	}

	private void booleanOperation(BinaryOperationNode bon, TokenType type, Node right, Context context)
			throws CompilerLogicException {

		MethodVisitor mv = context.getVisitor();

		switch(type) {

			case AND: {

				Label notTrue = new Label();
				Label endIf = new Label();

				mv.visitJumpInsn(Opcodes.IFEQ, notTrue);

				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();

				out: if(!rightDesc.equals("Z")) {

					if(rightDesc.equals("I")) {
						break out;
					}

					throw new CompilerLogicException("Expected boolean, got " + Descriptors.descriptorToType(rightDesc),
							right.getStart(), right.getEnd());
				}

				mv.visitJumpInsn(Opcodes.IFEQ, notTrue);

				mv.visitInsn(Opcodes.ICONST_1);

				mv.visitJumpInsn(Opcodes.GOTO, endIf);

				mv.visitLabel(notTrue);

				mv.visitInsn(Opcodes.ICONST_0);

				mv.visitLabel(endIf);

				break;
			}

			case OR: {

				Label isTrue = new Label();
				Label notTrue = new Label();
				Label endIf = new Label();

				mv.visitJumpInsn(Opcodes.IFNE, isTrue);

				context.getCompilerInstance().visit(bon.getRight(), context);

				String rightDesc = context.getLastDescriptor();

				out: if(!rightDesc.equals("Z")) {
					if(rightDesc.equals("I")) {
						break out;
					}
					throw new CompilerLogicException(rightDesc, right.getStart(), right.getEnd());
				}

				mv.visitJumpInsn(Opcodes.IFEQ, notTrue);

				mv.visitLabel(isTrue);

				mv.visitInsn(Opcodes.ICONST_1);

				mv.visitJumpInsn(Opcodes.GOTO, endIf);

				mv.visitLabel(notTrue);

				mv.visitInsn(Opcodes.ICONST_0);

				mv.visitLabel(endIf);

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
				
				mv.visitJumpInsn(Opcodes.IF_ICMPNE, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
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
				
				mv.visitJumpInsn(Opcodes.IF_ICMPEQ, falseL);
				
				mv.visitInsn(Opcodes.ICONST_1);
				
				mv.visitJumpInsn(Opcodes.GOTO, end);
				
				mv.visitLabel(falseL);
				
				mv.visitInsn(Opcodes.ICONST_0);
				
				mv.visitLabel(end);
				
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
				
				context.getVisitor().visitVarInsn(Opcodes.ISTORE, var.getIndex());
				break;
			}
			
			default:
				throw new CompilerLogicException("Invalid Operation", right.getStart(), right.getEnd());
		}

		context.setLastDescriptor("Z");
		context.setLastWasConstant(false);
	}

}
