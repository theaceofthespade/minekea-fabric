package com.chimericdream.minekea.block.containers;

import com.chimericdream.minekea.MinekeaMod;
import com.chimericdream.minekea.ModInfo;
import com.chimericdream.minekea.entities.blocks.containers.GlassJarBlockEntity;
import com.chimericdream.minekea.resource.MinekeaResourcePack;
import com.chimericdream.minekea.util.FluidHelpers;
import com.chimericdream.minekea.util.MinekeaBlock;
import net.devtech.arrp.json.recipe.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GlassJarBlock extends Block implements MinekeaBlock, BlockEntityProvider {
    private final Identifier BLOCK_ID = new Identifier(ModInfo.MOD_ID, "containers/glass_jar");
    private final String modId;
    private static final VoxelShape MAIN_SHAPE;
    private static final VoxelShape LID_SHAPE;

    static {
        MAIN_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 9.0, 11.0);
        LID_SHAPE = Block.createCuboidShape(6.0, 9.0, 6.0, 10.0, 10.0, 10.0);
    }

    public GlassJarBlock() {
        this(ModInfo.MOD_ID);
    }

    public GlassJarBlock(String modId) {
        super(Settings.copy(Blocks.GLASS).nonOpaque());

        validateMaterials(null);

        this.modId = modId;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GlassJarBlockEntity(ContainerBlocks.GLASS_JAR_BLOCK_ENTITY, pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        GlassJarBlockEntity entity;

        try {
            entity = (GlassJarBlockEntity) world.getBlockEntity(pos);
            assert entity != null;
        } catch (Exception e) {
            MinekeaMod.LOGGER.error(String.format("The glass jar at %s had an invalid block entity.\nBlock Entity: %s", pos, world.getBlockEntity(pos)));

            return ActionResult.FAIL;
        }

        ItemStack heldItem = player.getMainHandStack();

        if (isFilledBucket(heldItem)) {
            Identifier heldItemId = Registry.ITEM.getId(heldItem.getItem());
            Fluid bucketFluid = getFluidType(heldItemId);

            if (!bucketFluid.matchesType(Fluids.EMPTY) && entity.tryInsert(bucketFluid)) {
                replaceHeldItemOrDont(world, player, heldItem, Items.BUCKET.getDefaultStack());
                entity.playEmptyBucketSound(bucketFluid);
                entity.markDirty();
            }
        } else if (isFilledBottle(heldItem)) {
            if (
                heldItem.isItemEqual(Items.HONEY_BOTTLE.getDefaultStack())
                    && entity.tryInsert(com.chimericdream.minekea.fluid.Fluids.HONEY, GlassJarBlockEntity.BOTTLE_SIZE)
            ) {
                replaceHeldItemOrDont(world, player, heldItem, Items.GLASS_BOTTLE.getDefaultStack());
                entity.playEmptyBottleSound();
                entity.markDirty();
            } else if (
                heldItem.isItemEqual(Items.POTION.getDefaultStack())
                    && PotionUtil.getPotion(heldItem) == Potions.WATER
                    && entity.tryInsert(Fluids.WATER, GlassJarBlockEntity.BOTTLE_SIZE)
            ) {
                replaceHeldItemOrDont(world, player, heldItem, Items.GLASS_BOTTLE.getDefaultStack());
                entity.playEmptyBottleSound();
                entity.markDirty();
            }
        } else if (
            heldItem.isItemEqual(Items.GLASS_BOTTLE.getDefaultStack())
                && entity.hasFluid()
                && (entity.getStoredFluid() == Fluids.WATER || entity.getStoredFluid() == com.chimericdream.minekea.fluid.Fluids.HONEY)
        ) {
            ItemStack bottle = entity.getBottle();

            if (bottle != null && !bottle.isItemEqual(Items.GLASS_BOTTLE.getDefaultStack())) {
                replaceHeldItemOrDont(world, player, heldItem, bottle);
                entity.playFillBottleSound();
            }
        } else if (isEmptyBucket(heldItem) && entity.hasFluid()) {
            Fluid fluid = entity.getBucket();

            if (!fluid.matchesType(Fluids.EMPTY)) {
                if (fluid.matchesType(Fluids.WATER)) {
                    replaceHeldItemOrDont(world, player, heldItem, Items.WATER_BUCKET.getDefaultStack());
                } else if (fluid.matchesType(Fluids.LAVA)) {
                    replaceHeldItemOrDont(world, player, heldItem, Items.LAVA_BUCKET.getDefaultStack());
                } else if (fluid.matchesType(com.chimericdream.minekea.fluid.Fluids.MILK)) {
                    replaceHeldItemOrDont(world, player, heldItem, Items.MILK_BUCKET.getDefaultStack());
                } else if (fluid.matchesType(com.chimericdream.minekea.fluid.Fluids.HONEY)) {
                    replaceHeldItemOrDont(world, player, heldItem, com.chimericdream.minekea.fluid.Fluids.HONEY_BUCKET.getDefaultStack());
                }

                entity.playFillBucketSound(fluid);
                entity.markDirty();
            }
        } else if (!heldItem.isEmpty() && entity.canAcceptItem(heldItem)) {
            ItemStack originalStack = heldItem.copy();

            // Try to insert the item in the player's hand into the jar
            ItemStack remainingStack = entity.tryInsert(heldItem);

            if (remainingStack.isEmpty() || originalStack.getCount() > remainingStack.getCount()) {
                player.setStackInHand(hand, remainingStack);
                entity.playAddItemSound();
                entity.markDirty();
            }
        } else if (player.isSneaking() && heldItem.isEmpty()) {
            if (entity.hasItem()) {
                ItemScatterer.spawn(
                    world,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    entity.removeStack()
                );
                entity.playRemoveItemSound();
                entity.markDirty();
            }
        }

        world.markDirty(pos);

        return ActionResult.SUCCESS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof GlassJarBlockEntity entity) {
            if (!world.isClient) {
                if (entity.isEmpty() && !player.isCreative()) {
                    ItemEntity itemEntity = new ItemEntity(
                        world,
                        (double) pos.getX() + 0.5D,
                        (double) pos.getY() + 0.5D,
                        (double) pos.getZ() + 0.5D,
                        ContainerBlocks.GLASS_JAR_ITEM.getDefaultStack()
                    );

                    itemEntity.setToDefaultPickupDelay();

                    world.spawnEntity(itemEntity);
                } else if (!entity.isEmpty()) {
                    ItemStack itemStack = new ItemStack(ContainerBlocks.GLASS_JAR_ITEM);
                    NbtCompound nbt = new NbtCompound();
                    entity.writeNbt(nbt);

                    if (!nbt.isEmpty()) {
                        itemStack.setSubNbt("BlockEntityTag", nbt);
                    }

                    ItemEntity itemEntity = new ItemEntity(
                        world,
                        (double) pos.getX() + 0.5D,
                        (double) pos.getY() + 0.5D,
                        (double) pos.getZ() + 0.5D,
                        itemStack
                    );

                    itemEntity.setToDefaultPickupDelay();

                    world.spawnEntity(itemEntity);
                }
            }
        }
    }

    private void replaceHeldItemOrDont(World world, PlayerEntity player, ItemStack heldItem, ItemStack item) {
        ItemStack remaining = heldItem.copy();
        remaining.decrement(1);

        if (remaining.getCount() == 0) {
            player.setStackInHand(Hand.MAIN_HAND, item);
        } else {
            player.setStackInHand(Hand.MAIN_HAND, remaining);
            ItemScatterer.spawn(
                world,
                player.getX(),
                player.getY(),
                player.getZ(),
                item
            );
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);

        NbtCompound nbt = stack.getSubNbt("BlockEntityTag");

        if (nbt != null) {
            String storedFluid = nbt.getString(GlassJarBlockEntity.FLUID_KEY);
            if (!storedFluid.equals("") && !storedFluid.equals("NONE")) {
                Fluid fluid = Registry.FLUID.get(new Identifier(storedFluid));

                if (fluid != Fluids.EMPTY) {
                    MutableText text = FluidHelpers.getFluidName(fluid).shallowCopy().formatted(Formatting.GREEN);

                    double fluidAmount = nbt.getDouble(GlassJarBlockEntity.FLUID_AMT_KEY);

                    String format = Math.round(fluidAmount) == fluidAmount ? " (%.0f buckets)" : " (%.1f buckets)";
                    text.append(
                        fluidAmount != 1.0
                            ? String.format(format, fluidAmount)
                            : " (1 bucket)"
                    );

                    tooltip.add(text);
                }
            } else {
                DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
                Inventories.readNbt(nbt, items);

                ItemStack storedItem = items.get(0);
                if (!storedItem.isEmpty()) {
                    int fullStacks = nbt.getInt(GlassJarBlockEntity.ITEM_AMT_KEY);
                    int total = storedItem.getCount() + (fullStacks * storedItem.getMaxCount());

                    MutableText text = storedItem.getName().shallowCopy().formatted(Formatting.GREEN);
                    text.append(String.format(" (%d)", total));

                    tooltip.add(text);
                }
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity entity = world.getBlockEntity(pos);

            if (entity instanceof GlassJarBlockEntity) {
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    private Fluid getFluidType(Identifier heldItemId) {
        Optional<Fluid> foundFluid = Registry.FLUID.stream()
            .filter(fluid -> {
                Item bucket = fluid.getBucketItem();
                return Registry.ITEM.getId(bucket).compareTo(heldItemId) == 0;
            })
            .findFirst();

        return foundFluid.orElse(Fluids.EMPTY);
    }

    private boolean isEmptyBucket(ItemStack item) {
        if (item.isEmpty()) {
            return false;
        }

        return item.isItemEqual(Items.BUCKET.getDefaultStack());
    }

    private boolean isFilledBottle(ItemStack item) {
        if (item.isEmpty()) {
            return false;
        }

        if (item.isItemEqual(Items.POTION.getDefaultStack()) && PotionUtil.getPotion(item) == Potions.WATER) {
            return true;
        }

        return item.isItemEqual(Items.HONEY_BOTTLE.getDefaultStack());
    }

    private boolean isFilledBucket(ItemStack item) {
        if (item.isEmpty()) {
            return false;
        }

        if (!(item.getItem() instanceof BucketItem) && !(item.getItem() instanceof MilkBucketItem)) {
            return false;
        }

        Identifier itemId = Registry.ITEM.getId(item.getItem());

        return itemId.compareTo(Registry.ITEM.getId(Items.BUCKET.asItem())) != 0;
    }

    public Identifier getBlockID() {
        return BLOCK_ID;
    }

    public void register() {
        Registry.register(Registry.BLOCK, BLOCK_ID, this);
        ContainerBlocks.GLASS_JAR_ITEM = Registry.register(Registry.ITEM, BLOCK_ID, new BlockItem(this, new Item.Settings().group(ItemGroup.DECORATIONS).maxCount(8)));

        setupResources();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(MAIN_SHAPE, LID_SHAPE);
    }

    public void setupResources() {
        MinekeaResourcePack.EN_US.blockRespect(this, "Glass Jar");

        MinekeaResourcePack.RESOURCE_PACK.addRecipe(
            BLOCK_ID,
            JRecipe.shaped(
                JPattern.pattern(" L ", "G G", "GGG"),
                JKeys.keys()
                    .key("G", JIngredient.ingredient().item("minecraft:glass_pane"))
                    .key("L", JIngredient.ingredient().item("minecraft:acacia_planks")),
                JResult.stackedResult(BLOCK_ID.toString(), 3)
            )
        );
    }
}
