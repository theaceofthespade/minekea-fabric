package com.chimericdream.minekea.block.stairs;

import com.chimericdream.minekea.ModInfo;
import com.chimericdream.minekea.resource.MinekeaResourcePack;
import com.chimericdream.minekea.resource.Texture;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.loot.JCondition;
import net.devtech.arrp.json.loot.JEntry;
import net.devtech.arrp.json.loot.JLootTable;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.devtech.arrp.json.recipe.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public class GenericBookshelfStairs extends StairsBlock {
    private final String modId;
    private final String woodType;
    private final Identifier BLOCK_ID;
    private final Map<String, Identifier> materials;

    public GenericBookshelfStairs(String woodType, Map<String, Identifier> materials) {
        this(woodType, ModInfo.MOD_ID, materials);
    }

    public GenericBookshelfStairs(String woodType, String modId, Map<String, Identifier> materials) {
        super(Blocks.OAK_STAIRS.getDefaultState(), FabricBlockSettings.copyOf(Blocks.OAK_STAIRS).sounds(BlockSoundGroup.WOOD));

        validateMaterials(materials);

        this.modId = modId;
        this.woodType = woodType;
        this.materials = materials;

        BLOCK_ID = new Identifier(ModInfo.MOD_ID, String.format("stairs/%s%s_bookshelf_stairs", ModInfo.getModPrefix(modId), woodType));
    }

    protected void validateMaterials(Map<String, Identifier> materials) {
        String[] keys = new String[]{"bookshelf", "planks"};

        for (String key : keys) {
            if (!materials.containsKey(key)) {
                throw new IllegalArgumentException(String.format("The materials must contain a '%s' key", key));
            }
        }
    }

    public void register() {
        Registry.register(Registry.BLOCK, BLOCK_ID, this);
        Registry.register(Registry.ITEM, BLOCK_ID, new BlockItem(this, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));

        setupResources();
    }

    protected void setupResources() {
        Identifier shelf = materials.get("bookshelf");
        Identifier planks = materials.get("planks");

        Identifier ITEM_MODEL_ID = new Identifier(ModInfo.MOD_ID, "item/" + BLOCK_ID.getPath());

        Identifier MAIN_MODEL_ID = new Identifier(ModInfo.MOD_ID, "block/" + BLOCK_ID.getPath());
        Identifier INNER_MODEL_ID = new Identifier(ModInfo.MOD_ID, "block/" + BLOCK_ID.getPath() + "_inner");
        Identifier OUTER_MODEL_ID = new Identifier(ModInfo.MOD_ID, "block/" + BLOCK_ID.getPath() + "_outer");

        MinekeaResourcePack.RESOURCE_PACK.addRecipe(
            BLOCK_ID,
            JRecipe.shaped(
                JPattern.pattern("#  ", "## ", "###"),
                JKeys.keys().key("#", JIngredient.ingredient().item(shelf.toString())),
                JResult.stackedResult(BLOCK_ID.toString(), 8)
            )
        );

        MinekeaResourcePack.RESOURCE_PACK.addLootTable(
            new Identifier(BLOCK_ID.getNamespace(), "blocks/" + BLOCK_ID.getPath()),
            JLootTable.loot("minecraft:block")
                .pool(
                    JLootTable.pool()
                        .rolls(1)
                        .entry(
                            new JEntry()
                                .type("minecraft:item")
                                .name(BLOCK_ID.toString())
                        )
                        .condition(new JCondition().condition("minecraft:survives_explosion"))
                )
        );

        JTextures textures = new JTextures()
            .var("planks", Texture.getBlockTextureID(planks).toString())
            .var("shelf", ModInfo.MOD_ID + ":block/bookshelves/shelf0");

        MinekeaResourcePack.RESOURCE_PACK.addModel(
            JModel.model("minekea:block/bookshelf_stairs").textures(textures),
            MAIN_MODEL_ID
        );

        MinekeaResourcePack.RESOURCE_PACK.addModel(
            JModel.model("minekea:block/bookshelf_inner_stairs").textures(textures),
            INNER_MODEL_ID
        );

        MinekeaResourcePack.RESOURCE_PACK.addModel(
            JModel.model("minekea:block/bookshelf_outer_stairs").textures(textures),
            OUTER_MODEL_ID
        );

        MinekeaResourcePack.RESOURCE_PACK.addModel(JModel.model(MAIN_MODEL_ID), ITEM_MODEL_ID);

        MinekeaResourcePack.RESOURCE_PACK.addBlockState(
            JState.state(
                JState.variant()
                    .put("facing=east,half=bottom,shape=inner_left", new JBlockModel(INNER_MODEL_ID).y(270).uvlock())
                    .put("facing=east,half=bottom,shape=inner_right", new JBlockModel(INNER_MODEL_ID))
                    .put("facing=east,half=bottom,shape=outer_left", new JBlockModel(OUTER_MODEL_ID).y(270).uvlock())
                    .put("facing=east,half=bottom,shape=outer_right", new JBlockModel(OUTER_MODEL_ID))
                    .put("facing=east,half=bottom,shape=straight", new JBlockModel(MAIN_MODEL_ID))
                    .put("facing=east,half=top,shape=inner_left", new JBlockModel(INNER_MODEL_ID).x(180).uvlock())
                    .put("facing=east,half=top,shape=inner_right", new JBlockModel(INNER_MODEL_ID).x(180).y(90).uvlock())
                    .put("facing=east,half=top,shape=outer_left", new JBlockModel(OUTER_MODEL_ID).x(180).uvlock())
                    .put("facing=east,half=top,shape=outer_right", new JBlockModel(OUTER_MODEL_ID).x(180).y(90).uvlock())
                    .put("facing=east,half=top,shape=straight", new JBlockModel(MAIN_MODEL_ID).x(180).uvlock())
                    .put("facing=north,half=bottom,shape=inner_left", new JBlockModel(INNER_MODEL_ID).y(180).uvlock())
                    .put("facing=north,half=bottom,shape=inner_right", new JBlockModel(INNER_MODEL_ID).y(270).uvlock())
                    .put("facing=north,half=bottom,shape=outer_left", new JBlockModel(OUTER_MODEL_ID).y(180).uvlock())
                    .put("facing=north,half=bottom,shape=outer_right", new JBlockModel(OUTER_MODEL_ID).y(270).uvlock())
                    .put("facing=north,half=bottom,shape=straight", new JBlockModel(MAIN_MODEL_ID).y(270).uvlock())
                    .put("facing=north,half=top,shape=inner_left", new JBlockModel(INNER_MODEL_ID).x(180).y(270).uvlock())
                    .put("facing=north,half=top,shape=inner_right", new JBlockModel(INNER_MODEL_ID).x(180).uvlock())
                    .put("facing=north,half=top,shape=outer_left", new JBlockModel(OUTER_MODEL_ID).x(180).y(270).uvlock())
                    .put("facing=north,half=top,shape=outer_right", new JBlockModel(OUTER_MODEL_ID).x(180).uvlock())
                    .put("facing=north,half=top,shape=straight", new JBlockModel(MAIN_MODEL_ID).x(180).y(270).uvlock())
                    .put("facing=south,half=bottom,shape=inner_left", new JBlockModel(INNER_MODEL_ID))
                    .put("facing=south,half=bottom,shape=inner_right", new JBlockModel(INNER_MODEL_ID).y(90).uvlock())
                    .put("facing=south,half=bottom,shape=outer_left", new JBlockModel(OUTER_MODEL_ID))
                    .put("facing=south,half=bottom,shape=outer_right", new JBlockModel(OUTER_MODEL_ID).y(90).uvlock())
                    .put("facing=south,half=bottom,shape=straight", new JBlockModel(MAIN_MODEL_ID).y(90).uvlock())
                    .put("facing=south,half=top,shape=inner_left", new JBlockModel(INNER_MODEL_ID).x(180).y(90).uvlock())
                    .put("facing=south,half=top,shape=inner_right", new JBlockModel(INNER_MODEL_ID).x(180).y(180).uvlock())
                    .put("facing=south,half=top,shape=outer_left", new JBlockModel(OUTER_MODEL_ID).x(180).y(90).uvlock())
                    .put("facing=south,half=top,shape=outer_right", new JBlockModel(OUTER_MODEL_ID).x(180).y(180).uvlock())
                    .put("facing=south,half=top,shape=straight", new JBlockModel(MAIN_MODEL_ID).x(180).y(90).uvlock())
                    .put("facing=west,half=bottom,shape=inner_left", new JBlockModel(INNER_MODEL_ID).y(90).uvlock())
                    .put("facing=west,half=bottom,shape=inner_right", new JBlockModel(INNER_MODEL_ID).y(180).uvlock())
                    .put("facing=west,half=bottom,shape=outer_left", new JBlockModel(OUTER_MODEL_ID).y(90).uvlock())
                    .put("facing=west,half=bottom,shape=outer_right", new JBlockModel(OUTER_MODEL_ID).y(180).uvlock())
                    .put("facing=west,half=bottom,shape=straight", new JBlockModel(MAIN_MODEL_ID).y(180).uvlock())
                    .put("facing=west,half=top,shape=inner_left", new JBlockModel(INNER_MODEL_ID).x(180).y(180).uvlock())
                    .put("facing=west,half=top,shape=inner_right", new JBlockModel(INNER_MODEL_ID).x(180).y(270).uvlock())
                    .put("facing=west,half=top,shape=outer_left", new JBlockModel(OUTER_MODEL_ID).x(180).y(180).uvlock())
                    .put("facing=west,half=top,shape=outer_right", new JBlockModel(OUTER_MODEL_ID).x(180).y(270).uvlock())
                    .put("facing=west,half=top,shape=straight", new JBlockModel(MAIN_MODEL_ID).x(180).y(180).uvlock())
            ),
            BLOCK_ID
        );
    }
}
