package quaz.compiler.compiler.visitors;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Compiler;
import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.opStack.nodes.InsnNode;
import quaz.compiler.compiler.opStack.nodes.IntInsnNode;
import quaz.compiler.compiler.opStack.nodes.InvokeDynamicNode;
import quaz.compiler.compiler.opStack.nodes.LdcNode;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.value.BooleanNode;
import quaz.compiler.parser.nodes.value.ByteNode;
import quaz.compiler.parser.nodes.value.CharNode;
import quaz.compiler.parser.nodes.value.DoubleNode;
import quaz.compiler.parser.nodes.value.ExprStringNode;
import quaz.compiler.parser.nodes.value.FloatNode;
import quaz.compiler.parser.nodes.value.IntNode;
import quaz.compiler.parser.nodes.value.LongNode;
import quaz.compiler.parser.nodes.value.ShortNode;

public class ValueVisitor {
	
	public void visitStringNode(Node node, Context context) {
		context.getOpStack().push(new LdcNode(node.getValue()));
		context.setLastDescriptor("Ljava/lang/String;");
		context.setLastWasConstant(true);
	}
	
	public void visitIntNode(Node node, Context context) {
		
		IntNode in = (IntNode) node;
		
		int val = in.getVal();
		
		OperationStack stack = context.getOpStack();
		if(val >= 0) { // Is positive
			if(val <= 5) {
				
				switch(val) {
					case 0:
						stack.push(new InsnNode(Opcodes.ICONST_0));
						break;
					case 1:
						stack.push(new InsnNode(Opcodes.ICONST_1));
						break;
					case 2:
						stack.push(new InsnNode(Opcodes.ICONST_2));
						break;
					case 3:
						stack.push(new InsnNode(Opcodes.ICONST_3));
						break;
					case 4:
						stack.push(new InsnNode(Opcodes.ICONST_4));
						break;
					case 5:
						stack.push(new InsnNode(Opcodes.ICONST_5));
						break;
				}
				
			}
			
			else if(val < 256) {
				stack.push(new IntInsnNode(Opcodes.BIPUSH, val));
			}
			
			else if(val < 65536) {
				stack.push(new IntInsnNode(Opcodes.SIPUSH, val));
			}
			
			else {
				stack.push(new LdcNode(val));
			}
		
		}
		
		else {
			
			if(val == -1) {
				stack.push(new InsnNode(Opcodes.ICONST_M1));
			}
			
			else if(val >= -128) {
				stack.push(new IntInsnNode(Opcodes.BIPUSH, val));
			}
			
			else if(val >= 32768) {
				stack.push(new IntInsnNode(Opcodes.SIPUSH, val));
			}
			
			else {
				stack.push(new LdcNode(val));
			}
			
		}
		
		context.setLastDescriptor("I");
		context.setLastWasConstant(true);
	}
	
	public void visitDoubleNode(Node node, Context context) {
		
		DoubleNode dn = (DoubleNode) node;
		
		double val = dn.getVal();
		
		OperationStack stack = context.getOpStack();
		
		if(val == 0) {
			stack.push(new InsnNode(Opcodes.DCONST_0));
		}
		else if(val == 1) {
			stack.push(new InsnNode(Opcodes.DCONST_1));
		}
		else {
			stack.push(new LdcNode(val));
		}
		
		context.setLastDescriptor("D");
		context.setLastWasConstant(true);
	}
	
	public void visitFloatNode(Node node, Context context) {
		
		FloatNode fn = (FloatNode) node;
		
		float val = fn.getVal();
		
		OperationStack stack = context.getOpStack();
		
		if(val == 0) {
			stack.push(new InsnNode(Opcodes.FCONST_0));
		}
		else if(val == 1) {
			stack.push(new InsnNode(Opcodes.FCONST_1));
		}
		else if(val == 2) {
			stack.push(new InsnNode(Opcodes.FCONST_2));
		}
		else {
			stack.push(new LdcNode(val));
		}
		
		context.setLastDescriptor("F");
		context.setLastWasConstant(true);
	}
	
	public void visitBooleanNode(Node node, Context context) {
		BooleanNode bn = (BooleanNode) node;
		
		if(bn.getVal()) {
			context.getOpStack().push(new InsnNode(Opcodes.ICONST_1));
		}
		else {
			context.getOpStack().push(new InsnNode(Opcodes.ICONST_0));
		}
		
		context.setLastDescriptor("Z");
		context.setLastWasConstant(true);
	}
	
