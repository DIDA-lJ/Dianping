package com.linqi.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linqi.constants.SystemConstants;
import com.linqi.dto.Result;
import com.linqi.entity.Blog;
import com.linqi.entity.User;
import com.linqi.mapper.BlogMapper;
import com.linqi.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linqi.service.IUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 * @author linqi
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;
    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            queryBlogUser(blog);

        });
        return Result.ok(records);
    }

    @Override
    public Result queryBlogById(Long id) {
        //1.查询 blog
        Blog blog = getById(id);
        if(blog == null){
            return Result.fail("笔记不存在");
        }
        queryBlogUser(blog);
        return Result.ok(blog);
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

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
