# 生产 bug 排查计划：GET 路由参数 base64 解码 Illegal base64 character 40

> 状态：待排查（**仅排查，未修改代码**）
> 分支：`registered_bug_dev`
> 日期：2026-10-12

---

## 1. 现象

生产日志出现 `InvocationTargetException`，根因是插件方法内 `Base64.getDecoder().decode` 抛出：

```
java.lang.IllegalArgumentException: Illegal base64 character 40
        at java.util.Base64$Decoder.decode0(Base64.java:714)
        at java.util.Base64$Decoder.decode(Base64.java:526)
        at org.filemapper.main.FileLeader.decodeBase64(FileLeader.java:191)
        at org.base.main.router.FileRouter.getPathByModifier(FileRouter.java:387)
        ... 53 more
```

- 非法字符是 `(`（十进制 40 = 0x28）。`(` **不是 base64 字母表字符**（标准 alphabet 为 `A-Za-z0-9+/`）。
- 说明插件收到的字符串在 URL 解码后被注入了字面 `(`，例如某处 `%28` 被解开，或客户端原始带入 `(`。

异常经由反射被包装：

```
java.lang.reflect.InvocationTargetException
  at com.chbcraft.internals.components.listen.RegisteredRouter.execute(RegisteredRouter.java:106)
  at com.chbcraft.net.handlers.router.GetRequestSorter.handlerRestGet(GetRequestSorter.java:75)
  at com.chbcraft.net.handlers.router.GetRequestSorter.handler(GetRequestSorter.java:52)
```

---

## 2. 框架侧参数解码链路（已确认代码路径）

| 文件 | 行 | 作用 |
|------|----|------|
| `RegisteredRouter.java` | 106 | 反射 `handlerMethod.invoke(routers, params)` 调用插件方法，插件异常被包成 `InvocationTargetException`。**已标注「执行器参数解码错误bug」** |
| `GetRequestSorter.java` | 26 | 取 `request.uri()` 原始字符串，未解码 |
| `GetRequestSorter.java` | 30-35 | 按 `?` 拆 query 与 path，query 交给 `RequestUtil.decodeGetPath` |
| `GetRequestSorter.java` | 43-46 / 136-148 | REST 匹配 `getRestRouter`：按 `/` 切分，把**仍带 `%xx` 的原始段**压入 `pathParamsStack` |
| `GetRequestSorter.java` | 71-74 | **仅当**路由带 `@URLDecode` 才 `URLDecoder.decode`，否则原样传参 |
| `CodeUtil.java` | 18-37 | 框架自己的 `decodeBase64`：`URLDecoder.decode` → `Base64.decode`，异常时用 `replace(" ","+")` 兜底后返回原串 |
| `CodeUtil.java` | 45-56 | 框架自己的 `encodeBase64`：`Base64.encode` → `URLEncoder.encode`（即产出 **URL 编码的 base64**） |
| `RequestUtil.java` | 140-150 | `decodeGetPath`：仅按 `=` 切分，未处理 `+`/`=` 补齐、未 URL 解码 |

### 框架侧最可疑的两点

1. **路径参数默认不解码（opt-in 的 `@URLDecode`）**
   `getRestRouter` 传给插件的是**原始、仍带百分号编码**的段。插件侧 `decodeBase64` 自己先 `URLDecoder.decode` 再 base64。若客户端传入形式与插件期望不一致（例如原始 `(` vs `%28`、`+` vs `%2B`），URL 解码后会引入 base64 非法字符。

2. **base64 + URL 编码的传递方案不一致 / 未归一化**
   - `encodeBase64` 产出的是「URL 编码的 base64」，内含 `%xx`；而标准 base64 含 `+`、`=`、`/`。
   - `/` 在 REST 路径里会被 `getRestRouter` 的 `/` 切分打断 → 参数被截断、错位。
   - `=` 补齐符在 `decodeGetPath` 里被 `split("=")` 误解。
   - query 场景里 `+`（application/x-www-form-urlencoded 语义为空格）与 base64 的 `+` 冲突，框架仅在 `CodeUtil` 里 `replace(" ","+")` 兜底。

