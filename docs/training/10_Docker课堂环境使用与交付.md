# Docker课堂环境使用与交付

> 用途：构建、验证、分发和使用 `linux/amd64` 课堂镜像。  
> 状态：课程环境说明，不是生产部署手册。  
> 限制：不得把镜像内测试结果当作当前分支结果；依赖、POM、JDK、起始代码或构建方式变化后必须重建并重新做断网验证。

> 目标：使用一个预热的课堂镜像提供 Java 17、Maven 3.9.9、Node 22 和项目依赖，学员无需在主机安装 Java、Maven 或 Node。

## 1. 设计结论

- 镜像名：`training-wms-classroom:0.7`。
- 运行工具链：Eclipse Temurin JDK 17 + Maven 3.9.9 + Node 22。
- 基础镜像默认从AWS Public ECR的Docker官方镜像源获取，避免依赖单一Docker Hub通道；内容仍为官方Maven/Temurin镜像。
- 源码编译级别：保持项目 `pom.xml` 现有 Java 8 源码/目标兼容，不因镜像修改业务行为。
- 镜像内置来自`s2-t01-start`的课程初始代码，不包含Git历史、后续任务答案或客户材料。
- 学员当前T01—T06工作目录以只有容器运行时可见的bind mount挂载到`/workspace`。
- Compose 长期运行后端和前端开发服务；源码修改由热更新流程自动应用。

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

### 3.1 一键启动

```bash
./scripts/classroom-up.sh
```

脚本自动处理当前用户、镜像检查、Compose 启动和健康等待。重复执行不重建容器。启动后访问 `http://localhost:8080`。

### 3.2 宿主持久化运行空间

Compose 将容器的 `HOME`、`MAVEN_CONFIG`、`TMPDIR` 和 Java 临时目录统一指向项目根目录的 `.classroom-runtime/`。该目录位于学员电脑的项目工作区中，经 `/workspace` 挂载后供容器使用，因此容器退出或重建后仍然保留，也不会继续占用容器可写层的 `/tmp`。

- `.classroom-runtime/` 已加入 `.gitignore`，不得提交到仓库。
- Maven 依赖仍读取镜像内的 `/opt/training-m2/repository`；宿主目录只保存运行期配置、日志和临时文件，不重复复制整套离线依赖。
- 每个学员工作目录拥有独立运行空间，不应在多个小组之间共享该目录。
- 删除 `.classroom-runtime/` 只会清理本机课堂缓存，不会删除源码；执行前应停止对应课堂容器。
- 该设置避免课堂进程持续写满 Docker 容器可写层，但 Docker Engine 已经没有可用空间时，仍需先清理无用镜像、构建缓存或扩大 Docker Desktop 磁盘容量。

### 3.3 开发、测试和停止

```bash
./scripts/classroom-status.sh
./scripts/classroom-test.sh T02 baseline
./scripts/classroom-verify.sh T02
./scripts/classroom-down.sh
```

任务卡会给出当前任务的测试范围。前端修改由 Vite 热更新；后端 Java 和配置修改会触发离线编译和 Spring Boot 自动重启。只有 POM、依赖锁文件、Dockerfile、Compose 或环境变量变化时才需要重建镜像或重启环境。

学员不需要直接执行 Docker、Maven 或 Node 命令。如果对实现感兴趣，可阅读 `scripts/classroom-*.sh`、`compose.classroom.yml` 和 `docker/classroom/`。

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
- [ ] 仅在讲师恢复环境挂载 `s3-t06-answer` 时，`mvn -o clean verify` 通过16条业务测试 + 3条平台契约测试；学员镜像和工作目录不可见该答案 ref。
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

## T01—T03 测试与 QA 交接口径

开发仅提交当前任务的 TDD 与必要回归（`02_verification.md`）；代码评审记录于 `03_review.md`。测试团队的工程师独立执行功能、页面/API、异常和回归验证，结果写入 `functional-test.md`；另一团队的 QA 审核规范、需求追溯、覆盖、证据和缺陷闭环，结论写入 `qa-review.md`。业务所有者另行验收，交付负责人在 `04_decision.md` 记录最终裁决。不能用开发全绿替代独立测试，不能用 QA 审核替代业务验收。

各阶段分别记录对象版本、具名负责人及团队、待确认事项、状态、结论时间与证据。测试工程师与 QA 不得由同一人员兼任；缺少负责人由讲师/交付负责人指定，未执行保持待执行，未确认保持待确认，不预填 PASS。修复后先更新开发证据，再由测试工程师重测，QA 审核新证据，必要时重新业务验收。

T01 仍按完整手写 Prompt 操作；职责分离不意味着引入预置 Skill。T02/T03 保留各自命令入口。操作与模板以对应学员卡及 `docs/ai-governance/roles-and-approvals.md` 为准。
