# 秒杀系统设计文档 v1

## 项目目标
实现一个支持高并发的电商秒杀后端系统，具备限流、缓存库存、异步下单、幂等控制等核心能力。

## 技术栈
- Spring Boot
- Redis
- Kafka
- MySQL
- Swagger
- Guava RateLimiter
- Docker

## 模块规划
- 接口层（Controller）
- 服务层（Service）
- 数据访问层（Repository）
- 消息处理层（Kafka Consumer）
- Redis 缓存层

## 功能模块
- 商品库存预加载
- 秒杀下单接口
- 限流防刷
- Kafka 异步下单
- Redis 库存扣减 + 幂等判断