package org.aliyun.serverless.manager;

import org.aliyun.serverless.scaler.Scaler;
import org.aliyun.serverless.scaler.SimpleScaler;
import org.aliyun.serverless.config.Config;
import org.aliyun.serverless.model.Function;
import protobuf.SchedulerProto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class Manager {
    private static final Logger logger = Logger.getLogger(Manager.class.getName());
    private final ReadWriteLock rw = new ReentrantReadWriteLock(); // 避免对同一个requestId的多次请求
    private final Map<String, Scaler> schedulers = new HashMap<>();
    private final Config config;
    private final ArrayBlockingQueue<Scaler> warmupQueue = new ArrayBlockingQueue<Scaler>(1);

    public Manager(Config config) {
        this.config = config;
        // 这里是否可以init一个实例？或者提供一个warmup参数用来实例化？
        if (config.getWarmUp()) {
            // 预热
            this.warmupQueue.offer(this.warmUpOneInstance());
        }
    }

    public Scaler GetOrCreate(Function function) {
        String key = function.getKey(); // what's the meaning of the key? String to requestId.
        // readLock 表示可以有多个reader，大家都可以进来。一旦有writeLock，那么就不能再进来。
        rw.readLock().lock();
        try {
            // 如果为它预热，那是不是要绑定这个key？
            Scaler scheduler = schedulers.get(key);
            if (scheduler != null) {
                return scheduler;
            }
        } finally {
            rw.readLock().unlock();
        }
        // writeLock，如果还有reader或者writer，就不能进来
        rw.writeLock().lock();
        try {
            Scaler scheduler = schedulers.get(key);
            if (scheduler != null) {
                return scheduler;
            }
            if (config.getWarmUp() && !this.warmupQueue.isEmpty()) {
                Scaler warmup = this.warmupQueue.poll();
                logger.info("Get from warmup queue "  + function.getKey());
                schedulers.put(function.getKey(), warmup);
                return warmup;
            } else {
                logger.info("Create new scaler for app " + function.getKey());
                scheduler = new SimpleScaler(function, config);
                schedulers.put(function.getKey(), scheduler);
                return scheduler;
            }
        } finally {
            rw.writeLock().unlock();
        }
    }

    public Scaler Get(String functionKey) throws Exception {
        rw.readLock().lock();
        try {
            Scaler scheduler = schedulers.get(functionKey);
            if (scheduler != null) {
                return scheduler;
            }
            throw new Exception("scaler of app: " + functionKey + " not found");
        } finally {
            rw.readLock().unlock();
        }
    }

    private Scaler warmUpOneInstance() {
        try {
            logger.info("Create new scaler for warmUp");
            SchedulerProto.Meta meta = SchedulerProto.Meta.newBuilder()
                    .setKey(UUID.randomUUID().toString())
                    .build();
            return new SimpleScaler(new Function(meta), config);
        } catch (Exception error) {
            logger.info("Warm up instance failed:" + error.getMessage());
            return null;
        }
    }
}
