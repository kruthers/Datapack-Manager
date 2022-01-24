package com.kruthers.datapackmanager.utils

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.kruthers.datapackmanager.DatapackManager
import org.bukkit.configuration.file.FileConfiguration
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.*
import org.eclipse.jgit.util.FS

class SshCallback(plugin: DatapackManager): TransportConfigCallback {
    val config: FileConfiguration = plugin.config
    val passphase: String = config.getString("auth.ssh.password") ?: ""

    val sshSessionFactory: SshSessionFactory = object : JschConfigSessionFactory() {
        override fun configure(hc: OpenSshConfig.Host?, session: Session?) {
            session?.setConfig("StrictHostKeyChecking","no")
            super.configure(hc, session)
        }

        override fun createDefaultJSch(fs: FS?): JSch {
            val jSch: JSch = super.createDefaultJSch(fs)
            jSch.addIdentity("${plugin.dataFolder}/key.txt",passphase)

            return jSch
        }
    }

    override fun configure(p0: Transport?) {
        if (p0 is SshTransport) {
            val sshTransport: SshTransport = p0 as SshTransport
            sshTransport.sshSessionFactory = sshSessionFactory
        }
    }


}

fun getUsernamePasswordAuth(plugin: DatapackManager): UsernamePasswordCredentialsProvider {
    val username: String = plugin.config.getString("auth.normal.username")?: "username"
    val password: String = plugin.config.getString("auth.normal.password")?: "password"
    return UsernamePasswordCredentialsProvider(username,password)
}

fun getTokenAuth(plugin: DatapackManager): UsernamePasswordCredentialsProvider {
    val token: String = plugin.config.getString("auth.token.token")?: "token"
    return UsernamePasswordCredentialsProvider(token,"")
}
