package ladysnake.spawnercontrol.config;

import ladysnake.spawnercontrol.SpawnerControl;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomSpawnersConfig {
    public static final String MAIN_CONFIG_FILE = SpawnerControl.MOD_ID + "/" + SpawnerControl.MOD_ID;
    public static final String VANILLA_CONFIG_CATEGORY = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "vanillaSpawnerConfig";
    public static final String CUSTOM_CONFIG_FOLDER = SpawnerControl.MOD_ID + "/" + "custom_spawners";
    public static File configDir;
    // we want to sync dynamically generated config to objects for convenience
    // that method does exactly that, sadly it's private
    static MethodHandle configManager$sync;
    /**Maps custom spawner names to their configuration*/
    private static final Map<String, SpawnerConfigHolder> customSpawnerConfigs = new HashMap<>();

    static {
        try {
            Method m = ConfigManager.class.getDeclaredMethod("sync", Configuration.class, Class.class, String.class, String.class, boolean.class, Object.class);
            m.setAccessible(true);
            configManager$sync = MethodHandles.lookup().unreflect(m);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            SpawnerControl.LOGGER.error("Error while reflecting ConfigManager#sync", e);
        }
    }

    public static void initCustomConfig() {
        Configuration baseConfig = new Configuration(new File(configDir, MAIN_CONFIG_FILE + ".cfg"));
        for (String name : MSCConfig.customSpawners)
            generateConfig(baseConfig.getCategory(VANILLA_CONFIG_CATEGORY), name);
    }

    public static Collection<SpawnerConfigHolder> getCustomSpawnerConfigs() {
        return customSpawnerConfigs.values();
    }

    private static void generateConfig(ConfigCategory baseCategory, String name) {
        Configuration config = new Configuration(new File(configDir, CUSTOM_CONFIG_FOLDER + "/" + name + ".cfg"));
        ConfigCategory category = config.getCategory(name);
        // duplicate the config from the vanilla spawner
        baseCategory.getValues().forEach((key, value) -> {
            if (!category.containsKey(key)) // don't overwrite existing values
                category.put(key, value);
        });
        SpawnerConfigHolder holder = new SpawnerConfigHolder(config, name);
        holder.sync();
        customSpawnerConfigs.put(name, holder);
    }

}
