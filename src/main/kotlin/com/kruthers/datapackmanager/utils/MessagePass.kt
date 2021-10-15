package com.kruthers.datapackmanager.utils

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class MessagePass {


}

fun parse(msg:String, sender: CommandSender): String {
    var string = msg.replace("%user%",sender.name)
    string = string.replace("%name%",sender.name)

    string = parse(string)

    return string;
}

fun parse(msg:String): String {
    var string = ChatColor.translateAlternateColorCodes('&',msg)

    return string;
}