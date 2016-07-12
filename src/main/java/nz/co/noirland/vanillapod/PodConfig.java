package nz.co.noirland.vanillapod;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.Collection;
import java.util.logging.Logger;

public class PodConfig {

    private static PodConfig inst;

    private ConfigurationProvider loader;
    private Configuration config;
    private File configFile;
    private Logger logger;

    public static PodConfig inst() {
        if(inst == null) {
            inst = new PodConfig();
        }
        return inst;
    }


    private PodConfig() {
        logger = VanillaPod.inst().getLogger();
        loader = ConfigurationProvider.getProvider(YamlConfiguration.class);
        configFile = new File(VanillaPod.inst().getDataFolder(), "config.yml");
        load();
    }

    /**
     * Gets the server name for the specified protocol version.
     * @param protocol Client's protocol number
     * @return Server name, or null of not found
     */
    public String getServer(int protocol) {
        Configuration servers = config.getSection("servers");
        for(String name : servers.getKeys()) {
            for(int p : servers.getIntList(name)) {
                if(p == protocol) {
                    return name;
                }
            }
        }
        return config.getString("default");
    }

    public Collection<String> getServers() {
        return config.getSection("servers").getKeys();
    }

    public long getPingCacheDelay() {
        return config.getLong("ping-cache-delay", 5000);
    }

    /**
     * Whether or not to pass through the protocol-backed server's
     * Ping, or use Bungee's own ping.
     * @return ping-passthrough's value (true by default)
     */
    public boolean getPingPassthrough() {
        return config.getBoolean("ping-passthrough", true);
    }


    // -- Utility Methods -- //

    public void save() {
        try {
            loader.save(config, configFile);
        } catch (IOException e) {
            logger.severe("Unable to save config file!");
            e.printStackTrace();
        }
    }

    public void load() {
        if(!configFile.exists()) {
            createFile();
        }
        try {
            config = loader.load(configFile);
        } catch (IOException e) {
            logger.severe("Unable to load config file!");
            e.printStackTrace();
        }
    }

    private void createFile() {
        if(!configFile.getParentFile().mkdirs()) {
            logger.severe("Could not create plugin config directory!");
            return;
        }

        InputStream iStream = VanillaPod.inst().getResourceAsStream("config.yml");

        if(iStream == null) {
            logger.severe("Config file missing from jar!");
            return;
        }

        OutputStream oStream = null;

        try {
            oStream = new FileOutputStream(configFile);

            int read;
            byte[] bytes = new byte[1024];

            while((read = iStream.read(bytes)) != -1) {
                oStream.write(bytes, 0, read);
            }

        } catch(IOException e) {
            e.printStackTrace();
        }finally{

            if(oStream != null) {
                try {
                    oStream.close();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                iStream.close();
            }catch(IOException e) {
                e.printStackTrace();
            }

        }
    }
}
