package nz.co.noirland.vanillapod;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.Exchanger;

public class VanillaPod extends Plugin implements Listener {

    private static VanillaPod inst;

    public static VanillaPod inst() {
        return inst;
    }

    @Override
    public void onEnable() {
        inst = this;
        PodConfig.inst();

        getProxy().setReconnectHandler(new ProtocolReconnectManager());
        getProxy().getPluginManager().registerListener(this, this);
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

        final Exchanger<ServerPing> exch = new Exchanger<>();

        // Get the child server's ping, pass it to back to main ping.
        info.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing result, Throwable error) {
                try {
                    exch.exchange(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            ServerPing ping = exch.exchange(null); // Wait for child ping thread to finish
            if(ping == null) return;
            event.setResponse(ping);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
}
