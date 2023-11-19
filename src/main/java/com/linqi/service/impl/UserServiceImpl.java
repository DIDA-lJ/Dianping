package com.linqi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linqi.entity.User;
import com.linqi.mapper.UserMapper;
import com.linqi.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *

 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
