# Dianping
基于 SpringBoot + Redis 的店铺点评 APP，实现了找店铺 =>写点评 => 看热评 => 点赞关注=>关注 Feed 流的完整业务流程



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
