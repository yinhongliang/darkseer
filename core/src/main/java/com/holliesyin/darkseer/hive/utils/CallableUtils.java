package com.holliesyin.darkseer.hive.utils;

import java.util.concurrent.*;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class CallableUtils {

    public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> future = executor.submit(callable);
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try {
            return future.get(timeout, timeUnit);
        }
        catch (TimeoutException e) {
            //remove this if you do not want to cancel the job in progress
            //or set the argument to 'false' if you do not want to interrupt the thread
            future.cancel(true);
            throw e;
        }
        catch (ExecutionException e) {
            //unwrap the root cause
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof Exception) {
                throw (Exception) e;
            } else {
                throw new IllegalStateException(t);
            }
        }
    }
}