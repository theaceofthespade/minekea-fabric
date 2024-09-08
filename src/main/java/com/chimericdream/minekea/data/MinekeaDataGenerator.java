package com.chimericdream.minekea.data;

import com.chimericdream.minekea.MinekeaMod;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class MinekeaDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(MinekeaModelGenerator::new);
        pack.addProvider(MinekeaEnglishLangProvider::new);
        pack.addProvider(MinekeaBlockLootTables::new);
        pack.addProvider(MinekeaBlockTagGenerator::new);
        pack.addProvider(MinekeaItemTagGenerator::new);
        pack.addProvider(MinekeaRecipeGenerator::new);
    }

    private static class MinekeaRecipeGenerator extends FabricRecipeProvider {
        public MinekeaRecipeGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        public void generate(RecipeExporter exporter) {
            MinekeaMod.BUILDING_BLOCKS.configureRecipes(exporter);
            MinekeaMod.CROPS.configureRecipes(exporter);
            MinekeaMod.FURNITURE_BLOCKS.configureRecipes(exporter);
            MinekeaMod.FLUIDS.configureRecipes(exporter);
            MinekeaMod.CONTAINER_BLOCKS.configureRecipes(exporter);
        }
    }

    private static class MinekeaBlockTagGenerator extends FabricTagProvider.BlockTagProvider {
        public MinekeaBlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            MinekeaMod.BUILDING_BLOCKS.configureBlockTags(arg, this::getOrCreateTagBuilder);
            MinekeaMod.CROPS.configureBlockTags(arg, this::getOrCreateTagBuilder);
            MinekeaMod.FURNITURE_BLOCKS.configureBlockTags(arg, this::getOrCreateTagBuilder);
            MinekeaMod.FLUIDS.configureBlockTags(arg, this::getOrCreateTagBuilder);
            MinekeaMod.CONTAINER_BLOCKS.configureBlockTags(arg, this::getOrCreateTagBuilder);
        }
    }

    private static class MinekeaItemTagGenerator extends FabricTagProvider.ItemTagProvider {
        public MinekeaItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            MinekeaMod.BUILDING_BLOCKS.configureItemTags(arg, this::getOrCreateTagBuilder);
            MinekeaMod.CROPS.configureItemTags(arg, this::getOrCreateTagBuilder);
            MinekeaMod.FURNITURE_BLOCKS.configureItemTags(arg, this::getOrCreateTagBuilder);
            MinekeaMod.FLUIDS.configureItemTags(arg, this::getOrCreateTagBuilder);
            MinekeaMod.CONTAINER_BLOCKS.configureItemTags(arg, this::getOrCreateTagBuilder);
        }
    }

    private static class MinekeaBlockLootTables extends FabricBlockLootTableProvider {
        protected MinekeaBlockLootTables(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generate() {
            MinekeaMod.BUILDING_BLOCKS.configureBlockLootTables(this.registryLookup, this);
            MinekeaMod.CROPS.configureBlockLootTables(this.registryLookup, this);
            MinekeaMod.FURNITURE_BLOCKS.configureBlockLootTables(this.registryLookup, this);
            MinekeaMod.FLUIDS.configureBlockLootTables(this.registryLookup, this);
            MinekeaMod.CONTAINER_BLOCKS.configureBlockLootTables(this.registryLookup, this);
        }
    }

    private static class MinekeaEnglishLangProvider extends FabricLanguageProvider {
        protected MinekeaEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
            MinekeaMod.BUILDING_BLOCKS.configureTranslations(registryLookup, translationBuilder);
            MinekeaMod.CROPS.configureTranslations(registryLookup, translationBuilder);
            MinekeaMod.FURNITURE_BLOCKS.configureTranslations(registryLookup, translationBuilder);
            MinekeaMod.FLUIDS.configureTranslations(registryLookup, translationBuilder);
            MinekeaMod.CONTAINER_BLOCKS.configureTranslations(registryLookup, translationBuilder);

            translationBuilder.add("item_group.minekea.blocks.building.beams", "Minekea: Beams");
            translationBuilder.add("item_group.minekea.blocks.building.compressed", "Minekea: Compressed Blocks");
            translationBuilder.add("item_group.minekea.blocks.building.covers", "Minekea: Covers");
            translationBuilder.add("item_group.minekea.blocks.furniture", "Minekea: Furniture");

//            translationBuilder.add(TOOLTIP_LEVEL, "%dx Compressed");
//            translationBuilder.add(TOOLTIP_COUNT, "(%s blocks)");
        }
    }

    private static class MinekeaModelGenerator extends FabricModelProvider {
        private MinekeaModelGenerator(FabricDataOutput generator) {
            super(generator);
        }

        @Override
        public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
            MinekeaMod.BUILDING_BLOCKS.configureBlockStateModels(blockStateModelGenerator);
            MinekeaMod.CROPS.configureBlockStateModels(blockStateModelGenerator);
            MinekeaMod.FURNITURE_BLOCKS.configureBlockStateModels(blockStateModelGenerator);
            MinekeaMod.FLUIDS.configureBlockStateModels(blockStateModelGenerator);
            MinekeaMod.CONTAINER_BLOCKS.configureBlockStateModels(blockStateModelGenerator);
        }

        @Override
        public void generateItemModels(ItemModelGenerator itemModelGenerator) {
            // ...
        }
    }
}

/*
        Identifier myEndStone = Identifier.of("minekea:end_stone");

        MinekeaResourcePack.RESOURCE_PACK.addModel(
            JModel.model("minecraft:block/cube_all")
                .textures(
                    new JTextures().var("all", "minekea:block/building/general/end_stone")
                ),
            Model.getBlockModelID(myEndStone)
        );

        MinekeaResourcePack.RESOURCE_PACK.addLootTable(
            LootTable.blockID(myEndStone),
            JLootTable.loot("minecraft:block")
                .pool(
                    JLootTable.pool()
                        .rolls(1)
                        .entry(
                            new JEntry()
                                .type("minecraft:alternatives")
                                .child(
                                    new JEntry()
                                        .type("minecraft:item")
                                        .name("minecraft:end_stone")
                                        .condition(
                                            new JCondition()
                                                .condition("minecraft:match_tool")
                                                .parameter("predicate", LootTable.silkTouchPredicate())
                                        )
                                )
                                .child(new JEntry().type("minecraft:item").name(COBBLED_END_STONE_BLOCK.getBlockID().toString()))
                        )
                        .condition(new JCondition().condition("minecraft:survives_explosion"))
                )
        );
 */