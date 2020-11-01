package quaz.compiler.position;

import java.io.File;

public class Position {

	private int column;
	private int line;
	private File file;
	
	public Position(int column, int line, File file) {
		this.column = column;
		this.line = line;
		this.file = file;
	}
	
	public int getColumn() {
		return column;
	}
	public void setColumn(int column) {
		this.column = column;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	
}
