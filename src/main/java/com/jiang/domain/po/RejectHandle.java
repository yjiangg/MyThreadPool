package com.jiang.domain.po;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public interface RejectHandle {

    void reject(Runnable rejectCommand, MyThreadPool threadPool);
}
