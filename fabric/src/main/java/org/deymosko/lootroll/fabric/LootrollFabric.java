package org.deymosko.lootroll.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.fml.config.ModConfig;
import org.deymosko.lootroll.ClientConfig;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.Lootroll;

public class LootrollFabric implements ModInitializer {

    @Override
    public void onInitialize()
    {
        ForgeConfigRegistry.INSTANCE.register(Lootroll.MODID, ModConfig.Type.COMMON, Config.CONFIG);
        ForgeConfigRegistry.INSTANCE.register(Lootroll.MODID, ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        Lootroll.init();
    }
}
