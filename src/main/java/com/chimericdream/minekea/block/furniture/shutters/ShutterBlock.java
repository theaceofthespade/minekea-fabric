package com.chimericdream.minekea.block.furniture.shutters;

import com.chimericdream.minekea.ModInfo;
import com.chimericdream.minekea.util.MinekeaBlock;
import com.chimericdream.minekea.util.MinekeaTextures;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.MultipartBlockStateSupplier;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.When;
import net.minecraft.data.server.loottable.BlockLootTableGenerator;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.chimericdream.minekea.item.MinekeaItemGroups.FURNITURE_ITEM_GROUP_KEY;

public class ShutterBlock extends Block implements MinekeaBlock, Waterloggable {
    protected static final Model CLOSED_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/furniture/shutters/closed")),
        Optional.empty(),
        MinekeaTextures.PANEL,
        MinekeaTextures.FRAME
    );
    protected static final Model OPEN_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/furniture/shutters/open")),
        Optional.empty(),
        MinekeaTextures.PANEL,
        MinekeaTextures.FRAME
    );

    public final Identifier BLOCK_ID;

    protected final Block plankIngredient;
    protected final Block logIngredient;
    protected final String materialName;
    protected final boolean isFlammable;
    protected final BlockSetType blockSetType;

    public static final BooleanProperty OPEN;
    public static final BooleanProperty POWERED;
    public static final DirectionProperty WALL_SIDE;
    public static final BooleanProperty WATERLOGGED;

    private static final Map<String, VoxelShape> OUTLINE_CLOSED;
    private static final Map<String, VoxelShape> HITBOX_OPEN;

    static {
        OPEN = Properties.OPEN;
        POWERED = Properties.POWERED;
        WALL_SIDE = DirectionProperty.of("wall_side", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        WATERLOGGED = Properties.WATERLOGGED;

        OUTLINE_CLOSED = Map.of(
            "north", Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 2.0),
            "south", Block.createCuboidShape(0.0, 0.0, 14.0, 16.0, 16.0, 16.0),
            "east", Block.createCuboidShape(14.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            "west", Block.createCuboidShape(0.0, 0.0, 0.0, 2.0, 16.0, 16.0)
        );
        HITBOX_OPEN = Map.of(
            "north", VoxelShapes.union(
                Block.createCuboidShape(16.0, 0.0, 0.0, 24.0, 16.0, 2.0),
                Block.createCuboidShape(-8.0, 0.0, 0.0, 0.0, 16.0, 2.0)
            ),
            "south", VoxelShapes.union(
                Block.createCuboidShape(16.0, 0.0, 14.0, 24.0, 16.0, 16.0),
                Block.createCuboidShape(-8.0, 0.0, 14.0, 0.0, 16.0, 16.0)
            ),
            "east", VoxelShapes.union(
                Block.createCuboidShape(14.0, 0.0, -8.0, 16.0, 16.0, 0.0),
                Block.createCuboidShape(14.0, 0.0, 16.0, 16.0, 16.0, 24.0)
            ),
            "west", VoxelShapes.union(
                Block.createCuboidShape(0.0, 0.0, -8.0, 2.0, 16.0, 0.0),
                Block.createCuboidShape(0.0, 0.0, 16.0, 2.0, 16.0, 24.0)
            )
        );
    }

    public ShutterBlock(BlockSetType type, String materialName, Block plankIngredient, Block logIngredient) {
        this(type, materialName, plankIngredient, logIngredient, true);
    }

    public ShutterBlock(BlockSetType type, String materialName, Block plankIngredient, Block logIngredient, boolean isFlammable) {
        super(AbstractBlock.Settings.copy(plankIngredient));

        this.setDefaultState(
            this.stateManager.getDefaultState()
                .with(OPEN, false)
                .with(POWERED, false)
                .with(WALL_SIDE, Direction.NORTH)
                .with(WATERLOGGED, false)
        );

        BLOCK_ID = makeBlockId(materialName);

        this.blockSetType = type;
        this.materialName = materialName;
        this.plankIngredient = plankIngredient;
        this.logIngredient = logIngredient;
        this.isFlammable = isFlammable;
    }

    public static Identifier makeBlockId(String materialName) {
        String material = materialName.toLowerCase().replaceAll(" ", "_");

        return Identifier.of(ModInfo.MOD_ID, String.format("furniture/shutters/%s", material));
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (state.get(OPEN)) {
            removeOpenHalves(state, world, pos);
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN, POWERED, WALL_SIDE, WATERLOGGED);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, NavigationType type) {
        return switch (type) {
            case LAND, AIR -> state.get(OPEN);
            case WATER -> state.get(WATERLOGGED);
        };
    }

    private static boolean hasSpaceToOpen(BlockState state, BlockView world, BlockPos pos) {
        BlockPos pos1 = pos.east();
        BlockPos pos2 = pos.west();

        Direction wallSide = state.get(WALL_SIDE);
        if (wallSide == Direction.EAST || wallSide == Direction.WEST) {
            pos1 = pos.north();
            pos2 = pos.south();
        }

        BlockState pos1State = world.getBlockState(pos1);
        BlockState pos2State = world.getBlockState(pos2);

        return (pos1State.isOf(Blocks.AIR) || pos1State.isOf(Blocks.WATER))
            && (pos2State.isOf(Blocks.AIR) || pos2State.isOf(Blocks.WATER));
    }

    private void placeOpenHalves(BlockState state, World world, BlockPos pos) {
        BlockPos left;
        BlockPos right;

        switch (state.get(WALL_SIDE)) {
            case NORTH -> {
                left = pos.west();
                right = pos.east();
            }
            case EAST -> {
                left = pos.north();
                right = pos.south();
            }
            case SOUTH -> {
                left = pos.east();
                right = pos.west();
            }
            default -> {
                left = pos.south();
                right = pos.north();
            }
        }

        BlockState leftState = world.getBlockState(left);
        BlockState rightState = world.getBlockState(right);

        BlockState newLeftState = Shutters.OPEN_SHUTTER_HALVES
            .get(materialName)
            .getDefaultState()
            .with(OpenShutterHalf.HALF, OpenShutterHalf.ShutterHalf.LEFT)
            .with(OpenShutterHalf.WALL_SIDE, state.get(WALL_SIDE));

        BlockState newRightState = Shutters.OPEN_SHUTTER_HALVES
            .get(materialName)
            .getDefaultState()
            .with(OpenShutterHalf.HALF, OpenShutterHalf.ShutterHalf.RIGHT)
            .with(OpenShutterHalf.WALL_SIDE, state.get(WALL_SIDE));

        if (leftState.isOf(Blocks.WATER)) {
            world.setBlockState(
                left,
                newLeftState.with(OpenShutterHalf.WATERLOGGED, true)
            );
        } else {
            world.setBlockState(left, newLeftState);
        }

        if (rightState.isOf(Blocks.WATER)) {
            world.setBlockState(right, newRightState.with(OpenShutterHalf.WATERLOGGED, true));
        } else {
            world.setBlockState(right, newRightState);
        }
    }

    private void removeOpenHalves(BlockState state, World world, BlockPos pos) {
        BlockPos pos1 = pos.east();
        BlockPos pos2 = pos.west();

        Direction wallSide = state.get(WALL_SIDE);
        if (wallSide == Direction.EAST || wallSide == Direction.WEST) {
            pos1 = pos.north();
            pos2 = pos.south();
        }

        BlockState pos1State = world.getBlockState(pos1);
        BlockState pos2State = world.getBlockState(pos2);

        if (pos1State.getProperties().contains(Properties.WATERLOGGED) && pos1State.get(WATERLOGGED)) {
            world.setBlockState(pos1, Blocks.WATER.getDefaultState());
        } else {
            world.setBlockState(pos1, Blocks.AIR.getDefaultState());
        }

        if (pos2State.getProperties().contains(Properties.WATERLOGGED) && pos2State.get(WATERLOGGED)) {
            world.setBlockState(pos2, Blocks.WATER.getDefaultState());
        } else {
            world.setBlockState(pos2, Blocks.AIR.getDefaultState());
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        boolean isOpen = state.get(OPEN);

        if (!isOpen && !hasSpaceToOpen(state, world, pos)) {
            return ActionResult.FAIL;
        }

        if (isOpen) {
            removeOpenHalves(state, world, pos);
        } else {
            placeOpenHalves(state, world, pos);
        }

        state = state.cycle(OPEN);
        world.setBlockState(pos, state, 2);
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        this.playToggleSound(player, world, pos, state.get(OPEN));
        return ActionResult.success(world.isClient);
    }

    protected void playToggleSound(@Nullable PlayerEntity player, World world, BlockPos pos, boolean open) {
        world.playSound(player, pos, open ? this.blockSetType.trapdoorOpen() : this.blockSetType.trapdoorClose(), SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
        world.emitGameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            boolean isReceivingPower = world.isReceivingRedstonePower(pos);
            boolean isPowered = state.get(POWERED);
            boolean isOpen = state.get(OPEN);

            if (isReceivingPower != isPowered) {
                if (isOpen != isReceivingPower) {
                    if (isOpen) {
                        removeOpenHalves(state, world, pos);
                    } else {
                        placeOpenHalves(state, world, pos);
                    }

                    state = state.with(OPEN, isReceivingPower);
                    this.playToggleSound(null, world, pos, isReceivingPower);
                }

                world.setBlockState(pos, state.with(POWERED, isReceivingPower), 2);
                if (state.get(WATERLOGGED)) {
                    world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
                }
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());

        BlockState blockState = this.getDefaultState().with(WALL_SIDE, ctx.getPlayerLookDirection());

        if (ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos())) {
            blockState = blockState.with(OPEN, true).with(POWERED, true);
        }

        return blockState.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(OPEN)) {
            return VoxelShapes.empty();
        }

        return switch (state.get(WALL_SIDE)) {
            case SOUTH -> OUTLINE_CLOSED.get("south");
            case WEST -> OUTLINE_CLOSED.get("west");
            case EAST -> OUTLINE_CLOSED.get("east");
            default -> OUTLINE_CLOSED.get("north");
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(OPEN)) {
            return switch (state.get(WALL_SIDE)) {
                case SOUTH -> HITBOX_OPEN.get("south");
                case WEST -> HITBOX_OPEN.get("west");
                case EAST -> HITBOX_OPEN.get("east");
                default -> HITBOX_OPEN.get("north");
            };
        }

        return switch (state.get(WALL_SIDE)) {
            case SOUTH -> OUTLINE_CLOSED.get("south");
            case WEST -> OUTLINE_CLOSED.get("west");
            case EAST -> OUTLINE_CLOSED.get("east");
            default -> OUTLINE_CLOSED.get("north");
        };
    }

    @Override
    public void register() {
        Registry.register(Registries.BLOCK, BLOCK_ID, this);
        Registry.register(Registries.ITEM, BLOCK_ID, new BlockItem(this, new Item.Settings()));

        ItemGroupEvents.modifyEntriesEvent(FURNITURE_ITEM_GROUP_KEY).register(itemGroup -> itemGroup.add(this));

        if (isFlammable) {
            FuelRegistry.INSTANCE.add(this, 300);
            FlammableBlockRegistry.getDefaultInstance().add(this, 30, 20);
        }
    }

    @Override
    public void configureRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, this, 6)
            .pattern("#X#")
            .pattern("#X#")
            .pattern("#X#")
            .input('X', plankIngredient)
            .input('#', logIngredient)
            .criterion(FabricRecipeProvider.hasItem(plankIngredient),
                FabricRecipeProvider.conditionsFromItem(plankIngredient))
            .criterion(FabricRecipeProvider.hasItem(logIngredient),
                FabricRecipeProvider.conditionsFromItem(logIngredient))
            .offerTo(exporter);
    }

    @Override
    public void configureBlockTags(RegistryWrapper.WrapperLookup registryLookup, Function<TagKey<Block>, FabricTagProvider<Block>.FabricTagBuilder> getBuilder) {
        getBuilder.apply(BlockTags.AXE_MINEABLE).setReplace(false).add(this);
    }

    @Override
    public void configureBlockLootTables(RegistryWrapper.WrapperLookup registryLookup, BlockLootTableGenerator generator) {
        generator.addDrop(this);
    }

    @Override
    public void configureTranslations(RegistryWrapper.WrapperLookup registryLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        translationBuilder.add(this, String.format("%s Shutters", materialName));
    }

    @Override
    public void configureBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        TextureMap textures = new TextureMap()
            .put(MinekeaTextures.FRAME, Registries.BLOCK.getId(logIngredient).withPrefixedPath("block/"))
            .put(MinekeaTextures.PANEL, Registries.BLOCK.getId(plankIngredient).withPrefixedPath("block/"));

        Identifier closedModelId = blockStateModelGenerator.createSubModel(this, "", CLOSED_MODEL, unused -> textures);
        Identifier openModelId = blockStateModelGenerator.createSubModel(this, "_open", OPEN_MODEL, unused -> textures);

        blockStateModelGenerator.blockStateCollector
            .accept(
                MultipartBlockStateSupplier.create(this)
                    .with(
                        When.create()
                            .set(WALL_SIDE, Direction.NORTH)
                            .set(OPEN, false),
                        BlockStateVariant.create()
                            .put(VariantSettings.Y, VariantSettings.Rotation.R180)
                            .put(VariantSettings.MODEL, closedModelId)
                    )
                    .with(
                        When.create()
                            .set(WALL_SIDE, Direction.SOUTH)
                            .set(OPEN, false),
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, closedModelId)
                    )
                    .with(
                        When.create()
                            .set(WALL_SIDE, Direction.EAST)
                            .set(OPEN, false),
                        BlockStateVariant.create()
                            .put(VariantSettings.Y, VariantSettings.Rotation.R270)
                            .put(VariantSettings.MODEL, closedModelId)
                    )
                    .with(
                        When.create()
                            .set(WALL_SIDE, Direction.WEST)
                            .set(OPEN, false),
                        BlockStateVariant.create()
                            .put(VariantSettings.Y, VariantSettings.Rotation.R90)
                            .put(VariantSettings.MODEL, closedModelId)
                    )
                    .with(
                        When.create()
                            .set(OPEN, true),
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, openModelId)
                    )
            );
    }
}
