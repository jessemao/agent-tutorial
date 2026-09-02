# Docker课堂环境使用与交付

> 用途：构建、验证、分发和使用 `linux/amd64` 课堂镜像。  
> 状态：课程环境说明，不是生产部署手册。  
> 限制：不得把镜像内测试结果当作当前分支结果；依赖、POM、JDK、起始代码或构建方式变化后必须重建并重新做断网验证。

> 目标：使用一个预热的课堂镜像提供Java 17、Maven 3.9.9和项目依赖，学员无需在主机安装Java/Maven，断网后仍可构建、测试和启动。

## 1. 设计结论

- 镜像名：`training-wms-classroom:0.7`。
- 运行工具链：Eclipse Temurin JDK 17 + Maven 3.9.9。
- 基础镜像默认从AWS Public ECR的Docker官方镜像源获取，避免依赖单一Docker Hub通道；内容仍为官方Maven/Temurin镜像。
- 源码编译级别：保持项目 `pom.xml` 现有 Java 8 源码/目标兼容，不因镜像修改业务行为。
- 镜像内置来自`s2-t01-start`的课程初始代码，不包含Git历史、后续任务答案或客户材料。
- 学员当前T01—T06工作目录以只有容器运行时可见的bind mount挂载到`/workspace`。
- 默认命令是真正的Maven离线验收：`mvn -o -B -ntp clean verify`。

## 2. 制作镜像（有网络准备阶段）

在仓库根目录执行：

```bash
docker compose -f compose.classroom.yml build classroom
```

构建过程会：

1. 下载Java 17 + Maven基础镜像。
2. 在临时阶段复制项目POM和源码。
3. 执行测试并允许T01预期的红灯继续制镜，同时预热编译、测试、安装、Spring Boot和运行依赖。
4. 在最终镜像中保留初始代码和Maven依赖缓存，删除构建产物。

制镜阶段使用`docker/classroom/settings.xml`中的公共Maven镜像以应对Maven Central网络握手不稳定。最终镜像保留这份不含密钥的仓库标识配置，使Maven在离线模式下能识别依赖缓存的来源；课堂运行仍使用`-o`，不发起网络请求。若甲方必须使用内部Nexus/Artifactory，将该URL替换为甲方已审批的镜像并重新完成全量及断网验收。

任何依赖或插件变更后，必须重新构建镜像，不能假设旧缓存仍可离线使用。

## 3. 学员使用

### 3.1 macOS/Linux设置文件所有者

```bash
export CLASSROOM_UID=$(id -u)
export CLASSROOM_GID=$(id -g)
```

Windows Docker Desktop可使用Compose默认值；如组织策略要求非默认用户，由讲师在T-5联调时统一确认。

### 3.2 宿主持久化运行空间

Compose 将容器的 `HOME`、`MAVEN_CONFIG`、`TMPDIR` 和 Java 临时目录统一指向项目根目录的 `.classroom-runtime/`。该目录位于学员电脑的项目工作区中，经 `/workspace` 挂载后供容器使用，因此容器退出或重建后仍然保留，也不会继续占用容器可写层的 `/tmp`。

- `.classroom-runtime/` 已加入 `.gitignore`，不得提交到仓库。
- Maven 依赖仍读取镜像内的 `/opt/training-m2/repository`；宿主目录只保存运行期配置、日志和临时文件，不重复复制整套离线依赖。
- 每个学员工作目录拥有独立运行空间，不应在多个小组之间共享该目录。
- 删除 `.classroom-runtime/` 只会清理本机课堂缓存，不会删除源码；执行前应停止对应课堂容器。
- 该设置避免课堂进程持续写满 Docker 容器可写层，但 Docker Engine 已经没有可用空间时，仍需先清理无用镜像、构建缓存或扩大 Docker Desktop 磁盘容量。

### 3.3 离线构建和测试

```bash
docker compose -f compose.classroom.yml run --rm classroom
```

默认会运行：

```bash
mvn -o -B -ntp clean verify
```

### 3.4 运行指定测试

```bash
docker compose -f compose.classroom.yml run --rm classroom \
  mvn -o -B -ntp -pl training-server -am \
  -Dtest=WmsFlowIntegrationTest test
```

### 3.5 启动训练服务

```bash
docker compose -f compose.classroom.yml run --rm --service-ports classroom \
  training-wms-start
```

`training-wms-start`先使用`-DskipTests clean package`离线打包当前挂载的学员版本，再直接运行`training-server`产生的Spring Boot JAR。这避免向镜像内的只读Maven缓存写入，也避免学员起始版本误用制镜时的答案构件。

启动后访问`http://localhost:8080/actuator/health`。每个学员工作目录单独运行；同一主机并行多组时需为每组分配不同端口。

## 4. 导出和分发（有网络准备阶段）

导出为可移动离线文件：

```bash
docker image save training-wms-classroom:0.7 \
  -o training-wms-classroom-0.7.tar
```

同时生成校验值：

```bash
shasum -a 256 training-wms-classroom-0.7.tar \
  > training-wms-classroom-0.7.tar.sha256
```

不将大体积`tar`文件提交到Git仓库。将它与对应SHA-256通过甲方允许的离线介质分发。

## 5. 培训机导入（可完全断网）

```bash
shasum -a 256 -c training-wms-classroom-0.7.tar.sha256
docker image load -i training-wms-classroom-0.7.tar
docker image inspect training-wms-classroom:0.7
```

导入后禁用网络做一次真实验收。macOS/Linux示例：

```bash
docker run --rm --network none \
  --user "$(id -u):$(id -g)" \
  -v "$PWD:/workspace" \
  training-wms-classroom:0.7
```

只有断网状态下的`clean verify`成功，才能将离线环境标记为已放行。

## 6. 验收清单

- [ ] `docker image inspect training-wms-classroom:0.7`可见。
- [ ] 镜像中`java -version`为17、`mvn -version`为3.9.9。
- [ ] 镜像中`/workspace`为`s2-t01-start`初始代码，且不包含`.git`、任务卡和答案。
- [ ] 镜像标签记录`io.training.course.ref=s2-t01-start`及对应提交号。
- [ ] 断网挂载`s3-t06-answer`时，`mvn -o clean verify`通过16条业务测试 + 3条平台契约测试。
- [ ] 不挂载项目时，T01初始代码显示8条测试中2条预期失败。
- [ ] 服务在容器中可启动，主机可访问8080健康检查。
- [ ] 非root学员用户可在挂载目录创建`target/`，课后不产生无法删除的root文件。
- [ ] 导出的`tar`通过SHA-256校验，并在一台无构建缓存的培训机成功导入。

当前本机验证记录见`docs/training/evidence/Docker课堂镜像验证记录.md`。默认标签`training-wms-classroom:0.7`与显式标签`0.7-amd64`均为`linux/amd64`；原本机架构版仍以`0.7-arm64`保留。正式交付前应在一台原生amd64培训机上再做一次断网复验。

## 7. 更新规则

以下任一项变更后，原镜像不再自动视为有效：

- 根POM、子模块POM或Spring Boot/Maven插件版本变更。
- 新增需要下载的依赖或测试运行器。
- Java 运行版本变更。
- 任何起始/答案标签的构建方式变更。

重建后重新执行断网验收，更新镜像标签、导出文件和SHA-256。
