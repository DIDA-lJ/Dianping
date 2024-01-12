package com.linqi.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linqi.constants.SystemConstants;
import com.linqi.dto.Result;
import com.linqi.entity.Shop;
import com.linqi.mapper.ShopMapper;
import com.linqi.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linqi.utils.RedisData;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.linqi.constants.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Override
    public Result queryById(Long id) {
        // 缓存穿透解决方案
//         Shop shop = queryWithPassThrough(id);
        // 互斥锁解决缓存击穿问题
        Shop shop = queryWithMutex(id);
        if(id == null){
            return Result.fail("店铺不存在!");
        }
        return Result.ok(shop);
    }

    /**
     * 互斥锁解决缓存击穿问题
     *
     * @param id
     * @return
     */
    private Shop queryWithMutex(Long id) {
        Shop shop = null;

        // 0. 定义店铺查询缓存
        String key = CACHE_SHOP_KEY + id;

        // 1. 从 Redis 中查询用户缓存
        String cache = stringRedisTemplate.opsForValue().get(key);

        // 2.查询用户信息是否存在
        if (StrUtil.isNotBlank(cache)) {
            // 3.存在，直接返回用户信息
            return JSONUtil.toBean(cache, Shop.class);
        }

        // 判断命中的是否是空值
        if (cache != null) {
            return null;
        }
        // 4. 实现缓存重构
        // 4.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 判断是否获取锁成功
            if (!isLock) {
                // 4.3 失败,则休眠并且重试
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            // 5. 不存在，获取锁成功，查询数据库
            shop = getById(id);

            // 6. 数据库中依旧不存在，直接返回错误
            if (shop == null) {
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }

            // 7.查询数据库存在，将结果写入 Redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

            // 8. 释放互斥锁
            unlock(lockKey);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unlock(lockKey);
        }

        // 9.返回信息
        return shop;
    }

    public Shop queryWithLogicalExpire(Long id) {
        // 0. 定义店铺查询缓存
        String key = CACHE_SHOP_KEY + id;

        // 1. 从 Redis 中查询用户缓存
        String cache = stringRedisTemplate.opsForValue().get(key);

        // 2.查询用户信息是否存在
        if (StrUtil.isBlank(cache)) {
            // 3.存在，直接返回用户信息
            return null;
        }

        // 4.命中，需要先把 Json 反序列化为对象
        RedisData redisData = JSONUtil.toBean(cache,RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 5.判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())){
            // 5.1 未过期，直接返回店铺信息
            return  shop;
        }

        // 5.2 已过期，需要缓存重建
        // 6.缓存重建
        String lockKey = LOCK_SHOP_KEY + id;
        // 6.1 获取互斥锁
        boolean isLock = tryLock(lockKey);
        // 6.2 判断锁是否获取成功
        if(isLock){
            // 6.3 锁获取成功，直接开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() ->{
                try {
                    // 重建缓存
                    this.saveShop2Redis(id,20L);
                } finally {
                    unlock(lockKey);
                }
            });
        }

        // 6.4 返回过期的店铺信息
        return shop;
    }

    /**
     * 解决缓存穿透代码
     *
     * @param id 店铺 id
     * @return
     */

    public Shop queryWithPassThrough(Long id) {
        // 0. 定义店铺查询缓存
        String key = CACHE_SHOP_KEY + id;

        // 1. 从 Redis 中查询用户缓存
        String cache = stringRedisTemplate.opsForValue().get(key);

        // 2.查询用户信息是否存在
        if (StrUtil.isNotBlank(cache)) {
            // 3.存在，直接返回用户信息
            return JSONUtil.toBean(cache, Shop.class);
        }

        // 判断命中的是否是空值
        if (cache != null) {
            return null;
        }

        // 4. 不存在，查询数据库
        Shop shop = getById(id);

        // 5. 数据库中依旧不存在，直接返回错误
        if (shop == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        // 6.查询数据库存在，将结果写入 Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 7.返回信息
        return shop;
    }

    public boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    public void saveShop2Redis(Long id,Long expireSeconds){
        // 1. 查询店铺数据
        Shop shop = getById(id);

        // 2. 封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));

        // 3.写入 Redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id,JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();

        if (id == null) {
            return Result.fail("店铺 id 不能为空!");
        }

        // 1. 更新数据库
        updateById(shop);
        // 2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 1.判断是否需要根据坐标查询
        if (x == null || y == null) {
            // 不需要坐标查询，按数据库查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            // 返回数据
            return Result.ok(page.getRecords());
        }

        // 2.计算分页参数
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;

        // 3.查询redis、按照距离排序、分页。结果：shopId、distance
        String key = SHOP_GEO_KEY + typeId;
        // GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDISTANCE
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(
                        key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );
        // 4.解析出id
        if (results == null) {
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            // 没有下一页了，结束
            return Result.ok(Collections.emptyList());
        }
        // 4.1.截取 from ~ end的部分
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            // 4.2.获取店铺id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            // 4.3.获取距离
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });
        // 5.根据id查询Shop
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        // 6.返回
        return Result.ok(shops);
    }
}
