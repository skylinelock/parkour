package mc.sky_lock.parkour;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import mc.sky_lock.parkour.api.Parkour;
import mc.sky_lock.parkour.api.ParkourPlayer;
import mc.sky_lock.parkour.command.CommandHandler;
import mc.sky_lock.parkour.command.tabcomplete.ParkourTabCompleter;
import mc.sky_lock.parkour.config.ConfigFile;
import mc.sky_lock.parkour.json.ParkourFile;
import mc.sky_lock.parkour.listener.EntityListener;
import mc.sky_lock.parkour.listener.PlayerListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author sky_lock
 */

public class ParkourHandler {

    private final ParkourPlugin plugin;
    private final PluginManager pluginManager;
    private ConfigFile configFile;
    private ParkourFile parkourFile;
    private List<Parkour> parkours;
    private Set<ParkourPlayer> parkourPlayers;
    private Logger logger;

    private MongoClient mongoClient;
    private MongoDatabase database;

    public ParkourHandler(@NotNull ParkourPlugin plugin) {
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();
        this.logger = plugin.getLogger();
    }

    void onEnable() {
        connectDB();

        parkourPlayers = new HashSet<>();
        configFile = new ConfigFile(plugin);
        parkourFile = new ParkourFile(plugin.getDataFolder());
        try {
            parkours = parkourFile.loadParkours();
        } catch (IOException ex) {
            logger.warning("An error occurred while loading parkours");
            parkours = new ArrayList<>();
        }

        registerListeners();
        registerParkourCommand();
    }

    void onDisable() {
        try {
            parkourFile.saveParkours(parkours);
        } catch (IOException ex) {
            logger.warning("An error occurred while saving parkours");
        }

        closeDB();
    }

    private void connectDB() {
        mongoClient = new MongoClient();
        database = mongoClient.getDatabase("parkour");
    }

    private void closeDB() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    private void registerListeners() {
        pluginManager.registerEvents(new PlayerListener(this), plugin);
        pluginManager.registerEvents(new EntityListener(this), plugin);
    }

    private void registerParkourCommand() {
        Constructor<?> constructor;
        try {
            constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        } catch (NoSuchMethodException ex) {
            logger.warning("An error occurred while registering command");
            return;
        }
        constructor.setAccessible(true);
        PluginCommand parkourCmd;
        try {
            parkourCmd = (PluginCommand)constructor.newInstance("parkour", plugin);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            logger.warning("An error occurred while registering command");
            return;
        }
        parkourCmd.setExecutor(new CommandHandler(this));
        parkourCmd.setTabCompleter(new ParkourTabCompleter(this));
        ((CraftServer)plugin.getServer()).getCommandMap().register("parkour", parkourCmd);
    }

    PluginManager getPluginManager() {
        return pluginManager;
    }

    ConfigFile getConfigFile() {
        return configFile;
    }

    public ParkourFile getParkourFile() {
        return parkourFile;
    }

    public List<Parkour> getParkours() {
        return parkours;
    }

    public Set<ParkourPlayer> getParkourPlayers() {
        return parkourPlayers;
    }

    public Logger getLogger() {
        return logger;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

}
