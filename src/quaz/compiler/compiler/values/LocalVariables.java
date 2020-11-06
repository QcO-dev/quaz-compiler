package quaz.compiler.compiler.values;

import java.util.HashMap;

public class LocalVariables extends HashMap<String, LocalVariable> {
	private static final long serialVersionUID = -4251523996840386145L;
	
	public LocalVariables() {}
	
	public LocalVariables(LocalVariables lv) {
		super(lv);
		this.nextIndex = lv.getNextIndex();
	}
	
	private int nextIndex = 0;
	
	public int getNextIndex() {
		return nextIndex;
	}
	
	public void incrementIndex()  {
		nextIndex++;
	}
	
}
