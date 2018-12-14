package com.distributed.framework.idempotency;


import com.distributed.framework.redis.RedisReentrantLock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class IdemTest {
	private static Long commidityId1 = 10000001L;
	//幂等测试
	@Test
	public void testSecKill(){
		int threadCount = 100;
		int splitPoint = 100;
		CountDownLatch endCount = new CountDownLatch(threadCount);
		CountDownLatch beginCount = new CountDownLatch(1);

		Thread[] threads = new Thread[threadCount];
		//10个线程并发
		for(int i= 0;i < splitPoint;i++){
			int finalI = i;
			threads[i] = new Thread(new  Runnable() {
				public void run() {
					try {
						//等待在一个信号量上，挂起
						beginCount.await();
						boolean result=RedisReentrantLock.getInstance().tryLock(commidityId1.toString(),0,TimeUnit.MILLISECONDS);
						if(!result){
							System.out.println("幂等验证：正在处理中");
							return;
						}
						System.out.println("处理业务逻辑");
						//	Thread.sleep(1000);
					}  catch (InterruptedException e) {
						e.printStackTrace();
					}finally {
						RedisReentrantLock.getInstance().unlock(commidityId1.toString());
						endCount.countDown();
					}
				}
			});
			threads[i].start();

		}

		long startTime = System.currentTimeMillis();
		//主线程释放开始信号量，并等待结束信号量
		beginCount.countDown();
		
		try {
			//主线程等待结束信号量
			endCount.await();
			System.out.println("total cost " + (System.currentTimeMillis() - startTime));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
