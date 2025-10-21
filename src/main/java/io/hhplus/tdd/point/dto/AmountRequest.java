package io.hhplus.tdd.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AmountRequest {
	@NotNull
	@Positive
	Long amount;

	public AmountRequest(Long amount) {
		this.amount = amount;
	}
}
