package nz.co.noirland.vanillapod;

import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * A ReconnectManager that only looks at what the player's protocol version is.
 * Useful for players wanting to keep their legacy server and vanilla 1.8 server
 * at the same address.
 */
public class ProtocolReconnectManager implements ReconnectHandler {

    @Override
    public ServerInfo getServer(ProxiedPlayer player) {
        int protocol = player.getPendingConnection().getVersion();
        ServerInfo info = VanillaPod.inst().getServer(protocol);
        if(info == null) {
            VanillaPod.inst().getLogger().warning("Unable to find server to send protocol " + protocol + "!");
            return null;
        }
        return info;
    }

    @Override
    public void setServer(ProxiedPlayer player) {}

    @Override
    public void save() {}

    @Override
    public void close() {}
}
