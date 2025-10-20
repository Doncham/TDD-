package io.hhplus.tdd.point.exception;

import io.hhplus.tdd.ErrorCode;
import lombok.Getter;

@Getter
public class PointException extends RuntimeException {
	private final ErrorCode code;

	public PointException(ErrorCode code, String message) {
		super(message);
		this.code = code;
	}
}
