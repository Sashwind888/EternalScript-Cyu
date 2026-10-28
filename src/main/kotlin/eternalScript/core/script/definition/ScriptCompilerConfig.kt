package eternalScript.core.script.definition

import eternalScript.core.data.Config
import eternalScript.core.data.Resource
import eternalScript.core.extension.searchAllSequence
import eternalScript.core.manager.ConfigManager
import eternalScript.core.script.Script
import eternalScript.core.the.Root
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.classpathFromClassloader

fun pluginClasspath(): List<File> = Root.plugins().flatMap { plugin ->
    classpathFromClassloader(plugin.javaClass.classLoader) ?: emptyList()
}

fun libraryClasspath(): List<File> = ConfigManager.value<List<String>>(Config.LIBS).flatMap { lib ->
    Resource.PLUGINS.child(lib).searchAllSequence({ it.extension == "jar" })
}

private val classpath: List<File> = buildSet {
    addAll(pluginClasspath())
    addAll(libraryClasspath())
}.toList()

// 核心默认导入白名单：仅包含 Java 核心、Bukkit/Paper 基础 API 及 EternalScript DSL
// 绝不将第三方插件或阴影打包类库（如 vavr、scala、datafixers 等）自动注入全局命名空间
val WHITELISTED_DEFAULT_IMPORTS: List<String> = listOf(
    // Java 核心
    "java.io.File",
    "java.util.UUID",
    "java.util.List",
    "java.util.Map",
    "java.util.Set",
    "java.util.ArrayList",
    "java.util.HashMap",
    "java.util.HashSet",
    "java.util.concurrent.*",

    // Bukkit / Paper 核心
    "org.bukkit.Bukkit",
    "org.bukkit.Location",
    "org.bukkit.Material",
    "org.bukkit.Particle",
    "org.bukkit.Sound",
    "org.bukkit.World",
    "org.bukkit.entity.Player",
    "org.bukkit.entity.Entity",
    "org.bukkit.event.Event",
    "org.bukkit.event.EventPriority",
    "org.bukkit.event.Listener",
    "org.bukkit.event.block.*",
    "org.bukkit.event.entity.*",
    "org.bukkit.event.inventory.*",
    "org.bukkit.event.player.*",
    "org.bukkit.inventory.Inventory",
    "org.bukkit.inventory.InventoryHolder",
    "org.bukkit.inventory.ItemStack",
    "org.bukkit.inventory.meta.ItemMeta",
    "org.bukkit.plugin.Plugin",
    "org.bukkit.scheduler.BukkitRunnable",
    "org.bukkit.scheduler.BukkitTask",
    "org.bukkit.command.CommandSender",

    // Adventure API
    "net.kyori.adventure.text.Component",

    // EternalScript DSL
    "eternalScript.core.script.*",
    "eternalScript.core.script.command.*",
    "eternalScript.core.script.data.*"
)

var debugImportsCache: Set<String> = WHITELISTED_DEFAULT_IMPORTS.toSet()

fun ScriptCompilationConfiguration.Builder.applyWhitelistedImports() {
    defaultImports.append(WHITELISTED_DEFAULT_IMPORTS)
}

object ScriptCompilerConfig : ScriptCompilationConfiguration({
    baseClass(Script::class)

    isStandalone(false)

    // 应用干净的白名单默认导入
    applyWhitelistedImports()

    jvm {
        // Classpath 依然完整包含服务端各插件类路径，允许脚本显式 import 第三方插件
        updateClasspath(classpath)
        compilerOptions.append("-jvm-target=21")
    }

    refineConfiguration {
        onAnnotations<Import>(ScriptImportHandler)
    }
})
