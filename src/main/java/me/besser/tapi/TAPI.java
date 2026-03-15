package me.besser.tapi;

import com.mojang.logging.LogUtils;
import me.besser.tapi.database.DatabaseManager;
import me.besser.tapi.database.InsertMethods;
import me.besser.tapi.listeners.ChatTracker;
import me.besser.tapi.listeners.CombatTracker;
import me.besser.tapi.listeners.PlayerTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(value = TAPI.MODID, dist = Dist.DEDICATED_SERVER)
public class TAPI {
    public static final String MODID = "tapi";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TAPI(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        // Set up config
        modContainer.registerConfig(ModConfig.Type.COMMON, TAPIConfig.SPEC);

        // Set up database
        modEventBus.addListener(this::databaseSetup);

        // Event listeners
        NeoForge.EVENT_BUS.register(new ChatTracker());
        NeoForge.EVENT_BUS.register(new PlayerTracker());
        NeoForge.EVENT_BUS.register(new CombatTracker());

        NeoForge.EVENT_BUS.addListener(this::onServerStopped);


        LOGGER.info("TEAW API 2 started!"); // TODO: print version number from gradle.properties
    }


    private void databaseSetup(final FMLCommonSetupEvent event) {
        DatabaseManager.initialize();
    }

    private void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        InsertMethods.shutdown();

        LOGGER.info("TEAW API 2 stopped!"); // TODO: print version number from gradle.properties
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}
}
