package com.kruthers.datapackmanager.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.kruthers.datapackmanager.DatapackManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;

public class Utils {

    public static File getDatapackFolder(){
        File datapackFolder = null;
        for (World world : Bukkit.getWorlds()){
            File testPath = new File(world.getWorldFolder()+"/datapacks");
            if (testPath.exists()){
                datapackFolder=testPath;
                break;
            }
        }

        return datapackFolder;

    }

    public static void deleateFolder(File folder){
        File[] files = folder.listFiles();
        if (files!=null) {
            for (File file : files){
                if (file.isDirectory()) {
                    deleateFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

}
