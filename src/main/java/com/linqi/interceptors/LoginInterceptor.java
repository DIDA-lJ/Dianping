package com.linqi.interceptors;

import com.linqi.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author linqi
 * @version 1.0.0
 * @description 用户登录拦截器的编写
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        if (UserHolder.getUser()==null){
            response.setStatus(401);
            return false;
        }
        return true;

    }

}
