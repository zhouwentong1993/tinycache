# tinycache
[![codecov](https://codecov.io/gh/zhouwentong1993/tinycache/branch/master/graph/badge.svg?token=E25NIF4IKF)](https://codecov.io/gh/zhouwentong1993/tinycache)

# 简介
tinycache 是一个二级缓存结构的存储组件。由内存级别的 LRU 缓存和带持久化功能的二级缓存组成。
# 功能
提供了常用的 get、put、ttl 操作。
# 性能测试
在我的电脑上（macOS，8 核 16G）
- LRU 在 8 读 8 写线程混合读写情况下能达到 10w+ qps。
- 持久化存储在 8 读 8 写线程混合读写能达到 1w+ qps。
# TODO
- [ ] 提供更多的超时配置，参考 Guava。
- [ ] 针对磁盘写情形，通过可配置的刷盘策略来提高写入效率，在写入效率和准确性上达到平衡，参考 RocketMQ 刷盘策略。
- [ ] 完善单元测试，覆盖率达到 85%。