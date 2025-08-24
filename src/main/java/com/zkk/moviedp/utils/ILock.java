package com.zkk.moviedp.utils;

public interface ILock {

    boolean tryLock(long timeoutSec);

    void unLock();
}
