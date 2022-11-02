package com.kruthers.datapackmanager.commands

import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.utils.parse
import com.kruthers.datapackmanager.utils.setupRepoChecks
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import java.util.*

class CoreCommand(private val plugin: DatapackManager, private val properties: Properties) {

    private val versionString: String = "You are running Datapack Manager version <version>"

    private val tags: TagResolver = TagResolver.resolver(
        Placeholder.parsed("version", this.properties.getProperty("version"))
    )

    @CommandMethod("datapackmanager")
    @CommandDescription("Core plugin command")
    @CommandPermission("datapackmanager.core")
    fun coreCommand(sender: CommandSender) {
        sender.sendMessage(parse(this.versionString,tags))
    }

    @CommandMethod("datapackmanager version")
    @CommandDescription("Get the plugin version")
    @CommandPermission("datapackmanager.core")
    fun versionCommand(sender: CommandSender) {
        sender.sendMessage(parse(this.versionString,tags))
    }

    @CommandMethod("datapackmanager reload")
    @CommandDescription("Reload the plugins config")
    @CommandPermission("datapackmanager.reload")
    fun reloadCommand(sender: CommandSender) {
        plugin.logger.info(sender.name+" Is reloading the plugin")
        sender.sendMessage(parse("<yellow>Reloading Datapack Manager"))
        plugin.logger.info("Reloading config...")
        plugin.reloadConfig()

        //reset values
        DatapackManager.setup = false
        DatapackManager.confirmation = HashMap()

        //perform github checks again
        setupRepoChecks(plugin)

        sender.sendMessage(parse("<green>Reloaded Datapack Manager"))
        plugin.logger.info("Reloaded successfully")
    }

}