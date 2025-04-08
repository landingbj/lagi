package ai.common.utils;

import ai.common.exception.RRException;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class ThreadPoolManager {

    private static Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);

    private static final Map<String, ExecutorService> executors = new ConcurrentHashMap<>();


    public static void registerExecutor(String name, ExecutorService executor) {
        ExecutorService executorService = executors.putIfAbsent(name, executor);
        if(executorService != null) {
            throw new RRException(501, StrUtil.format("线程池({})注册失败", name));
        }
    }

    public static void registerExecutor(String name) {
        ExecutorService e = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500),
                (r, executor)->{
                    logger.error(StrUtil.format("线程池队({})任务过多请求被拒绝", name));
                }
        );
        registerExecutor(name, e);
    }

    public static ExecutorService getExecutor(String name) {
        return executors.get(name);
    }

    public static void shutdown(String name) {
        ExecutorService executor = getExecutor(name);
        if(executor != null) {
            executor.shutdown();
        }
    }

 }
