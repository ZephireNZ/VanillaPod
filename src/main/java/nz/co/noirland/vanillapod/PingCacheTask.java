package nz.co.noirland.vanillapod;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

class PingCacheTask implements Runnable {

    private static Map<ServerInfo, PingCacheTask> tasks = new ConcurrentHashMap<>();

    private ServerInfo server;

    public static PingCacheTask get(ServerInfo info) {
        if(!tasks.containsKey(info)) {
            tasks.put(info, new PingCacheTask(info));
        }
        return tasks.get(info);
    }

    private PingCacheTask(ServerInfo info) {
        this.server = info;
    }

    @Override
    public void run() {
        server.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing result, Throwable e) {
                if(e != null) return;
                VanillaPod.inst().setPing(server, result);
            }
        });
    }

    public void runNow() {
        ProxyServer.getInstance().getScheduler().runAsync(VanillaPod.inst(), this);
    }

    public void runTimer(long period) {
        ProxyServer.getInstance().getScheduler().schedule(VanillaPod.inst(), this, 0, period, TimeUnit.MILLISECONDS);
    }
}