> 注：插件 `FileLeader.decodeBase64` 是插件内副本，自己不吞 `IllegalArgumentException`，故一旦传入非法串就直接抛 500。框架的 `CodeUtil.decodeBase64` 能兜底，但**保护不了插件自己的副本**。

---

## 3. 根因假设（待验证，按可能性排序）

- **H1（最可能）**：GET REST 路径参数被框架以「未 URL 解码」的原始形式传给插件，插件收到含 `%28` 或原始 `(`/其它非法字符的串，其内部 `URLDecoder.decode` + `Base64.decode` 失败。
- **H2**：插件期望的 modifier 是「URL 编码 base64」，但客户端/上游用「纯 base64」或「URL-safe base64」发送，`+`↔空格、`=` 补齐、`/` 切分等在某处发生错位，最终产生 `(`。
- **H3**：路径里 `/` 把 base64（含 `/`）切碎，导致后续参数错位、混入非 base64 字符。

**需要抓真实请求串才能定论**——见第 4 节。

---

## 4. 验证 / 执行计划（不修改生产逻辑，只加临时观测）

### 步骤 1 — 记录真实入参（最小侵入）
- 在 `GetRequestSorter.handlerRestGet`（GetRequestSorter.java:64）与 `handler` 入口临时打印：
  - `request.uri()` 原文
  - 拆分后 `pathParamsStack` 内每个段的**字节/字符（含转义显示）**
  - 是否命中 `@URLDecode` 分支
- 或直接在插件方法约见签之处抓原始串（若插件有日志），确认到达插件的字符串长什么样。

### 步骤 2 — 复现
- 用与生产相同路径构造请求，分别测试 modifier 的几种形态：
  - a) URL 编码 base64（`encodeBase64` 产物，含 `%xx`）
  - b) 纯 base64（含 `+` `=` `/` 原文）
  - c) 含原始 `(`/`)` 的畸形串
- 观察哪种能在本地稳定触发 `(0x28)`，锁定 H1/H2/H3。

### 步骤 3 — 定位归一化层
- 确认框架「发送方 encodeBase64」与「框架 decode / 插件 decode」是否一一对应：
  - 生成时做了 `URLEncoder.encode`，接收端是否必然先 `URLDecoder.decode`？
  - `/`、`+`、`=`、空格 在路径 vs query 两种携带方式下的行为是否一致？

### 步骤 4 — 定边界
- 修正点预期落在框架的**统一参数归一化**（`GetRequestSorter` / `CodeUtil` / `RequestUtil`），而非插件：
  - REST 路径段统一 URL 解码后再分发，或
  - base64 按统一规范（如 URL-safe base64，或统一先 URL 解码再 base64）归一化。
- 框架侧需能兜住「非法 base64」，返回可读的 4xx/日志，而不是一条 stderr 堆栈 + 500。

### 步骤 5 — 回归
- 固定复现用例；确认修复后 a/b/c 三种形态行为符合预期、`Illegal base64 character` 不再出现，且不影响既有 `@URLDecode` / 普通 GET 路由。

---

## 5. 涉及文件清单（待排查/可能改动）

- `src/main/java/com/chbcraft/net/handlers/router/GetRequestSorter.java`（参数提取、REST 切分、解码分支）
- `src/main/java/com/chbcraft/internals/components/listen/RegisteredRouter.java:106`（已标注 bug，反射调用）
- `src/main/java/com/chbcraft/net/util/CodeUtil.java`（encode/decodeBase64 归一化）
- `src/main/java/com/chbcraft/net/util/RequestUtil.java`（query 解析 `decodeGetPath`）
- `com.chbcraft.net.handlers.inbound.HttpMessageHandler`（链路入口，确认 500 兜底行为）

---

## 6. 未决问题（需要插件侧信息）

- 插件 `FileRouter.getPathByModifier` 期望的 modifier 确切编码格式？（URL 编码 base64 / 纯 base64 / URL-safe）
- 生产触发请求的**真实 URI**（步骤 1 抓取）。
- `@URLDecode` 标签在那个路由上是否被声明。