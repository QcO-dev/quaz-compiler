package quaz.compiler.standardLibrary.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import quaz.util.IniFile;

public class Config {
	
	private static Map<String, Properties> defaults;
	
	private static boolean hasConfig;
	
	private static IniFile iniFile;
	
	public static void buildDefaults() {
		
		hasConfig = true;
		
		defaults = new HashMap<>();
		
		// BUILTINS
		
		{
			Properties prop = new Properties();
			prop.put("input", "true");
			prop.put("println", "true");
			defaults.put("BUILTINS", prop);
		}
		
	}

	public static Map<String, Properties> getDefaults() {
		return defaults;
	}

	public static void setDefaults(Map<String, Properties> defaults) {
		Config.defaults = defaults;
	}

	public static boolean isHasConfig() {
		return hasConfig;
	}

	public static void setHasConfig(boolean hasConfig) {
		Config.hasConfig = hasConfig;
	}

	public static IniFile getIniFile() {
		return iniFile;
	}

	public static void setIniFile(IniFile iniFile) {
		Config.iniFile = iniFile;
	}
	
}
