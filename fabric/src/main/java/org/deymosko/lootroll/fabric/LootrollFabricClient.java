package org.deymosko.lootroll.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.deymosko.lootroll.client.LootrollClient;

public class LootrollFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient()
    {
        LootrollClient.init();
    }
}
