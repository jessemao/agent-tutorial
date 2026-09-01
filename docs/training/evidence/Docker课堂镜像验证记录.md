# Docker课堂镜像验证记录

> 验证日期：2026-08-28  
> 镜像：`training-wms-classroom:0.7`

## 1. 产物信息

| 字段 | 结果 |
|---|---|
| Image ID | `sha256:e4aa9722736925f4b03cc446dd27cda37a496c6bfa54c9f2a997d16d32e19c06` |
| 架构 | `linux/amd64` |
| 大小 | 756,065,882 bytes（约721 MiB） |
| 初始代码标签 | `s2-t01-start` |
| 初始代码提交 | `d8f732d5a0903b8b0c655a1b31820ed02baf6a30` |
| Java | Eclipse Temurin 17.0.15 |
| Maven | Apache Maven 3.9.9 |
| Spring Boot | 2.7.18（项目版本） |
| 源码编译级别 | Java 8 source/target（保持根POM现状） |

## 2. 构建阶段证据

- 使用AWS Public ECR的Docker官方Maven/Temurin镜像源。
- Maven Central在当前网络中出现TLS握手中断，制镜阶段改用`docker/classroom/settings.xml`中的公共Maven镜像。
- 构建上下文由只读标签`s2-t01-start`导出，不使用当前T06答案工作目录。
- 构建阶段真实执行测试以预热运行器，通过`maven.test.failure.ignore=true`允许T01教学预置失败后继续安装依赖。
- T01 起始结果：8 条业务集成测试，6 通过、2 失败，与首个 Bug 任务的预期红灯一致。

## 3. 第一次断网验收发现

首次在`--network none`中执行失败：依赖已缓存，但Maven记录的下载仓库ID与运行时默认Central不一致，导致离线模式拒绝使用Spring Boot parent POM。

修正：

- 最终镜像保留不含密钥的`/opt/classroom/settings.xml`。
- 通过`MAVEN_ARGS=-s /opt/classroom/settings.xml`保证所有课堂Maven命令使用与缓存一致的仓库ID。
- 课堂命令仍使用`-o`，且验收容器使用`--network none`，不会访问远程仓库。

## 4. 最终断网验收

执行方式：

```text
docker run --rm --network none --user 501:20 \
  -v <training-wms>:/workspace training-wms-classroom:0.7
```

结果：

| 检查 | 结果 |
|---|---|
| 容器网络 | `none` |
| 运行用户 | 非root，与主机工作目录UID/GID一致 |
| Maven模式 | `-o -B -ntp clean verify` |
| 业务集成测试 | 8条：6通过、2失败、0错误 |
| 预期失败 | 取消预占后库存未释放的2个场景 |
| 总耗时 | 21.169秒（Apple Silicon上跨架构验证） |
| 结论 | `PASS`（成功复现T01预期红灯） |

## 5. 镜像隔离检查

- `java -version`与`mvn -version`通过。
- 最终镜像不存在`/seed`，`/workspace`中包含已清理`target/`的T01初始代码。
- 镜像内不包含`.git`、Git历史、任务卡或后续答案源码。
- OCI标签已写入课程起点和精确提交号，可供发布前复核。

## 6. 服务启动验收

- 按主机UID/GID以非root用户启动容器。
- `training-wms-start`先在挂载的学员项目中离线打包，再直接运行Spring Boot JAR，不会误用制镜时的答案产物。
- 映射主机`18080`到容器`8080`，请求`/actuator/health`返回`{"status":"UP"}`。
- 结论：`PASS`。

## 7. 未完成的交付验收

- 尚未导出`training-wms-classroom-0.7.tar`和SHA-256；应在最终架构确认后完成。
- 尚未在第二台无缓存培训机执行`docker image load`和断网复验。

当前放行结论：`PASS_WITH_FOLLOW_UP`。`linux/amd64`镜像已在当前arm64机器上通过Docker跨架构断网验证；正式分发前仍需推送镜像仓库，并在一台原生amd64培训机复验。
