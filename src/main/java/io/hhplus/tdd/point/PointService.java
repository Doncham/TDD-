package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.exception.InvalidPointAmountException;
import io.hhplus.tdd.point.exception.NotEnoughPointException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final UserPointTable userPointTable;

	public UserPoint chargePoint(Long userId, Long amount) {
		if(amount <= 0) {
			throw new InvalidPointAmountException(amount);
		}
		UserPoint currentUserPoint = userPointTable.selectById(userId);

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
		return new UserPoint(
				userId,
				userPoint.point() - useAmount,
				System.currentTimeMillis()
		);
	}
}
