package com.kruthers.datapackmanager.utils

import com.kruthers.datapackmanager.DatapackManager
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

fun checkForDatapackFolder(pl: DatapackManager): Boolean {
    val path: String = ""+pl.config.getString("datapack_world")+"/datapacks/"
    return File(path).exists()
}

fun checkForDatapackRepo(pl: DatapackManager): Boolean {
    if (!checkForDatapackFolder(pl)) {
        return false
    }
    val gitFolder = File(pl.config.getString("datapack_world")+"/datapacks/.git/")

    return gitFolder.exists()


}

fun setupRepoChecks(pl: DatapackManager) {
    if (checkForDatapackFolder(pl)) {
        pl.logger.info("Found datapack Folder, checking for a local repository")
        if (checkForDatapackRepo(pl)) {
            pl.logger.info("Found a local repository, git pull is enabled")
            DatapackManager.setup = true
        } else {
            pl.logger.info("Datapack repository has not yet been cloned.")
        }
    } else {
        pl.logger.warning("The world given is invalid as a datapack world. Please check your config")
    }

    return
}

fun deleteFile(path: String, pl: DatapackManager, log: Boolean) {
    val file = File(path)
    if (file.isDirectory) {
        val files = file.listFiles()
        files?.forEach { deleteFile(it.path,pl, log) }
        if (log) pl.logger.info("Deleting folder $path")
        file.delete()
    } else {
        if (log) pl.logger.info("Deleting file $path")
        file.delete()
    }
}

fun saveData(repo: String, branch: String, auth: AuthType, pl: DatapackManager) {
    val dataFile = File("${pl.dataFolder}/data.yml")
    try {
        if (!dataFile.exists()) {
            dataFile.createNewFile()
        }

        val ymlData = YamlConfiguration.loadConfiguration(dataFile)
        ymlData.set("github.repo",repo)
        ymlData.set("github.branch",branch)
        ymlData.set("github.auth.type",auth.toString())
        ymlData.save(dataFile)
    } catch (err: IOException) {
        pl.logger.warning("Failed to save github data with error:\n${err.localizedMessage}")
    }
}

fun getAuthMethod(pl: DatapackManager): AuthType {
    val dataFile = File("${pl.dataFolder}/data.yml")

    val ymlData = YamlConfiguration.loadConfiguration(dataFile)
    val auth: AuthType = AuthType.valueOf(ymlData.getString("github.auth.type")?: "NONE") //  as

    return auth
}
