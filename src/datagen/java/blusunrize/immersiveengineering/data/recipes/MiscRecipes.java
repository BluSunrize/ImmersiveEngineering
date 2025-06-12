/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.api.energy.WindmillBiome;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.WarningSignBlock.WarningSignIcon;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import blusunrize.immersiveengineering.common.crafting.NoContainersShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.RGBColourationRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.BasicShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.crafting.fluidaware.ShapelessFluidAwareRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.register.*;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEItems.Metals;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.*;
import blusunrize.immersiveengineering.data.recipes.builder.BlueprintCraftingRecipeBuilder;
import blusunrize.immersiveengineering.data.recipes.builder.MineralMixBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.api.utils.TagUtils.createItemWrapper;
import static net.minecraft.data.recipes.SimpleCookingRecipeBuilder.smelting;

public class MiscRecipes extends IERecipeProvider
{

	public MiscRecipes(PackOutput p_248933_, CompletableFuture<Provider> provider)
	{
		super(p_248933_, provider);
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		recipesVanilla(out);
		recipesMisc(out);

		buildGeneratedList(out, rl("arc_recycling_list"));
		buildGeneratedList(out, rl("mixer_potion_list"));
		buildGeneratedList(out, rl("potion_bottling_list"));
		out.accept(
				toRL("curtain_colour"),
				new RGBColourationRecipe(Ingredient.of(Cloth.STRIP_CURTAIN)),
				null
		);

		recipesBlueprint(out);

		mineralMixes(out);

		out.accept(toRL("generator_fuel/biodiesel"), new GeneratorFuel(IETags.fluidBiodiesel, 250), null);
		out.accept(toRL("generator_fuel/high_power_biodiesel"), new GeneratorFuel(IETags.fluidHighPowerBiodiesel, 275), null);
		out.accept(toRL("generator_fuel/creosote"), new GeneratorFuel(IETags.fluidCreosote, 20), null);

		thermoelectricFuels(out);
		out.accept(toRL("windmill/ocean"), new WindmillBiome(BiomeTags.IS_OCEAN, 1.15f), null);
	}

