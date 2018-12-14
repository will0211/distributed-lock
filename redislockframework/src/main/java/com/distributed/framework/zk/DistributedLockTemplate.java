package com.distributed.framework.zk;

/**
 * 分布式锁模板类
 */
public interface DistributedLockTemplate {

    /**
     *
     * @param lockId 锁id(对应业务唯一ID)
     * @param timeout 单位毫秒
     * @param callback 回调函数
     * @return
     */
    public Object execute(String lockId, int timeout, Callback callback);
}
