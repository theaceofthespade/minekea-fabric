package com.chimericdream.minekea.block.furniture.displaycases;

import com.chimericdream.minekea.ModInfo;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class StrippedCrimsonDisplayCase extends GenericDisplayCase {
    public static final Identifier BLOCK_ID = Identifier.of(ModInfo.MOD_ID, "furniture/display_cases/stripped/crimson");

    public StrippedCrimsonDisplayCase() {
        super(AbstractBlock.Settings.copy(Blocks.CRIMSON_PLANKS));
    }

    @Override
    protected Block getPlanksBlock() {
        return Blocks.CRIMSON_PLANKS;
    }

    @Override
    protected Block getLogBlock() {
        return Blocks.CRIMSON_STEM;
    }

    @Override
    protected Block getStrippedLogBlock() {
        return Blocks.STRIPPED_CRIMSON_STEM;
    }

    @Override
    protected Block getLogForRecipe() {
        return Blocks.STRIPPED_CRIMSON_STEM;
    }

    @Override
    protected String getMaterialName() {
        return "Stripped Crimson";
    }

    @Override
    public void register() {
        Registry.register(Registries.BLOCK, BLOCK_ID, this);
        Registry.register(Registries.ITEM, BLOCK_ID, new BlockItem(this, new Item.Settings()));
    }
}
