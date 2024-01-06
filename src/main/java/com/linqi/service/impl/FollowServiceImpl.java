package com.linqi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linqi.dto.Result;
import com.linqi.entity.Follow;
import com.linqi.mapper.FollowMapper;
import com.linqi.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linqi.utils.UserHolder;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *

 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();

        // 2.判断关注还是取关
        String flag = "关注失败!";
        if(isFollow){
            // 3.关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            save(follow);
            flag = "关注成功!";
        }else{
            // 4.取关，删除数据
            remove(new QueryWrapper<Follow>()
                    .eq("user_id",userId).eq("follow_user_id",followUserId));
            flag = "取关成功!";
        }
        return Result.ok(flag);
    }

    @Override
    public Result isFollow(Long followUserId) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.查询是否关注
        Integer count = query().eq("user_id",userId).eq("follow_user_id",followUserId).count();
        //3.判断是否关注
        return Result.ok(count >0);
    }

    @Override
    public Result followCommons(Long id) {
        return null;
    }
}
