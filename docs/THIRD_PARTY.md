# Third-party Source Register

> 用途：记录第三方来源、固定版本、许可证和本地使用方式。  
> 限制：未登记来源和许可证的复制文件不得合并；参考思想不等于允许复制源码。

| 来源 | 许可证 | 使用方式 | 是否复制代码 |
|---|---|---|---|
| [YunaiV/ruoyi-vue-pro](https://github.com/YunaiV/ruoyi-vue-pro) | MIT | 参考多模块、库存测试和公共能力分层思想 | 否 |
| [yiruantong2014/open-wms](https://github.com/yiruantong2014/open-wms) | Apache-2.0（以根 LICENSE 为准） | 参考提交 `de4ede14b1d03d393069e415a55c762b85f823a2` 中 `images/so-images/581c3f484d98369f54ad40b966b061b.png` 与 `images/inv-images/1de21a46538423985a8ce438e2fc9d7.png` 的页面结构、仓储术语和信息层级 | 否；未复制源码或图片资源 |
| [southliu/south-admin-react](https://github.com/southliu/south-admin-react) | MIT | 基于提交 `9810d29697f914d91de24f58a5be4d41c4e0a789` 的 React/Vite/Ant Design 依赖组合、后台壳层和组件模式进行课堂 WMS 最小化适配；裁剪鉴权、动态菜单、国际化和通用系统模块；Vite 升至安全补丁 `7.3.6`，esbuild 固定 `0.28.1` | 是，适配源码位于 `training-server/frontend/`；许可证 SHA-256 `5d7d9663174c6e6a1b436725b66ac68283d18795953dc4332974fcbb903c9e41`；锁文件 SHA-256 `854d448e44b3f65af6462f50c46dd0b613abf869061b70fc1212305acb9d250f` |
| [ertugrul-dmr/clean-code-skills](https://github.com/ertugrul-dmr/clean-code-skills) | MIT | 复制五个专项 Skill 到 `.agents/skills/`；固定提交 `1b6b3cc1264b8fbe921c65002d05a3bf90ede178` | 是；见 `skills-lock.json` 与 `docs/licenses/clean-code-skills-LICENSE` |
| [mattpocock/skills](https://github.com/mattpocock/skills) | MIT | 复制 AI Hero 工程 Skills 到 `.agents/skills/`；本轮新增 `codebase-design`，固定提交 `6654f6b60cd9d5be8b54c6fafe44346dabeb3b76`，不做本地修改 | 是；来源与哈希见 `skills-lock.json`，许可证见 `docs/licenses/mattpocock-skills-LICENSE` |

后续若复制具体文件，必须补充文件路径、提交号、版权声明和修改说明。

更新第三方 Skill 或代码时必须重新核对许可证、固定提交/版本、更新 `skills-lock.json` 和本地 License，并运行来源哈希校验。不得静默修改下载文件后仍声称与上游一致。
