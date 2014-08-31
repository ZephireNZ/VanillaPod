package nz.co.noirland.vanillapod;

public enum MinecraftVersion {
    ONE_SEVEN(4, 5),
    ONE_EIGHT(44, 46);
    private final int min;
    private final int max;

    MinecraftVersion(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static MinecraftVersion getVersion(int protocol) {
        for(MinecraftVersion version : MinecraftVersion.values()) {
            if(protocol >= version.getMin() && protocol <= version.getMax()) return version;
        }
        VanillaPod.inst().getLogger().warning("Could not find Version of protocol v" + protocol);
        return null;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
