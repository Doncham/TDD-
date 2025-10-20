package io.hhplus.tdd.point;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.exception.InvalidPointAmountException;
import io.hhplus.tdd.point.exception.NotEnoughPointException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;
	private final Clock clock;

	public UserPoint chargePoint(Long userId, Long amount) {
		if(amount <= 0) {
			throw new InvalidPointAmountException(amount);
		}
		UserPoint currentUserPoint = userPointTable.selectById(userId);
		long now = Instant.now(clock).toEpochMilli();
		pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, now);
		return new UserPoint(
				userId,
				currentUserPoint.point() + amount,
				System.currentTimeMillis()
		);
	}

	public UserPoint getPoint(Long userId) {
		return userPointTable.selectById(userId);
	}

	public UserPoint usePoint(Long userId, Long useAmount) {
		UserPoint userPoint = userPointTable.selectById(userId);
		if(userPoint.point() < useAmount){
			throw new NotEnoughPointException(userPoint.point());
		}
		long now = Instant.now(clock).toEpochMilli();
		pointHistoryTable.insert(userId, useAmount, TransactionType.USE, now);
		return new UserPoint(
				userId,
				userPoint.point() - useAmount,
				System.currentTimeMillis()
		);
	}

	public List<PointHistory> getPointHistories(Long userId) {
		return pointHistoryTable.selectAllByUserId(userId);
	}
}
