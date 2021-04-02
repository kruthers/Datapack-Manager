package com.kruthers.datapackmanager.actions

import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.events.BookEvents
import com.kruthers.datapackmanager.events.sendAuthBook
import com.kruthers.datapackmanager.utils.checkForDatapackFolder
import com.kruthers.datapackmanager.utils.deleteFile
import com.kruthers.datapackmanager.utils.parse
import com.kruthers.datapackmanager.utils.saveData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.scheduler.BukkitRunnable
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.lang.Exception
import java.util.logging.Logger

class Clone(private val player: Player, private val plugin: DatapackManager, val repo: String, val branch: String, val authenticate: Boolean, val storeAuth: Boolean): BukkitRunnable(), GitAction {
    private val command: CloneCommand = Git.cloneRepository()
    private val folder: File = File(plugin.config.getString("datapack_world")+"/datapacks")

    init {
        command.setDirectory(folder)
        if (branch != "") {
            command.setBranch(branch)
        }
        command.setCloneSubmodules(true)
        command.setURI(repo)
    }

    override fun run() {
        if (checkForDatapackFolder(plugin)) {
            val log: Logger = plugin.logger;
            log.info("${player.name}Is cloning the repository")
            log.info("Clearing the datapacks folder")
            player.sendMessage("${ChatColor.GREEN}Starting cloning process... Clearing datapack folder")

            //clear datapacks folder
            folder.listFiles().forEach {
                log.info("Removing datapack: ${it.name}")
                deleteFile(it.path,plugin,false)
            }

            //files cleared, starting clone
            log.info("Datapack folder successfully cleared starting clone")
            player.sendMessage("${ChatColor.GREEN}Folder cleared, cloneing from $repo")
            Bukkit.broadcast(parse(""+plugin.config.get("messages.setup"),player),"datapackmanager.notify")
            try {
                command.call()
            } catch (e: GitAPIException) {
                player.sendMessage("${ChatColor.RED}Failed to clone from github, and API exception occurred, check the console for more details")
                log.warning("Failed to clone from repository with error: \n${e.localizedMessage}")
                return
            } catch (e: InvalidRemoteException) {
                player.sendMessage("${ChatColor.RED}Failed to clone from github, invalid link given, check the console for more details.")
                log.warning("Failed to clone from repository with error: \n${e.localizedMessage}")
                return
            } catch (e: Exception) {
                player.sendMessage("${ChatColor.RED}Failed to clone from github, a unknown Exception occurred, check the console for more details.")
                log.warning("Failed to clone from repository with error: \n${e.localizedMessage}")
                return
            }

            log.info("Datapack folder successfully cloned, updating repo config")
//            val git: Git = Git.open(folder)
//            val repoCofig: StoredConfig = git.repository.config;
//            if (branch == "") {
//                repoCofig.setString("branch", "master", "merge", "refs/heads/master")
//                repoCofig.setString("branch", "master", "remote", "origin")
//            } else {
//                repoCofig.setString("branch", branch, "merge", "refs/heads/$branch")
//                repoCofig.setString("branch", branch, "remote", "origin")
//            }
//            repoCofig.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
//            repoCofig.setString("remote", "origin", "url", repo);

            log.info("Local repo config fully set, reloading datapacks")
            player.sendMessage("${ChatColor.GREEN}Datapacks cloned. Reloading")


            DatapackManager.setup = true
            Bukkit.getServer().reloadData();


        } else {
            player.sendMessage("${ChatColor.RED}The world provided in config does not contain a datapack folder, can not setup.")
        }

    }

    override fun trigger() {
        if (authenticate) {
            sendAuthBook(player,this,storeAuth)
        } else {
            saveData(repo,branch,"","",false,plugin)
            this.runTaskAsynchronously(plugin)
        }

        super.trigger()
    }

    override fun triggerWithAuth(auth: UsernamePasswordCredentialsProvider) {
        command.setCredentialsProvider(auth)
        this.runTaskAsynchronously(plugin)

        super.triggerWithAuth(auth)
    }

}