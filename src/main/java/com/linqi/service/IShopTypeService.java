package com.linqi.service;

import com.linqi.dto.Result;
import com.linqi.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IShopTypeService extends IService<ShopType> {

    Result queryList();
}
