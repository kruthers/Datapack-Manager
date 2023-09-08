package com.kruthers.datapackmanager.utils

class MultipleAuthType: Exception("Multiple methods of authentication provided, provide only one")
class NoActionFound: Exception("No action found await your confirmation")

class NotSshRemote: Exception("Unable to close using ssh, the provided git remote is not a ssh clone remote. These " +
        "start with git@ not https://")