	public void visitCharNode(Node node, Context context) {
		
		CharNode cn = (CharNode) node;
		
		int val = cn.getVal();
		
		OperationStack stack = context.getOpStack();
		
		if(val <= 5) {
			
			switch(val) {
				case 0:
					stack.push(new InsnNode(Opcodes.ICONST_0));
					break;
				case 1:
					stack.push(new InsnNode(Opcodes.ICONST_1));
					break;
				case 2:
					stack.push(new InsnNode(Opcodes.ICONST_2));
					break;
				case 3:
					stack.push(new InsnNode(Opcodes.ICONST_3));
					break;
				case 4:
					stack.push(new InsnNode(Opcodes.ICONST_4));
					break;
				case 5:
					stack.push(new InsnNode(Opcodes.ICONST_5));
					break;
			}
			
		}
		
		else if(val < 256) {
			stack.push(new IntInsnNode(Opcodes.BIPUSH, val));
		}
		
		else if(val < 65536) {
			stack.push(new IntInsnNode(Opcodes.SIPUSH, val));
		}
		
		else {
			stack.push(new LdcNode(val));
		}
		
		context.setLastDescriptor("C");
		context.setLastWasConstant(true);
		
	}
	
	public void visitByteNode(Node node, Context context) {
		
		byte val = ((ByteNode) node).getVal();
		
		context.getOpStack().add(new IntInsnNode(Opcodes.BIPUSH, val));
		context.setLastDescriptor("B");
		context.setLastWasConstant(true);
	}
	
	public void visitLongNode(Node node, Context context) {
		
		LongNode ln = (LongNode) node;
		
		long val = ln.getVal();
		
		OperationStack stack = context.getOpStack();
		
		if(val == 0) {
			stack.push(new InsnNode(Opcodes.LCONST_0));
		}
		else if(val == 1) {
			stack.push(new InsnNode(Opcodes.LCONST_1));
		}
		else {
			stack.push(new LdcNode(val));
		}
		
		context.setLastDescriptor("J");
		context.setLastWasConstant(true);
	}
	
	public void visitShortNode(Node node, Context context) {
		ShortNode in = (ShortNode) node;
		
		short val = in.getVal();
		
		OperationStack stack = context.getOpStack();
		if(val >= 0) { // Is positive
			if(val <= 5) {
				
				switch(val) {
					case 0:
						stack.push(new InsnNode(Opcodes.ICONST_0));
						break;
					case 1:
						stack.push(new InsnNode(Opcodes.ICONST_1));
						break;
					case 2:
						stack.push(new InsnNode(Opcodes.ICONST_2));
						break;
					case 3:
						stack.push(new InsnNode(Opcodes.ICONST_3));
						break;
					case 4:
						stack.push(new InsnNode(Opcodes.ICONST_4));
						break;
					case 5:
						stack.push(new InsnNode(Opcodes.ICONST_5));
						break;
				}
				
			}
			
			else if(val < 256) {
				stack.push(new IntInsnNode(Opcodes.BIPUSH, val));
			}
			
			else if(val < 65536) {
				stack.push(new IntInsnNode(Opcodes.SIPUSH, val));
			}
			
			else {
				stack.push(new LdcNode(val));
			}
		
		}
		
		else {
			
			if(val == -1) {
				stack.push(new InsnNode(Opcodes.ICONST_M1));
			}
			
			else if(val >= -128) {
				stack.push(new IntInsnNode(Opcodes.BIPUSH, val));
			}
			
			else if(val >= 32768) {
				stack.push(new IntInsnNode(Opcodes.SIPUSH, val));
			}
			
			else {
				stack.push(new LdcNode(val));
			}
			
		}
		
		context.setLastDescriptor("S");
		context.setLastWasConstant(true);
	}
	
	public void visitExprStringNode(Node node, Context context) throws CompilerLogicException {
		
		ExprStringNode esn = (ExprStringNode) node;
		
		StringBuilder descriptor = new StringBuilder("(");
		
		Compiler ci = context.getCompilerInstance();
		
		for(Node expr : esn.getExprs()) {
			ci.visit(expr, context);
			descriptor.append(context.getLastDescriptor());
		}
		
		descriptor.append(")Ljava/lang/String;");
		
		context.getOpStack().push(new InvokeDynamicNode("makeConcatWithConstants", descriptor.toString(), new Handle(Opcodes.H_INVOKESTATIC, 
				"java/lang/invoke/StringConcatFactory", 
				"makeConcatWithConstants", 
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
				false), 
			new Object[] {esn.getRep()}));
	
		context.setLastDescriptor("Ljava/lang/String;");
		
	}
	
}
