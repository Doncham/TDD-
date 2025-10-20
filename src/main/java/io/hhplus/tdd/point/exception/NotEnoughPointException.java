package io.hhplus.tdd.point.exception;

import io.hhplus.tdd.ErrorCode;

public class NotEnoughPointException extends PointException {
	public static String MESSAGE = "포인트가 부족합니다. 현재 포인트=";
	public NotEnoughPointException(long currentPoint) {
		super(ErrorCode.NOT_ENOUGH_POINT, MESSAGE + currentPoint);
	}
}
