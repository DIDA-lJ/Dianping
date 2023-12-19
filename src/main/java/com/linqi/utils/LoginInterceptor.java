package com.linqi.utils;

import com.linqi.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author linqi
 * @version 1.0.0
 * @description 用户登录拦截器的编写
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取 session
        HttpSession session = request.getSession();

        // 2.通过 session 获取用户登录信息
        Object user = session.getAttribute("user");

        // 3. 判断用户是否存在
        if(user == null){
            // 4. 用户如果不存在，就拦截
            log.info("用户信息不存在，返回错误.");
            response.setStatus(401);
            return false;
        }

        // 5. 存在，保存用户信息到 ThreadLocal
        UserHolder.saveUser((UserDTO) user) ;
        // 6. 放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
       // 移除用户，防止内存泄露
        UserHolder.removeUser();
    }
}
