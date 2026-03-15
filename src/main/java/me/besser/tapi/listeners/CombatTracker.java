package me.besser.tapi.listeners;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.besser.tapi.database.InsertMethods;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class CombatTracker {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        String deathMsg = event.getSource().getLocalizedDeathMessage(victim).getString();
        ItemStack weapon = killer.getMainHandItem();
        String weaponId = BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString();
        JsonObject weaponJson = new JsonObject();

        weaponJson.addProperty("type", weaponId);

        // Name
        if (weapon.has(DataComponents.CUSTOM_NAME)) {
            weaponJson.addProperty("name", weapon.getHoverName().getString());
        }

        // Enchants
        JsonArray enchantsArray = new JsonArray();
        ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(weapon);

        enchants.keySet().forEach(enchantmentHolder -> {
            JsonObject enchantJson = new JsonObject();

            String enchantId = enchantmentHolder.unwrapKey()
                    .map(key -> key.identifier().toString())
                    .orElse("unknown");

            int level = enchants.getLevel(enchantmentHolder);

            enchantJson.addProperty("id", enchantId);
            enchantJson.addProperty("level", level);
            enchantsArray.add(enchantJson);
        });

        weaponJson.add("enchantments", enchantsArray);


        InsertMethods.logKill(
                killer.getUUID(),
                killer.getName().getString(),
                victim.getUUID(),
                victim.getName().getString(),
                deathMsg,
                weaponJson
        );
    }
}