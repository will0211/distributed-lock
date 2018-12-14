package com.distributed.framework;


import com.distributed.framework.annotation.CacheLock;
import com.distributed.framework.annotation.LockedObject;

public interface SeckillInterface {
	@CacheLock(lockedPrefix="TEST_PREFIX")
	public void secKill(String arg1,@LockedObject Long arg2);
}
