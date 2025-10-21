package io.hhplus.tdd.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class AmountRequest {
	@NotNull
	@Positive
	Long amount;

	public AmountRequest(Long amount) {
		this.amount = amount;
	}
}
