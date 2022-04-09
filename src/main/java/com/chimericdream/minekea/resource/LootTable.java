package com.chimericdream.minekea.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/*
 * Inspired by similar utils in the following mod:
 * https://github.com/Azagwen/ATBYW
 */
public class LootTable {
    public static JsonObject silkTouchPredicate() {
        var level = new JsonObject();
        level.addProperty("min", 1);

        var silkTouch = new JsonObject();
        silkTouch.addProperty("enchantment", "minecraft:silk_touch");
        silkTouch.add("levels", level);

        JsonArray enchantments = new JsonArray();
        enchantments.add(silkTouch);

        var predicate = new JsonObject();
        predicate.add("enchantments", enchantments);

        return predicate;
    }
}
