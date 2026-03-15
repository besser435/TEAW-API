package me.besser.tapi;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TAPIConfig {
    public static final ModConfigSpec SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ModConfigSpec.IntValue afkTimeout;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("tapi_settings");

            afkTimeout = builder
                    .comment("How many seconds someone has to not move for before being declared AFK.")
                    .defineInRange("afkTimeout", 180, 0, 1024);


            builder.pop();
        }
    }
}
