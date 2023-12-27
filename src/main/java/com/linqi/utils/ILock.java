package com.linqi.utils;

/**
 * @InterfaceName ILock
 * @Description
 * @Version 1.0.0
 * @Author LinQi
 * @Date 2023/12/27
 */
public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec 锁的持有时间，过期之后自动释放
     * @return true 代表锁获取成功; false 代表锁获取失败
     */
    boolean tryLock(long  timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
