# EternalScript-Cyu

> 适用于 Minecraft Paper / Purpur / Folia 服务端的高性能 Kotlin 运行时脚本插件。

**EternalScript-Cyu** 允许服主与开发者在服务器运行期间动态编写、加载、热重载 Kotlin 脚本代码，无需重启服务器即可实现复杂的自定义玩法、游戏机制、指令拓展及事件监听。

---

## 🌟 相较于原版的修改

- **动态热重载调试信息**：在热重载后增加脚本加载的信息的输出，便于定位问题。
- **类加载白名单**：默认导入限制为 Java 与 Bukkit API，避免第三方类库造成的符号污染与二义性冲突。
- **添加国际化支持**：默认集成简体中文（`zh_CN`），支持用户自定义翻译并在缺失时自动回退。
- **更新指令**：添加 `/es call` 动态函数调用填写参数。

---

## 📋 指令列表

| 指令 | 说明 | 示例与参数规范 |
| :--- | :--- | :--- |
| **`/es help`** | 查看插件指令帮助列表 | 也可直接输入 `/es` 查看帮助菜单，支持聊天框点击补全。 |
| **`/es compile`** | 编译并加载所有脚本 | 异步遍历并编译配置目录（`scripts/`）中的所有 Kotlin 脚本。 |
| **`/es clear`** | 卸载当前所有已加载脚本 | 触发所有脚本的 `disable` 回调并安全清理内存和监听器。 |
| **`/es config`** | 重新加载配置文件 | 重新载入 `config.yml` 与 `lang/*.json` 语言文件。 |
| **`/es call <脚本> <函数> [参数...]`** | 调用脚本中的函数 | 支持传递文本、整数、小数和布尔值参数。<br>`/es call "test.kt" addScore "Steve" 100 true` |
| **`/es load <路径>`** | 加载指定的脚本文件 | 路径相对于服务器 `plugins/` 目录（支持 Tab 补全）。<br>`/es load "EternalScript-Cyu/scripts/hello.kt"` |
| **`/es unload <脚本>`** | 卸载指定的脚本文件 | 参数为脚本名称（支持 Tab 补全）。<br>`/es unload "hello.kt"` |
| **`/es list`** | 列出所有已加载脚本 | 在聊天框或控制台展示当前生效中的脚本列表。 |
| **`/es find <关键词>`** | 搜索类白名单导入缓存 | 快速检索全局白名单中的类名与包名。<br>`/es find Player` |

---

## 🚀 快速上手

### 1. 安装方法

1. 将 `EternalScript-Cyu-1.0.0.jar` 放入服务端的 `plugins` 文件夹。
2. 启动服务器，插件将自动生成配置目录：
   - `plugins/EternalScript-Cyu/config.yml`（主配置）
   - `plugins/EternalScript-Cyu/lang/zh_CN.json`（语言文件）
   - `plugins/EternalScript-Cyu/scripts/`（脚本目录）
   - `plugins/EternalScript-Cyu/utils/`（通用工具库脚本）

### 2. 编写你的第一个脚本

在 `plugins/EternalScript-Cyu/scripts/` 下创建 `hello.kt`：

```kotlin
// plugins/EternalScript-Cyu/scripts/hello.kt

import org.bukkit.Bukkit

enable {
    Bukkit.broadcastMessage("§a[EternalScript] 脚本已成功加载！")
}

disable {
    Bukkit.broadcastMessage("§c[EternalScript] 脚本已安全卸载！")
}
```

在游戏内或控制台输入：
```bash
/es load "EternalScript-Cyu/scripts/hello.kt"
```
即可立即看到启用广播！

---

## 🛠️ 脚本语法指南

### 1. 脚本生命周期

通过 `enable` 与 `disable` 块定义脚本装载和卸载时的逻辑：

```kotlin
enable {
    // 脚本被加载/启用时执行（注册任务、初始化数据等）
    println("初始化完成")
}

disable {
    // 脚本被卸载/清理时执行（注销数据、释放资源等）
    println("清理资源完成")
}
```

### 2. 事件监听

无需繁琐的 Listener 类，一行代码直接注册事件监听：

```kotlin
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

// 普通监听
register<PlayerJoinEvent> { event ->
    event.player.sendMessage("§e欢迎回到服务器，${event.player.name}！")
}

// 指定优先级监听
register<PlayerQuitEvent>(priority = EventPriority.HIGH) { event ->
    println("玩家 ${event.player.name} 退出了服务器")
}
```

### 3. 指令注册

在脚本中动态注册自定义游戏命令：

```kotlin
command("mycmd") {
    permission = "myscript.use"
    executor = { sender, label, args ->
        sender.sendMessage("§a你执行了脚本命令！参数: ${args.joinToString()}")
    }
}
```

### 4. 函数定义与 `/es call` 外部调用

脚本中声明的顶层函数可被 `/es call` 或其他脚本直接调用：

```kotlin
// 定义供调用的函数
fun giveReward(playerName: String, amount: Int, notify: Boolean): String {
    val player = Bukkit.getPlayerExact(playerName) ?: return "玩家不在线"
    if (notify) {
        player.sendMessage("§a恭喜你获得 $amount 金币奖励！")
    }
    return "成功向 $playerName 发放 $amount 奖励"
}
```

控制台或管理员执行：
```bash
/es call "reward.kt" giveReward "Steve" 500 true
```

---

## 📁 脚本加载过滤规则

EternalScript-Cyu 支持通过前缀定义脚本的载入行为：

- **`*.kt`**：默认异步加载匹配的 Kotlin 脚本。
- **`-*.kt` 或 `-<目录>/`**：**忽略**带有 `-` 前缀的脚本或文件夹（例如 `-example/`），不会被自动编译。
- **`!*.kt`**：**同步优先加载**，带 `!` 前缀的脚本会在服务器启动阶段优先同步编译加载（适用于被其他脚本依赖的基础前置脚本）。

---

## ⚙️ 配置文件说明 (`config.yml`)

```yaml
class-loader: EternalScript-Cyu  # 类加载器标识
debug: true                     # 开启调试日志输出
lang: zh_CN                     # 默认语言 (对应 lang/ 目录下的 json 文件)
libs:
  - EternalScript-Cyu/libs      # 外部依赖 Jar 扫描路径
scripts:
  - EternalScript-Cyu/scripts   # 主脚本目录
utils:
  - EternalScript-Cyu/utils     # 工具脚本目录
```

---

## 🌐 国际化支持 (`lang/*.json`)

插件的所有提示信息与命令帮助均存放在 `plugins/EternalScript-Cyu/lang/` 目录下。
- 默认提供 `zh_CN.json` 与 `en_US.json`；
- 支持 Paper 的 MiniMessage 颜色格式标签（如 `<gold>`, `<yellow>`, `<click:...>` 等）；
- 如需新增语言，只需复制一份新建 `<语言代码>.json`，并将 `config.yml` 中的 `lang` 改为该名称即可。

---

## 🔨 构建与编译

本项目基于 Gradle 构建，建议使用 **JDK 21** 及以上环境：

```bash
# 编译并构建插件 Jar 包
./gradlew build -x test
```
构建产物输出位置：`build/libs/EternalScript-Cyu-1.0.0.jar`。
