package io.github.squareslab.common;

import java.util.Objects;

public class ErrorReport {
	public ErrorReport(ErrorMessage message, int line, String name) {
		this.message = message;
		this.line = line;
		this.declName = name;
	}
	public ErrorReport(ErrorMessage message, int line) {
		this(message, line, null);
	}
	public ErrorReport(ErrorMessage message, String name) {
		this(message, -1, name);
	}


	private int line;
	private ErrorMessage message;
	private String declName;

	public ErrorMessage getMessage() {
		return message;
	}
	public int getLine() {
		return line;
	}
	/** Returns the name of the declaration causing the error,
	 * or null if it was not caused by a declaration */
	public String getName() {
		return declName;
	}
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof ErrorReport))
			return false;
		ErrorReport r = (ErrorReport) o;
		boolean namesEqual = (r.getName() == null && declName == null)
				|| (declName != null && declName.equals(r.getName()));
		return  namesEqual && r.getLine() == line && r.getMessage() == message;
	}
	@Override
	public int hashCode() {
		return Objects.hash(message, line, declName);
	}
	@Override
	public String toString() {
		return "error(" + message + ", " + line + ", " + declName + ")";
	}
}