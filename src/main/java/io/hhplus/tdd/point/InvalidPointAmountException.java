package io.hhplus.tdd.point;

import io.hhplus.tdd.ErrorCode;

public class InvalidPointAmountException extends PointException{
	public static String MESSAGE = "충전 금액은 0보다 커야 합니다. amount=";
	public InvalidPointAmountException(long amount) {
		super(ErrorCode.NEGATIVE_CHARGE, MESSAGE + amount);
	}
}
