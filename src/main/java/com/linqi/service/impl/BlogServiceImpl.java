package com.linqi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linqi.constants.SystemConstants;
import com.linqi.dto.Result;
import com.linqi.dto.ScrollResult;
import com.linqi.dto.UserDTO;
import com.linqi.entity.Blog;
import com.linqi.entity.Follow;
import com.linqi.entity.User;
import com.linqi.mapper.BlogMapper;
import com.linqi.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linqi.service.IFollowService;
import com.linqi.service.IUserService;
import com.linqi.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.linqi.constants.RedisConstants.BLOG_LIKED_KEY;
import static com.linqi.constants.RedisConstants.FEED_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {


    @Override
    public Result queryHotBlog(Integer current) {
        return null;
    }

    @Override
    public Result queryBlogById(Long id) {
        return null;
    }

    @Override
    public Result likeBlog(Long id) {
        return null;
    }

    @Override
    public Result queryBlogLikes(Long id) {
        return null;
    }

    @Override
    public Result saveBlog(Blog blog) {
        return null;
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        return null;
    }
}
