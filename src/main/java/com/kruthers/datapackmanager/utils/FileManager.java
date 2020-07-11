package com.kruthers.datapackmanager.utils;

import com.kruthers.datapackmanager.DatapackManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class FileManager {
    private static DatapackManager plugin = DatapackManager.getPlugin(DatapackManager.class);
    private static File pluginPath = plugin.getDataFolder();
    private static Logger LOGGER = DatapackManager.LOGGER;

    private static FileConfiguration storedData;
    private static File dataFile = new File(pluginPath+"/data.yml");

    public static boolean init(){
        File keyFile = new File(pluginPath+"/key.txt");
        System.out.print(keyFile.getAbsoluteFile().toString()+"\n"+dataFile.getPath());
        if (!keyFile.exists()){
            try {
                keyFile.createNewFile();
                LOGGER.info("Created new file, key.txt");
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.severe("Failed to create key.txt aborting launch");
                return false;
            }
        } else {
            LOGGER.info("Found key.txt");
        }

        if (!dataFile.exists()){
            try {
                dataFile.createNewFile();
                storedData = YamlConfiguration.loadConfiguration(dataFile);
                storedData.set("github.cloned",false);
                storedData.save(dataFile);
                LOGGER.info("Created new file, data.yml and set default values");
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.severe("Failed to create data.yml aborting launch");
                return false;
            }
        } else {
            storedData=YamlConfiguration.loadConfiguration(dataFile);
            LOGGER.info("Found and loaded data.yml");
        }

        return true;
    }

    public static FileConfiguration getStoredData() {
        return storedData;
    }

    public static void saveStoredData(){
        try {
            storedData.save(dataFile);
            LOGGER.info("Saved data into data.yml");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("Failed to save data.yml file");
        }
    }

}
