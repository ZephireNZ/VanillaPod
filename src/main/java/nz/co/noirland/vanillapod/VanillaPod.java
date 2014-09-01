package nz.co.noirland.vanillapod;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VanillaPod extends Plugin implements Listener {

    private static VanillaPod inst;

    private Map<ServerInfo, ServerPing> cachedPings = new ConcurrentHashMap<>(); // Cached server pings
    private Set<ServerInfo> protoServers; // All the servers that need to be cached

    public static VanillaPod inst() {
        return inst;
    }

    @Override
    public void onEnable() {
        inst = this;
        PodConfig.inst();

        getProxy().setReconnectHandler(new ProtocolReconnectManager());
        getProxy().getPluginManager().registerListener(this, this);

        HashSet<ServerInfo> servers = new HashSet<>();
        for(String server : PodConfig.inst().getServers()) {
            ServerInfo info = getProxy().getServerInfo(server);
            servers.add(info);
        }
        protoServers = Collections.unmodifiableSet(servers);

        for(ServerInfo server : protoServers) {
            PingCacheTask.get(server).runTimer(PodConfig.inst().getPingCacheDelay());
        }
    }

    /**
     * Used to passthrough the ping of the relevant server
     */
    @EventHandler
    public void onPing(ProxyPingEvent event) {
        if(!PodConfig.inst().getPingPassthrough()) return;

        int protocol = event.getConnection().getVersion();
        ServerInfo info = getServer(protocol);
        if(info == null) return;
        event.setResponse(getPing(info));
    }

    /**
     * Force refresh the MOTD when a player leaves watched server.
     */
    @EventHandler
    public void onDisconnect(ServerDisconnectEvent event) {
        ServerInfo left = event.getTarget();
        if(!protoServers.contains(left)) return;

        PingCacheTask.get(left).runNow();
    }

    /**
     * Force refresh the MOTD when a player joins watched server.
     */
    @EventHandler
    public void onConnect(ServerConnectEvent event) {
        ServerInfo joined = event.getTarget();
        if(!protoServers.contains(joined)) return;

        PingCacheTask.get(joined).runNow();
    }

    /**
     * Gets the server associated with the given protocol version.
     * @param protocol Protocol version to look for
     * @return the ServerInfo to point the player at
     */
    public ServerInfo getServer(int protocol) {
        String name = PodConfig.inst().getServer(protocol);
        return getProxy().getServerInfo(name);
    }

    /**
     * Get the cached ping for this server
     * @param server Server to get
     * @return A cached ping
     */
    public ServerPing getPing(ServerInfo server) {
        return cachedPings.get(server);
    }

    /**
     * Sets the cached ping for the given server
     * @param server Server's info
     * @param ping Ping to cache
     */
    public void setPing(ServerInfo server, ServerPing ping) {
        cachedPings.put(server, ping);
    }
}
