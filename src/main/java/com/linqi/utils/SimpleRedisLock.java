package com.linqi.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;


/**
 * @author linqi
 * @version 1.0.0
 * @description
 */

public class SimpleRedisLock implements ILock {
    private String name;
    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        // 调用 lua 脚本
        stringRedisTemplate.execute(
          UNLOCK_SCRIPT,
          Collections.singletonList(KEY_PREFIX + name),
          ID_PREFIX + Thread.currentThread().getId()
        );
    }

/**    v1 版本分布式锁
 //    @Override
 //    public void unlock() {
 //        // 获取线程 id
 //        String threadId = ID_PREFIX +Thread.currentThread().getId();
 //
 //        // 获取锁中的标示
 //        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
 //
 //        // 释放锁
 //        if (threadId.equals(id)) {
 //            stringRedisTemplate.delete(KEY_PREFIX + name);
 /*        }
  *      }
 */

}
