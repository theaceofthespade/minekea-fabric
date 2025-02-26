package com.chimericdream.minekea.block.building.beams;

import com.chimericdream.minekea.ModInfo;
import com.chimericdream.minekea.item.MinekeaItemGroups;
import com.chimericdream.minekea.tag.MinekeaTags;
import com.chimericdream.minekea.util.MinekeaBlock;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.MultipartBlockStateSupplier;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.When;
import net.minecraft.data.server.loottable.BlockLootTableGenerator;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import java.util.Optional;
import java.util.function.Function;

public class GenericBeamBlock extends Block implements MinekeaBlock, Waterloggable {
    protected static final Model CONNECTED_NORTH_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/building/beams/connected_north")),
        Optional.empty(),
        TextureKey.SIDE,
        TextureKey.END
    );
    protected static final Model CONNECTED_SOUTH_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/building/beams/connected_south")),
        Optional.empty(),
        TextureKey.SIDE,
        TextureKey.END
    );
    protected static final Model CONNECTED_EAST_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/building/beams/connected_east")),
        Optional.empty(),
        TextureKey.SIDE,
        TextureKey.END
    );
    protected static final Model CONNECTED_WEST_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/building/beams/connected_west")),
        Optional.empty(),
        TextureKey.SIDE,
        TextureKey.END
    );
    protected static final Model CONNECTED_UP_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/building/beams/connected_up")),
        Optional.empty(),
        TextureKey.SIDE,
        TextureKey.END
    );
    protected static final Model CONNECTED_DOWN_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/building/beams/connected_down")),
        Optional.empty(),
        TextureKey.SIDE,
        TextureKey.END
    );
    protected static final Model CORE_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "block/building/beams/core")),
        Optional.empty(),
        TextureKey.SIDE,
        TextureKey.END
    );
    protected static final Model ITEM_MODEL = new Model(
        Optional.of(Identifier.of(ModInfo.MOD_ID, "item/building/beam")),
        Optional.empty(),
        TextureKey.SIDE,
        TextureKey.END
    );

    public final BeamConfig config;

    protected static final VoxelShape CORE_SHAPE = Block.createCuboidShape(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    protected static final VoxelShape CONNECTED_NORTH_SHAPE = Block.createCuboidShape(5.0, 5.0, 0.0, 11.0, 11.0, 5.0);
    protected static final VoxelShape CONNECTED_SOUTH_SHAPE = Block.createCuboidShape(5.0, 5.0, 11.0, 11.0, 11.0, 16.0);
    protected static final VoxelShape CONNECTED_EAST_SHAPE = Block.createCuboidShape(11.0, 5.0, 5.0, 16.0, 11.0, 11.0);
    protected static final VoxelShape CONNECTED_WEST_SHAPE = Block.createCuboidShape(0.0, 5.0, 5.0, 5.0, 11.0, 11.0);
    protected static final VoxelShape CONNECTED_UP_SHAPE = Block.createCuboidShape(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);
    protected static final VoxelShape CONNECTED_DOWN_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 5.0, 11.0);

    public static final BooleanProperty CONNECTED_NORTH;
    public static final BooleanProperty CONNECTED_SOUTH;
    public static final BooleanProperty CONNECTED_EAST;
    public static final BooleanProperty CONNECTED_WEST;
    public static final BooleanProperty CONNECTED_UP;
    public static final BooleanProperty CONNECTED_DOWN;

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    static {
        CONNECTED_NORTH = BooleanProperty.of("connected_north");
        CONNECTED_SOUTH = BooleanProperty.of("connected_south");
        CONNECTED_EAST = BooleanProperty.of("connected_east");
        CONNECTED_WEST = BooleanProperty.of("connected_west");
        CONNECTED_UP = BooleanProperty.of("connected_up");
        CONNECTED_DOWN = BooleanProperty.of("connected_down");
    }

    public GenericBeamBlock(
        AbstractBlock.Settings settings,
        BeamConfig config
    ) {
        super(settings);

        this.config = config;

        this.setDefaultState(
            this.stateManager
                .getDefaultState()
                .with(CONNECTED_NORTH, false)
                .with(CONNECTED_SOUTH, false)
                .with(CONNECTED_EAST, false)
                .with(CONNECTED_WEST, false)
                .with(CONNECTED_UP, false)
                .with(CONNECTED_DOWN, false)
                .with(WATERLOGGED, false)
        );
    }

    private boolean shouldConnect(ItemPlacementContext ctx, Direction direction) {
        return shouldConnect(ctx.getWorld(), ctx.getBlockPos(), direction);
    }

    private boolean shouldConnect(BlockView world, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.offset(direction);
        BlockState neighborState = world.getBlockState(neighborPos);

        if (neighborState.isOf(Blocks.AIR)) {
            return false;
        }

        return neighborState.getFluidState().isEmpty() || neighborState.isIn(MinekeaTags.BEAMS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
            .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER)
            .with(CONNECTED_NORTH, shouldConnect(ctx, Direction.NORTH))
            .with(CONNECTED_SOUTH, shouldConnect(ctx, Direction.SOUTH))
            .with(CONNECTED_EAST, shouldConnect(ctx, Direction.EAST))
            .with(CONNECTED_WEST, shouldConnect(ctx, Direction.WEST))
            .with(CONNECTED_UP, shouldConnect(ctx, Direction.UP))
            .with(CONNECTED_DOWN, shouldConnect(ctx, Direction.DOWN))
            .with(getConnectionProperty(ctx.getSide()), true);
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

        if (neighborState.isOf(Blocks.AIR)) {
            return state.with(getConnectionProperty(direction), false);
        }

        return state.with(
            getConnectionProperty(direction),
            neighborState.getFluidState().isEmpty() || neighborState.isIn(MinekeaTags.BEAMS)
        );
    }

    public static BooleanProperty getConnectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> CONNECTED_NORTH;
            case SOUTH -> CONNECTED_SOUTH;
            case EAST -> CONNECTED_EAST;
            case WEST -> CONNECTED_WEST;
            case UP -> CONNECTED_UP;
            case DOWN -> CONNECTED_DOWN;
        };
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(
            CONNECTED_NORTH,
            CONNECTED_SOUTH,
            CONNECTED_EAST,
            CONNECTED_WEST,
            CONNECTED_UP,
            CONNECTED_DOWN,
            WATERLOGGED
        );
    }

    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.NORMAL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        boolean connectedNorth = state.get(CONNECTED_NORTH);
        boolean connectedSouth = state.get(CONNECTED_SOUTH);
        boolean connectedEast = state.get(CONNECTED_EAST);
        boolean connectedWest = state.get(CONNECTED_WEST);
        boolean connectedUp = state.get(CONNECTED_UP);
        boolean connectedDown = state.get(CONNECTED_DOWN);

        VoxelShape outline = CORE_SHAPE;

        if (connectedNorth) {
            outline = VoxelShapes.union(outline, CONNECTED_NORTH_SHAPE);
        }
        if (connectedSouth) {
            outline = VoxelShapes.union(outline, CONNECTED_SOUTH_SHAPE);
        }
        if (connectedEast) {
            outline = VoxelShapes.union(outline, CONNECTED_EAST_SHAPE);
        }
        if (connectedWest) {
            outline = VoxelShapes.union(outline, CONNECTED_WEST_SHAPE);
        }
        if (connectedUp) {
            outline = VoxelShapes.union(outline, CONNECTED_UP_SHAPE);
        }
        if (connectedDown) {
            outline = VoxelShapes.union(outline, CONNECTED_DOWN_SHAPE);
        }

        return outline;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public void register() {
        Registry.register(Registries.BLOCK, config.getBlockID(), this);
        Registry.register(Registries.ITEM, config.getBlockID(), new BlockItem(this, new Item.Settings()));

        if (config.isFlammable) {
            FuelRegistry.INSTANCE.add(this, 300);
            FlammableBlockRegistry.getDefaultInstance().add(this, 30, 20);
        }

        ItemGroupEvents.modifyEntriesEvent(MinekeaItemGroups.BEAM_ITEM_GROUP_KEY)
            .register(itemGroup -> itemGroup.add(this.asItem()));
    }

    @Override
    public void configureBlockTags(RegistryWrapper.WrapperLookup registryLookup, Function<TagKey<Block>, FabricTagProvider<Block>.FabricTagBuilder> getBuilder) {
        getBuilder.apply(MinekeaTags.BEAMS)
            .setReplace(false)
            .add(this);
    }

    @Override
    public void configureRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, this, 6)
            .pattern("# #")
            .pattern("# #")
            .pattern("# #")
            .input('#', config.ingredient)
            .criterion(FabricRecipeProvider.hasItem(config.ingredient),
                FabricRecipeProvider.conditionsFromItem(config.ingredient))
            .offerTo(exporter);
    }

    @Override
    public void configureBlockLootTables(RegistryWrapper.WrapperLookup registryLookup, BlockLootTableGenerator generator) {
        generator.addDrop(this);
    }

    @Override
    public void configureTranslations(RegistryWrapper.WrapperLookup registryLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        translationBuilder.add(this, config.name + " Beam");
    }

    @Override
    public void configureBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        TextureMap textures = config.getTextures();

        Identifier coreModelId = blockStateModelGenerator.createSubModel(this, "", CORE_MODEL, unused -> textures);
        Identifier northModelId = blockStateModelGenerator.createSubModel(this, "_connected_north", CONNECTED_NORTH_MODEL, unused -> textures);
        Identifier southModelId = blockStateModelGenerator.createSubModel(this, "_connected_south", CONNECTED_SOUTH_MODEL, unused -> textures);
        Identifier eastModelId = blockStateModelGenerator.createSubModel(this, "_connected_east", CONNECTED_EAST_MODEL, unused -> textures);
        Identifier westModelId = blockStateModelGenerator.createSubModel(this, "_connected_west", CONNECTED_WEST_MODEL, unused -> textures);
        Identifier upModelId = blockStateModelGenerator.createSubModel(this, "_connected_up", CONNECTED_UP_MODEL, unused -> textures);
        Identifier downModelId = blockStateModelGenerator.createSubModel(this, "_connected_down", CONNECTED_DOWN_MODEL, unused -> textures);

        blockStateModelGenerator.blockStateCollector
            .accept(
                MultipartBlockStateSupplier.create(this)
                    .with(
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, coreModelId)
                    )
                    .with(
                        When.create().set(CONNECTED_NORTH, true),
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, northModelId)
                    )
                    .with(
                        When.create().set(CONNECTED_SOUTH, true),
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, southModelId)
                    )
                    .with(
                        When.create().set(CONNECTED_EAST, true),
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, eastModelId)
                    )
                    .with(
                        When.create().set(CONNECTED_WEST, true),
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, westModelId)
                    )
                    .with(
                        When.create().set(CONNECTED_UP, true),
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, upModelId)
                    )
                    .with(
                        When.create().set(CONNECTED_DOWN, true),
                        BlockStateVariant.create()
                            .put(VariantSettings.MODEL, downModelId)
                    )
            );
    }

    @Override
    public void configureItemModels(ItemModelGenerator itemModelGenerator) {
        ITEM_MODEL.upload(
            config.getBlockID().withPrefixedPath("item/"),
            config.getTextures(),
            itemModelGenerator.writer
        );
    }

    public static final class BeamConfig {
        public final Block ingredient;
        public final String material;
        public final String name;
        public final boolean isFlammable;
        public final boolean isTranslucent;
        public final Identifier sideTexture;
        public final Identifier endTexture;

        public BeamConfig(
            Block ingredient,
            String material,
            String name,
            boolean isFlammable,
            boolean isTranslucent,
            Identifier texture
        ) {
            this(
                ingredient,
                material,
                name,
                isFlammable,
                isTranslucent,
                texture,
                texture
            );
        }

        public BeamConfig(
            Block ingredient,
            String material,
            String name,
            boolean isFlammable,
            boolean isTranslucent,
            Identifier sideTexture,
            Identifier endTexture
        ) {
            this.ingredient = ingredient;
            this.material = material;
            this.name = name;
            this.isFlammable = isFlammable;
            this.isTranslucent = isTranslucent;
            this.sideTexture = sideTexture;
            this.endTexture = endTexture;
        }

        public Identifier getBlockID() {
            return Identifier.of(ModInfo.MOD_ID, "building/beams/" + material);
        }

        public TextureMap getTextures() {
            return new TextureMap()
                .put(TextureKey.SIDE, sideTexture)
                .put(TextureKey.END, endTexture);
        }
    }
}
