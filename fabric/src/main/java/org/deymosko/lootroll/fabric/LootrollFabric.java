package org.deymosko.lootroll.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;
import org.deymosko.lootroll.ClientConfig;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.Lootroll;

public class LootrollFabric implements ModInitializer {

    @Override
    public void onInitialize()
    {
        NeoForgeConfigRegistry.INSTANCE.register(Lootroll.MODID, ModConfig.Type.COMMON, Config.CONFIG);
        NeoForgeConfigRegistry.INSTANCE.register(Lootroll.MODID, ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        Lootroll.init();
    }
}
