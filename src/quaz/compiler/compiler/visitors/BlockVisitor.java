package quaz.compiler.compiler.visitors;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Compiler;
import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.opStack.nodes.InsnNode;
import quaz.compiler.compiler.opStack.nodes.JumpNode;
import quaz.compiler.compiler.opStack.nodes.LabelNode;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.block.ForNode;
import quaz.compiler.parser.nodes.block.IfNode;
import quaz.compiler.parser.nodes.block.WhileNode;

public class BlockVisitor {
	
	public void visitStatementsNode(Node node, Context context) throws CompilerLogicException {
		
		Node[] nodes = (Node[]) node.getValue();
		
		Compiler compiler = context.getCompilerInstance();
		
		for(Node n : nodes) {
			compiler.visit(n, context, true);
		}
	}
	
	public void visitIfNode(Node node, Context context) throws CompilerLogicException {
		
		IfNode in = (IfNode) node;
		
		Label elseLabel = new Label();
		Label endIf = new Label();
		
		Context condCont = context.copy();
		
		context.getCompilerInstance().visit(in.getCondition(), condCont);
		
		if(!condCont.getLastDescriptor().equals("Z")) {
			throw new CompilerLogicException("Expected boolean value.", in.getCondition().getStart(), in.getCondition().getEnd());
		}
		
		OperationStack stack = context.getOpStack();
		
		stack.push(new JumpNode(Opcodes.IFEQ, elseLabel));
		
		Context bodyContext = context.copy();
		
		context.getCompilerInstance().visit(in.getBody(), bodyContext);
		
		if(in.getElseBody() != null) {
			stack.push(new JumpNode(Opcodes.GOTO, endIf));
		}
		
		stack.push(new LabelNode(elseLabel));
		
		if(in.getElseBody() != null) {
			Context elseContext = context.copy();
			context.getCompilerInstance().visit(in.getElseBody(), elseContext);
			stack.push(new LabelNode(endIf));
		}
		
		context.setLastWasConstant(false);
		
	}
	
	public void visitWhileNode(Node node, Context context) throws CompilerLogicException {
		
		WhileNode wn = (WhileNode) node;
		
		Label condition = new Label();
		Label end = new Label();
		
		
		OperationStack stack = context.getOpStack();
		
		stack.push(new LabelNode(condition));
		
		Context condContext = context.copy();
		
		context.getCompilerInstance().visit(wn.getCondition(), condContext);
		
		stack.push(new JumpNode(Opcodes.IFEQ, end));
		
		context.setLoop(true);
		context.setLoopCondition(condition);
		context.setLoopEnd(end);
		
		Context whileContext = context.copy();
		
		context.getCompilerInstance().visit(wn.getBody(), whileContext);
		
		whileContext.setLoop(false);
		
		stack.push(new JumpNode(Opcodes.GOTO, condition));
		
		stack.push(new LabelNode(end));
		
		context.setLastWasConstant(false);
		
	}
	
	public void visitForNode(Node node, Context context) throws CompilerLogicException {
		
		ForNode fn = (ForNode) node;
		
		Label condition = new Label();
		Label end = new Label();
		
		OperationStack stack = context.getOpStack();
		
		boolean wasLoop = context.isLoop();
		
		context.setLoop(true);
		context.setLoopCondition(condition);
		context.setLoopEnd(end);
		
		Context forContext = context.copy();
		
		forContext.getCompilerInstance().visit(fn.getInit(), forContext);
		
		stack.push(new LabelNode(condition));
		
		forContext.getCompilerInstance().visit(fn.getCondition(), forContext);
		
		stack.push(new JumpNode(Opcodes.IFEQ, end));
		
		forContext.getCompilerInstance().visit(fn.getBody(), forContext);
		
		forContext.getCompilerInstance().visit(fn.getIncrement(), forContext);
		
		context.setLoop(wasLoop);
		
		stack.push(new JumpNode(Opcodes.GOTO, condition));
		
		stack.push(new LabelNode(end));
		
		context.setLastWasConstant(false);
		
	}
	
	public void visitPassNode(Node node, Context context) {
		context.getOpStack().push(new InsnNode(Opcodes.NOP));
		context.setLastWasConstant(false);
	}
	
	public void visitBreakNode(Node node, Context context) throws CompilerLogicException {
		
		if(!context.isLoop()) {
			throw new CompilerLogicException("Break statements can only be used inside of a loop.", node.getStart(), node.getEnd());
		}
		
		context.getOpStack().push(new JumpNode(Opcodes.GOTO, context.getLoopEnd()));
		context.setLastWasConstant(false);
	}

	public void visitContinueNode(Node node, Context context) throws CompilerLogicException {
		
		if(!context.isLoop()) {
			throw new CompilerLogicException("Continue statements can only be used inside of a loop.", node.getStart(), node.getEnd());
		}
		
		context.getOpStack().push(new JumpNode(Opcodes.GOTO, context.getLoopCondition()));
		context.setLastWasConstant(false);
		
	}
	
}
