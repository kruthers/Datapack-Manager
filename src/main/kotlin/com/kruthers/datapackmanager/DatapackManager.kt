package com.kruthers.datapackmanager

import cloud.commandframework.CommandManager
import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.meta.SimpleCommandMeta
import cloud.commandframework.minecraft.extras.AudienceProvider
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.paper.PaperCommandManager
import com.kruthers.datapackmanager.actions.GitAction
import com.kruthers.datapackmanager.commands.CoreCommand
import com.kruthers.datapackmanager.commands.GitCommand
import com.kruthers.datapackmanager.utils.setupRepoChecks
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.function.Function
import kotlin.collections.HashMap


class DatapackManager: JavaPlugin() {
    private var properties: Properties = Properties()

    companion object {
        var setup: Boolean = false
        var confirmation: HashMap<CommandSender, GitAction> = HashMap()
    }

    override fun onEnable() {
        logger.info( "${ChatColor.GREEN}Loading datapack manager by kruthers")
        this.properties.load(this.classLoader.getResourceAsStream(".properties"))
        logger.info("Loading config")
        config.options().copyDefaults(true)
        this.saveConfig()

        logger.info("Config Loaded, Checking datapacks folder for git ")
        setupRepoChecks(this)

        logger.info("Datapacks folder checked, loading commands")
        val cmdManager: CommandManager<CommandSender> = PaperCommandManager<CommandSender>(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )

        val parser: AnnotationParser<CommandSender> = AnnotationParser(
            cmdManager,
            CommandSender::class.java
        ) {
            SimpleCommandMeta.empty()
        }

        MinecraftExceptionHandler<CommandSender>()
            .withDefaultHandlers()
            .withDecorator { component ->
                Component.text()
                    .append(component)
                    .build()
            }
            .apply(cmdManager, AudienceProvider.nativeAudience())

        parser.parse(GitCommand(this))
        parser.parse(CoreCommand(this, this.properties))

        super.onEnable()
    }

    override fun onDisable() {
        logger.info{ ""+ ChatColor.RED+"Disabling datapack manager by kruthers"}
        super.onDisable()
    }
}