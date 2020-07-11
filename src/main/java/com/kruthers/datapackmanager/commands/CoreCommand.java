package com.kruthers.datapackmanager.commands;

import com.kruthers.datapackmanager.DatapackManager;
import com.kruthers.datapackmanager.utils.FileManager;
import com.kruthers.datapackmanager.utils.SshTransportConfigCallback;
import com.kruthers.datapackmanager.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import com.jcraft.jsch.jce.Random;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Logger;

public class CoreCommand implements CommandExecutor {
    private Logger LOGGER = DatapackManager.LOGGER;
    private Properties properties = DatapackManager.properties;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DatapackManager plugin = JavaPlugin.getPlugin(DatapackManager.class);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(plugin.getConfig().getString("date_format"));
        FileConfiguration config = plugin.getConfig();
        FileConfiguration storedData = FileManager.getStoredData();

        if (args.length==0){
            sender.sendMessage(ChatColor.AQUA+"You are running "+properties.getProperty("full_name"));
            return true;
        } else if (args.length==1){
            String argument = args[0];
            if (argument.equalsIgnoreCase("version")){
                sender.sendMessage(ChatColor.AQUA+"You are running "+properties.getProperty("full_name"));
                return true;
            } else if (argument.equalsIgnoreCase("setup")) {
                if (sender.hasPermission("datapackmanager.setup")){
                    sender.sendMessage(ChatColor.RED+"Warning this will clear the datapack folder!");
                    sender.sendMessage(ChatColor.RED+"If you are sure you want todo this run /datapackmanager setup confirm");
                }else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',config.getString("invalid_permission")));
                }
                return true;
            } else if (argument.equalsIgnoreCase("reload")){
                if (sender.hasPermission("datapackmanager.reload")){
                    LOGGER.info(sender.getName()+" is reloading config");
                    plugin.reloadConfig();
                    LOGGER.info("Reload config");
                    sender.sendMessage(ChatColor.GREEN+"Config Reload");
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',config.getString("invalid_permission")));
                }
                return true;
            } else {
                if (sender.hasPermission("datapackmanager.setup")&& sender.hasPermission("datapackmanager.reload")){
                    sender.sendMessage(ChatColor.RED+"Invalid Argument, usage: /datapackmanager <version|setup|reload>");
                } else if (sender.hasPermission("datapackmanager.setup")){
                    sender.sendMessage(ChatColor.RED+"Invalid Argument, usage: /datapackmanager <version|setup>");
                } else if (sender.hasPermission("datapackmanager.reload")){
                    sender.sendMessage(ChatColor.RED+"Invalid Argument, usage: /datapackmanager <version|reload>");
                } else {
                    sender.sendMessage(ChatColor.RED+"Invalid Argument, usage: /datapackmanager <version>");
                }
                return true;
            }
        } else if (args.length==2){
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("confirm")){
                //check they have permission
                if (!sender.hasPermission("datapackmanager.setup")){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',config.getString("invalid_permission")));
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN+"Starting Datapack setup...");
                LOGGER.warning(sender.getName()+", is initializing the datapack folder");
                //get datapack folder
                File datapackFolder = Utils.getDatapackFolder();
                if (datapackFolder==null){
                    sender.sendMessage(ChatColor.RED+"Failed to find a datapacks folder in any world");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD+"Clearing datapack folder...");
                //empty datapack folder
                for (File folder : datapackFolder.listFiles()){
                    Utils.deleateFolder(folder);
                    LOGGER.info("Deleated datapack "+folder.getPath());
                }
                sender.sendMessage(ChatColor.GREEN+"Datapack folder cleared. "+ChatColor.GOLD+"Cloning from github..");
                LOGGER.info("Cleared datapack folder, cloning from github");
                //setup git
                try {
                    CloneCommand cloneCommand = new CloneCommand()
                            .setDirectory(datapackFolder)
                            .setURI(config.getString("github.url"));

                    String auth_method=config.getString("github.login_method");
                    switch (auth_method){
                        case "ssh":
                            //TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback(plugin);
                            //cloneCommand.setTransportConfigCallback(transportConfigCallback);
                            //temp code
                            sender.sendMessage(ChatColor.RED+"Due to some issue ssh does not currently work, coming soon. Clone aborted");
                            LOGGER.warning("Due to a few bugs SSH is currently unavailable, it will be added soon, clone aborted");
                            return true;
                            //break;
                        case "login":
                            cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(config.getString("authentication.username"),config.getString("authentication.password")));
                            break;
                        case "none":
                            break;
                        default:
                            LOGGER.warning("Invalid authentication mode in config.yml cannot clone from git");
                            sender.sendMessage(ChatColor.RED+"Failed to authenticate, invalid authentication method in config.yml, aborting clone");
                            return true;
                    }

                    cloneCommand.call();

                    LOGGER.info("Datapack folder cloned from, "+config.getString("github.url"));
                    sender.sendMessage(ChatColor.GREEN+"Successfully cloned datapacks from, "+config.getString("github.url")+ChatColor.GOLD+" Finalizing setup");
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.severe("Exception occurred when cloning from git. Clone Failed");
                    sender.sendMessage(ChatColor.RED+"Exception occurred when cloning from git. Setup failed, see the console for more info");
                    return false;
                }

                storedData.set("github.cloned",true);
                storedData.set("github.last_pull.user",sender.getName());
                storedData.set("github.last_pull.time", dateFormat.format(LocalDateTime.now()));
                FileManager.saveStoredData();
                LOGGER.info("Settings set, reloading datapacks");
                sender.sendMessage(ChatColor.GREEN+"Setup finished. "+ChatColor.GOLD+"Reloading datapacks");
                Bukkit.getServer().reloadData();
                LOGGER.info("Setup finished, datapack folder initialized from "+config.getString("github.url"));
                sender.sendMessage(ChatColor.GREEN+"Datapack folder is now setup you can now run /gitpull to updated it from the git.");


            } else {
                if (sender.hasPermission("datapackmanager.setup")&& sender.hasPermission("datapackmanager.reload")){
                    sender.sendMessage(ChatColor.RED+"Invalid Argument, usage: /datapackmanager <version|setup|reload> <confirm>");
                } else if (sender.hasPermission("datapackmanager.setup")){
                    sender.sendMessage(ChatColor.RED+"Invalid Argument, usage: /datapackmanager <version|setup> <confirm>");
                } else if (sender.hasPermission("datapackmanager.reload")){
                    sender.sendMessage(ChatColor.RED+"Invalid Argument, usage: /datapackmanager <version|reload>");
                } else {
                    sender.sendMessage(ChatColor.RED+"Invalid Argument, usage: /datapackmanager <version>");
                }
                return true;
            }
        } else {
            if (sender.hasPermission("datapackmanager.setup")&& sender.hasPermission("datapackmanager.reload")){
                sender.sendMessage(ChatColor.RED+"Invalid Arguments, usage: /datapackmanager <version|setup|reload>");
            } else if (sender.hasPermission("datapackmanager.setup")){
                sender.sendMessage(ChatColor.RED+"Invalid Arguments, usage: /datapackmanager <version|setup>");
            } else if (sender.hasPermission("datapackmanager.reload")){
                sender.sendMessage(ChatColor.RED+"Invalid Arguments, usage: /datapackmanager <version|reload>");
            } else {
                sender.sendMessage(ChatColor.RED+"Invalid Arguments, usage: /datapackmanager <version>");
            }
            return true;
        }


        return true;
    }
}
