package io.hhplus.tdd.point;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hhplus.tdd.point.dto.AmountRequest;

@SpringBootTest
public class PointControllerConcurrencyTest {
	@Autowired
	private PointService pointService;
	// 동시성이 깨지는 테스트
	@Test
	void givenMultiRequest_whenChargePointAndUsePoint_thenHappenRaceCondition() throws Exception {
		// given
		long userId = 1L;
		long chargeAmount = 1L;
		long useAmount = 1L;
		// charge/use 각각 n회 -> 총 200회 수행
		int n = 100;
		// 잔액 부족 방지를 위해 100L 미리 충전
		pointService.chargePoint(userId, new AmountRequest(100L));

		ExecutorService pool = Executors.newFixedThreadPool(16);
		CountDownLatch startGate = new CountDownLatch(1);
		CountDownLatch doneGate = new CountDownLatch(n * 2);

		Runnable chargeTask = () -> {
			try {
				startGate.await();
				pointService.chargePoint(userId, new AmountRequest(chargeAmount));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				doneGate.countDown();
			}
		};
		Runnable useTask = () -> {
			try {
				startGate.await();
				pointService.usePoint(userId, new AmountRequest(useAmount));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		 	finally {
				doneGate.countDown();
			}
		};
		// when

		// 작업 제출
		for (int i = 0; i < n; i++) {
			pool.submit(chargeTask);
			pool.submit(useTask);
		}

		// 모든 스레드 동시에 출발
		startGate.countDown();

		// 완료 대기 + 타임아웃
		boolean finished = doneGate.await(10, TimeUnit.SECONDS);
		pool.shutdownNow();
		// then
		UserPoint finalPoint = pointService.getPoint(userId);
		Assertions.assertThat(finalPoint.point()).isEqualTo(100L);
	}
}
