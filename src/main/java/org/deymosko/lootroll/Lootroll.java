package org.deymosko.lootroll;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.deymosko.lootroll.commands.RollCommand;
import org.deymosko.lootroll.events.VoteManager;
import org.deymosko.lootroll.events.VoteSession;
import org.deymosko.lootroll.network.Packets;
import org.deymosko.lootroll.network.s2c.VoteStartS2CPacket;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Lootroll.MODID)
public class Lootroll {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "lootroll";
    // Directly reference a slf4j logger
    static final Logger LOGGER = LogUtils.getLogger();

    public Lootroll()
    {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG);
        modBus.addListener(this::onCommonSetup);

    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            VoteManager.tick();
        }
    }

    @SubscribeEvent
    public static void onEntityDrops(LivingDropsEvent event)
    {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide) return;


        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null || !Config.LOOT_ENTITIES.get().contains(entityId.toString())) return;

        // Блокуємо дроп
        event.getDrops().clear();

        ServerLevel serverLevel = (ServerLevel) level;
        List<ServerPlayer> serverPlayers = new ArrayList<>();
        for (Player p : level.getNearbyPlayers(TargetingConditions.forNonCombat(), entity, entity.getBoundingBox().inflate(100.0d))) {
            if (p instanceof ServerPlayer sp) {
                serverPlayers.add(sp);
            }
        }

        if (serverPlayers.isEmpty()) return;

        // Тимчасово генеруємо один предмет як тест
        ItemStack testItem = new ItemStack(Items.DIAMOND_SWORD); // Потім замінимо на лут із loot table

        VoteSession session = new VoteSession(testItem, serverPlayers, 30); // 30 секунд
        VoteManager.addSession(session);

        Lootroll.LOGGER.info("Почато голосування за {}, учасників: {}", testItem.getDisplayName().getString(), serverPlayers.size());

        serverPlayers.forEach(p -> Packets.sendToClient(new VoteStartS2CPacket(session.getId(), testItem), p));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        LOGGER.info("Реєструємо команду /roll");
        RollCommand.register(event.getDispatcher());
    }

    public void onCommonSetup(final FMLCommonSetupEvent event)
    {
        Packets.register();
    }
}
