<div align="center">

# 🚀 BaseEventLoopServer

**一个基于 Netty 的 NIO 高性能 HTTP / WebSocket 服务器框架**

内置插件化模块系统 · 注解路由 · 事件驱动 · 文件无缝传输

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Netty](https://img.shields.io/badge/Netty-4.1.9-blue.svg)](https://netty.io/)
[![Version](https://img.shields.io/badge/version-1.0--SNAPSHOT-green.svg)](#)
[![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)](#)

</div>

---

## ✨ 项目简介

**BaseEventLoopServer** 是一款构建在 **Netty NIO** 之上的轻量级 HTTP / WebSocket 服务器。它不像 Spring Boot 那样"全家桶"式的重量级，而是采用**事件循环 + 插件化**的极简架构 —— 核心只负责高性能网络收发，业务能力全部通过 **热插拔插件** 来赋予。

你可以把它理解为一个 **Java 版的 Bukkit / Spigot**：服务器内核保持稳定，所有功能都以插件（Jar）的形式动态加载、独立隔离开启或卸载，互不干扰。

> 🎯 核心设计目标：**内核只管网络，业务交给插件，动静相离，随插随用。**

---

## 🧩 核心特性

| 分类 | 特性 |
|------|------|
| ⚡ **高性能网络** | 基于 Netty 的 Reactor 事件循环模型，NIO 非阻塞 IO，支持高并发连接 |
| 📦 **插件化框架** | 插件以 Jar 形式动态加载，**独立 ClassLoader 隔离**，支持热插拔、依赖管理、安全沙箱 |
| 🛣️ **注解式路由** | `@Get` / `@Post` / `@Delete` 零配置声明 HTTP 接口，自动解析 `@PathParams` / `@MapParam` 参数 |
| 📡 **双协议支持** | HTTP 与 **WebSocket** 无缝切换，一条管道自适应协议升级 |
| 🎉 **事件驱动系统** | 请求入站 / 响应出站 / 插件启停 / WebSocket 生命周期的全链路事件订阅 |
| 📁 **文件传输引擎** | WebSocket **断点续传** 文件上传、Chunked 流式下载，全流程事件可监听 |
| 🖼️ **静态资源服务** | 自动识别 Mime 类型、秒级图片压缩、结果缓存，开箱即得静态资源托管 |
| 🧩 **丰富配置** | 支持 YAML / Properties 多格式配置，全局与插件级配置隔离 |
| ⏱️ **内置调度** | `FloatSchedule` 定时 / 周期任务调度，统一管控服务器线程数量 |
| 🧬 **插件安全沙箱** | `PluginSecurityManager` 限制插件越权访问，可配置跨插件类加载策略 |

---

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────┐
│                     Application                      │
│   ┌──────────────┐   ┌──────────────────┐           │
│   │ CommandLiner  │   │   FloatSphere    │           │
│   │ (命令行入口)  │   │   (全局枢纽)     │           │
│   └──────────────┘   └───────┬──────────┘           │
│                              │                       │
│   ┌─────────────────────────▼─────────────────────┐ │
│   │              FloatPluginManager                │ │
│   │   插件加载 / 事件分发 / 路由注册 / 生命周期      │ │
│   └─────────────────────────┬─────────────────────┘ │
│                              │                       │
│   ┌─────────────────────────▼─────────────────────┐ │
│   │               HttpProcessor                    │ │
│   │         NioHttpAcceptor (Netty Server)         │ │
│   │   ┌──────────────────────────────────────┐    │ │
│   │   │  Pipeline: 编解码→协议切换→路由→响应   │    │ │
│   │   └──────────────────────────────────────┘    │ │
│   └───────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

- **FloatSphere**：全局单例枢纽，持有配置、根路径、插件加载器等核心对象
- **PluginManager**：插件从 Jar 中被独立类加载器加载，支持依赖与隔离
- **EventLoop**：定时任务与事件分发走统一调度的单线程事件循环，线程数可控

---

## 🚀 快速开始

### 1️⃣ 克隆并构建

```bash
git clone <your-repo-url>
cd NIO-Server
mvn clean package
```

构建产物为可独立运行的 shaded fat jar。

### 2️⃣ 配置服务器 `src/main/resources/config/config.properties`

```properties
# 服务器监听端口
server-port=25565
# WebSocket 接入地址
ws-url=/ws
# 连接超时（秒）
timeout=20
# 日志等级
log-level=info
```

### 3️⃣ 启动

```bash
java -jar target/BaseEventLoopServer-1.0-SNAPSHOT.jar
```

启动即自动加载 `plugins/` 目录下的所有插件，并开启 HTTP 监听与命令行交互。

---

## 🧩 编写你的第一个插件

插件的开发和普通服务端完全解耦。只需继承 `CustomPlugin`，重写生命周期方法：

```java
public class HelloPlugin extends CustomPlugin {

    @Override
    public void onEnable() {
        getLogger().log("Hello from {} !", getName());
    }

    // 注解路由：GET 请求 /hello
    @Get("/hello")
    public String hello(@MapParam("name") String name) {
        return "Hello, " + name + "!";
    }

    @Override
    public void onDisable() {
        getLogger().log("{} closed.", getName());
    }
}
```

打包为 Jar 放入服务器 `plugins/` 目录，即可被自动发现并加载运行。

---

## 📖 使用示例

### 参数注入

```java
// 路径参数：/user/123  →  id = "123"
@Get("/user/{id}")
public User getUser(@PathParams("id") String id) { ... }

// 查询参数：/search?query=java  →  query = "java"
@Get("/search")
public List<Result> search(@MapParam("query") String query) { ... }

// POJO 自动绑定 JSON 请求体
@Post("/api/user")
public User createUser(@PojoRequest User user) { ... }
```

### 事件订阅

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onRequest(RequestInboundEvent event) {
    getLogger().log("新的请求到来: {}", event.getRequest().uri());
}
```

### 静态资源

```java
@Resource(compress = true)
public void staticResource() { /* 自动托管静态文件并压缩图片 */ }
```

---

## 🤝 工作原理一览

- **插件隔离**：每个插件拥有独立的 `PluginClassLoader`，插件间类互不可见（除非开启跨域），互不污染。
- **依赖管理**：`libs/` 目录作为公共依赖库，由 `LibrariesClassLoader` 统一加载供插件共享。
- **协议自适应**：`SwitchProtocolAdaptor` 根据首次请求自动区分 HTTP 与 WebSocket，将透传进对应处理器。
- **文件断点续传**：WebSocket 上传记录 `FileOffSet` 偏移量，支持中断恢复（含事件 `FileUploadCancelEvent` / `FileUploadInterruptEvent` 等）。
- **流式下载**：基于 `DefaultFileRegion` 零拷贝 chunked 输出，文件多大都不怕内存爆掉。

---

## 🗂️ 目录结构

```
NIO-Server/
├── src/main/java/
│   ├── com.chbcraft.main/          # 启动入口 Application / Main
│   ├── com.chbcraft.net/           # Netty 管道、协议切换、路由分发
│   ├── com.chbcraft.internals/     # 插件框架 / 事件 / 配置 / 调度
│   └── com.chbcraft.plugin/        # 插件 SPI 接口
└── src/main/resources/
    ├── config/config.properties    # 服务器配置
    ├── plugins/                    # 插件放置目录（已内置示例插件）
    └── libs/                       # 公共依赖库
```

---

## 🧰 内置示例插件

仓库自带 3 个可参考的示例插件：

- **NormalRouterPlugin** —— 路由编写范例
- **FileGarbageCollector** —— 文件资源回收
- **Updater** —— 版本更新处理

---

## 📌 版本路线

- [x] Netty NIO 服务器内核
- [x] 插件化框架与类加载隔离
- [x] HTTP / WebSocket 双协议切换
- [x] 文件上传 / 下载与断点续传
- [x] 注解路由与事件系统
- [ ] 更完善的日志可视化与监控面板（规划中）

---

## 📄 License

[MIT](./LICENSE)

---

<div align="center">
  Made with ❤️ by <b>Headphone_Sang</b>
</div>