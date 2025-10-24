package io.hhplus.tdd;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
		List<ErrorDetail> errors
) {
	public record ErrorDetail(String field, String message, Object rejectedValue){}
}
