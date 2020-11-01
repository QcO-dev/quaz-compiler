package quaz.compiler;

import java.lang.reflect.InvocationTargetException;

import com.datumware.cli.CliStarter;
import com.datumware.cli.HelpCliException;
import com.datumware.cli.InvalidCliException;
import com.datumware.cli.UserCliException;

public class QuazCompilerLauncher {
	
	public static void main(String[] args) {
		start(args);
	}
	
	public static void start(String[] args) {
		try {
			CliStarter.start(new CliHandler(), args);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | InvalidCliException e) {
			e.printStackTrace();
		} catch(UserCliException | HelpCliException e) {
			System.err.println(e.getMessage());
		}
	}

}
