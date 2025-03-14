package com.jiang.domain.po;





import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
    private final int corePoolSize;
    private final int maxSize;
    private final int timeout;
    private final TimeUnit timeUnit;
    public final BlockingQueue<Runnable> blockingQueue;
    private final RejectHandle rejectHandle;

    public MyThreadPool(int corePoolSize, int maxSize, int timeout, TimeUnit timeUnit,
                        BlockingQueue<Runnable> blockingQueue, RejectHandle rejectHandle) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.blockingQueue = blockingQueue;
        this.rejectHandle = rejectHandle;
    }



    List<Thread> coreList = new ArrayList<>();
    List<Thread> supportList= new ArrayList<>();



    //以下方法不是原子操作（线程不安全），可以加cas原子变量，或者加锁来解决
    void execute(Runnable comand){
        if (coreList.size() < corePoolSize){
            Thread thread =new CoreThread();
            coreList.add(thread);
            thread.start();
        }
        if (blockingQueue.offer(comand)){
            return;
        }
        if(coreList.size() + supportList.size()<maxSize) {
            Thread thread = new SupportThread();
            supportList.add(thread);
            thread.start();

        }
        if (!blockingQueue.offer(comand)) {
            rejectHandle.reject(comand, this);
        }
    }

    class CoreThread extends  Thread{
        @Override
        public void run() {
                while(true){
                    try {
                        Runnable comand =blockingQueue.take();
                        comand.run();
                    }catch (InterruptedException e){
                        throw new RuntimeException(e);
                    }
                }
        }
    }

    class SupportThread extends Thread{
        @Override
        public void run() {
                while(true){
                    try {
                        Runnable comand =blockingQueue.poll(timeout, timeUnit);
                        if(comand == null){
                            break;
                        }
                        comand.run();
                    }catch (InterruptedException e){
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(Thread.currentThread().getName() + "线程结束了，线程池处于不忙碌状态");

        }

    }

}
