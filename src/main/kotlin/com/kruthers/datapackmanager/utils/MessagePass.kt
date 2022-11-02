package com.kruthers.datapackmanager.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

val mm = MiniMessage.miniMessage()

fun parse(msg: String, tags: TagResolver = TagResolver.empty()): Component {
    return mm.deserialize(msg,tags)
}

fun parse(msg: String, sender: CommandSender, tags: TagResolver = TagResolver.empty()): Component {
    val placeholders: TagResolver = TagResolver.resolver(
        tags,
        Placeholder.parsed("user",sender.name),
        Placeholder.parsed("name",sender.name)
    )

    return parse(msg,placeholders)
}

