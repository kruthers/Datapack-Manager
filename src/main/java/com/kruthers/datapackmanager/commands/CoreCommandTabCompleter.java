package com.kruthers.datapackmanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CoreCommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)){
            return null;
        }

        List<String> list= new ArrayList<>();
        if (args.length==1){
            if ("version".contains(args[0].toLowerCase()) || args[0].length()==0){
                list.add("version");
            }
            if (sender.hasPermission("datapackmanager.setup") && ("setup".contains(args[0].toLowerCase()) || args[0].length()==0)){
                list.add("setup");
            }
            if (sender.hasPermission("datapackmanager.reload") && ("reload".contains(args[0].toLowerCase()) || args[0].length()==0)){
                list.add("reload");
            }
        } else if (args.length==2 && sender.hasPermission("datapackmanager.setup") && args[0].equalsIgnoreCase("setup")) {
            if ("confirm".contains(args[1].toLowerCase())){
                list.add("confirm");
            }
        }

        return list;
    }
}
