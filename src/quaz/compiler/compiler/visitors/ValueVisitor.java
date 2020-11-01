package quaz.compiler.compiler.visitors;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Context;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.value.BooleanNode;
import quaz.compiler.parser.nodes.value.DoubleNode;
import quaz.compiler.parser.nodes.value.FloatNode;
import quaz.compiler.parser.nodes.value.IntNode;

public class ValueVisitor {
	
	public void visitStringNode(Node node, Context context) {
		context.getVisitor().visitLdcInsn(node.getValue());
		context.setLastDescriptor("Ljava/lang/String;");
		context.setLastWasConstant(true);
	}
	
	public void visitIntNode(Node node, Context context) {
		
		IntNode in = (IntNode) node;
		
		int val = in.getVal();
		
		MethodVisitor visitor = context.getVisitor();
		if(val >= 0) { // Is positive
			if(val <= 5) {
				
				switch(val) {
					case 0:
						visitor.visitInsn(Opcodes.ICONST_0);
						break;
					case 1:
						visitor.visitInsn(Opcodes.ICONST_1);
						break;
					case 2:
						visitor.visitInsn(Opcodes.ICONST_2);
						break;
					case 3:
						visitor.visitInsn(Opcodes.ICONST_3);
						break;
					case 4:
						visitor.visitInsn(Opcodes.ICONST_4);
						break;
					case 5:
						visitor.visitInsn(Opcodes.ICONST_5);
						break;
				}
				
			}
			
			else if(val < 256) {
				visitor.visitIntInsn(Opcodes.BIPUSH, val);
			}
			
			else if(val < 65536) {
				visitor.visitIntInsn(Opcodes.SIPUSH, val);
			}
			
			else {
				visitor.visitLdcInsn(val);
			}
		
		}
		
		else {
			
			if(val == -1) {
				visitor.visitInsn(Opcodes.ICONST_M1);
			}
			
			else if(val >= -128) {
				visitor.visitIntInsn(Opcodes.BIPUSH, val);
			}
			
			else if(val >= 32768) {
				visitor.visitIntInsn(Opcodes.SIPUSH, val);
			}
			
			else {
				visitor.visitLdcInsn(val);
			}
			
		}
		
		context.setLastDescriptor("I");
		context.setLastWasConstant(true);
	}
	
	public void visitDoubleNode(Node node, Context context) {
		
		DoubleNode dn = (DoubleNode) node;
		
		double val = dn.getVal();
		
		MethodVisitor mv = context.getVisitor();
		
		if(val == 0) {
			mv.visitInsn(Opcodes.DCONST_0);
		}
		else if(val == 1) {
			mv.visitInsn(Opcodes.DCONST_1);
		}
		else {
			mv.visitLdcInsn(val);
		}
		
		context.setLastDescriptor("D");
		context.setLastWasConstant(true);
	}
	
	public void visitFloatNode(Node node, Context context) {
		
		FloatNode fn = (FloatNode) node;
		
		float val = fn.getVal();
		
		MethodVisitor mv = context.getVisitor();
		
		if(val == 0) {
			mv.visitInsn(Opcodes.FCONST_0);
		}
		else if(val == 1) {
			mv.visitInsn(Opcodes.FCONST_1);
		}
		else if(val == 2) {
			mv.visitInsn(Opcodes.FCONST_2);
		}
		else {
			mv.visitLdcInsn(val);
		}
		
		context.setLastDescriptor("F");
		context.setLastWasConstant(true);
	}
	
	public void visitBooleanNode(Node node, Context context) {
		BooleanNode bn = (BooleanNode) node;
		
		if(bn.getVal()) {
			context.getVisitor().visitInsn(Opcodes.ICONST_1);
		}
		else {
			context.getVisitor().visitInsn(Opcodes.ICONST_0);
		}
		
		context.setLastDescriptor("Z");
		context.setLastWasConstant(true);
	}
	
}
