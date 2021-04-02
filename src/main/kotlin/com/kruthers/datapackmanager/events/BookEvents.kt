package com.kruthers.datapackmanager.events

import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.actions.Clone
import com.kruthers.datapackmanager.actions.GitAction
import com.kruthers.datapackmanager.utils.saveData
import net.minecraft.server.v1_16_R3.EnumHand
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenBook
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.util.*
import kotlin.collections.HashMap

class BookEvents(val pl: DatapackManager): Listener {

    companion object {
        val awaitingAuth: HashMap<UUID, GitAction> = HashMap();
//        val returnItem: HashMap<UUID, ItemStack?> = HashMap();

        private val emailRegex: Regex = Regex("^[\\w._%+-@]+")
        private val passRegex: Regex = Regex("[\\w~`! @#\$%^&*()_+={}:;\"'<,>.?\\|\\/\\-\\[\\]]{8,}")
    }

    @EventHandler
    fun onBookEdit(event: PlayerEditBookEvent) {
        val player: Player = event.player;
        //check if the player needs to close a book
        if (awaitingAuth.contains(player.uniqueId)) {
            val action = awaitingAuth[player.uniqueId]
            awaitingAuth.remove(player.uniqueId)

            if (action == null) return;

            val pages = event.newBookMeta.pages
            if (pages.size != 3) {
                player.sendMessage("${ChatColor.RED}Invalid format, please put the email on page 2 and password on page 3")
            } else {
                val email = pages[1].replace("\n", "")
                val password = pages[2].replace("\n", "")

                //validate email
                if (!emailRegex.matches(email)) {
                    player.sendMessage("${ChatColor.RED}Invalid email provided, please ensure it is just the email on that page...")
                }

                if (!passRegex.matches(password)) {
                    player.sendMessage("${ChatColor.RED}Invalid password provided, please ensure it is just the password on that page...")
                }

                val auth: UsernamePasswordCredentialsProvider = UsernamePasswordCredentialsProvider(email,password)

                if (action is Clone) {
                    saveData(action.repo,action.branch,email,password,true,pl)
                }

                player.sendMessage("${ChatColor.GREEN}Authentication Details entered, starting action...")
                action.triggerWithAuth(auth)


            }

            val slot = player.inventory.heldItemSlot
            player.inventory.setItem(slot,null)

            //event.isCancelled = true;
        }
    }
}

public fun sendAuthBook(player: Player, action: GitAction, storeAuth: Boolean) {
    val authBook: ItemStack = ItemStack(Material.WRITABLE_BOOK)
    var mainPage: String = ("${ChatColor.UNDERLINE}Welcome to the git authenticator.\n\n${ChatColor.RESET}"+
            "Please Fill out your email on the next page and the password on the page after.\n")

    val meta: BookMeta = authBook.itemMeta as BookMeta
    mainPage += if (storeAuth) {
        "This data is stored locally for future pull requests. To disable this exit the book and add replace the -a with -af"
    } else {
        "The authentication data is not being saved future pull requests will require the -a statement"
    }

    meta.addPage(mainPage)
    meta.setDisplayName("${ChatColor.GOLD}Datapack Git Authentication Book")
    meta.lore = mutableListOf("${ChatColor.DARK_PURPLE}This book is used to authenditcate Github for the datapacks folder")
    authBook.itemMeta = meta;

    //Save the old item in there main hand and set it to the book
//    val slot: Int = player.inventory.heldItemSlot
//    val oldItem: ItemStack? = player.inventory.getItem(slot);

    player.inventory.addItem(authBook)
    player.sendMessage("\n${ChatColor.GREEN}Please fill out the Git Auth book you got to authenticate the action. ${ChatColor.RED}Warning don't close or edit any other book until you have done this!")

    BookEvents.awaitingAuth.set(player.uniqueId,action)
}