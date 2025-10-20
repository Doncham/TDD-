package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;
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
}