	private void recipesBlueprint(RecipeOutput out)
	{
		BlueprintCraftingRecipeBuilder.builder()
				.category("components")
				.output(new ItemStack(Ingredients.COMPONENT_IRON))
				.input(IETags.getTagsFor(EnumMetals.IRON).plate, 2)
				.input(IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.build(out, toRL("blueprint/component_iron"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("components")
				.output(new ItemStack(Ingredients.COMPONENT_STEEL))
				.input(IETags.getTagsFor(EnumMetals.STEEL).plate, 2)
				.input(IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.build(out, toRL("blueprint/component_steel"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("components")
				.output(new ItemStack(Ingredients.COMPONENT_ELECTRONIC))
				.input(IETags.getItemTag(IETags.treatedWoodSlab))
				.input(Tags.Items.GEMS_QUARTZ)
				.input(Tags.Items.DUSTS_REDSTONE)
				.input(IETags.electrumWire)
				.build(out, toRL("blueprint/component_electronic"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("components")
				.output(new ItemStack(Ingredients.COMPONENT_ELECTRONIC_ADV))
				.input(IETags.plasticPlate)
				.input(Ingredients.ELECTRON_TUBE, 2)
				.input(IETags.aluminumWire)
				.build(out, toRL("blueprint/component_electronic_adv"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("components")
				.output(new ItemStack(Ingredients.LIGHT_BULB, 3))
				.input(Tags.Items.GLASS_BLOCKS)
				.input(Ingredient.fromValues(Stream.of(new Ingredient.TagValue(IETags.paper), new Ingredient.ItemValue(Items.BAMBOO.getDefaultInstance()))), 3)
				.input(IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.build(out, toRL("blueprint/light_bulb"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("components")
				.output(new ItemStack(Ingredients.ELECTRON_TUBE, 3))
				.input(Tags.Items.GLASS_BLOCKS)
				.input(IETags.getTagsFor(EnumMetals.NICKEL).plate)
				.input(IETags.copperWire)
				.input(Tags.Items.DUSTS_REDSTONE)
				.build(out, toRL("blueprint/electron_tube"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("components")
				.output(new ItemStack(Ingredients.CIRCUIT_BOARD))
				.input(IETags.plasticPlate)
				.input(IETags.getTagsFor(EnumMetals.COPPER).plate)
				.build(out, toRL("blueprint/circuit_board"));

		ItemLike[] molds = {Molds.MOLD_PLATE, Molds.MOLD_GEAR, Molds.MOLD_ROD, Molds.MOLD_BULLET_CASING, Molds.MOLD_WIRE, Molds.MOLD_PACKING_4, Molds.MOLD_PACKING_9, Molds.MOLD_UNPACKING};
		for(ItemLike mold : molds)
			BlueprintCraftingRecipeBuilder.builder()
					.category("molds")
					.output(mold.asItem())
					.input(IETags.getTagsFor(EnumMetals.STEEL).plate, 3)
					.input(Tools.WIRECUTTER)
					.build(out, toRL("blueprint/"+toPath(mold)));


		BlueprintCraftingRecipeBuilder.builder()
				.category("bullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.CASULL), 4))
				.input(BulletHandler.emptyCasing, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.LEAD).nugget)
				.build(out, toRL("blueprint/bullet_casull"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.ARMOR_PIERCING), 4))
				.input(BulletHandler.emptyCasing, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.STEEL).nugget)
				.input(IETags.getTagsFor(EnumMetals.CONSTANTAN).nugget, 2)
				.build(out, toRL("blueprint/bullet_armorpiercing"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.ARMOR_PIERCING), 4))
				.input(BulletHandler.emptyCasing, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.netheriteNugget, 3)
				.build(out, toRL("blueprint/bullet_armorpiercing_netherite"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.SILVER), 4))
				.input(BulletHandler.emptyCasing, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.LEAD).nugget)
				.input(IETags.getTagsFor(EnumMetals.SILVER).nugget)
				.build(out, toRL("blueprint/bullet_silver"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.BUCKSHOT), 4))
				.input(BulletHandler.emptyShell, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.STEEL).dust)
				.build(out, toRL("blueprint/bullet_buckshot"));

		BlueprintCraftingRecipeBuilder.builder()
				.category("specialBullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.HIGH_EXPLOSIVE), 4))
				.input(BulletHandler.emptyShell, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(Items.TNT)
				.build(out, toRL("blueprint/bullet_explosive"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("specialBullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.DRAGONS_BREATH), 4))
				.input(BulletHandler.emptyShell, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.ALUMINUM).dust, 2)
				.build(out, toRL("blueprint/bullet_dragonsbreath"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("specialBullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.POTION), 4))
				.input(BulletHandler.emptyShell, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(Items.GLASS_BOTTLE, 4)
				.build(out, toRL("blueprint/bullet_potion"));

		ItemStack flare = new ItemStack(BulletHandler.getBulletItem(IEBullets.FLARE), 4);
		DataComponentType<Color4> flareComponent = IEDataComponents.getBulletData(IEBullets.FLARE_TYPE);
		flare.set(flareComponent, Color4.fromRGB(0xcc2e06));
		BlueprintCraftingRecipeBuilder.builder()
				.category("specialBullet")
				.output(flare.copy())
				.input(BulletHandler.emptyShell, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.ALUMINUM).dust)
				.input(Tags.Items.DYES_RED)
				.build(out, toRL("blueprint/bullet_flare_red"));
		flare.set(flareComponent, Color4.fromRGB(0x2ca30b));
		BlueprintCraftingRecipeBuilder.builder()
				.category("specialBullet")
				.output(flare.copy())
				.input(BulletHandler.emptyShell, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.ALUMINUM).dust)
				.input(Tags.Items.DYES_GREEN)
				.build(out, toRL("blueprint/bullet_flare_green"));
		flare.set(flareComponent, Color4.fromRGB(0xffff82));
		BlueprintCraftingRecipeBuilder.builder()
				.category("specialBullet")
				.output(flare.copy())
				.input(BulletHandler.emptyShell, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.ALUMINUM).dust)
				.input(Tags.Items.DYES_YELLOW)
				.build(out, toRL("blueprint/bullet_flare_yellow"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("specialBullet")
				.output(new ItemStack(BulletHandler.getBulletItem(IEBullets.HOMING), 4))
				.input(BulletHandler.emptyCasing, 4)
				.input(Tags.Items.GUNPOWDERS)
				.input(IETags.getTagsFor(EnumMetals.LEAD).nugget)
				.input(Items.ENDER_EYE)
				.build(out, toRL("blueprint/bullet_homing"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("specialBullet")
				.output(BulletHandler.getBulletItem(IEBullets.WOLFPACK))
				.input(BulletHandler.emptyShell)
				.input(Tags.Items.GUNPOWDERS)
				.input(BulletHandler.getBulletItem(IEBullets.HOMING), 4)
				.build(out, toRL("blueprint/bullet_wolfpack"));

		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.HAMMER.item())
				.input(IETags.paper)
				.input(Tools.HAMMER)
				.build(out, toRL("blueprint/banner_hammer"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.WIRECUTTER.item())
				.input(IETags.paper)
				.input(Tools.WIRECUTTER)
				.build(out, toRL("blueprint/banner_wirecutter"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.SCREWDRIVER.item())
				.input(IETags.paper)
				.input(Tools.SCREWDRIVER)
				.build(out, toRL("blueprint/banner_screwdriver"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.BEVELS.item())
				.input(IETags.paper)
				.input(IETags.plates)
				.build(out, toRL("blueprint/banner_bevels"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.ORNATE.item())
				.input(IETags.paper)
				.input(IETags.getTagsFor(EnumMetals.SILVER).dust)
				.build(out, toRL("blueprint/banner_ornate"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.TREATED_WOOD.item())
				.input(IETags.paper)
				.input(IETags.getItemTag(IETags.treatedWood))
				.build(out, toRL("blueprint/banner_treatedwood"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.WINDMILL.item())
				.input(IETags.paper)
				.input(WoodenDevices.WINDMILL)
				.build(out, toRL("blueprint/banner_windmill"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.WARNING.item())
				.input(IETags.paper)
				.input(Ingredient.fromValues(MetalDecoration.WARNING_SIGNS.values().stream().map(b -> new Ingredient.ItemValue(new ItemStack(b)))))
				.build(out, toRL("blueprint/banner_warning"));
		BlueprintCraftingRecipeBuilder.builder()
				.category("bannerpatterns")
				.output(IEBannerPatterns.WOLF.item())
				.input(IETags.paper)
				.input(BulletHandler.getBulletItem(IEBullets.WOLFPACK))
				.build(out, toRL("blueprint/banner_wolf"));

		for(WarningSignIcon icon : WarningSignIcon.values())
			BlueprintCraftingRecipeBuilder.builder()
					.category("warning_sign")
					.output(MetalDecoration.WARNING_SIGNS.get(icon))
					.input(IETags.plates)
					.input(Tags.Items.DYES_YELLOW)
					.build(out, toRL("blueprint/"+toPath(MetalDecoration.WARNING_SIGNS.get(icon))));

		BlueprintCraftingRecipeBuilder.builder()
				.category("automatons")
				.output(SpawnEggs.ROBOT_WOLF)
				.input(MetalDevices.THERMOELECTRIC_GEN)
				.input(MetalDecoration.RADIATOR)
				.input(IETags.getTagsFor(EnumMetals.URANIUM).ingot, 3)
				.input(IETags.getTagsFor(EnumMetals.STEEL).plate, 2)
				.input(Ingredients.COMPONENT_ELECTRONIC_ADV)
				.input(Ingredients.COMPONENT_STEEL, 2)
				.build(out, toRL("blueprint/"+toPath(SpawnEggs.ROBOT_WOLF)));
	}

	private void mineralMixes(RecipeOutput out)
	{
		// Metals
		TagKey<Item> iron = Tags.Items.ORES_IRON;
		TagKey<Item> gold = Tags.Items.ORES_GOLD;
		TagKey<Item> copper = Tags.Items.ORES_COPPER;
		TagKey<Item> aluminum = IETags.getItemTag(IETags.getTagsFor(EnumMetals.ALUMINUM).ore);
		TagKey<Item> lead = IETags.getItemTag(IETags.getTagsFor(EnumMetals.LEAD).ore);
		TagKey<Item> silver = IETags.getItemTag(IETags.getTagsFor(EnumMetals.SILVER).ore);
		TagKey<Item> nickel = IETags.getItemTag(IETags.getTagsFor(EnumMetals.NICKEL).ore);
		TagKey<Item> uranium = IETags.getItemTag(IETags.getTagsFor(EnumMetals.URANIUM).ore);
		TagKey<Item> tin = createItemWrapper(IETags.getOre("tin"));
		TagKey<Item> titanium = createItemWrapper(IETags.getOre("titanium"));
		TagKey<Item> thorium = createItemWrapper(IETags.getOre("thorium"));
		TagKey<Item> tungsten = createItemWrapper(IETags.getOre("tungsten"));
		TagKey<Item> manganese = createItemWrapper(IETags.getOre("manganese"));
		TagKey<Item> platinum = createItemWrapper(IETags.getOre("platinum"));
		TagKey<Item> osmium = createItemWrapper(IETags.getOre("osmium"));
		TagKey<Item> mercury = createItemWrapper(IETags.getOre("mercury"));
		// Gems & Dusts
		TagKey<Item> sulfur = IETags.sulfurDust;
		TagKey<Item> phosphorus = createItemWrapper(IETags.getDust("phosphorus"));
		TagKey<Item> redstone = Tags.Items.ORES_REDSTONE;
		TagKey<Item> emerald = Tags.Items.ORES_EMERALD;
		Block prismarine = Blocks.PRISMARINE;
		TagKey<Item> aquamarine = createItemWrapper(IETags.getGem("aquamarine"));

		// Rocks & decoration
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.biomeCondition(BiomeTags.IS_RIVER, IETags.is_swamp, BiomeTags.IS_OCEAN)
				.addSoilSpoils()
				.ore(Items.CLAY, .6f)
				.ore(Items.SAND, .4f)
				.weight(25)
				.failchance(.2f)
				.build(out, toRL("mineral/silt"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(Blocks.GRANITE, .3f)
				.ore(Blocks.DIORITE, .3f)
				.ore(Blocks.ANDESITE, .3f)
				.ore(Blocks.OBSIDIAN, .1f)
				.weight(25)
				.failchance(.05f)
				.build(out, toRL("mineral/igneous_rock"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.biomeCondition(IETags.generateClaypan)
				.addSoilSpoils()
				.ore(Items.TERRACOTTA, .6f)
				.ore(Items.RED_SANDSTONE, .3f)
				.ore(Items.RED_SAND, .1f)
				.weight(15)
				.failchance(.05f)
				.build(out, toRL("mineral/hardened_clay_pan"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.biomeCondition(IETags.generateSeabed)
				.addSeabedSpoils()
				.ore(Blocks.CALCITE, .65f)
				.ore(Blocks.DRIPSTONE_BLOCK, .3f)
				.ore(Blocks.BONE_BLOCK, .05f)
				.weight(15)
				.failchance(.05f)
				.build(out, toRL("mineral/ancient_seabed"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(Blocks.AMETHYST_BLOCK, .4f)
				.ore(Blocks.CALCITE, .3f)
				.ore(Blocks.SMOOTH_BASALT, .3f)
				.weight(10)
				.failchance(.1f)
				.build(out, toRL("mineral/amethyst_crevasse"));

		// Core resources
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(Tags.Items.ORES_COAL, .8f)
				.ore(sulfur, .2f)
				.ore(phosphorus, .2f, getTagCondition(phosphorus))
				.weight(25)
				.failchance(.05f)
				.build(out, toRL("mineral/bituminous_coal"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(iron, .8f)
				.ore(Blocks.DRIPSTONE_BLOCK, .2f)
				.weight(25)
				.failchance(.05f)
				.build(out, toRL("mineral/banded_iron"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.biomeCondition(BiomeTags.IS_MOUNTAIN)
				.addOverworldSpoils()
				.ore(iron, .5f)
				.ore(copper, .45f)
				.ore(sulfur, .05f)
				.weight(25)
				.failchance(.05f)
				.build(out, toRL("mineral/chalcopyrite"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(redstone, .6f)
				.ore(sulfur, .4f)
				.ore(mercury, .3f, getTagCondition(mercury))
				.weight(20)
				.failchance(.1f)
				.build(out, toRL("mineral/cinnabar"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(copper, .7f)
				.ore(gold, .3f)
				.weight(20)
				.failchance(.1f)
				.build(out, toRL("mineral/auricupride"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.biomeCondition(BiomeTags.IS_BADLANDS)
				.addOverworldSpoils()
				.ore(gold, .6f)
				.ore(copper, .4f)
				.weight(20)
				.failchance(.1f)
				.build(out, toRL("mineral/rich_auricupride"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(aluminum, .6f)
				.ore(iron, .3f)
				.ore(nickel, .1f)
				.ore(titanium, .1f, getTagCondition(titanium))
				.weight(20)
				.failchance(.05f)
				.build(out, toRL("mineral/laterite"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(lead, .4f)
				.ore(silver, .25f)
				.ore(gold, .25f)
				.ore(sulfur, .1f)
				.weight(20)
				.failchance(.05f)
				.build(out, toRL("mineral/galena"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(nickel, .65f)
				.ore(iron, .25f)
				.ore(sulfur, .1f)
				.ore(platinum, .1f, getTagCondition(platinum))
				.ore(osmium, .1f, getTagCondition(osmium))
				.weight(15)
				.failchance(.1f)
				.build(out, toRL("mineral/pentlandite"));

		// Rare resources
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(Tags.Items.ORES_LAPIS, .75f)
				.ore(gold, .15f)
				.ore(sulfur, .1f)
				.weight(15)
				.failchance(.1f)
				.build(out, toRL("mineral/lazulitic_intrusion"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.biomeCondition(BiomeTags.IS_MOUNTAIN, BiomeTags.IS_OCEAN)
				.addOverworldSpoils()
				.ore(emerald, .3f)
				.ore(prismarine, .7f)
				.ore(aquamarine, .3f, getTagCondition(aquamarine))
				.weight(15)
				.failchance(.2f)
				.build(out, toRL("mineral/beryl"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.biomeCondition(BiomeTags.IS_RIVER)
				.addSoilSpoils()
				.ore(Tags.Items.GEMS_DIAMOND, .2f)
				.ore(Items.CLAY, .4f)
				.ore(Items.SAND, .4f)
				.weight(15)
				.failchance(.2f)
				.build(out, toRL("mineral/alluvial_sift"));
		MineralMixBuilder.builder()
				.dimensionOverworld()
				.biomeCondition(BiomeTags.IS_BADLANDS, BiomeTags.IS_TAIGA)
				.addOverworldSpoils()
				.ore(uranium, .6f)
				.ore(lead, .4f)
				.ore(thorium, .1f, getTagCondition(thorium))
				.weight(15)
				.failchance(.15f)
				.build(out, toRL("mineral/uraninite"));
		MineralMixBuilder.builder()
				.addCondition(getTagCondition(tungsten)) // Vein is only found when tungsten is present
				.dimensionOverworld()
				.addOverworldSpoils()
				.ore(tungsten, .7f)
				.ore(iron, .3f)
				.ore(manganese, .3f, getTagCondition(manganese))
				.ore(tin, .3f, getTagCondition(tin))
				.weight(5)
				.failchance(.15f)
				.build(out, toRL("mineral/wolframite"));

		// Nether
		MineralMixBuilder.builder()
				.dimensionNether()
				.addNetherSpoils()
				.ore(Blocks.NETHER_QUARTZ_ORE, .6f)
				.ore(Blocks.NETHER_GOLD_ORE, .2f)
				.ore(sulfur, .2f)
				.weight(20)
				.failchance(.15f)
				.background(Blocks.NETHERRACK)
				.build(out, toRL("mineral/mephitic_quarzite"));
		MineralMixBuilder.builder()
				.dimensionNether()
				.addNetherSpoils()
				.ore(Blocks.POLISHED_BLACKSTONE_BRICKS, .4f)
				.ore(Blocks.POLISHED_BLACKSTONE, .3f)
				.ore(Blocks.ANCIENT_DEBRIS, .2f)
				.ore(Blocks.GILDED_BLACKSTONE, .1f)
				.weight(8)
				.failchance(.5f)
				.background(Blocks.POLISHED_BLACKSTONE)
				.build(out, toRL("mineral/ancient_debris"));
		MineralMixBuilder.builder()
				.dimensionNether()
				.addNetherSpoils()
				.ore(Items.SOUL_SOIL, .5f)
				.ore(Items.SOUL_SAND, .3f)
				.ore(Items.GRAVEL, .2f)
				.weight(15)
				.failchance(.05f)
				.background(Blocks.SOUL_SOIL)
				.build(out, toRL("mineral/nether_silt"));
		MineralMixBuilder.builder()
				.dimensionNether()
				.addNetherSpoils()
				.ore(Items.MAGMA_BLOCK, .5f)
				.ore(Items.SMOOTH_BASALT, .3f)
				.ore(Items.OBSIDIAN, .2f)
				.weight(15)
				.failchance(.05f)
				.background(Blocks.NETHERRACK)
				.build(out, toRL("mineral/cooled_lava_tube"));
	}

	private void thermoelectricFuels(RecipeOutput out)
	{
		out.accept(toRL("thermoelectric/magma"), new ThermoelectricSource(Blocks.MAGMA_BLOCK, 1300), null);
		out.accept(toRL("thermoelectric/snow"), new ThermoelectricSource(BlockTags.SNOW, 273), null);
		out.accept(toRL("thermoelectric/ice"), new ThermoelectricSource(Blocks.ICE, 260), null);
		out.accept(toRL("thermoelectric/packed_ice"), new ThermoelectricSource(Blocks.PACKED_ICE, 240), null);
		out.accept(toRL("thermoelectric/blue_ice"), new ThermoelectricSource(Blocks.BLUE_ICE, 200), null);
		out.accept(
				toRL("thermoelectric/uranium"),
				new ThermoelectricSource(IETags.getTagsFor(EnumMetals.URANIUM).storage, 2000),
				null
		);
	}

	private void recipesVanilla(RecipeOutput out)
	{
		shapedMisc(Items.TORCH, 12)
				.pattern("wc ")
				.pattern("sss")
				.define('w', ItemTags.WOOL)
				.define('c', new Ingredient(new IngredientFluidStack(IETags.fluidCreosote, FluidType.BUCKET_VOLUME)))
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_wool", has(ItemTags.WOOL))
				.unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
				.unlockedBy("has_creosote", has(IEFluids.CREOSOTE.getBucket()))
				.save(
						new WrappingRecipeOutput<>(out, BasicShapedRecipe::new),
						toRL(toPath(Items.TORCH))
				);
		shapelessMisc(Items.STRING)
				.requires(Ingredient.of(IETags.fiberHemp), 3)
				.unlockedBy("has_hemp_fiber", has(Ingredients.HEMP_FIBER))
				.save(out, toRL(toPath(Items.STRING)));
		shapelessMisc(Items.GUNPOWDER, 6)
				.requires(Ingredient.of(IETags.saltpeterDust), 4)
				.requires(IETags.sulfurDust)
				.requires(Items.CHARCOAL)
				.unlockedBy("has_sulfur", has(IETags.sulfurDust))
				.save(out, toRL("gunpowder_from_dusts"));
		shapelessMisc(Items.PAPER, 2)
				.requires(Ingredient.of(IETags.sawdust), 4)
				.requires(new Ingredient(new IngredientFluidStack(FluidTags.WATER, FluidType.BUCKET_VOLUME)))
				.unlockedBy("has_sawdust", has(IETags.sawdust))
				.save(
						new WrappingRecipeOutput<ShapelessRecipe>(out, ShapelessFluidAwareRecipe::new),
						toRL("paper_from_sawdust")
				);
	}

	private void recipesMisc(RecipeOutput out)
	{
		ItemLike wireCoilCopper = Misc.WIRE_COILS.get(WireType.COPPER);
		shapedMisc(wireCoilCopper, 4)
				.pattern(" w ")
				.pattern("wsw")
				.pattern(" w ")
				.define('w', IETags.copperWire)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(wireCoilCopper)));
		ItemLike wireCoilElectrum = Misc.WIRE_COILS.get(WireType.ELECTRUM);
		shapedMisc(wireCoilElectrum, 4)
				.pattern(" w ")
				.pattern("wsw")
				.pattern(" w ")
				.define('w', IETags.electrumWire)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.save(out, toRL(toPath(wireCoilElectrum)));
		ItemLike wireCoilSteel = Misc.WIRE_COILS.get(WireType.STEEL);
		shapedMisc(wireCoilSteel, 4)
				.pattern(" w ")
				.pattern("asa")
				.pattern(" w ")
				.define('w', IETags.steelWire)
				.define('a', IETags.aluminumWire)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(
						new WrappingRecipeOutput<ShapedRecipe>(
								out, r -> new TurnAndCopyRecipe(r).allowQuarterTurn()
						),
						toRL(toPath(wireCoilSteel))
				);

		ItemLike wireCoilRope = Misc.WIRE_COILS.get(WireType.STRUCTURE_ROPE);
		shapedMisc(wireCoilRope, 4)
				.pattern(" w ")
				.pattern("wsw")
				.pattern(" w ")
				.define('w', Ingredients.HEMP_FIBER)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_hemp_fiber", has(Ingredients.HEMP_FIBER))
				.save(out, toRL(toPath(wireCoilRope)));
		ItemLike wireCoilStructure = Misc.WIRE_COILS.get(WireType.STRUCTURE_STEEL);
		shapedMisc(wireCoilStructure, 4)
				.pattern(" w ")
				.pattern("wsw")
				.pattern(" w ")
				.define('w', Ingredients.WIRE_STEEL)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(wireCoilStructure)));
		addCornerStraightMiddle(Misc.WIRE_COILS.get(WireType.COPPER_INSULATED), 4,
				makeIngredient(IETags.fabricHemp),
				makeIngredient(Misc.WIRE_COILS.get(WireType.COPPER)),
				makeIngredient(IETags.fabricHemp),
				has(Misc.WIRE_COILS.get(WireType.COPPER)), out);
		addCornerStraightMiddle(Misc.WIRE_COILS.get(WireType.ELECTRUM_INSULATED), 4,
				makeIngredient(IETags.fabricHemp),
				makeIngredient(Misc.WIRE_COILS.get(WireType.ELECTRUM)),
				makeIngredient(IETags.fabricHemp),
				has(Misc.WIRE_COILS.get(WireType.ELECTRUM)), out);
		ItemLike wireCoilRedstone = Misc.WIRE_COILS.get(WireType.REDSTONE);
		shapedMisc(wireCoilRedstone, 4)
				.pattern(" w ")
				.pattern("asa")
				.pattern(" w ")
				.define('w', IETags.aluminumWire)
				.define('a', Tags.Items.DUSTS_REDSTONE)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.save(
						new WrappingRecipeOutput<ShapedRecipe>(out, r -> new TurnAndCopyRecipe(r).allowQuarterTurn()),
						toRL(toPath(wireCoilRedstone))
				);

		shapelessMisc(wireCoilCopper)
				.requires(Misc.WIRE_COILS.get(WireType.COPPER_INSULATED))
				.requires(Tools.WIRECUTTER)
				.unlockedBy("has_copper_coil", has(Misc.WIRE_COILS.get(WireType.COPPER)))
				.save(out, toRL("strip_lv"));
		shapelessMisc(wireCoilElectrum)
				.requires(Misc.WIRE_COILS.get(WireType.ELECTRUM_INSULATED))
				.requires(Tools.WIRECUTTER)
				.unlockedBy("has_electrum_coil", has(Misc.WIRE_COILS.get(WireType.ELECTRUM)))
				.save(out, toRL("strip_mv"));

		shapedMisc(Misc.JERRYCAN)
				.pattern(" ii")
				.pattern("ibb")
				.pattern("ibb")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('b', Items.BUCKET)
				.unlockedBy("has_bucket", has(Items.BUCKET))
				.save(out, toRL("jerrycan"));
		shapedMisc(Misc.POWERPACK)
				.pattern("srs")
				.pattern("clc")
				.pattern("wrw")
				.define('s', IETags.treatedStick)
				.define('l', Tags.Items.LEATHERS)
				.define('r', IETags.steelRod)
				.define('w', Misc.WIRE_COILS.get(WireType.COPPER_INSULATED))
				.define('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.unlockedBy("has_leather", has(Items.LEATHER))
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_"+toPath(MetalDevices.CAPACITOR_LV), has(MetalDevices.CAPACITOR_LV))
				.unlockedBy("has_"+toPath(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)), has(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)))
				.save(out, toRL(toPath(Misc.POWERPACK)));
		shapedMisc(Misc.MAINTENANCE_KIT)
				.pattern("sc ")
				.pattern("fff")
				.define('c', Tools.WIRECUTTER)
				.define('s', Tools.SCREWDRIVER)
				.define('f', IETags.fabricHemp)
				.unlockedBy("has_"+toPath(Tools.WIRECUTTER), has(Tools.WIRECUTTER))
				.save(
						new WrappingRecipeOutput<ShapedRecipe>(out, NoContainersShapedRecipe::new),
						toRL(toPath(Misc.MAINTENANCE_KIT))
				);
		shapedMisc(Misc.SHIELD)
				.pattern("sws")
				.pattern("scs")
				.pattern("sws")
				.define('s', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('c', Items.SHIELD)
				.unlockedBy("has_shield", has(Items.SHIELD))
				.save(out, toRL(toPath(Misc.SHIELD)));
		shapedMisc(Misc.FLUORESCENT_TUBE)
				.pattern("GeG")
				.pattern("GgG")
				.pattern("GgG")
				.define('g', Tags.Items.DUSTS_GLOWSTONE)
				.define('e', Misc.GRAPHITE_ELECTRODE)
				.define('G', Tags.Items.GLASS_BLOCKS)
				.unlockedBy("has_electrode", has(Misc.GRAPHITE_ELECTRODE))
				.save(out, toRL(toPath(Misc.FLUORESCENT_TUBE)));
		shapedMisc(Misc.EARMUFFS)
				.pattern(" S ")
				.pattern("S S")
				.pattern("W W")
				.define('S', IETags.ironRod)
				.define('W', ItemTags.WOOL)
				.unlockedBy("has_iron_rod", has(IETags.ironRod))
				.save(out, toRL(toPath(Misc.EARMUFFS)));
		shapelessMisc(Misc.FERTILIZER, 3)
				.requires(IETags.saltpeterDust)
				.requires(IETags.slag)
				.requires(IETags.sulfurDust)
				.requires(new Ingredient(new IngredientFluidStack(FluidTags.WATER, FluidType.BUCKET_VOLUME)))
				.unlockedBy("has_saltpeter", has(IETags.saltpeterDust))
				.unlockedBy("has_sulfur", has(IETags.sulfurDust))
				.unlockedBy("has_slag", has(IETags.slag))
				.save(out, toRL(toPath(Misc.FERTILIZER)));

		shapedMisc(Minecarts.CART_WOODEN_CRATE)
				.pattern("B")
				.pattern("C")
				.define('B', WoodenDevices.CRATE)
				.define('C', Items.MINECART)
				.unlockedBy("has_minecart", has(Items.MINECART))
				.save(out, toRL(toPath(Minecarts.CART_WOODEN_CRATE)));
		shapedMisc(Minecarts.CART_REINFORCED_CRATE)
				.pattern("B")
				.pattern("C")
				.define('B', WoodenDevices.REINFORCED_CRATE)
				.define('C', Items.MINECART)
				.unlockedBy("has_minecart", has(Items.MINECART))
				.save(out, toRL(toPath(Minecarts.CART_REINFORCED_CRATE)));
		shapedMisc(Minecarts.CART_WOODEN_BARREL)
				.pattern("B")
				.pattern("C")
				.define('B', WoodenDevices.WOODEN_BARREL)
				.define('C', Items.MINECART)
				.unlockedBy("has_minecart", has(Items.MINECART))
				.save(out, toRL(toPath(Minecarts.CART_WOODEN_BARREL)));
		shapedMisc(Minecarts.CART_METAL_BARREL)
				.pattern("B")
				.pattern("C")
				.define('B', MetalDevices.BARREL)
				.define('C', Items.MINECART)
				.unlockedBy("has_minecart", has(Items.MINECART))
				.save(out, toRL(toPath(Minecarts.CART_METAL_BARREL)));
		shapelessMisc(StoneDecoration.GRIT_SAND, 5)
				.requires(Ingredient.of(Tags.Items.GRAVELS), 1)
				.requires(Ingredient.of(Tags.Items.SANDS_COLORLESS), 4)
				.unlockedBy("has_sand", has(Tags.Items.GRAVELS))
				.unlockedBy("has_gravel", has(Tags.Items.SANDS_COLORLESS))
				.save(out, toRL("grit_sand"));

		//Lead to dye recipes
		shapelessMisc(Items.WHITE_DYE, 16)
				.requires(new Ingredient(new IngredientFluidStack(IETags.fluidEthanol, FluidType.BUCKET_VOLUME)))
				.requires(Ingredient.of(Items.ROTTEN_FLESH), 3)
				.requires(IETags.getTagsFor(EnumMetals.LEAD).dust)
				.unlockedBy("has_ethanol", has(IEFluids.ETHANOL.getBucket()))
				.save(out, toRL("lead_white"));
		smelting(Ingredient.of(IETags.getTagsFor(EnumMetals.LEAD).nugget), RecipeCategory.MISC, Items.RED_DYE, 0.1f, standardSmeltingTime)
				.unlockedBy("has_lead", has(Metals.INGOTS.get(EnumMetals.LEAD)))
				.save(out, toRL("smelting/lead_red"));

		//Lead glass recipes
		shapedMisc(Items.TINTED_GLASS, 3)
				.pattern("LAL")
				.pattern("AGA")
				.pattern("LAL")
				.define('L', Ingredients.WIRE_LEAD)
				.define('A', Items.AMETHYST_SHARD)
				.define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
				.unlockedBy("has_amethyst", has(Items.AMETHYST_SHARD))
				.unlockedBy("has_lead", has(Metals.INGOTS.get(EnumMetals.LEAD)))
				.save(out, toRL("tinted_glass_lead_wire"));
	}

	private void buildGeneratedList(RecipeOutput out, ResourceLocation name)
	{
		out.accept(name, GeneratedListRecipe.from(name), null);
	}
}
