package com.kruthers.datapackmanager.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.kruthers.datapackmanager.DatapackManager;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

public class SshTransportConfigCallback implements TransportConfigCallback {
    private DatapackManager plugin;
    public SshTransportConfigCallback(DatapackManager pl){
        plugin=pl;
    }

    private final SshSessionFactory sessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
            session.setConfig("StrictHostKeyChecking","no");
        }
        /*
        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch jSch = super.createDefaultJSch(fs);
            if (plugin.getConfig().getBoolean("authentication.use_passphrase")){
                jSch.addIdentity(plugin.getDataFolder()+"/key.txt",plugin.getConfig().getString("authentication.ssh_passphrase").getBytes());
            } else {
                jSch.addIdentity(plugin.getDataFolder()+"/key.txt");
            }

            return jSch;

        }*/
    };

    @Override
    public void configure(Transport transport) {
        SshTransport sshTransport = (SshTransport) transport;
        sshTransport.setSshSessionFactory(sessionFactory);
    }
}
