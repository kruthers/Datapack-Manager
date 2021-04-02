package com.kruthers.datapackmanager.actions

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

interface GitAction {

    fun trigger() { }
    fun triggerWithAuth(auth: UsernamePasswordCredentialsProvider) { }

}