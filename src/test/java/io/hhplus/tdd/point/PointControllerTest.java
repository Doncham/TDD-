package io.hhplus.tdd.point;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.ApiControllerAdvice;
import io.hhplus.tdd.point.dto.AmountRequest;
import io.hhplus.tdd.point.exception.NotEnoughPointException;

@WebMvcTest(PointController.class)
@Import(ApiControllerAdvice.class)
class PointControllerTest {
	@Autowired
	MockMvc mvc;
	@Autowired
	ObjectMapper objectMapper;
	@MockBean
	PointService pointService;
	// Get /point/{id} - 성공
	@Test
	void givenValidUser_whenGetPoint_thenReturnUserPoint() throws Exception {
		long userId = 1L;
		UserPoint up = new UserPoint(userId, 5000L, 123L);
		given(pointService.getPoint(userId)).willReturn(up);

		mvc.perform(get("/point/{id}", userId))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(userId))
			.andExpect(jsonPath("$.point").value(5000))
			.andExpect(jsonPath("$.updateMillis").value(123));

		verify(pointService).getPoint(userId);
	}
	// GET /point/{id}/history
	@Test
	void givenValidUser_whenGetPointHistories_thenReturnPointHistories() throws Exception {
		long userId = 1L;

		mvc.perform(get("/point/{id}/histories", userId))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON));

		verify(pointService).getPointHistories(userId);
	}
	// PATCH /point/{id}/charge - 성공
	@Test
	void givenValidChargeRequest_whenChargePoint_thenReturnUpdatedUserPoint() throws Exception {
		long userId = 1L;
		long chargeAmount = 1000L;
		UserPoint updatedPoint = new UserPoint(userId, 6000L, 123L);

		when(pointService.chargePoint(eq(userId), any(AmountRequest.class)))
			.thenReturn(updatedPoint);

		mvc.perform(patch("/point/{id}/charge", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AmountRequest(chargeAmount))))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(userId))
			.andExpect(jsonPath("$.point").value(6000))
			.andExpect(jsonPath("$.updateMillis").value(123));

		verify(pointService).chargePoint(eq(userId), any(AmountRequest.class));
	}
	// PATCH /point/{id}/charge - 잘못된 요청 (음수 금액)
	@Test
	void givenInvalidChargeRequest_whenChargePoint_thenReturnBadRequest() throws Exception {
		long userId = 1L;
		long chargeAmount = -1000L;

		mvc.perform(patch("/point/{id}/charge", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AmountRequest(chargeAmount))))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}
	// PATCH /point/{id}/use - 성공
	@Test
	void givenValidUseRequest_whenUsePoint_thenReturnUpdatedUserPoint() throws Exception {
		long userId = 1L;
		long useAmount = 2000L;
		UserPoint updatedPoint = new UserPoint(userId, 3000L, 123L);

		when(pointService.usePoint(eq(userId), any(AmountRequest.class)))
			.thenReturn(updatedPoint);

		mvc.perform(patch("/point/{id}/use", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AmountRequest(useAmount))))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.id").value(userId))
			.andExpect(jsonPath("$.point").value(3000))
			.andExpect(jsonPath("$.updateMillis").value(123));

		verify(pointService).usePoint(eq(userId), any(AmountRequest.class));
	}
	// PATCH /point/{id}/use - 잔액 부족
	@Test
	void givenUseRequestExceedingPoint_whenUsePoint_thenReturnBadRequest() throws Exception {
		long userId = 1L;
		long useAmount = 10000L;

		doThrow(new NotEnoughPointException(5000L))
			.when(pointService).usePoint(eq(userId), any(AmountRequest.class));

		mvc.perform(patch("/point/{id}/use", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AmountRequest(useAmount))))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value("NOT_ENOUGH_POINT"));

		verify(pointService).usePoint(eq(userId), any(AmountRequest.class));
	}
	// /{id} - 잘못된 사용자 ID
	@Test
	void givenInvalidUserId_whenGetPoint_thenReturnBadRequest() throws Exception {
		long userId = -1L;

		mvc.perform(get("/point/{id}", userId))
			.andExpect(status().isBadRequest());
	}




}