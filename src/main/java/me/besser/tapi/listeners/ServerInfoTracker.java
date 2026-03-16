package me.besser.tapi.listeners;

import me.besser.tapi.database.InsertMethods;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import me.besser.tapi.TAPI;

public class ServerInfoTracker {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        if (server.getTickCount() % 200 != 0) return;

        ServerLevel level = server.overworld();

        // Weather
        String weather = "Clear";
        if (level.isThundering()) {
            weather = "Thunderstorms";
        } else if (level.isRaining()) {
            weather = "Rainy";
        }

        // Time
        long gameTime = level.getGameTime();
        long timeInDay = level.getDayTime() % 24000;
        long daysPassed = gameTime / 24000;

        int hours = (int) ((timeInDay / 1000 + 6) % 24);
        int minutes = (int) ((timeInDay % 1000) * 60 / 1000);
        String time24h = String.format("%02d:%02d", hours, minutes);


        InsertMethods.upsertVariable("weather", weather);
        InsertMethods.upsertVariable("world_time_24h", time24h);
        InsertMethods.upsertVariable("day", String.valueOf(daysPassed));
        InsertMethods.upsertVariable("tapi_version", TAPI.VERSION);
    }
}