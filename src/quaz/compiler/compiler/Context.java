package quaz.compiler.compiler;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.values.Function;
import quaz.compiler.compiler.values.FunctionList;
import quaz.compiler.compiler.values.LocalVariables;
import quaz.compiler.standardLibrary.Pair;
import quaz.compiler.standardLibrary.StandardLibrary;

public class Context {
	
	private Compiler compilerInstance;
	private ClassWriter writer;
	//private MethodVisitor visitor;
	private OperationStack opStack;
	private String lastDescriptor = "";
	private String methodReturnType = "void";
	private boolean hasReturnedLast = false;
	private boolean lastWasConstant = false;
	private boolean isLoop = false;
	private Label loopEnd;
	private Label loopCondition;
	
	private final FunctionList functions;
	private final LocalVariables localVariables;
	private final Map<String, String> typeReferences;
	private final String name;
	
	public Context(Compiler compilerInstance, ClassWriter writer, String name) {
		
		this.compilerInstance = compilerInstance;
		this.writer = writer;
		this.name = name;
		functions = new FunctionList();
		localVariables = new LocalVariables();
		typeReferences = new HashMap<>();
		
		for(Pair<?, ?> p : StandardLibrary.STATIC_IMPORTS) {
			Function f = (Function) p.getSecond();
			String desc = (String) p.getFirst();
			
			functions.put(f.getName(), desc, f);
		}
		
		for(Pair<?, ?> p : StandardLibrary.TYPE_REFERENCES)
			typeReferences.put((String)p.getFirst(), (String)p.getSecond());
		
	}
	
	/**
	 * The copy constructor
	 * 
	 * @param compilerInstance Copied value
	 * @param writer Copied value
	 * @param name Copied value
	 * @param functions Copied value
	 * @param locals Copied value
	 */
	
	private Context(Compiler compilerInstance, ClassWriter writer, OperationStack opStack, String name, FunctionList functions, LocalVariables locals, Map<String, String> typeReferences, String methodReturnType, boolean hasReturnedLast, boolean isLoop, Label loopCondition, Label loopEnd, boolean lastWasConstant) {
		this.compilerInstance = compilerInstance;
		this.writer = writer;
		this.name = name;
		this.functions = functions;
		this.localVariables = locals;
		this.typeReferences = typeReferences;
		this.methodReturnType = methodReturnType;
		this.hasReturnedLast = hasReturnedLast;
		this.isLoop = isLoop;
		this.loopCondition = loopCondition;
		this.loopEnd = loopEnd;
		this.lastWasConstant = lastWasConstant;
		this.opStack = opStack;
	}

	public Compiler getCompilerInstance() {
		return compilerInstance;
	}

	public void setCompilerInstance(Compiler compilerInstance) {
		this.compilerInstance = compilerInstance;
	}

	public ClassWriter getWriter() {
		return writer;
	}

	public void setWriter(ClassWriter writer) {
		this.writer = writer;
	}

	/*public MethodVisitor getVisitor() {
		return visitor;
	}

	public void setVisitor(MethodVisitor visitor) {
		this.visitor = visitor;
	}*/

	public FunctionList getFunctions() {
		return functions;
	}

	public String getName() {
		return name;
	}

	public LocalVariables getLocalVariables() {
		return localVariables;
	}
	
	public Map<String, String> getTypeReferences() {
		return typeReferences;
	}

	public String getLastDescriptor() {
		return lastDescriptor;
	}

	public void setLastDescriptor(String lastDescriptor) {
		this.lastDescriptor = lastDescriptor;
	}

	public Context copy() {
		return new Context(compilerInstance, writer, opStack, name, functions, new LocalVariables(localVariables), typeReferences, methodReturnType, hasReturnedLast, isLoop, loopCondition, loopEnd, lastWasConstant);
	}

	public String getMethodReturnType() {
		return methodReturnType;
	}

	public void setMethodReturnType(String methodReturnType) {
		this.methodReturnType = methodReturnType;
	}

	public boolean hasReturnedLast() {
		return hasReturnedLast;
	}

	public void setHasReturnedLast(boolean hasReturnedLast) {
		this.hasReturnedLast = hasReturnedLast;
	}

	public boolean isLoop() {
		return isLoop;
	}

	public void setLoop(boolean isLoop) {
		this.isLoop = isLoop;
	}

	public Label getLoopEnd() {
		return loopEnd;
	}

	public void setLoopEnd(Label loopEnd) {
		this.loopEnd = loopEnd;
	}

	public Label getLoopCondition() {
		return loopCondition;
	}

	public void setLoopCondition(Label loopCondition) {
		this.loopCondition = loopCondition;
	}

	public boolean isHasReturnedLast() {
		return hasReturnedLast;
	}

	public boolean getLastWasConstant() {
		return lastWasConstant;
	}

	public void setLastWasConstant(boolean lastWasConstant) {
		this.lastWasConstant = lastWasConstant;
	}

	public OperationStack getOpStack() {
		return opStack;
	}

	public void setOpStack(OperationStack opStack) {
		this.opStack = opStack;
	}
	
}
