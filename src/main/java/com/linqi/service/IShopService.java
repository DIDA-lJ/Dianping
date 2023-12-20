package com.linqi.service;

import com.linqi.dto.Result;
import com.linqi.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);
}
