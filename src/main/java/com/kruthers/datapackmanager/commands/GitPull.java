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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Logger;

public class GitPull implements CommandExecutor {
    private Logger LOGGER = DatapackManager.LOGGER;
    private Properties properties = DatapackManager.properties;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DatapackManager plugin = JavaPlugin.getPlugin(DatapackManager.class);
        FileConfiguration config = plugin.getConfig();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(config.getString("date_format"));
        FileConfiguration storedData = FileManager.getStoredData();

        if(!command.getName().equalsIgnoreCase("gitpull")){
            return true;
        }
        if (!storedData.getBoolean("github.cloned")){
            sender.sendMessage(ChatColor.RED+"The data pack folder has not yet been settup run \"/datapackmanager setup\" first");
            return true;
        }
        sender.sendMessage(ChatColor.GREEN+"Stating pull into datapacks");
        LOGGER.warning(sender.getName()+", is updating the datapack folder");
        Bukkit.broadcast(ChatColor.YELLOW+"[STAFF] "+ChatColor.GRAY+sender.getName()+" is updated the datapack folder from github","datapackmanager.broadcast");

        File datapackFolder = Utils.getDatapackFolder();
        if (datapackFolder==null){
            sender.sendMessage(ChatColor.RED+"Failed to find a datapacks folder in any world");
            return true;
        }
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository;
        try {
              repository = repositoryBuilder.setGitDir(new File(datapackFolder+"/.git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            LOGGER.info("Datapack local repository found, prepping for pull");
            sender.sendMessage(ChatColor.GREEN+"Successfully found the local repository, "+ChatColor.GOLD+" pulling from upstream now");
        } catch (IOException e){
            e.printStackTrace();
            LOGGER.severe("Exception occurred when locating local git");
            sender.sendMessage(ChatColor.RED+"Exception occurred when locating local git. Updated failed, see the console for more info");
            return false;
        }

        try {
            PullCommand pullCmd = new Git(repository).pull();

            String auth_method=config.getString("github.login_method");
            switch (auth_method){
                case "ssh":
                    //TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback(plugin);
                    //pullCmd.setTransportConfigCallback(transportConfigCallback);
                    //temp code
                    sender.sendMessage(ChatColor.RED+"Due to some issue ssh does not currently work, coming soon. Clone aborted");
                    LOGGER.warning("Due to a few bugs SSH is currently unavailable, it will be added soon, clone aborted");
                    return true;
                    //break;
                case "login":
                    pullCmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(config.getString("authentication.username"),config.getString("authentication.password")));
                    break;
                case "none":
                    break;
                default:
                    LOGGER.warning("Invalid authentication mode in config.yml cannot clone from git");
                    sender.sendMessage(ChatColor.RED+"Failed to authenticate, invalid authentication method in config.yml, aborting clone");
                    return true;
            }

            PullResult result = pullCmd.call();

            LOGGER.info(result.getFetchResult().toString());
            LOGGER.info(result.getMergeResult().toString());
            LOGGER.info("Datapack folder pulled from, "+result.getFetchedFrom());
            sender.sendMessage(ChatColor.GREEN+"Successfully pulled datapacks from, "+result.getFetchedFrom()+ChatColor.GOLD+" Finalizing pull");
        } catch (GitAPIException e){
            e.printStackTrace();
            LOGGER.severe("Exception occurred when pulling from git");
            sender.sendMessage(ChatColor.RED+"Exception occurred when pulling from git. Updated failed, see the console for more info");
            return false;
        }



        storedData.set("github.last_pull.user",sender.getName());
        storedData.set("github.last_pull.time", dateFormat.format(LocalDateTime.now()));
        FileManager.saveStoredData();
        LOGGER.info("Settings set, reloading datapacks");
        sender.sendMessage(ChatColor.GREEN+"Pull finished. "+ChatColor.GOLD+"Reloading datapacks");
        Bukkit.getServer().reloadData();
        LOGGER.info("Pull finished, datapack folder updated!");
        sender.sendMessage(ChatColor.GREEN+"Datapack folder is now up to date");

        return true;
    }
}
