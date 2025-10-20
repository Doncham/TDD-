package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
	@InjectMocks
	private PointService pointService;
	@Mock
	private PointHistoryTable pointHistoryTable;
	@Mock
	private UserPointTable userPointTable;
	private final Clock fixedClock =
		Clock.fixed(Instant.parse("2025-10-20T00:00:00Z"), ZoneId.systemDefault());
	// 포인트 충전 성공 테스트
	@Test
	void givenValidUserAndPointAmount_whenChargePoint_thenIncreaseUserPoint()
	{
		// given
		Long userId = 1L;
		Long chargeAmount = 1000L;
		Long currentPoint = 500L;
		UserPoint userPoint = new UserPoint(userId, currentPoint, Instant.now(fixedClock).toEpochMilli());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		// when
		UserPoint updatedUserPoint = pointService.chargePoint(userId, chargeAmount);

		// then
		Assertions.assertThat(updatedUserPoint.point()).isEqualTo(currentPoint + chargeAmount);
	}

	// 포인트 충전 음수 방지 테스트
	@Test
	void givenValidUserAndInvalidPointAmount_whenChargePoint_thenThrowException()
	{
		// given
		Long userId = 1L;
		Long chargeAmount = -1000L;

		// when & then
		InvalidPointAmountException ex = assertThrows(InvalidPointAmountException.class, () -> {
			pointService.chargePoint(userId, chargeAmount);
		});
		Assertions.assertThat(ex.getMessage()).isEqualTo(InvalidPointAmountException.MESSAGE + chargeAmount);
	}
	// 포인트 조회 성공 테스트
	@Test
	void givenValidUser_whenGetPoint_thenReturnUserPoint(){
		// given
		Long userId = 1L;
		// 이 부분은 재현이 안되니까 따로 Clock을 만들어야지
		UserPoint userPoint = new UserPoint(userId, 5000L, Instant.now(fixedClock).toEpochMilli());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		// when
		// 이 메서드에서 포인트를 조회해서 UserPoint를 반환하겠지
		// 없는 경우에 대해서도 테스트 해야 함
		UserPoint result = pointService.getPoint(userId);

		// then
		Assertions.assertThat(result.point()).isEqualTo(userPoint.point());
	}

	// 포인트 조회 테스트 - 없는 사용자
	@Test
	void givenInvalidUser_whenGetPoint_thenReturnZeroPoint(){
		// given
		Long userId = 999L;
		when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

		// when
		UserPoint result = pointService.getPoint(userId);

		// then
		Assertions.assertThat(result.point()).isEqualTo(0L);
	}

	// 포인트 사용 테스트
	@Test
	void givenValidUserAndAmount_whenUsePoint_thenDecreaseUserPoint(){

	}

	// 포인트 사용 테스트 - 잔액 부족
	@Test
	void givenValidUserAndExcessiveAmount_whenUsePoint_thenThrowException(){

	}
}