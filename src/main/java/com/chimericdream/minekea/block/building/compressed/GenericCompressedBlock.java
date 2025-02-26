package com.chimericdream.minekea.block.building.compressed;

import com.chimericdream.minekea.ModInfo;
import com.chimericdream.minekea.data.TextureGenerator;
import com.chimericdream.minekea.util.MinekeaBlock;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.server.loottable.BlockLootTableGenerator;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

public class GenericCompressedBlock extends Block implements MinekeaBlock {
    public final Identifier BLOCK_ID;
    protected final Identifier PARENT_BLOCK_ID;

    public static final String TOOLTIP_LEVEL = "block.minekea.building.compressed.tooltip.level";
    public static final String TOOLTIP_COUNT = "block.minekea.building.compressed.tooltip.count";

    public static final EnumProperty<Direction.Axis> AXIS;

    protected final String materialName;
    protected final String textureKey;
    protected final Block baseBlock;
    protected final int compressionLevel;

    static {
        AXIS = Properties.AXIS;
    }

    public GenericCompressedBlock(String materialName, String textureKey, Block baseBlock, int compressionLevel) {
        this(AbstractBlock.Settings.copy(baseBlock), materialName, textureKey, baseBlock, compressionLevel);
    }

    public GenericCompressedBlock(
        AbstractBlock.Settings settings,
        String materialName,
        String textureKey,
        Block baseBlock,
        int compressionLevel
    ) {
        super(settings.strength(
            getHardness(compressionLevel, baseBlock.getHardness()),
            getResistance(compressionLevel, baseBlock.getBlastResistance())
        ).requiresTool());

        this.materialName = materialName;
        this.textureKey = textureKey;
        this.baseBlock = baseBlock;
        this.compressionLevel = compressionLevel;

        BLOCK_ID = Identifier.of(ModInfo.MOD_ID, String.format("building/compressed/%s/%dx", textureKey, compressionLevel));

        if (compressionLevel > 1) {
            PARENT_BLOCK_ID = Identifier.of(ModInfo.MOD_ID, String.format("building/compressed/%s/%dx", textureKey, compressionLevel - 1));
        } else {
            PARENT_BLOCK_ID = Registries.BLOCK.getId(baseBlock);
        }

        this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.Y));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState) this.getDefaultState().with(AXIS, ctx.getSide().getAxis());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        DecimalFormat df = new DecimalFormat("###,###,###");

        tooltip.add(Text.translatable(TOOLTIP_LEVEL, compressionLevel));
        tooltip.add(Text.translatable(TOOLTIP_COUNT, df.format(Math.pow(9, compressionLevel))));
    }

    protected static float getHardness(int level, float baseHardness) {
        return (float) (baseHardness * Math.pow(3, level));
    }

    protected static float getResistance(int level, float baseResistance) {
        return (float) (baseResistance * Math.pow(3, level));
    }

    @Override
    public void register() {
        Registry.register(Registries.BLOCK, BLOCK_ID, this);
        Registry.register(Registries.ITEM, BLOCK_ID, new BlockItem(this, new Item.Settings()));
    }

    @Override
    public void configureRecipes(RecipeExporter exporter) {
        Block parentBlock = Registries.BLOCK.get(PARENT_BLOCK_ID);

        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, this, 1)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .input('#', parentBlock)
            .criterion(FabricRecipeProvider.hasItem(parentBlock),
                FabricRecipeProvider.conditionsFromItem(parentBlock))
            .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, parentBlock, 9)
            .input(this)
            .criterion(FabricRecipeProvider.hasItem(this),
                FabricRecipeProvider.conditionsFromItem(this))
            .offerTo(exporter, PARENT_BLOCK_ID.withSuffixedPath("_from_compressed"));
    }

    @Override
    public void configureBlockLootTables(RegistryWrapper.WrapperLookup registryLookup, BlockLootTableGenerator generator) {
        generator.addDrop(this);
    }

    @Override
    public void configureTranslations(RegistryWrapper.WrapperLookup registryLookup, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        translationBuilder.add(this, String.format("Compressed %s", materialName));
    }

    @Override
    public void configureBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleCubeAll(this);
    }

    @Override
    public void generateTextures() {
        TextureGenerator.getInstance().generate(Registries.BLOCK.getKey(), instance -> {
            final Optional<BufferedImage> source = instance.getImage(textureKey);

            if (source.isPresent()) {
                BufferedImage sourceImage = source.get();
                BufferedImage overlayImage = instance.getMinekeaImage(String.format("block/building/compressed/level-%d", compressionLevel)).orElse(null);

                int w = sourceImage.getWidth();
                int h = sourceImage.getHeight();

                BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                Graphics g = combined.getGraphics();
                g.drawImage(sourceImage, 0, 0, null);
                g.drawImage(overlayImage, 0, 0, w, h, null);

                g.dispose();

                instance.generate(BLOCK_ID, combined);
            }
        });
    }

//    @Override
//    public void setupResources() {
//        CompressedBlockSettings settings = (CompressedBlockSettings) this.settings;
//
//        MinekeaTags.addToolTag(settings.getTool(), getBlockID());
//
//        Identifier endTexture = settings.getBlockTexture("end");
//        Identifier sideTexture = settings.getBlockTexture("main");
//
//        Identifier MODEL_ID = Model.getBlockModelID(BLOCK_ID);
//        Identifier ITEM_MODEL_ID = Model.getItemModelID(BLOCK_ID);
//
//        JTextures textures = new JTextures()
//            .var("side", sideTexture.toString())
//            .var("end", endTexture.toString())
//            .var("overlay", String.format(ModInfo.MOD_ID + ":block/building/compressed/level-%d", settings.getCompressionLevel()));
//
//        MinekeaResourcePack.RESOURCE_PACK.addModel(JModel.model(ModInfo.MOD_ID + ":block/building/compressed_block").textures(textures), MODEL_ID);
//        MinekeaResourcePack.RESOURCE_PACK.addModel(JModel.model(MODEL_ID), ITEM_MODEL_ID);
//
//        if (settings.isColumn()) {
//            MinekeaResourcePack.RESOURCE_PACK.addBlockState(
//                JState.state(
//                    JState.variant()
//                        .put("axis=x", new JBlockModel(MODEL_ID).x(90).y(90))
//                        .put("axis=y", new JBlockModel(MODEL_ID))
//                        .put("axis=z", new JBlockModel(MODEL_ID).x(90))
//                ),
//                BLOCK_ID
//            );
//        } else {
//            MinekeaResourcePack.RESOURCE_PACK.addBlockState(
//                JState.state(JState.variant(new JBlockModel(MODEL_ID))),
//                BLOCK_ID
//            );
//        }
//    }
}
