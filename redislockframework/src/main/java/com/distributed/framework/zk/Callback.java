package com.distributed.framework.zk;


public interface Callback {

    public Object onGetLock() throws InterruptedException;

    public Object onTimeout() throws InterruptedException;
}
