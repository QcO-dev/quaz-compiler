package quaz.compiler.compiler.values;

import java.util.HashMap;

public class FunctionList extends HashMap<String, HashMap<String, Function>> {
	private static final long serialVersionUID = 5945444245452573638L;
	
	
	public void put(String name, String descriptor, Function function) {
		
		HashMap<String, Function> functions = this.get(name);
		
		if(functions == null) {
			functions = new HashMap<>();
			this.put(name, functions);
		}
		
		functions.put(descriptor, function);
	}
	
	public Function get(String name, String descriptor) {
		
		HashMap<String, Function> functions = this.get(name);
		
		if(functions == null) {
			return null;
		}
		
		return functions.get(descriptor);
	}

}
