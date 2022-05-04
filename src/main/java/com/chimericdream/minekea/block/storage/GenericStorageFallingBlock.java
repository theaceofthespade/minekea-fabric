package com.chimericdream.minekea.block.storage;

import com.chimericdream.minekea.ModInfo;
import com.chimericdream.minekea.resource.LootTable;
import com.chimericdream.minekea.resource.MinekeaResourcePack;
import com.chimericdream.minekea.resource.Model;
import com.chimericdream.minekea.resource.Texture;
import com.chimericdream.minekea.util.MinekeaBlock;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.devtech.arrp.json.recipe.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GenericStorageFallingBlock extends FallingBlock implements MinekeaBlock {
    public final Identifier BLOCK_ID;
    public final Identifier baseBlock;

    public GenericStorageFallingBlock(FabricBlockSettings settings, Identifier blockID, Identifier baseBlock) {
        super(settings);

        this.BLOCK_ID = blockID;
        this.baseBlock = baseBlock;
    }

    public GenericStorageFallingBlock(Identifier baseBlock) {
        super(FabricBlockSettings.of(Material.AGGREGATE).strength(1.0f));

        BLOCK_ID = new Identifier(ModInfo.MOD_ID, "storage/compressed_" + baseBlock.getPath());
        this.baseBlock = baseBlock;
    }

    @Override
    public Identifier getBlockID() {
        return BLOCK_ID;
    }

    @Override
    public void register() {
        Registry.register(Registry.BLOCK, BLOCK_ID, this);
        Registry.register(Registry.ITEM, BLOCK_ID, new BlockItem(this, new Item.Settings().group(ItemGroup.MISC)));

        setupResources();
    }

    @Override
    public void setupResources() {
        Identifier MODEL_ID = Model.getBlockModelID(BLOCK_ID);
        Identifier ITEM_MODEL_ID = Model.getItemModelID(BLOCK_ID);

        MinekeaResourcePack.RESOURCE_PACK.addRecipe(
            BLOCK_ID,
            JRecipe.shaped(
                JPattern.pattern("XXX", "XXX", "XXX"),
                JKeys.keys().key("X", JIngredient.ingredient().item(baseBlock.toString())),
                JResult.result(BLOCK_ID.toString())
            )
        );

        MinekeaResourcePack.RESOURCE_PACK.addRecipe(
            new Identifier(ModInfo.MOD_ID, "storage/" + baseBlock.getPath() + "_from_compressed"),
            JRecipe.shapeless(
                JIngredients.ingredients().add(JIngredient.ingredient().item(BLOCK_ID.toString())),
                JResult.stackedResult(baseBlock.toString(), 9)
            )
        );

        MinekeaResourcePack.RESOURCE_PACK.addLootTable(LootTable.blockID(BLOCK_ID), LootTable.dropSelf(BLOCK_ID));

        JTextures textures = new JTextures().var("all", Texture.getBlockTextureID(BLOCK_ID).toString());

        MinekeaResourcePack.RESOURCE_PACK.addModel(JModel.model("minecraft:block/cube_all").textures(textures), MODEL_ID);
        MinekeaResourcePack.RESOURCE_PACK.addModel(JModel.model(MODEL_ID), ITEM_MODEL_ID);

        MinekeaResourcePack.RESOURCE_PACK.addBlockState(
            JState.state(JState.variant().put("", new JBlockModel(MODEL_ID))),
            BLOCK_ID
        );
    }
}
