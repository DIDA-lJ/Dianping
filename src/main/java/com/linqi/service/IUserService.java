package com.linqi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linqi.dto.LoginFormDTO;
import com.linqi.dto.Result;
import com.linqi.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *

 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();
}
