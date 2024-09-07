package com.chimericdream.minekea.block.furniture.displaycases;

import com.chimericdream.minekea.ModInfo;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WarpedDisplayCase extends GenericDisplayCase {
    public static final Identifier BLOCK_ID = Identifier.of(ModInfo.MOD_ID, "furniture/display_cases/warped");

    public WarpedDisplayCase() {
        super(FabricBlockSettings.copyOf(Blocks.WARPED_PLANKS));
    }

    @Override
    protected Block getPlanksBlock() {
        return Blocks.WARPED_PLANKS;
    }

    @Override
    protected Block getLogBlock() {
        return Blocks.WARPED_STEM;
    }

    @Override
    protected Block getStrippedLogBlock() {
        return Blocks.STRIPPED_WARPED_STEM;
    }

    @Override
    protected Block getLogForRecipe() {
        return Blocks.WARPED_STEM;
    }

    @Override
    protected String getMaterialName() {
        return "Warped";
    }

    @Override
    public void register() {
        Registry.register(Registries.BLOCK, BLOCK_ID, this);
        Registry.register(Registries.ITEM, BLOCK_ID, new BlockItem(this, new Item.Settings()));
    }
}
