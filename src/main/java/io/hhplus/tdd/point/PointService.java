package io.hhplus.tdd.point;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.AmountRequest;
import io.hhplus.tdd.point.exception.InvalidPointAmountException;
import io.hhplus.tdd.point.exception.NotEnoughPointException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;
	private final Clock clock;
	// 충전하고 업데이트를 해야하는구나(이걸 왜 생각을 못 했지...)
	public UserPoint chargePoint(Long userId, AmountRequest requestAmount) {
		Long amount = requestAmount.getAmount();
		if(amount <= 0) {
			throw new InvalidPointAmountException(amount);
		}
		UserPoint currentUserPoint = userPointTable.selectById(userId);
		long now = Instant.now(clock).toEpochMilli();
		pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, now);
		userPointTable.insertOrUpdate(userId, currentUserPoint.point() + amount);
		return new UserPoint(
				userId,
				currentUserPoint.point() + amount,
				now
		);
	}

	public UserPoint getPoint(Long userId) {
		return userPointTable.selectById(userId);
	}
	// 이것도 포인트 차감 업데이트 안했네
	public UserPoint usePoint(Long userId, AmountRequest amountRequest) {
		Long useAmount = amountRequest.getAmount();
		UserPoint userPoint = userPointTable.selectById(userId);
		if(userPoint.point() < useAmount){
			throw new NotEnoughPointException(userPoint.point());
		}
		long now = Instant.now(clock).toEpochMilli();
		pointHistoryTable.insert(userId, useAmount, TransactionType.USE, now);
		userPointTable.insertOrUpdate(userId, userPoint.point() - useAmount);
		return new UserPoint(
				userId,
				userPoint.point() - useAmount,
				now
		);
	}

	public List<PointHistory> getPointHistories(Long userId) {
		return pointHistoryTable.selectAllByUserId(userId);
	}
}
