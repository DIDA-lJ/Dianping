package com.linqi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.linqi.dto.Result;
import com.linqi.entity.Shop;
import com.linqi.mapper.ShopMapper;
import com.linqi.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.linqi.constants.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *

 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryById(Long id) {
        // 0. 定义店铺查询缓存
        String key = CACHE_SHOP_KEY + id;

        // 1. 从 Redis 中查询用户缓存
        String cache = stringRedisTemplate.opsForValue().get(key);

        // 2.查询用户信息是否存在
        if(StrUtil.isNotBlank(cache)){
            // 3.存在，直接返回用户信息
            Shop shop = JSONUtil.toBean(cache,Shop.class);
            return Result.ok(shop);
        }

        // 判断命中的是否是空值
        if(cache != null){
            return Result.fail("店铺信息不存在！");
        }

        // 4. 不存在，查询数据库
        Shop shop = getById(id);

        // 5. 数据库中依旧不存在，直接返回错误
        if(shop == null){
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.fail("店铺不存在，抱歉!");
        }

        // 6.查询数据库存在，将结果写入 Redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 7.返回信息
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();

        if(id == null){
            return Result.fail("店铺 id 不能为空!");
        }

        // 1. 更新数据库
        updateById(shop);
        // 2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }
}
