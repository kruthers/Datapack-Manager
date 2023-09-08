package com.kruthers.datapackmanager.utils

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.kruthers.datapackmanager.DatapackManager
import org.bukkit.configuration.file.FileConfiguration
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig
import org.eclipse.jgit.util.FS


class SshCallback(plugin: DatapackManager): TransportConfigCallback {
    private val config: FileConfiguration = plugin.config
    private val passphrase: String = config.getString("auth.ssh.password") ?: ""
    private val key = "${plugin.dataFolder}/key.txt"

    private class SshFactory(private val key: String, private val passphrase: String): JschConfigSessionFactory() {
        override fun configure(hc: OpenSshConfig.Host?, session: Session?) {
            session?.setConfig("StrictHostKeyChecking","no")
            super.configure(hc, session)
        }

        override fun createDefaultJSch(fs: FS?): JSch {
            val jSch: JSch = super.createDefaultJSch(fs)
            jSch.addIdentity(key ,passphrase)

            return jSch
        }
    }

        /**
     * Add any additional transport-specific configuration required.
     *
     * @param transport
     * a [org.eclipse.jgit.transport.Transport] object.
     */
    override fun configure(transport: Transport?) {
        if (transport is SshTransport) {
            transport.setSshSessionFactory(SshFactory(key, passphrase))
        } else throw NotSshRemote()
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

