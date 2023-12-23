package com.linqi.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.linqi.constants.RedisConstants.LOCK_SHOP_KEY;
import static com.linqi.constants.RedisConstants.LOCK_SHOP_TTL;

/**
 * @author linqi
 * @version 1.0.0
 * @description 缓存工具类
 */
@Component
@Slf4j
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 自定义线程池，可以封装成一个新的类进行引用
     */
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        this.stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicExpire(String key, Object value, Long time, TimeUnit unit) {
        // 由于要设置逻辑过期时间，因此将其再封装成一个对象。
        RedisData redisData = new RedisData();
        redisData.setData(value);
        // 在现在时间的基础上再增加指定单位的时间--转换成秒
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 缓存击穿实现
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String shopKey = keyPrefix + id;
        // 1. 从redis中查询商铺缓存
        String jsonShop = stringRedisTemplate.opsForValue().get(shopKey);
        // 2. 判断缓存是否命中
        if (StrUtil.isNotBlank(jsonShop)) {
            // 3. 命中则直接返回商铺信息
            return JSONUtil.toBean(jsonShop, type);
        }
        // 可能是null 或者"";
        if (jsonShop != null) {
            return null;
        }
        R r = dbFallback.apply(id);
        // 4. 未命中根据id查询数据库
        if (r == null) {
            // 5. 空值写入redis。
            this.set(shopKey, "", time, unit);
            // 判断商铺是否存在-不存在返回报错信息
            return null;
        }
        // 6. 存在则写入Redis中
        this.set(shopKey, r, time, unit);
        // 7. 返回店铺信息
        return r;
    }

    /**
     * 缓存穿透- 基于逻辑缓存
     */
    public <R, ID> R queryWithExpireTime(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String shopKey = keyPrefix + id;
        // 1. 从redis中查询商铺缓存
        String jsonShop = stringRedisTemplate.opsForValue().get(shopKey);
        // 2. 判断缓存是否命中
        if (StrUtil.isBlank(jsonShop)) {
            // 3. 命中则直接返回商铺信息
            return null;
        }
        // 缓存穿透判断可能是null 或者"";
        // 4. 命中
        // 4.1 判断缓存是否过期
        RedisData redisData = JSONUtil.toBean(jsonShop, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(data, type);
        LocalDateTime expireTime = redisData.getExpireTime();
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 4.2 未过期
            return r;
        }
        // 4.3 过期尝试获取锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean flag = tryLock(lockKey);
        if (flag) {
            String doubleCheck = stringRedisTemplate.opsForValue().get(shopKey);
            if (StrUtil.isNotBlank(doubleCheck)) {
                if (expireTime.isAfter(LocalDateTime.now())) {
                    // 没有过期就直接返回
                    RedisData doubleData = JSONUtil.toBean(jsonShop, RedisData.class);
                    return JSONUtil.toBean((JSONObject) doubleData.getData(), type);
                }
            }
            // 5. 获取锁成功开启独立线程
            EXECUTOR_SERVICE.submit(() -> {
                try {
                    // 重建缓存
                    R r1 = dbFallback.apply(id);

                    // 写入redis
                    this.setWithLogicExpire(shopKey, r1, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 6. 释放锁
                    unlock(lockKey);
                }
            });
        }
        // 7. 返回店铺信息
        return r;
    }

    public boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    public void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

}
