package quaz.compiler.compiler.visitors;

import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.opStack.nodes.InsnNode;
import quaz.compiler.compiler.opStack.nodes.IntInsnNode;
import quaz.compiler.compiler.opStack.nodes.LdcNode;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.value.BooleanNode;
import quaz.compiler.parser.nodes.value.DoubleNode;
import quaz.compiler.parser.nodes.value.FloatNode;
import quaz.compiler.parser.nodes.value.IntNode;

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
	
}
