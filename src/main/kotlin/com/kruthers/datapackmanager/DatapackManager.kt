package com.kruthers.datapackmanager

import com.kruthers.datapackmanager.actions.GitAction
import com.kruthers.datapackmanager.commands.CoreCommand
import com.kruthers.datapackmanager.commands.Git
import com.kruthers.datapackmanager.commands.tabcompleters.CoreCommandTabber
import com.kruthers.datapackmanager.commands.tabcompleters.GitCommandTabber
import com.kruthers.datapackmanager.events.BookEvents
import com.kruthers.datapackmanager.utils.setupRepoChecks
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.collections.HashMap

class DatapackManager : JavaPlugin() {


    init {

    }

    companion object {
        public var properties: Properties = Properties();
        public var setup: Boolean = false;
        public var confirmation: HashMap<CommandSender,GitAction> = HashMap()
    }

    override fun onEnable() {
        logger.info{ ""+ChatColor.GREEN+"Loading datapack manager by kruthers"}
        properties.load(this.classLoader.getResourceAsStream(".properties"))
        logger.info("Loading config")
        config.options().copyDefaults(true)
        saveConfig();

        logger.info("Config Loaded, Checking datapacks folder for git ")
        setupRepoChecks(this)

        logger.info("Datapacks folder checked, loading commands")
        server.getPluginCommand("datapackmanager")!!.setExecutor(CoreCommand(this))
        server.getPluginCommand("datapackmanager")!!.tabCompleter = CoreCommandTabber()
        server.getPluginCommand("git")!!.setExecutor(Git(this))
        server.getPluginCommand("git")!!.tabCompleter = GitCommandTabber()

        logger.info("Loaded commands, loading Events")
        server.pluginManager.registerEvents(BookEvents(this),this)

        super.onEnable()
    }

    override fun onDisable() {
        logger.info{ ""+ChatColor.RED+"Disabling datapack manager by kruthers"}
        super.onDisable()
    }




}
