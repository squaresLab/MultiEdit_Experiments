package io.github.squareslab.common;

public enum ErrorMessage {
	NEGATIVE_SHIFT("shifting by a negative value"),
	SHIFT_TOO_LARGE("shifting by 32 bits or more"),
	UNREAD_FIELD("this field is never read"),
	NEGATIVE_INDEX_ERROR("negative array index"),
	POSSIBLE_NEGATIVE_INDEX_WARNING("array index may be negative"),
	DIVIDE_BY_ZERO("divide by zero")
	;

	private ErrorMessage(String message) {
		this.errorMessage = message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private String errorMessage;
}

