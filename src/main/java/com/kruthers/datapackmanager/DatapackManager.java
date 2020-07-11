package com.kruthers.datapackmanager;

import com.kruthers.datapackmanager.commands.CoreCommand;
import com.kruthers.datapackmanager.commands.CoreCommandTabCompleter;
import com.kruthers.datapackmanager.commands.GitPull;
import com.kruthers.datapackmanager.utils.FileManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public final class DatapackManager extends JavaPlugin {
    public static Logger LOGGER;
    public static Properties properties = new Properties();

    @Override
    public void onEnable() {
        LOGGER=getLogger();
        try {
            properties.load(this.getClassLoader().getResourceAsStream(".properties"));
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.severe("Failed to load plugin properties");
            return;
        }
        LOGGER.info("Logging enabled loading plugin");
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (!FileManager.init()){
            return;
        }
        LOGGER.info("Loaded config");
        getServer().getPluginCommand("datapackmanager").setExecutor(new CoreCommand());
        getServer().getPluginCommand("datapackmanager").setTabCompleter(new CoreCommandTabCompleter());
        getServer().getPluginCommand("gitpull").setExecutor(new GitPull());
        LOGGER.info("Loaded commands");

        this.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"Enabled "+properties.getProperty("full_name"));
    }

    @Override
    public void onDisable() {
        FileManager.saveStoredData();
        this.getServer().getConsoleSender().sendMessage(ChatColor.RED+"Disabled"+properties.getProperty("full_name"));
    }
}
