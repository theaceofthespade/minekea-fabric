package com.chimericdream.minekea.block.crates.entity;

import com.chimericdream.minekea.block.crates.Crates;
import com.chimericdream.minekea.screen.crate.CrateScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class AcaciaCrateBlockEntity extends GenericCrateBlockEntity {
    public AcaciaCrateBlockEntity(BlockPos pos, BlockState state) {
        super(Crates.ACACIA_CRATE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CrateScreenHandler(Crates.ACACIA_CRATE_SCREEN_HANDLER, syncId, playerInventory, this);
    }
}
