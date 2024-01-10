# Dianping
一款基于 Java 开发的店铺点评 APP，实现了找店铺 =>写点评 => 看热评 => 点赞关注=>关注 Feed 流的完整业务流程

## 实现功能
<ul>
    <li>店铺查询</li>
    <li>短信登录</li>
    <li>优惠券秒杀</li>
    <li>发布探店笔记</li>
    <li>查看热点评论</li>
    <li>点赞关注实现</li>
    <li>粉丝信息推送(推 Feed 流实现)</li>
    <li>粉丝信息查询(滚动分页优化)</li>
    <li>附近商户查询</li>
    <li>签到功能实现</li>
    <li>百万数据UV统计</li>
</ul>

## 项目架构流程图
![image](https://github.com/DIDA-lJ/dianping/assets/97254796/555f583c-5eed-4b4c-8d24-a663cda173f6)

## 目录代码结构介绍

```
├─main  项目核心目录
│  ├─java 项目 java 代码目录
│  │  └─com
│  │      └─linqi
│  │          ├─config        项目 公共配置 
│  │          ├─controller    项目 Controller 层
│  │          ├─dto           项目 请求信息层
│  │          ├─entity        项目 实体层
│  │          ├─mapper        项目 数据访问层
│  │          ├─service       项目 核心业务 Service 层
│  │          │  └─impl       项目 Service 层实现接口
│  │          └─utils
│  └─resources 项目资源目录
│      ├─db 数据库 SQL 文件目录
│      └─mapper 项目 Mapper
└─test
    └─java
        └─com
            └─linqi 项目 测试文件目录
```


## 项目实现
1. 短信登录功能：使用 Redis 实现分布式 Session,解决了集群件登录态同步的问题，并且使用 Hash 代替 String 来存储用户信息，节省了 10 % 左右的内存，且有利于单字段的修改，最后改进成了使用 Redis + Token 机制实现单点登录。
2. 店铺查询功能：使用 Redis 实现了对于高频访问店铺进行缓存，降低数据库压力的同时，提升了接近 90% 的数据查询性能。
3. 为方便其他业务后续使用缓存，使用泛型 + 函数式编程实现了通用缓存访问静态方法，并且解决了缓存雪崩、缓存穿透等问题。
4. 使用常量类全局管理 Reids Key 前缀、TTL 等内容，保证了键空间的业务隔离，减少冲突。
5. 对 Redis 的所有 Key 设置 N + n 的过期时间，从而合理使用内存并且防止缓存雪崩；
6. 对于热点店铺，使用逻辑过期的机制解决缓存击穿问题，防止数据库宕机；
7. 使用 Redis 自增的方式实现全局 ID 生成器；
   
## 项目完成结果
### 优惠券秒杀功能（Redis stream 消息队列实现）
![image](https://github.com/DIDA-lJ/Dianping/assets/97254796/baf48118-544d-4e37-97cd-2112e84d16a6)
