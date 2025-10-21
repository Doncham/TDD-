package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.exception.InvalidPointAmountException;
import io.hhplus.tdd.point.exception.NotEnoughPointException;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
	@Mock
	private PointService pointService;
	@Mock
	private PointHistoryTable pointHistoryTable;
	@Mock
	private UserPointTable userPointTable;
	private final Clock fixedClock =
		Clock.fixed(Instant.parse("2025-10-20T00:00:00Z"), ZoneId.systemDefault());
	long now = Instant.now(fixedClock).toEpochMilli();

	@BeforeEach
	void setUp() {
		pointService = new PointService(userPointTable, pointHistoryTable, fixedClock);
	}
	// 포인트 충전 성공 테스트
	@Test
	void givenValidUserAndPointAmount_whenChargePoint_thenIncreaseUserPoint()
	{
		// given
		Long userId = 1L;
		Long chargeAmount = 1000L;
		Long currentPoint = 500L;
		UserPoint userPoint = new UserPoint(userId, currentPoint, now);
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		// when
		UserPoint updatedUserPoint = pointService.chargePoint(userId, chargeAmount);

		// then
		assertThat(updatedUserPoint.point()).isEqualTo(currentPoint + chargeAmount);
		verify(userPointTable).insertOrUpdate(userId, currentPoint + chargeAmount);
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
		assertThat(ex.getMessage()).contains("충전 금액");
		verifyNoInteractions(pointHistoryTable);
	}
	// 포인트 충전 시 포인트 충전 내역을 기록한다.
	@Test
	void givenValidUserAndValidPointAmount_whenChargePoint_thenRecordPointHistory(){
		// given
		Long userId = 1L;
		Long chargeAmount = 2000L;
		Long currentPoint = 5000L;
		UserPoint userPoint = new UserPoint(userId, currentPoint, now);
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		// when
		pointService.chargePoint(userId, chargeAmount);
		// then
		verify(pointHistoryTable).insert(
				userId,
				chargeAmount,
				TransactionType.CHARGE,
				now
		);
	}
	// 포인트 사용 테스트
	@Test
	void givenValidUserAndAmount_whenUsePoint_thenDecreaseUserPoint(){
		// given
		Long userId = 1L;
		Long useAmount = 5000L;
		Long currentPoint = 5000L;
		UserPoint userPoint = new UserPoint(userId, currentPoint, now);
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		// when
		UserPoint updatedUserPoint = pointService.usePoint(userId, useAmount);
		// then
		assertThat(updatedUserPoint.point()).isEqualTo(currentPoint - useAmount);
		verify(userPointTable).insertOrUpdate(userId, currentPoint - useAmount);
	}

	// 포인트 사용 테스트 - 잔액 부족
	@Test
	void givenValidUserAndExcessiveAmount_whenUsePoint_thenThrowException(){
		// given
		Long userId = 1L;
		Long useAmount = 5001L;
		Long currentPoint = 5000L;
		UserPoint userPoint = new UserPoint(userId, currentPoint, now);
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		// when + then
		NotEnoughPointException ex = assertThrows(NotEnoughPointException.class, () -> {
			pointService.usePoint(userId, useAmount);
		});
		assertThat(ex.getMessage()).contains("포인트가 부족");
		verifyNoInteractions(pointHistoryTable);
	}
	// 포인트 사용 시 포인트 사용 내역을 기록한다.
	@Test
	void givenValidUserAndValidAmount_whenUsePoint_thenRecordPointHistory(){
		// given
		Long userId = 1L;
		Long useAmount = 2000L;
		Long currentPoint = 5000L;
		UserPoint userPoint = new UserPoint(userId, currentPoint, now);
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		// when
		pointService.usePoint(userId, useAmount);
		// then
		verify(pointHistoryTable).insert(
				userId,
				useAmount,
				TransactionType.USE,
				now
		);
	}

	// 포인트 조회 성공 테스트
	@Test
	void givenValidUser_whenGetPoint_thenReturnUserPoint(){
		// given
		Long userId = 1L;
		// 이 부분은 재현이 안되니까 따로 Clock을 만들어야지
		UserPoint userPoint = new UserPoint(userId, 5000L, now);
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		// when
		UserPoint result = pointService.getPoint(userId);
		// then
		assertThat(result.point()).isEqualTo(userPoint.point());
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
		assertThat(result.point()).isEqualTo(0L);
	}
	// 특정 유저의 포인트 충전/사용 내역 조회 테스트
	@Test
	void givenValidUser_whenGetPointHistories_thenReturnPointHistories() {
		// given
		Long userId = 1L;
		when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(
				List.of(
						new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, now),
						new PointHistory(2L, userId, 500L, TransactionType.USE, now),
						new PointHistory(3L, userId, 200L, TransactionType.USE, now)
				)
		);

		// when
		List<PointHistory> histories = pointService.getPointHistories(userId);
		// then
		assertThat(histories).hasSize(3);
		assertThat(histories)
			.extracting(
				PointHistory::id,
				PointHistory::userId,
				PointHistory::amount,
				PointHistory::type,
				PointHistory::updateMillis
			)
			.contains(
				tuple(1L, userId, 1000L, TransactionType.CHARGE, now),
				tuple(2L, userId,  500L, TransactionType.USE, now),
				tuple(3L, userId,  200L, TransactionType.USE, now)
			);
		verify(pointHistoryTable).selectAllByUserId(userId);
	}
}