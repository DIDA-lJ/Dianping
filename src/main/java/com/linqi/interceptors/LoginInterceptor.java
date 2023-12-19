package com.linqi.interceptors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.linqi.dto.UserDTO;
import com.linqi.utils.RedisConstants;
import com.linqi.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author linqi
 * @version 1.0.0
 * @description 用户登录拦截器的编写
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        // 1.从请求头中获取 token
       String token = request.getHeader("authorization");

       if(StrUtil.isBlank(token)){
           // 4. 用户如果不存在，就拦截
           log.info("用户 token 信息不存在，返回错误.");
           response.setStatus(401);
           return false;
       }

        // 2.基于 token 中获取 Redis 中的用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object,Object> userMap = stringRedisTemplate.opsForHash().entries(key);

        // 3. 判断用户是否存在
        if(userMap.isEmpty()){
            // 4. 用户如果不存在，就拦截,返回 401 状态码
            log.info("用户信息不存在，返回错误.");
            response.setStatus(401);
            return false;
        }
        // 5.将查询到的 Hash 数据转化为 UserDTO 对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 6. 存在，保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO) ;

        //  7. 刷新 token 的过期时间
        stringRedisTemplate.expire(key,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8. 放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)  {
       // 移除用户，防止内存泄露
        UserHolder.removeUser();
    }
}
