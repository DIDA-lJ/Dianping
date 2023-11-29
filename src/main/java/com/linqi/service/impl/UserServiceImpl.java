package com.linqi.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linqi.dto.Result;
import com.linqi.entity.User;
import com.linqi.mapper.UserMapper;
import com.linqi.service.IUserService;
import com.linqi.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 * @author linqi
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            // 2. 如果不符合，返回错误信息
            return Result.ok("手机号格式错误");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存验证码到 session
        session.setAttribute("code",code);
        // 5.发送验证码
        log.debug("手机号 {} 发送短信验证码成功，验证码:{}",phone,code);
        // 6.返回 ok
        return Result.ok();
    }
}
