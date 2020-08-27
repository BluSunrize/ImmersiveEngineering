/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction.ClocheRenderReference;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.*;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.crafting.HammerCrushingRecipeBuilder;
import blusunrize.immersiveengineering.common.crafting.IEConfigConditionSerializer.ConditionIEConfig;
import blusunrize.immersiveengineering.common.crafting.IngredientFluidStack;
import blusunrize.immersiveengineering.common.crafting.RevolverAssemblyRecipeBuilder;
import blusunrize.immersiveengineering.common.crafting.TurnAndCopyRecipeBuilder;
import blusunrize.immersiveengineering.common.data.resources.RecipeMetals;
import blusunrize.immersiveengineering.common.data.resources.RecipeMetals.AlloyProperties;
import blusunrize.immersiveengineering.common.data.resources.RecipeOres;
import blusunrize.immersiveengineering.common.data.resources.SecondaryOutput;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.*;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.data.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class Recipes extends RecipeProvider
{
	private final Path ADV_ROOT;
	private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

	private final int standardSmeltingTime = 200;
	private final int blastDivider = 2;

	public Recipes(DataGenerator gen)
	{
		super(gen);
		ADV_ROOT = gen.getOutputFolder().resolve("data/minecraft/advancements/recipes/root.json");
	}

	@Override
	protected void saveRecipeAdvancement(DirectoryCache cache, JsonObject json, Path path)
	{
		if(path.equals(ADV_ROOT)) return; //We NEVER care about this.
		super.saveRecipeAdvancement(cache, json, path);
	}

	@Override
	protected void registerRecipes(@Nonnull Consumer<IFinishedRecipe> out)
	{
		for(EnumMetals metal : EnumMetals.values())
		{
			IETags.MetalTags tags = IETags.getTagsFor(metal);

			Item nugget = Metals.nuggets.get(metal);
			Item ingot = Metals.ingots.get(metal);
			Item plate = Metals.plates.get(metal);
			Item dust = Metals.dusts.get(metal);
			Block block = IEBlocks.Metals.storage.get(metal);
			Block sheetMetal = IEBlocks.Metals.sheetmetal.get(metal);
			if(!metal.isVanillaMetal())
			{
				add3x3Conversion(ingot, nugget, tags.nugget, out);
				add3x3Conversion(block, ingot, tags.ingot, out);
				if(IEBlocks.Metals.ores.containsKey(metal))
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					addStandardSmeltingBlastingRecipe(ore, ingot, metal.smeltingXP, out);
				}
			}
			addStandardSmeltingBlastingRecipe(dust, ingot, 0, out, "_from_dust");
//			addStandardSmeltingBlastingRecipe(dust, ingot, metal.smeltingXP, out, "_from_dust"); //TODO: remove this, if 0 XP on dust is intentional. this bugs out because the alloys do not have metal.smeltingXP
			ShapelessRecipeBuilder.shapelessRecipe(plate)
					.addIngredient(IETags.getTagsFor(metal).ingot)
					.addIngredient(Tools.hammer)
					.addCriterion("has_"+metal.tagName()+"_ingot", hasItem(IETags.getTagsFor(metal).ingot))
					.build(out, toRL("plate_"+metal.tagName()+"_hammering"));
			ShapedRecipeBuilder.shapedRecipe(sheetMetal, 4)
					.patternLine(" p ")
					.patternLine("p p")
					.patternLine(" p ")
					.key('p', IETags.getTagsFor(metal).plate)
					.addCriterion("has_"+toPath(plate), hasItem(plate))
					.build(out, toRL(toPath(sheetMetal)));
		}
		addStandardSmeltingBlastingRecipe(IEItems.Ingredients.dustHopGraphite, Ingredients.ingotHopGraphite, 0.5F, out);

		addStandardSmeltingBlastingRecipe(Tools.steelAxe, Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_axe");
		addStandardSmeltingBlastingRecipe(Tools.steelPick, Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_pick");
		addStandardSmeltingBlastingRecipe(Tools.steelShovel, Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_shovel");
		addStandardSmeltingBlastingRecipe(Tools.steelSword, Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_sword");

		for(EquipmentSlotType slot : EquipmentSlotType.values())
			if(slot.getSlotType()==Group.ARMOR)
			{
				addStandardSmeltingBlastingRecipe(Tools.steelArmor.get(slot), Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_steel_"+slot.getName());
				addStandardSmeltingBlastingRecipe(Misc.faradaySuit.get(slot), Metals.nuggets.get(EnumMetals.ALUMINUM), 0.1F, out, "_recycle_faraday_"+slot.getName());
			}

		for(Entry<Block, SlabBlock> blockSlab : IEBlocks.toSlab.entrySet())
			addSlab(blockSlab.getKey(), blockSlab.getValue(), out);

		recipesStoneDecorations(out);
		recipesWoodenDecorations(out);
		recipesWoodenDevices(out);
		recipesMetalDecorations(out);
		recipesMetalDevices(out);
		recipesConnectors(out);
		recipesConveyors(out);
		recipesCloth(out);

		recipesTools(out);
		recipesIngredients(out);
		recipesVanilla(out);
		recipesWeapons(out);
		recipesMisc(out);

		CustomRecipeBuilder.func_218656_a(RecipeSerializers.SPEEDLOADER_LOAD.get())
				.build(out, ImmersiveEngineering.MODID+":speedloader_load");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.FLARE_BULLET_COLOR.get())
				.build(out, ImmersiveEngineering.MODID+":flare_bullet_color");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.POTION_BULLET_FILL.get())
				.build(out, ImmersiveEngineering.MODID+":potion_bullet_fill");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.POWERPACK_SERIALIZER.get())
				.build(out, ImmersiveEngineering.MODID+":powerpack_attach");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.EARMUFF_SERIALIZER.get())
				.build(out, ImmersiveEngineering.MODID+":earmuffs_attach");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.JERRYCAN_REFILL.get())
				.build(out, ImmersiveEngineering.MODID+":jerrycan_refill");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.REVOLVER_CYCLE_SERIALIZER.get())
				.build(out, ImmersiveEngineering.MODID+":revolver_cycle");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.IE_REPAIR.get())
				.build(out, ImmersiveEngineering.MODID+":ie_item_repair");
		addRGBRecipe(out, toRL("curtain_colour"), Ingredient.fromItems(Cloth.curtain), "colour");

		recipesBlast(out);
		recipesCoke(out);
		recipesCloche(out);
		recipesBlueprint(out);
		recipesMultiblockMachines(out);

		mineralMixes(out);
	}

	private void recipesBlast(@Nonnull Consumer<IFinishedRecipe> out)
	{
		BlastFurnaceFuelBuilder.builder(IETags.coalCoke)
				.setTime(1200)
				.build(out, toRL("blastfurnace/fuel_coke"));
		BlastFurnaceFuelBuilder.builder(IETags.getItemTag(IETags.coalCokeBlock))
				.setTime(10*1200)
				.build(out, toRL("blastfurnace/fuel_coke_block"));

		BlastFurnaceFuelBuilder.builder(IETags.charCoal)
				.setTime(300)
				.build(out, toRL("blastfurnace/fuel_charcoal"));
		BlastFurnaceFuelBuilder.builder(IETags.getItemTag(IETags.charCoalBlocks))
				.addCondition(new NotCondition(new TagEmptyCondition(IETags.charCoalBlocks.getId())))
				.setTime(10*300)
				.build(out, toRL("blastfurnace/fuel_charcoal_block"));

		BlastFurnaceRecipeBuilder.builder(IETags.getTagsFor(EnumMetals.STEEL).ingot, 1)
				.addInput(Tags.Items.INGOTS_IRON)
				.addSlag(IETags.slag, 1)
				.setTime(1200)
				.build(out, toRL("blastfurnace/steel"));

		BlastFurnaceRecipeBuilder.builder(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage), 1)
				.addInput(Tags.Items.STORAGE_BLOCKS_IRON)
				.addSlag(IETags.slag, 9)
				.setTime(9*1200)
				.build(out, toRL("blastfurnace/steel_block"));
	}

	private void recipesCoke(@Nonnull Consumer<IFinishedRecipe> out)
	{
		CokeOvenRecipeBuilder.builder(IETags.coalCoke, 1)
				.addInput(Items.COAL)
				.setOil(500)
				.setTime(1800)
				.build(out, toRL("cokeoven/coke"));
		CokeOvenRecipeBuilder.builder(IETags.getItemTag(IETags.coalCokeBlock), 1)
				.addInput(Blocks.COAL_BLOCK)
				.setOil(5000)
				.setTime(9*1800)
				.build(out, toRL("cokeoven/coke_block"));
		CokeOvenRecipeBuilder.builder(Items.CHARCOAL)
				.addInput(ItemTags.LOGS)
				.setOil(250)
				.setTime(900)
				.build(out, toRL("cokeoven/charcoal"));
	}

	private void recipesCloche(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ClocheFertilizerBuilder.builder(1.25f)
				.addInput(Items.BONE_MEAL)
				.build(out, toRL("fertilizer/bone_meal"));

		ClocheRecipeBuilder.builder(new ItemStack(Items.WHEAT, 2))
				.addResult(new ItemStack(Items.WHEAT_SEEDS, 1))
				.addInput(Items.WHEAT_SEEDS)
				.addSoil(Blocks.DIRT)
				.setTime(640)
				.setRender(new ClocheRenderReference("crop", Blocks.WHEAT))
				.build(out, toRL("cloche/wheat"));
		ClocheRecipeBuilder.builder(new ItemStack(Items.POTATO, 2))
				.addInput(Items.POTATO)
				.addSoil(Blocks.DIRT)
				.setTime(800)
				.setRender(new ClocheRenderReference("crop", Blocks.POTATOES))
				.build(out, toRL("cloche/potato"));
		ClocheRecipeBuilder.builder(new ItemStack(Items.CARROT, 2))
				.addInput(Items.CARROT)
				.addSoil(Blocks.DIRT)
				.setTime(800)
				.setRender(new ClocheRenderReference("crop", Blocks.CARROTS))
				.build(out, toRL("cloche/carrot"));
		ClocheRecipeBuilder.builder(new ItemStack(Items.BEETROOT, 2))
				.addResult(new ItemStack(Items.BEETROOT_SEEDS, 1))
				.addInput(Items.BEETROOT_SEEDS)
				.addSoil(Blocks.DIRT)
				.setTime(800)
				.setRender(new ClocheRenderReference("crop", Blocks.BEETROOTS))
				.build(out, toRL("cloche/beetroot"));
		ClocheRecipeBuilder.builder(new ItemStack(Items.NETHER_WART, 2))
				.addInput(Items.NETHER_WART)
				.addSoil(Blocks.SOUL_SAND)
				.setTime(800)
				.setRender(new ClocheRenderReference("crop", Blocks.NETHER_WART))
				.build(out, toRL("cloche/nether_wart"));
		ClocheRecipeBuilder.builder(new ItemStack(Items.SWEET_BERRIES, 2))
				.addInput(Items.SWEET_BERRIES)
				.addSoil(Blocks.DIRT)
				.setTime(560)
				.setRender(new ClocheRenderReference("crop", Blocks.SWEET_BERRY_BUSH))
				.build(out, toRL("cloche/sweet_berries"));

		ClocheRecipeBuilder.builder(Items.PUMPKIN)
				.addInput(Items.PUMPKIN_SEEDS)
				.addSoil(Blocks.DIRT)
				.setTime(800)
				.setRender(new ClocheRenderReference("stem", Blocks.PUMPKIN))
				.build(out, toRL("cloche/pumpkin"));
		ClocheRecipeBuilder.builder(Items.MELON)
				.addInput(Items.MELON_SEEDS)
				.addSoil(Blocks.DIRT)
				.setTime(800)
				.setRender(new ClocheRenderReference("stem", Blocks.MELON))
				.build(out, toRL("cloche/melon"));

		ClocheRecipeBuilder.builder(Items.SUGAR_CANE)
				.addInput(Items.SUGAR_CANE)
				.addSoil(Blocks.SAND)
				.setTime(560)
				.setRender(new ClocheRenderReference("stacking", Blocks.SUGAR_CANE))
				.build(out, toRL("cloche/sugar_cane"));
		ClocheRecipeBuilder.builder(Items.CACTUS)
				.addInput(Items.CACTUS)
				.addSoil(Blocks.SAND)
				.setTime(560)
				.setRender(new ClocheRenderReference("stacking", Blocks.CACTUS))
				.build(out, toRL("cloche/cactus"));
		ClocheRecipeBuilder.builder(Items.BAMBOO)
				.addInput(Items.BAMBOO)
				.addSoil(Blocks.DIRT)
				.setTime(560)
				.setRender(new ClocheRenderReference("stacking", Blocks.BAMBOO))
				.build(out, toRL("cloche/bamboo"));
		ClocheRecipeBuilder.builder(Items.CHORUS_FRUIT)
				.addInput(Items.CHORUS_FLOWER)
				.addSoil(Blocks.END_STONE)
				.setTime(480)
				.setRender(new ClocheRenderReference("chorus", Blocks.CHORUS_FLOWER))
				.build(out, toRL("cloche/chorus_fruit"));
		ClocheRecipeBuilder.builder(Ingredients.hempFiber)
				.addResult(new ItemStack(Misc.hempSeeds, 2))
				.addInput(Misc.hempSeeds)
				.addSoil(Blocks.DIRT)
				.setTime(800)
				.setRender(new ClocheRenderReference("hemp", IEBlocks.Misc.hempPlant))
				.build(out, toRL("cloche/hemp"));

		Ingredient shroomSoil = Ingredient.fromItems(Blocks.MYCELIUM, Blocks.PODZOL);
		ClocheRecipeBuilder.builder(Items.RED_MUSHROOM)
				.addInput(Items.RED_MUSHROOM)
				.addSoil(shroomSoil)
				.setTime(480)
				.setRender(new ClocheRenderReference("generic", Blocks.RED_MUSHROOM))
				.build(out, toRL("cloche/red_mushroom"));
		ClocheRecipeBuilder.builder(Items.BROWN_MUSHROOM)
				.addInput(Items.BROWN_MUSHROOM)
				.addSoil(shroomSoil)
				.setTime(480)
				.setRender(new ClocheRenderReference("generic", Blocks.BROWN_MUSHROOM))
				.build(out, toRL("cloche/brown_mushroom"));
	}

	private void recipesBlueprint(@Nonnull Consumer<IFinishedRecipe> out)
	{
		BlueprintCraftingRecipeBuilder.builder("components", new ItemStack(Ingredients.componentIron))
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.IRON).plate, 2))
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL("blueprint/component_iron"));
		BlueprintCraftingRecipeBuilder.builder("components", new ItemStack(Ingredients.componentSteel))
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.STEEL).plate, 2))
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL("blueprint/component_steel"));
		BlueprintCraftingRecipeBuilder.builder("components", new ItemStack(Ingredients.electronTube, 3))
				.addInput(Tags.Items.GLASS)
				.addInput(IETags.getTagsFor(EnumMetals.NICKEL).plate)
				.addInput(IETags.copperWire)
				.addInput(Tags.Items.DUSTS_REDSTONE)
				.build(out, toRL("blueprint/electron_tube"));
		BlueprintCraftingRecipeBuilder.builder("components", new ItemStack(Ingredients.circuitBoard))
				.addInput(StoneDecoration.insulatingGlass)
				.addInput(IETags.getTagsFor(EnumMetals.COPPER).plate)
				.addInput(new ItemStack(Ingredients.electronTube, 2))
				.build(out, toRL("blueprint/circuit_board"));

		Item[] molds = {Molds.moldPlate, Molds.moldGear, Molds.moldRod, Molds.moldBulletCasing, Molds.moldWire, Molds.moldPacking4, Molds.moldPacking9, Molds.moldUnpacking};
		for(Item mold : molds)
			BlueprintCraftingRecipeBuilder.builder("molds", mold)
					.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.STEEL).plate, 5))
					.addInput(Tools.wirecutter)
					.build(out, toRL("blueprint/"+toPath(mold)));


		BlueprintCraftingRecipeBuilder.builder("bullet", BulletHandler.getBulletItem(BulletItem.CASULL))
				.addInput(BulletHandler.emptyCasing)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.LEAD).nugget, 2))
				.build(out, toRL("blueprint/bullet_casull"));
		BlueprintCraftingRecipeBuilder.builder("bullet", BulletHandler.getBulletItem(BulletItem.ARMOR_PIERCING))
				.addInput(BulletHandler.emptyCasing)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.STEEL).nugget, 2))
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.CONSTANTAN).nugget, 2))
				.build(out, toRL("blueprint/bullet_armorpiercing"));
		BlueprintCraftingRecipeBuilder.builder("bullet", BulletHandler.getBulletItem(BulletItem.SILVER))
				.addInput(BulletHandler.emptyCasing)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.LEAD).nugget, 2))
				.addInput(IETags.getTagsFor(EnumMetals.SILVER).nugget)
				.build(out, toRL("blueprint/bullet_silver"));
		BlueprintCraftingRecipeBuilder.builder("bullet", BulletHandler.getBulletItem(BulletItem.BUCKSHOT))
				.addInput(BulletHandler.emptyShell)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.STEEL).dust, 2))
				.build(out, toRL("blueprint/bullet_buckshot"));

		BlueprintCraftingRecipeBuilder.builder("specialBullet", BulletHandler.getBulletItem(BulletItem.HIGH_EXPLOSIVE))
				.addInput(BulletHandler.emptyCasing)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(Items.TNT)
				.build(out, toRL("blueprint/bullet_explosive"));
		BlueprintCraftingRecipeBuilder.builder("specialBullet", BulletHandler.getBulletItem(BulletItem.DRAGONS_BREATH))
				.addInput(BulletHandler.emptyShell)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.ALUMINUM).dust, 4))
				.build(out, toRL("blueprint/bullet_dragonsbreath"));
		BlueprintCraftingRecipeBuilder.builder("specialBullet", BulletHandler.getBulletItem(BulletItem.POTION))
				.addInput(BulletHandler.emptyCasing)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(Items.GLASS_BOTTLE)
				.build(out, toRL("blueprint/bullet_potion"));

		ItemStack flare = BulletHandler.getBulletStack(BulletItem.FLARE);
		ItemNBTHelper.putInt(flare, "flareColour", 0xcc2e06);
		BlueprintCraftingRecipeBuilder.builder("specialBullet", flare.copy())
				.addInput(BulletHandler.emptyShell)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(IETags.getTagsFor(EnumMetals.ALUMINUM).dust)
				.addInput(Tags.Items.DYES_RED)
				.build(out, toRL("blueprint/bullet_flare_red"));
		ItemNBTHelper.putInt(flare, "flareColour", 0x2ca30b);
		BlueprintCraftingRecipeBuilder.builder("specialBullet", flare.copy())
				.addInput(BulletHandler.emptyShell)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(IETags.getTagsFor(EnumMetals.ALUMINUM).dust)
				.addInput(Tags.Items.DYES_GREEN)
				.build(out, toRL("blueprint/bullet_flare_green"));
		ItemNBTHelper.putInt(flare, "flareColour", 0xffff82);
		BlueprintCraftingRecipeBuilder.builder("specialBullet", flare.copy())
				.addInput(BulletHandler.emptyShell)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(IETags.getTagsFor(EnumMetals.ALUMINUM).dust)
				.addInput(Tags.Items.DYES_YELLOW)
				.build(out, toRL("blueprint/bullet_flare_yellow"));
		BlueprintCraftingRecipeBuilder.builder("specialBullet", BulletHandler.getBulletItem(BulletItem.HOMING))
				.addInput(BulletHandler.emptyCasing)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.LEAD).nugget, 2))
				.addInput(Items.ENDER_EYE)
				.build(out, toRL("blueprint/bullet_homing"));
		BlueprintCraftingRecipeBuilder.builder("specialBullet", BulletHandler.getBulletItem(BulletItem.WOLFPACK))
				.addInput(BulletHandler.emptyShell)
				.addInput(Tags.Items.GUNPOWDER)
				.addInput(new IngredientWithSize(Ingredient.fromItems(BulletHandler.getBulletItem(BulletItem.HOMING)), 4))
				.build(out, toRL("blueprint/bullet_wolfpack"));

		BlueprintCraftingRecipeBuilder.builder("electrode", Misc.graphiteElectrode)
				.addInput(new IngredientWithSize(IETags.hopGraphiteIngot, 4))
				.build(out, toRL("blueprint/electrode"));
	}

	private void recipesMultiblockMachines(@Nonnull Consumer<IFinishedRecipe> out)
	{
		HammerCrushingRecipeBuilder hammerBuilder;
		CrusherRecipeBuilder crusherBuilder;
		ArcFurnaceRecipeBuilder arcBuilder;
		MetalPressRecipeBuilder pressBuilder;
		AlloyRecipeBuilder alloyBuilder;

		/* Common Metals */
		for(RecipeMetals metal : RecipeMetals.values())
		{
			if(metal.getOre()!=null)
			{
				SecondaryOutput[] secondaryOutputs = metal.getSecondaryOutputs();

				// Hammer crushing
				hammerBuilder = HammerCrushingRecipeBuilder.builder(metal.getDust());
				if(!metal.isNative())
					hammerBuilder.addCondition(getTagCondition(metal.getDust())).addCondition(getTagCondition(metal.getOre()));
				hammerBuilder.addInput(metal.getOre())
						.build(out, toRL("crafting/hammercrushing_"+metal.getName()));

				// Crush ore
				crusherBuilder = CrusherRecipeBuilder.builder(metal.getDust(), 2);
				if(!metal.isNative())
					crusherBuilder.addCondition(getTagCondition(metal.getDust())).addCondition(getTagCondition(metal.getOre()));
				if(secondaryOutputs!=null)
					for(SecondaryOutput secondaryOutput : secondaryOutputs)
						crusherBuilder.addSecondary(secondaryOutput.getItem(), secondaryOutput.getChance(), secondaryOutput.getConditions());
				crusherBuilder.addInput(metal.getOre())
						.setEnergy(6000)
						.build(out, toRL("crusher/ore_"+metal.getName()));

				// Crush ingot
				crusherBuilder = CrusherRecipeBuilder.builder(metal.getDust(), 1);
				if(!metal.isNative())
					crusherBuilder.addCondition(getTagCondition(metal.getDust())).addCondition(getTagCondition(metal.getIngot()));
				crusherBuilder.addInput(metal.getIngot())
						.setEnergy(3000)
						.build(out, toRL("crusher/ingot_"+metal.getName()));

				// Arcfurnace ore
				arcBuilder = ArcFurnaceRecipeBuilder.builder(metal.getIngot(), 2);
				if(!metal.isNative())
					arcBuilder.addCondition(getTagCondition(metal.getIngot())).addCondition(getTagCondition(metal.getOre()));
				arcBuilder.addIngredient("input", metal.getOre())
						.addSlag(IETags.slag, 1)
						.setTime(200)
						.setEnergy(102400)
						.build(out, toRL("arcfurnace/ore_"+metal.getName()));

				// Arcfurnace dust
				arcBuilder = ArcFurnaceRecipeBuilder.builder(metal.getIngot(), 1);
				if(!metal.isNative())
					arcBuilder.addCondition(getTagCondition(metal.getIngot())).addCondition(getTagCondition(metal.getDust()));
				arcBuilder.addIngredient("input", metal.getDust())
						.setTime(100)
						.setEnergy(51200)
						.build(out, toRL("arcfurnace/dust_"+metal.getName()));
			}

			// Plate
			Tag<Item> plate = new ItemTags.Wrapper(IETags.getPlate(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder(Molds.moldPlate, plate, 1);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot())).addCondition(getTagCondition(plate));
			pressBuilder.addInput(metal.getIngot())
					.setEnergy(2400)
					.build(out, toRL("metalpress/plate_"+metal.getName()));

			// Gear
			Tag<Item> gear = new ItemTags.Wrapper(IETags.getGear(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder(Molds.moldGear, gear, 1);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()));
			pressBuilder.addCondition(getTagCondition(gear))
					.addInput(new IngredientWithSize(metal.getIngot(), 4))
					.setEnergy(2400)
					.build(out, toRL("metalpress/gear_"+metal.getName()));

			// Rod
			Tag<Item> rods = new ItemTags.Wrapper(IETags.getRod(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder(Molds.moldRod, rods, 2);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()));
			pressBuilder.addCondition(getTagCondition(rods))
					.addInput(metal.getIngot())
					.setEnergy(2400)
					.build(out, toRL("metalpress/rod_"+metal.getName()));

			// Wire
			Tag<Item> wire = new ItemTags.Wrapper(IETags.getWire(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder(Molds.moldWire, wire, 2);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()));
			pressBuilder.addCondition(getTagCondition(wire))
					.addInput(metal.getIngot())
					.setEnergy(2400)
					.build(out, toRL("metalpress/wire_"+metal.getName()));

			AlloyProperties alloy = metal.getAlloyProperties();
			if(alloy!=null)
			{
				IngredientWithSize[] ingredients = alloy.getAlloyIngredients();
				if(alloy.isSimple())
				{
					alloyBuilder = AlloyRecipeBuilder.builder(metal.getIngot(), alloy.getOutputSize());
					if(!metal.isNative())
						alloyBuilder.addCondition(getTagCondition(metal.getIngot()));
					for(ICondition condition : alloy.getConditions())
						alloyBuilder.addCondition(condition);
					for(IngredientWithSize ingr : ingredients)
						alloyBuilder.addInput(ingr);
					alloyBuilder.build(out, toRL("alloysmelter/"+metal.getName()));
				}

				arcBuilder = ArcFurnaceRecipeBuilder.builder(metal.getIngot(), alloy.getOutputSize());
				if(!metal.isNative())
					arcBuilder.addCondition(getTagCondition(metal.getIngot()));
				for(ICondition condition : alloy.getConditions())
					arcBuilder.addCondition(condition);
				arcBuilder.addIngredient("input", ingredients[0]);
				for(int i = 1; i < ingredients.length; i++)
					arcBuilder.addInput(ingredients[i]);
				arcBuilder.setTime(100)
						.setEnergy(51200)
						.build(out, toRL("arcfurnace/alloy_"+metal.getName()));
			}
		}

		// Non-metal ores
		for(RecipeOres ore : RecipeOres.values())
		{
			SecondaryOutput[] secondaryOutputs = ore.getSecondaryOutputs();
			crusherBuilder = CrusherRecipeBuilder.builder(ore.getOutput());
			if(!ore.isNative())
				crusherBuilder.addCondition(getTagCondition(ore.getOre()));
			if(secondaryOutputs!=null)
				for(SecondaryOutput secondaryOutput : secondaryOutputs)
					crusherBuilder.addSecondary(secondaryOutput.getItem(), secondaryOutput.getChance(), secondaryOutput.getConditions());
			crusherBuilder.addInput(ore.getOre())
					.setEnergy(6000)
					.build(out, toRL("crusher/ore_"+ore.getName()));
		}

		/* METAL PRESS */
		MetalPressRecipeBuilder.builder(Molds.moldBulletCasing, new ItemStack(Ingredients.emptyCasing, 2))
				.addInput(IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.setEnergy(2400)
				.build(out, toRL("metalpress/bullet_casing"));

		ItemStack electrode = new ItemStack(Misc.graphiteElectrode);
		electrode.setDamage(IEConfig.MACHINES.arcfurnace_electrodeDamage.get()/2);
		MetalPressRecipeBuilder.builder(Molds.moldRod, electrode)
				.addInput(new IngredientWithSize(IETags.hopGraphiteIngot, 4))
				.setEnergy(4800)
				.build(out, toRL("metalpress/electrode"));

		MetalPressRecipeBuilder.builder(Molds.moldUnpacking, new ItemStack(Items.MELON_SLICE, 9))
				.addInput(Items.MELON)
				.setEnergy(3200)
				.build(out, toRL("metalpress/melon"));

		/* ARC FURNACE */
		ArcFurnaceRecipeBuilder.builder(IETags.getTagsFor(EnumMetals.STEEL).ingot, 1)
				.addIngredient("input", Tags.Items.INGOTS_IRON)
				.addInput(IETags.coalCokeDust)
				.addSlag(IETags.slag, 1)
				.setTime(400)
				.setEnergy(204800)
				.build(out, toRL("arcfurnace/steel"));

		/* BOTTLING */
		BottlingMachineRecipeBuilder.builder(Items.WET_SPONGE)
				.addInput(Items.SPONGE)
				.addFluidTag(FluidTags.WATER, 1000)
				.build(out, toRL("bottling/sponge"));

		/* CRUSHER */
		CrusherRecipeBuilder.builder(Items.GRAVEL)
				.addInput(Tags.Items.COBBLESTONE)
				.setEnergy(1600)
				.build(out, toRL("crusher/cobblestone"));
		CrusherRecipeBuilder.builder(Items.SAND)
				.addSecondary(Items.FLINT, .1f)
				.addInput(Tags.Items.GRAVEL)
				.setEnergy(1600)
				.build(out, toRL("crusher/gravel"));
		CrusherRecipeBuilder.builder(Items.SAND)
				.addInput(IETags.slag)
				.setEnergy(1600)
				.build(out, toRL("crusher/slag"));
		CrusherRecipeBuilder.builder(Items.SAND)
				.addInput(Tags.Items.GLASS)
				.setEnergy(3200)
				.build(out, toRL("crusher/glass"));
		CrusherRecipeBuilder.builder(new ItemStack(Items.SAND, 2))
				.addSecondary(IETags.saltpeterDust, .5f)
				.addInput(Tags.Items.SANDSTONE)
				.setEnergy(3200)
				.build(out, toRL("crusher/sandstone"));
		CrusherRecipeBuilder.builder(new ItemStack(Items.CLAY_BALL, 4))
				.addInput(IETags.getItemTag(IETags.clayBlock))
				.setEnergy(1600)
				.build(out, toRL("crusher/clay"));
		CrusherRecipeBuilder.builder(new ItemStack(Items.QUARTZ, 4))
				.addInput(Tags.Items.STORAGE_BLOCKS_QUARTZ)
				.setEnergy(3200)
				.build(out, toRL("crusher/quartz"));
		CrusherRecipeBuilder.builder(new ItemStack(Items.GLOWSTONE_DUST, 4))
				.addInput(Blocks.GLOWSTONE)
				.setEnergy(3200)
				.build(out, toRL("crusher/quartz"));
		CrusherRecipeBuilder.builder(new ItemStack(Items.BLAZE_POWDER, 4))
				.addSecondary(IETags.sulfurDust, .5f)
				.addInput(Tags.Items.RODS_BLAZE)
				.setEnergy(1600)
				.build(out, toRL("crusher/blaze_powder"));
		CrusherRecipeBuilder.builder(new ItemStack(Items.BONE_MEAL, 6))
				.addInput(Items.BONE)
				.setEnergy(1600)
				.build(out, toRL("crusher/bone_meal"));
		CrusherRecipeBuilder.builder(IETags.coalCokeDust, 1)
				.addInput(IETags.coalCoke)
				.setEnergy(2400)
				.build(out, toRL("crusher/coke"));
		CrusherRecipeBuilder.builder(IETags.coalCokeDust, 9)
				.addInput(IETags.getItemTag(IETags.coalCokeBlock))
				.setEnergy(4800)
				.build(out, toRL("crusher/coke_block"));

		Tag<Item> coal_dust = new ItemTags.Wrapper(IETags.getDust("coal"));
		CrusherRecipeBuilder.builder(coal_dust, 1)
				.addCondition(getTagCondition(coal_dust))
				.addInput(Items.COAL)
				.setEnergy(2400)
				.build(out, toRL("crusher/coal"));
		CrusherRecipeBuilder.builder(coal_dust, 9)
				.addCondition(getTagCondition(coal_dust))
				.addInput(Items.COAL_BLOCK)
				.setEnergy(4800)
				.build(out, toRL("crusher/coal_block"));

		CrusherRecipeBuilder.builder(new ItemStack(Items.STRING, 4))
				.addInput(ItemTags.WOOL)
				.setEnergy(3200)
				.build(out, toRL("crusher/wool"));

		/* SQUEEZER */
		SqueezerRecipeBuilder.builder(IEContent.fluidPlantoil, 80)
				.addInput(Items.WHEAT_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/wheat_seeds"));
		SqueezerRecipeBuilder.builder(IEContent.fluidPlantoil, 60)
				.addInput(Items.BEETROOT_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/beetroot_seeds"));
		SqueezerRecipeBuilder.builder(IEContent.fluidPlantoil, 40)
				.addInput(Items.PUMPKIN_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/pumpkin_seeds"));
		SqueezerRecipeBuilder.builder(IEContent.fluidPlantoil, 20)
				.addInput(Items.MELON_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/melon_seeds"));
		SqueezerRecipeBuilder.builder(IEContent.fluidPlantoil, 120)
				.addInput(Misc.hempSeeds)
				.setEnergy(6400)
				.build(out, toRL("squeezer/hemp_seeds"));
		SqueezerRecipeBuilder.builder()
				.addResult(new IngredientWithSize(IETags.hopGraphiteDust))
				.addInput(new IngredientWithSize(IETags.coalCokeDust, 8))
				.setEnergy(19200)
				.build(out, toRL("squeezer/graphite_dust"));
		/* FERMENTER */
		FermenterRecipeBuilder.builder(IEContent.fluidEthanol, 80)
				.addInput(Items.SUGAR_CANE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/sugar_cane"));
		FermenterRecipeBuilder.builder(IEContent.fluidEthanol, 80)
				.addInput(Items.MELON_SLICE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/melon_slice"));
		FermenterRecipeBuilder.builder(IEContent.fluidEthanol, 80)
				.addInput(Items.APPLE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/apple"));
		FermenterRecipeBuilder.builder(IEContent.fluidEthanol, 80)
				.addInput(Tags.Items.CROPS_POTATO)
				.setEnergy(6400)
				.build(out, toRL("fermenter/potato"));
		/* REFINERY */
		RefineryRecipeBuilder.builder(IEContent.fluidBiodiesel, 16)
				.addInput(IETags.fluidPlantoil, 8)
				.addInput(IETags.fluidEthanol, 8)
				.setEnergy(80)
				.build(out, toRL("refinery/biodiesel"));
		/* MIXER */
		MixerRecipeBuilder.builder(IEContent.fluidConcrete, 500)
				.addFluidTag(FluidTags.WATER, 500)
				.addInput(new IngredientWithSize(Tags.Items.SAND, 2))
				.addInput(Tags.Items.GRAVEL)
				.addInput(IETags.clay)
				.setEnergy(3200)
				.build(out, toRL("mixer/concrete"));
		MixerRecipeBuilder.builder(IEContent.fluidConcrete, 500)
				.addFluidTag(FluidTags.WATER, 500)
				.addInput(new IngredientWithSize(IETags.slag, 2))
				.addInput(Tags.Items.GRAVEL)
				.addInput(IETags.clay)
				.setEnergy(3200)
				.build(out, toRL("mixer/concrete"));
	}

	private void mineralMixes(@Nonnull Consumer<IFinishedRecipe> out)
	{
		// Metals
		Tag<Item> iron = Tags.Items.ORES_IRON;
		Tag<Item> gold = Tags.Items.ORES_GOLD;
		Tag<Item> copper = IETags.getItemTag(IETags.getTagsFor(EnumMetals.COPPER).ore);
		Tag<Item> aluminum = IETags.getItemTag(IETags.getTagsFor(EnumMetals.ALUMINUM).ore);
		Tag<Item> lead = IETags.getItemTag(IETags.getTagsFor(EnumMetals.LEAD).ore);
		Tag<Item> silver = IETags.getItemTag(IETags.getTagsFor(EnumMetals.SILVER).ore);
		Tag<Item> nickel = IETags.getItemTag(IETags.getTagsFor(EnumMetals.NICKEL).ore);
		Tag<Item> uranium = IETags.getItemTag(IETags.getTagsFor(EnumMetals.URANIUM).ore);
		Tag<Item> tin = new ItemTags.Wrapper(IETags.getOre("tin"));
		Tag<Item> titanium = new ItemTags.Wrapper(IETags.getOre("titanium"));
		Tag<Item> thorium = new ItemTags.Wrapper(IETags.getOre("thorium"));
		Tag<Item> tungsten = new ItemTags.Wrapper(IETags.getOre("tungsten"));
		Tag<Item> manganese = new ItemTags.Wrapper(IETags.getOre("manganese"));
		Tag<Item> platinum = new ItemTags.Wrapper(IETags.getOre("platinum"));
		Tag<Item> paladium = new ItemTags.Wrapper(IETags.getOre("paladium"));
		Tag<Item> mercury = new ItemTags.Wrapper(IETags.getOre("mercury"));
		// Gems & Dusts
		Tag<Item> sulfur = IETags.sulfurDust;
		Tag<Item> phosphorus = new ItemTags.Wrapper(IETags.getDust("phosphorus"));
		Tag<Item> redstone = Tags.Items.ORES_REDSTONE;
		Tag<Item> emerald = Tags.Items.ORES_EMERALD;
		Block prismarine = Blocks.PRISMARINE;
		Tag<Item> aquamarine = new ItemTags.Wrapper(IETags.getGem("aquamarine"));

		// Common things
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(Tags.Items.ORES_COAL, .8f)
				.addOre(sulfur, .2f)
				.addOre(phosphorus, .2f, getTagCondition(phosphorus))
				.setWeight(25)
				.setFailchance(.05f)
				.build(out, toRL("mineral/bituminous_coal"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(Items.CLAY, .5f)
				.addOre(Items.SAND, .3f)
				.addOre(Items.GRAVEL, .2f)
				.setWeight(25)
				.setFailchance(.05f)
				.build(out, toRL("mineral/silt"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(Blocks.GRANITE, .3f)
				.addOre(Blocks.DIORITE, .3f)
				.addOre(Blocks.ANDESITE, .3f)
				.addOre(Blocks.OBSIDIAN, .1f)
				.setWeight(25)
				.setFailchance(.05f)
				.build(out, toRL("mineral/igneous_rock"));
		// Metals
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(iron, .35f)
				.addOre(nickel, .35f)
				.addOre(sulfur, .3f)
				.setWeight(25)
				.setFailchance(.05f)
				.build(out, toRL("mineral/pentlandite"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(iron, .35f)
				.addOre(copper, .35f)
				.addOre(sulfur, .3f)
				.setWeight(20)
				.setFailchance(.05f)
				.build(out, toRL("mineral/chalcopyrite"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(aluminum, .7f)
				.addOre(iron, .2f)
				.addOre(titanium, .1f, getTagCondition(titanium))
				.setWeight(20)
				.setFailchance(.05f)
				.build(out, toRL("mineral/laterite"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(copper, .75f)
				.addOre(gold, .25f)
				.setWeight(30)
				.setFailchance(.1f)
				.build(out, toRL("mineral/auricupride"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(lead, .4f)
				.addOre(sulfur, .4f)
				.addOre(silver, .2f)
				.setWeight(15)
				.setFailchance(.05f)
				.build(out, toRL("mineral/galena"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(redstone, .6f)
				.addOre(sulfur, .4f)
				.addOre(mercury, .3f, getTagCondition(mercury))
				.setWeight(15)
				.setFailchance(.1f)
				.build(out, toRL("mineral/cinnabar"));
		// Rare
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(uranium, .7f)
				.addOre(lead, .3f)
				.addOre(thorium, .1f, getTagCondition(thorium))
				.setWeight(10)
				.setFailchance(.15f)
				.build(out, toRL("mineral/uraninite"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addOre(emerald, .3f)
				.addOre(prismarine, .7f)
				.addOre(aquamarine, .3f, getTagCondition(aquamarine))
				.setWeight(5)
				.setFailchance(.2f)
				.build(out, toRL("mineral/beryl"));
		// Nether
		MineralMixBuilder.builder(DimensionType.THE_NETHER)
				.addOre(Blocks.NETHER_QUARTZ_ORE, .8f)
				.addOre(sulfur, .2f)
				.setWeight(20)
				.setFailchance(.15f)
				.setBackground(ForgeRegistries.BLOCKS.getKey(Blocks.NETHERRACK))
				.build(out, toRL("mineral/mephitic_quarzite"));

		// Compat
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addCondition(getTagCondition(tin))
				.addOre(tin, 1)
				.setWeight(20)
				.setFailchance(.05f)
				.build(out, toRL("mineral/cassiterite"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addCondition(getTagCondition(platinum))
				.addOre(platinum, .5f)
				.addOre(paladium, .5f, getTagCondition(paladium))
				.addOre(nickel, .5f)
				.setWeight(5)
				.setFailchance(.1f)
				.build(out, toRL("mineral/cooperite"));
		MineralMixBuilder.builder(DimensionType.OVERWORLD)
				.addCondition(getTagCondition(tungsten))
				.addOre(tungsten, .5f)
				.addOre(iron, .5f)
				.addOre(manganese, .5f, getTagCondition(manganese))
				.setWeight(5)
				.setFailchance(.1f)
				.build(out, toRL("mineral/wolframite"));
		//todo
		//	Lapis
		//	Cinnabar
	}

	private void recipesStoneDecorations(@Nonnull Consumer<IFinishedRecipe> out)
	{
		addCornerStraightMiddle(StoneDecoration.cokebrick, 3,
				makeIngredient(IETags.clay),
				makeIngredient(Tags.Items.INGOTS_BRICK),
				makeIngredient(Tags.Items.SANDSTONE),
				out);
		addCornerStraightMiddle(StoneDecoration.blastbrick, 3,
				makeIngredient(Tags.Items.INGOTS_NETHER_BRICK),
				makeIngredient(Tags.Items.INGOTS_BRICK),
				makeIngredient(Blocks.MAGMA_BLOCK),
				out);
		addSandwich(StoneDecoration.hempcrete, 6,
				makeIngredient(IETags.clay),
				makeIngredient(IETags.fiberHemp),
				makeIngredient(IETags.clay),
				out);
		add3x3Conversion(StoneDecoration.coke, IEItems.Ingredients.coalCoke, IETags.coalCoke, out);

		addStairs(StoneDecoration.hempcrete, StoneDecoration.hempcreteStairs, out);
		addStairs(StoneDecoration.concrete, StoneDecoration.concreteStairs[0], out);
		addStairs(StoneDecoration.concreteTile, StoneDecoration.concreteStairs[1], out);
		addStairs(StoneDecoration.concreteLeaded, StoneDecoration.concreteStairs[2], out);

		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.hempcrete), IEBlocks.toSlab.get(StoneDecoration.hempcrete), 2)
				.addCriterion("has_hempcrete", hasItem(StoneDecoration.hempcrete))
				.build(out, toRL("hempcrete_slab_from_hempcrete_stonecutting"));
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.hempcrete), StoneDecoration.hempcreteStairs)
				.addCriterion("has_hempcrete", hasItem(StoneDecoration.hempcrete))
				.build(out, toRL("hempcrete_stairs_from_hempcrete_stonecutting"));
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.concrete), IEBlocks.toSlab.get(StoneDecoration.concrete), 2)
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL("concrete_slab_from_concrete_stonecutting"));
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.concrete), StoneDecoration.concreteStairs[0])
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL("concrete_stairs_from_concrete_stonecutting"));
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.concreteTile), IEBlocks.toSlab.get(StoneDecoration.concreteTile), 2)
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL("concrete_tile_slab_from_concrete_tile_stonecutting"));
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.concreteTile), StoneDecoration.concreteStairs[1])
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL("concrete_tile_stairs_from_concrete_tile_stonecutting"));
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.concreteLeaded), IEBlocks.toSlab.get(StoneDecoration.concreteLeaded), 2)
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL("concrete_leaded_slab_from_concrete_leaded_stonecutting"));
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.concreteLeaded), StoneDecoration.concreteStairs[2])
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL("concrete_leaded_stairs_from_concrete_leaded_stonecutting"));
		SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(StoneDecoration.concrete), StoneDecoration.concreteTile)
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL("concrete_tile_from_concrete_stonecutting"));

		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.alloybrick, 2)
				.patternLine("sb")
				.patternLine("bs")
				.key('s', Tags.Items.SANDSTONE)
				.key('b', Tags.Items.INGOTS_BRICK)
				.addCriterion("has_brick", hasItem(Tags.Items.INGOTS_BRICK))
				.build(out, toRL(toPath(StoneDecoration.alloybrick)));
		ShapelessRecipeBuilder.shapelessRecipe(StoneDecoration.blastbrickReinforced)
				.addIngredient(StoneDecoration.blastbrick)
				.addIngredient(IETags.getTagsFor(EnumMetals.STEEL).plate)
				.addCriterion("has_blastbrick", hasItem(StoneDecoration.blastbrick))
				.build(out, toRL(toPath(StoneDecoration.blastbrickReinforced)));

		TurnAndCopyRecipeBuilder.builder(StoneDecoration.concrete, 8)
				.allowQuarterTurn()
				.setGroup("ie_concrete")
				.patternLine("scs")
				.patternLine("gbg")
				.patternLine("scs")
				.key('s', Tags.Items.SAND)
				.key('c', IETags.clay)
				.key('g', Tags.Items.GRAVEL)
				.key('b', new IngredientFluidStack(FluidTags.WATER, 1000))
				.addCriterion("has_clay", hasItem(IETags.clay))
				.build(out, toRL("concrete"));
		TurnAndCopyRecipeBuilder.builder(StoneDecoration.concrete, 12)
				.allowQuarterTurn()
				.setGroup("ie_concrete")
				.patternLine("scs")
				.patternLine("gbg")
				.patternLine("scs")
				.key('s', IEItems.Ingredients.slag)
				.key('c', IETags.clay)
				.key('g', Tags.Items.GRAVEL)
				.key('b', new IngredientFluidStack(FluidTags.WATER, 1000))
				.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
				.build(out, toRL("concrete"));
		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.concreteTile, 4)
				.setGroup("ie_concrete")
				.patternLine("cc")
				.patternLine("cc")
				.key('c', StoneDecoration.concrete)
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL(toPath(StoneDecoration.concreteTile)));
		ShapelessRecipeBuilder.shapelessRecipe(StoneDecoration.concreteLeaded)
				.addIngredient(StoneDecoration.concrete)
				.addIngredient(IETags.getTagsFor(EnumMetals.LEAD).plate)
				.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
				.build(out, toRL(toPath(StoneDecoration.concreteLeaded)));

		TurnAndCopyRecipeBuilder.builder(StoneDecoration.insulatingGlass, 2)
				.allowQuarterTurn()
				.patternLine(" g ")
				.patternLine("idi")
				.patternLine(" g ")
				.key('g', Tags.Items.GLASS)
				.key('i', IETags.getTagsFor(EnumMetals.IRON).dust)
				.key('d', Tags.Items.DYES_GREEN)
				.addCriterion("has_glass", hasItem(Tags.Items.GLASS))
				.build(out, toRL(toPath(StoneDecoration.insulatingGlass)));
	}

	private void recipesWoodenDecorations(@Nonnull Consumer<IFinishedRecipe> out)
	{
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			addStairs(WoodenDecoration.treatedWood.get(style), WoodenDecoration.treatedStairs.get(style), out);

		int numTreatedStyles = TreatedWoodStyles.values().length;
		for(TreatedWoodStyles from : TreatedWoodStyles.values())
		{
			TreatedWoodStyles to = TreatedWoodStyles.values()[(from.ordinal()+1)%numTreatedStyles];
			ShapelessRecipeBuilder.shapelessRecipe(WoodenDecoration.treatedWood.get(to))
					.addIngredient(WoodenDecoration.treatedWood.get(from))
					.addCriterion("has_"+toPath(WoodenDecoration.treatedWood.get(from)), hasItem(WoodenDecoration.treatedWood.get(from)))
					.build(out, toRL(toPath(WoodenDecoration.treatedWood.get(to))+"_from_"+from.toString().toLowerCase()));
		}
		ShapedRecipeBuilder.shapedRecipe(WoodenDecoration.treatedScaffolding, 6)
				.patternLine("iii")
				.patternLine(" s ")
				.patternLine("s s")
				.key('i', IETags.getItemTag(IETags.treatedWood))
				.key('s', IETags.treatedStick)
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.addCriterion("has_treated_sticks", hasItem(IETags.treatedStick))
				.build(out, toRL(toPath(WoodenDecoration.treatedScaffolding)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDecoration.treatedFence, 3)
				.patternLine("isi")
				.patternLine("isi")
				.key('i', IETags.getItemTag(IETags.treatedWood))
				.key('s', IETags.treatedStick)
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.addCriterion("has_treated_sticks", hasItem(IETags.treatedStick))
				.build(out, toRL(toPath(WoodenDecoration.treatedFence)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDecoration.treatedPost)
				.patternLine("f")
				.patternLine("f")
				.patternLine("s")
				.key('f', WoodenDecoration.treatedFence)
				.key('s', Blocks.STONE_BRICKS)
				.addCriterion("has_"+toPath(WoodenDecoration.treatedFence), hasItem(WoodenDecoration.treatedFence))
				.build(out, toRL(toPath(WoodenDecoration.treatedPost)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDecoration.treatedWood.get(TreatedWoodStyles.HORIZONTAL), 8)
				.patternLine("www")
				.patternLine("wbw")
				.patternLine("www")
				.key('w', ItemTags.PLANKS)
				.key('b', new IngredientFluidStack(IETags.fluidCreosote, 1000))
				.addCriterion("has_creosote", hasItem(IEContent.fluidCreosote.getFilledBucket()))
				.build(out, toRL(toPath(WoodenDecoration.treatedWood.get(TreatedWoodStyles.HORIZONTAL))));
	}

	private void recipesWoodenDevices(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.craftingTable)
				.patternLine("sss")
				.patternLine("rcr")
				.patternLine("r r")
				.key('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.key('r', IETags.treatedStick)
				.key('c', Blocks.CRAFTING_TABLE)
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(WoodenDevices.craftingTable)));
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.crate)
				.patternLine("ppp")
				.patternLine("p p")
				.patternLine("ppp")
				.key('p', IETags.getItemTag(IETags.treatedWood))
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(WoodenDevices.crate)));
		TurnAndCopyRecipeBuilder.builder(WoodenDevices.reinforcedCrate)
				.setNBTCopyTargetRecipe(4)
				.allowQuarterTurn()
				.patternLine("wpw")
				.patternLine("rcr")
				.patternLine("wpw")
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('p', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('r', IETags.ironRod)
				.key('c', IEBlocks.WoodenDevices.crate)
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(WoodenDevices.reinforcedCrate)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.treatedWallmount, 4)
				.patternLine("ww")
				.patternLine("ws")
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('s', IETags.treatedStick)
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(WoodenDevices.treatedWallmount)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.sorter)
				.patternLine("wrw")
				.patternLine("ici")
				.patternLine("wbw")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('b', ConveyorHandler.getBlock(BasicConveyor.NAME))
				.key('c', Ingredients.componentIron)
				.addCriterion("has_"+toPath(ConveyorHandler.getBlock(BasicConveyor.NAME)), hasItem(ConveyorHandler.getBlock(BasicConveyor.NAME)))
				.build(out, toRL(toPath(WoodenDevices.sorter)));
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.itemBatcher)
				.patternLine("wrw")
				.patternLine("ici")
				.patternLine("wpw")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('p', Ingredients.circuitBoard)
				.key('c', Ingredients.componentIron)
				.addCriterion("has_"+toPath(ConveyorHandler.getBlock(BasicConveyor.NAME)), hasItem(ConveyorHandler.getBlock(BasicConveyor.NAME)))
				.build(out, toRL(toPath(WoodenDevices.itemBatcher)));
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.fluidSorter)
				.patternLine("wrw")
				.patternLine("ici")
				.patternLine("wbw")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('b', MetalDevices.fluidPipe)
				.key('c', Ingredients.componentIron)
				.addCriterion("has_"+toPath(MetalDevices.fluidPipe), hasItem(MetalDevices.fluidPipe))
				.build(out, toRL(toPath(WoodenDevices.fluidSorter)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.turntable)
				.patternLine("iwi")
				.patternLine("rcr")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('c', MetalDecoration.lvCoil)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
				.build(out, toRL(toPath(WoodenDevices.turntable)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.windmill)
				.patternLine("ppp")
				.patternLine("pip")
				.patternLine("ppp")
				.key('p', Ingredients.windmillBlade)
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.addCriterion("has_"+toPath(Ingredients.windmillBlade), hasItem(Ingredients.windmillBlade))
				.build(out, toRL(toPath(WoodenDevices.windmill)));
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.watermill)
				.patternLine(" p ")
				.patternLine("pip")
				.patternLine(" p ")
				.key('p', Ingredients.waterwheelSegment)
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_"+toPath(Ingredients.waterwheelSegment), hasItem(Ingredients.waterwheelSegment))
				.build(out, toRL(toPath(WoodenDevices.watermill)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.gunpowderBarrel)
				.patternLine(" f ")
				.patternLine("gbg")
				.patternLine("ggg")
				.key('f', Ingredients.hempFiber)
				.key('g', Tags.Items.GUNPOWDER)
				.key('b', WoodenDevices.woodenBarrel)
				.addCriterion("has_"+toPath(WoodenDevices.woodenBarrel), hasItem(WoodenDevices.woodenBarrel))
				.build(out, toRL(toPath(WoodenDevices.gunpowderBarrel)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.workbench)
				.patternLine("iss")
				.patternLine("c f")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.key('c', WoodenDevices.craftingTable)
				.key('f', WoodenDecoration.treatedFence)
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(WoodenDevices.workbench)));

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.woodenBarrel)
				.patternLine("sss")
				.patternLine("w w")
				.patternLine("www")
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(WoodenDevices.woodenBarrel)));
	}

	private void recipesMetalDecorations(@Nonnull Consumer<IFinishedRecipe> out)
	{
		for(DyeColor dye : DyeColor.values())
		{
			Tag<Item> dyeTag = new ItemTags.Wrapper(new ResourceLocation("forge", "dyes/"+dye.getTranslationKey()));
			Block coloredSheetmetal = MetalDecoration.coloredSheetmetal.get(dye);
			ShapedRecipeBuilder.shapedRecipe(coloredSheetmetal)
					.patternLine("sss")
					.patternLine("sds")
					.patternLine("sss")
					.key('s', IETags.getItemTag(IETags.sheetmetals))
					.key('d', dyeTag)
					.addCriterion("has_sheetmetal", hasItem(IETags.getItemTag(IETags.sheetmetals)))
					.build(out, toRL(toPath(coloredSheetmetal)));
		}

		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			addStairs(MetalDecoration.steelScaffolding.get(type), MetalDecoration.steelScaffoldingStair.get(type), out);
			addStairs(MetalDecoration.aluScaffolding.get(type), MetalDecoration.aluScaffoldingStair.get(type), out);
		}

		int numScaffoldingTypes = MetalScaffoldingType.values().length;
		for(MetalScaffoldingType from : MetalScaffoldingType.values())
		{
			MetalScaffoldingType to = MetalScaffoldingType.values()[(from.ordinal()+1)%numScaffoldingTypes];
			ShapelessRecipeBuilder.shapelessRecipe(MetalDecoration.aluScaffolding.get(to))
					.addIngredient(MetalDecoration.aluScaffolding.get(from))
					.addCriterion("has_"+toPath(MetalDecoration.aluScaffolding.get(from)), hasItem(MetalDecoration.aluScaffolding.get(from)))
					.build(out, toRL("alu_scaffolding_"+to.name().toLowerCase()+"_from_"+from.name().toLowerCase()));
			ShapelessRecipeBuilder.shapelessRecipe(MetalDecoration.steelScaffolding.get(to))
					.addIngredient(MetalDecoration.steelScaffolding.get(from))
					.addCriterion("has_"+toPath(MetalDecoration.steelScaffolding.get(from)), hasItem(MetalDecoration.steelScaffolding.get(from)))
					.build(out, toRL("steel_scaffolding_"+to.name().toLowerCase()+"_from_"+from.name().toLowerCase()));
		}

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD), 6)
				.patternLine("iii")
				.patternLine(" s ")
				.patternLine("s s")
				.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.key('s', IETags.aluminumRod)
				.addCriterion("has_alu_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.addCriterion("has_alu_sticks", hasItem(IETags.aluminumRod))
				.build(out, toRL(toPath(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD))));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.slopeAlu)
				.patternLine("sss")
				.patternLine("ss ")
				.patternLine("s  ")
				.key('s', MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD))
				.addCriterion("has_"+toPath(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD)), hasItem(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD)))
				.build(out, toRL(toPath(MetalDecoration.slopeAlu)));

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD), 6)
				.patternLine("iii")
				.patternLine(" s ")
				.patternLine("s s")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('s', IETags.steelRod)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.addCriterion("has_steel_sticks", hasItem(IETags.steelRod))
				.build(out, toRL(toPath(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.slopeSteel)
				.patternLine("sss")
				.patternLine("ss ")
				.patternLine("s  ")
				.key('s', MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))
				.addCriterion("has_"+toPath(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)), hasItem(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)))
				.build(out, toRL(toPath(MetalDecoration.slopeSteel)));

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.aluFence, 3)
				.patternLine("isi")
				.patternLine("isi")
				.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.key('s', IETags.aluminumRod)
				.addCriterion("has_alu_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.addCriterion("has_alu_sticks", hasItem(IETags.aluminumRod))
				.build(out, toRL(toPath(MetalDecoration.aluFence)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.steelFence, 3)
				.patternLine("isi")
				.patternLine("isi")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('s', IETags.steelRod)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.addCriterion("has_steel_sticks", hasItem(IETags.steelRod))
				.build(out, toRL(toPath(MetalDecoration.steelFence)));

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.lvCoil)
				.patternLine("www")
				.patternLine("wiw")
				.patternLine("www")
				.key('i', Tags.Items.INGOTS_IRON)
				.key('w', Misc.wireCoils.get(WireType.COPPER))
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.COPPER)), hasItem(Misc.wireCoils.get(WireType.COPPER)))
				.build(out, toRL(toPath(MetalDecoration.lvCoil)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.mvCoil)
				.patternLine("www")
				.patternLine("wiw")
				.patternLine("www")
				.key('i', Tags.Items.INGOTS_IRON)
				.key('w', Misc.wireCoils.get(WireType.ELECTRUM))
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.ELECTRUM)), hasItem(Misc.wireCoils.get(WireType.ELECTRUM)))
				.build(out, toRL(toPath(MetalDecoration.mvCoil)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.hvCoil)
				.patternLine("www")
				.patternLine("wiw")
				.patternLine("www")
				.key('i', Tags.Items.INGOTS_IRON)
				.key('w', Misc.wireCoils.get(WireType.STEEL))
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.STEEL)), hasItem(Misc.wireCoils.get(WireType.STEEL)))
				.build(out, toRL(toPath(MetalDecoration.hvCoil)));

		TurnAndCopyRecipeBuilder.builder(MetalDecoration.engineeringRS, 4)
				.allowQuarterTurn()
				.patternLine("iri")
				.patternLine("rcr")
				.patternLine("iri")
				.key('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.addCriterion("has_iron_sheetmetal", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)))
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.addCriterion("has_redstone", hasItem(Items.REDSTONE))
				.build(out, toRL(toPath(MetalDecoration.engineeringRS)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.engineeringLight, 4)
				.patternLine("igi")
				.patternLine("gcg")
				.patternLine("igi")
				.key('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('g', Ingredients.componentIron)
				.addCriterion("has_iron_sheetmetal", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)))
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.addCriterion("has_component_iron", hasItem(Ingredients.componentIron))
				.build(out, toRL(toPath(MetalDecoration.engineeringLight)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.engineeringHeavy, 4)
				.patternLine("igi")
				.patternLine("geg")
				.patternLine("igi")
				.key('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.key('e', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.key('g', Ingredients.componentSteel)
				.addCriterion("has_steel_sheetmetal", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.addCriterion("has_component_steel", hasItem(Ingredients.componentSteel))
				.build(out, toRL(toPath(MetalDecoration.engineeringHeavy)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.generator, 4)
				.patternLine("iei")
				.patternLine("ede")
				.patternLine("iei")
				.key('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.key('e', IETags.getTagsFor(EnumMetals.ELECTRUM).plate)
				.key('d', MetalDevices.dynamo)
				.addCriterion("has_steel_sheetmetal", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.addCriterion("has_"+toPath(MetalDevices.dynamo), hasItem(MetalDevices.dynamo))
				.build(out, toRL(toPath(MetalDecoration.generator)));
		TurnAndCopyRecipeBuilder.builder(MetalDecoration.radiator, 4)
				.allowQuarterTurn()
				.patternLine("ici")
				.patternLine("cbc")
				.patternLine("ici")
				.key('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.key('b', new IngredientFluidStack(FluidTags.WATER, 1000))
				.addCriterion("has_steel_sheetmetal", hasItem(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.addCriterion("has_water_bucket", hasItem(Items.WATER_BUCKET))
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL(toPath(MetalDecoration.radiator)));

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.aluPost)
				.patternLine("f")
				.patternLine("f")
				.patternLine("s")
				.key('f', MetalDecoration.aluFence)
				.key('s', Blocks.STONE_BRICKS)
				.addCriterion("has_"+toPath(MetalDecoration.aluFence), hasItem(MetalDecoration.aluFence))
				.build(out, toRL(toPath(MetalDecoration.aluPost)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.steelPost)
				.patternLine("f")
				.patternLine("f")
				.patternLine("s")
				.key('f', MetalDecoration.steelFence)
				.key('s', Blocks.STONE_BRICKS)
				.addCriterion("has_"+toPath(MetalDecoration.steelFence), hasItem(MetalDecoration.steelFence))
				.build(out, toRL(toPath(MetalDecoration.steelPost)));

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.aluWallmount)
				.patternLine("ii")
				.patternLine("is")
				.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.key('s', IETags.aluminumRod)
				.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.build(out, toRL(toPath(MetalDecoration.aluWallmount)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.steelWallmount)
				.patternLine("ii")
				.patternLine("is")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('s', IETags.steelRod)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(MetalDecoration.steelWallmount)));

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.metalLadder.get(CoverType.NONE), 3)
				.patternLine("s s")
				.patternLine("sss")
				.patternLine("s s")
				.key('s', IETags.metalRods)
				.addCriterion("has_metal_rod", hasItem(IETags.metalRods))
				.build(out, toRL(toPath(MetalDecoration.metalLadder.get(CoverType.NONE))));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.metalLadder.get(CoverType.ALU), 3)
				.patternLine("s")
				.patternLine("l")
				.key('s', IETags.getItemTag(IETags.scaffoldingAlu))
				.key('l', MetalDecoration.metalLadder.get(CoverType.NONE))
				.addCriterion("has_metal_ladder", hasItem(MetalDecoration.metalLadder.get(CoverType.NONE)))
				.build(out, toRL(toPath(MetalDecoration.metalLadder.get(CoverType.ALU))));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.metalLadder.get(CoverType.STEEL), 3)
				.patternLine("s")
				.patternLine("l")
				.key('s', IETags.getItemTag(IETags.scaffoldingSteel))
				.key('l', MetalDecoration.metalLadder.get(CoverType.NONE))
				.addCriterion("has_metal_ladder", hasItem(MetalDecoration.metalLadder.get(CoverType.NONE)))
				.build(out, toRL(toPath(MetalDecoration.metalLadder.get(CoverType.STEEL))));
	}

	private void recipesMetalDevices(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.razorWire, 3)
				.patternLine("psp")
				.patternLine("sss")
				.patternLine("psp")
				.key('s', Ingredients.wireSteel)
				.key('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.addCriterion("has_"+toPath(Ingredients.wireSteel), hasItem(Ingredients.wireSteel))
				.build(out, toRL(toPath(MetalDevices.razorWire)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.capacitorLV)
				.patternLine("iii")
				.patternLine("clc")
				.patternLine("wrw")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('l', IETags.getTagsFor(EnumMetals.LEAD).ingot)
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.addCriterion("has_lead_ingot", hasItem(IETags.getTagsFor(EnumMetals.LEAD).ingot))
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(MetalDevices.capacitorLV)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.capacitorMV)
				.patternLine("iii")
				.patternLine("ele")
				.patternLine("wrw")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('l', IETags.getTagsFor(EnumMetals.LEAD).ingot)
				.key('e', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('r', Tags.Items.STORAGE_BLOCKS_REDSTONE)
				.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.addCriterion("has_lead_ingot", hasItem(IETags.getTagsFor(EnumMetals.LEAD).ingot))
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(MetalDevices.capacitorMV)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.capacitorHV)
				.patternLine("sss")
				.patternLine("ala")
				.patternLine("wrw")
				.key('s', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('a', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.key('l', IETags.getItemTag(IETags.getTagsFor(EnumMetals.LEAD).storage))
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('r', Tags.Items.STORAGE_BLOCKS_REDSTONE)
				.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(MetalDevices.capacitorHV)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.barrel)
				.patternLine("sss")
				.patternLine("w w")
				.patternLine("www")
				.key('w', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.key('s', IEBlocks.toSlab.get(IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON)))
				.addCriterion("has_iron_sheet_slab", hasItem(IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON)))
				.build(out, toRL(toPath(MetalDevices.barrel)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.fluidPump)
				.patternLine(" i ")
				.patternLine("ici")
				.patternLine("ppp")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('c', Ingredients.componentIron)
				.key('p', IEBlocks.MetalDevices.fluidPipe)
				.addCriterion("has_"+toPath(IEBlocks.MetalDevices.fluidPipe), hasItem(IEBlocks.MetalDevices.fluidPipe))
				.build(out, toRL(toPath(MetalDevices.fluidPump)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.blastFurnacePreheater)
				.patternLine("sss")
				.patternLine("s s")
				.patternLine("shs")
				.key('s', IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON))
				.key('h', MetalDevices.furnaceHeater)
				.addCriterion("has_"+toPath(MetalDevices.furnaceHeater), hasItem(MetalDevices.furnaceHeater))
				.build(out, toRL(toPath(MetalDevices.blastFurnacePreheater)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.furnaceHeater)
				.patternLine("ici")
				.patternLine("clc")
				.patternLine("iri")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('l', MetalDecoration.lvCoil)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
				.build(out, toRL(toPath(MetalDevices.furnaceHeater)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.dynamo)
				.patternLine("rlr")
				.patternLine("iii")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('l', MetalDecoration.lvCoil)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
				.build(out, toRL(toPath(MetalDevices.dynamo)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.thermoelectricGen)
				.patternLine("iii")
				.patternLine("ele")
				.patternLine("eee")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('l', MetalDecoration.lvCoil)
				.key('e', IETags.getTagsFor(EnumMetals.CONSTANTAN).plate)
				.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
				.build(out, toRL(toPath(MetalDevices.thermoelectricGen)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.electricLantern)
				.patternLine(" i ")
				.patternLine("pep")
				.patternLine("iri")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('e', Ingredients.electronTube)
				.key('p', Tags.Items.GLASS_PANES)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.addCriterion("has_"+toPath(Ingredients.electronTube), hasItem(Ingredients.electronTube))
				.build(out, toRL(toPath(MetalDevices.electricLantern)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.chargingStation)
				.patternLine("ici")
				.patternLine("ggg")
				.patternLine("wlw")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('l', MetalDecoration.lvCoil)
				.key('g', Tags.Items.GLASS)
				.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
				.build(out, toRL(toPath(MetalDevices.chargingStation)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.fluidPipe, 8)
				.patternLine("ppp")
				.patternLine("   ")
				.patternLine("ppp")
				.key('p', IETags.getTagsFor(EnumMetals.IRON).plate)
				.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
				.build(out, toRL(toPath(MetalDevices.fluidPipe)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.sampleDrill)
				.patternLine("sfs")
				.patternLine("sfs")
				.patternLine("efe")
				.key('s', MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))
				.key('f', MetalDecoration.steelFence)
				.key('e', MetalDecoration.engineeringLight)
				.addCriterion("has_"+toPath(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)), hasItem(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)))
				.addCriterion("has_"+toPath(MetalDecoration.steelFence), hasItem(MetalDecoration.steelFence))
				.addCriterion("has_"+toPath(MetalDecoration.engineeringLight), hasItem(MetalDecoration.engineeringLight))
				.build(out, toRL(toPath(MetalDevices.sampleDrill)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.teslaCoil)
				.patternLine("iii")
				.patternLine(" l ")
				.patternLine("hlh")
				.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.key('l', MetalDecoration.lvCoil)
				.key('h', MetalDevices.capacitorHV)
				.addCriterion("has_"+toPath(MetalDevices.capacitorHV), hasItem(MetalDevices.capacitorHV))
				.build(out, toRL(toPath(MetalDevices.teslaCoil)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.floodlight)
				.patternLine("iii")
				.patternLine("pel")
				.patternLine("ici")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('l', MetalDecoration.lvCoil)
				.key('e', Ingredients.electronTube)
				.key('c', Ingredients.componentIron)
				.key('p', Tags.Items.GLASS_PANES)
				.addCriterion("has_"+toPath(Ingredients.electronTube), hasItem(Ingredients.electronTube))
				.build(out, toRL(toPath(MetalDevices.floodlight)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.turretChem)
				.patternLine(" s ")
				.patternLine(" gc")
				.patternLine("bte")
				.key('s', Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
				.key('g', Weapons.chemthrower)
				.key('c', Ingredients.circuitBoard)
				.key('b', MetalDevices.barrel)
				.key('t', WoodenDevices.turntable)
				.key('e', MetalDecoration.engineeringRS)
				.addCriterion("has_"+toPath(Weapons.chemthrower), hasItem(Weapons.chemthrower))
				.build(out, toRL(toPath(MetalDevices.turretChem)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.turretGun)
				.patternLine(" s ")
				.patternLine(" gc")
				.patternLine("bte")
				.key('s', Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
				.key('g', Weapons.revolver)
				.key('c', Ingredients.circuitBoard)
				.key('b', Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))
				.key('t', WoodenDevices.turntable)
				.key('e', MetalDecoration.engineeringRS)
				.addCriterion("has_"+toPath(Weapons.revolver), hasItem(Weapons.revolver))
				.build(out, toRL(toPath(MetalDevices.turretGun)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.cloche)
				.patternLine("geg")
				.patternLine("g g")
				.patternLine("wcw")
				.key('g', Tags.Items.GLASS)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('e', Ingredients.electronTube)
				.key('c', Ingredients.componentIron)
				.addCriterion("has_"+toPath(Ingredients.electronTube), hasItem(Ingredients.electronTube))
				.build(out, toRL(toPath(MetalDevices.cloche)));
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.fluidPlacer)
				.patternLine("ibi")
				.patternLine("b b")
				.patternLine("ibi")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('b', Items.IRON_BARS)
				.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
				.build(out, toRL(toPath(MetalDevices.fluidPlacer)));
		for(Entry<EnumMetals, Block> chute : MetalDevices.chutes.entrySet())
			ShapedRecipeBuilder.shapedRecipe(chute.getValue(), 12)
					.patternLine("s s")
					.patternLine("s s")
					.patternLine("s s")
					.key('s', IETags.getItemTag(IETags.getTagsFor(chute.getKey()).sheetmetal))
					.addCriterion("has_plate", hasItem(IETags.getTagsFor(chute.getKey()).plate))
					.build(out, toRL(toPath(chute.getValue())));
	}

	private void recipesConnectors(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.breakerswitch)
				.patternLine(" l ")
				.patternLine("cic")
				.key('l', Items.LEVER)
				.key('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('c', Blocks.TERRACOTTA)
				.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.COPPER)), hasItem(Misc.wireCoils.get(WireType.COPPER)))
				.build(out, toRL(toPath(Connectors.breakerswitch)));
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.redstoneBreaker)
				.patternLine("h h")
				.patternLine("ici")
				.patternLine("iri")
				.key('h', IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false))
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('c', Items.REPEATER)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.addCriterion("has_hv_connector", hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)))
				.build(out, toRL(toPath(Connectors.redstoneBreaker)));

		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.currentTransformer)
				.patternLine(" m ")
				.patternLine("bcb")
				.patternLine("ici")
				.key('m', IEItems.Tools.voltmeter)
				.key('b', Blocks.TERRACOTTA)
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('c', MetalDecoration.lvCoil)
				.addCriterion("has_voltmeter", hasItem(IEItems.Tools.voltmeter))
				.build(out, toRL(toPath(Connectors.currentTransformer)));

		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.transformer)
				.patternLine("l m")
				.patternLine("ibi")
				.patternLine("iii")
				.key('l', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.key('m', IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false))
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('b', MetalDecoration.mvCoil)
				.addCriterion("has_mv_connector", hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false)))
				.build(out, toRL(toPath(Connectors.transformer)));
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.transformerHV)
				.patternLine("m h")
				.patternLine("ibi")
				.patternLine("iii")
				.key('m', IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false))
				.key('h', IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false))
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('b', MetalDecoration.hvCoil)
				.addCriterion("has_hv_connector", hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)))
				.build(out, toRL(toPath(Connectors.transformerHV)));

		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.connectorStructural, 8)
				.patternLine("isi")
				.patternLine("i i")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('s', IETags.steelRod)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Connectors.connectorStructural)));

		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.connectorRedstone, 4)
				.patternLine("iii")
				.patternLine("brb")
				.key('i', IETags.getTagsFor(EnumMetals.ELECTRUM).nugget)
				.key('b', Blocks.TERRACOTTA)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.addCriterion("has_electrum_nugget", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).nugget))
				.build(out, toRL(toPath(Connectors.connectorRedstone)));
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.connectorProbe)
				.patternLine(" c ")
				.patternLine("gpg")
				.patternLine(" q ")
				.key('c', Connectors.connectorRedstone)
				.key('g', Tags.Items.GLASS_PANES)
				.key('p', Ingredients.circuitBoard)
				.key('q', Tags.Items.GEMS_QUARTZ)
				.addCriterion("has_connector", hasItem(Connectors.connectorRedstone))
				.build(out, toRL(toPath(Connectors.connectorProbe)));
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.connectorBundled)
				.patternLine(" w ")
				.patternLine("wcw")
				.patternLine(" w ")
				.key('c', Connectors.connectorRedstone)
				.key('w', IETags.aluminumWire)
				.addCriterion("has_connector", hasItem(Connectors.connectorRedstone))
				.build(out, toRL(toPath(Connectors.connectorBundled)));

		// Connectors and Relays
		ShapedRecipeBuilder.shapedRecipe(Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), 4)
				.patternLine(" i ")
				.patternLine("cic")
				.patternLine("cic")
				.key('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('c', Blocks.TERRACOTTA)
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL("connector_lv"));
		ShapedRecipeBuilder.shapedRecipe(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), 8)
				.patternLine(" i ")
				.patternLine("cic")
				.key('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('c', Blocks.TERRACOTTA)
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL("connector_lv_relay"));
		ShapedRecipeBuilder.shapedRecipe(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), 4)
				.patternLine(" i ")
				.patternLine("cic")
				.patternLine("cic")
				.key('i', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.key('c', Blocks.TERRACOTTA)
				.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.build(out, toRL("connector_mv"));
		ShapedRecipeBuilder.shapedRecipe(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), 8)
				.patternLine(" i ")
				.patternLine("cic")
				.key('i', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.key('c', Blocks.TERRACOTTA)
				.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.build(out, toRL("connector_mv_relay"));
		ShapedRecipeBuilder.shapedRecipe(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), 4)
				.patternLine(" i ")
				.patternLine("cic")
				.patternLine("cic")
				.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.key('c', Blocks.TERRACOTTA)
				.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.build(out, toRL("connector_hv"));
		ShapedRecipeBuilder.shapedRecipe(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), 8)
				.patternLine(" i ")
				.patternLine("cic")
				.patternLine("cic")
				.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.key('c', StoneDecoration.insulatingGlass)
				.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.build(out, toRL("connector_hv_relay"));
	}

	private void recipesConveyors(@Nonnull Consumer<IFinishedRecipe> out)
	{
		IItemProvider basic = ConveyorHandler.getBlock(BasicConveyor.NAME);
		IItemProvider redstone = ConveyorHandler.getBlock(RedstoneConveyor.NAME);
		IItemProvider covered = ConveyorHandler.getBlock(CoveredConveyor.NAME);
		IItemProvider dropper = ConveyorHandler.getBlock(DropConveyor.NAME);
		IItemProvider dropperCovered = ConveyorHandler.getBlock(DropCoveredConveyor.NAME);
		IItemProvider extract = ConveyorHandler.getBlock(ExtractConveyor.NAME);
		IItemProvider extractCovered = ConveyorHandler.getBlock(ExtractCoveredConveyor.NAME);
		IItemProvider splitter = ConveyorHandler.getBlock(SplitConveyor.NAME);
		IItemProvider splitterCovered = ConveyorHandler.getBlock(SplitCoveredConveyor.NAME);
		IItemProvider vertical = ConveyorHandler.getBlock(VerticalConveyor.NAME);
		IItemProvider verticalCovered = ConveyorHandler.getBlock(VerticalCoveredConveyor.NAME);
		addCoveyorCoveringRecipe(covered, basic, out);
		addCoveyorCoveringRecipe(dropperCovered, dropper, out);
		addCoveyorCoveringRecipe(extractCovered, extract, out);
		addCoveyorCoveringRecipe(splitterCovered, splitter, out);
		addCoveyorCoveringRecipe(verticalCovered, vertical, out);
		ShapedRecipeBuilder.shapedRecipe(basic, 8)
				.patternLine("lll")
				.patternLine("iri")
				.key('l', Tags.Items.LEATHER)
				.key('i', Tags.Items.INGOTS_IRON)
				.key('r', Tags.Items.DUSTS_REDSTONE)
				.addCriterion("has_leather", hasItem(Items.LEATHER))
				.build(out, toRL(toPath(basic)));
		//TODO
		//ShapedRecipeBuilder.shapedRecipe(basic, 8)
		//		.patternLine("rrr")
		//		.patternLine("iri")
		//		.key('r', RUBBER)
		//		.key('i', Tags.Items.INGOTS_IRON)
		//		.key('r', Tags.Items.DUSTS_REDSTONE)
		//		.build(out);
		ShapedRecipeBuilder.shapedRecipe(redstone)
				.patternLine("c")
				.patternLine("r")
				.key('c', basic)
				.key('r', Blocks.REDSTONE_TORCH)
				.addCriterion("has_conveyor", hasItem(basic))
				.build(out, toRL(toPath(redstone)));
		ShapedRecipeBuilder.shapedRecipe(dropper)
				.patternLine("c")
				.patternLine("t")
				.key('c', basic)
				.key('t', Blocks.IRON_TRAPDOOR)
				.addCriterion("has_trapdoor", hasItem(Blocks.IRON_TRAPDOOR))
				.addCriterion("has_conveyor", hasItem(basic))
				.build(out, toRL(toPath(dropper)));
		ShapedRecipeBuilder.shapedRecipe(extract)
				.patternLine("ws")
				.patternLine("mc")
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('s', Cloth.curtain)
				.key('m', Ingredients.componentIron)
				.key('c', basic)
				.addCriterion("has_conveyor", hasItem(basic))
				.build(out, toRL(toPath(extract)));
		ShapedRecipeBuilder.shapedRecipe(splitter, 3)
				.patternLine("cic")
				.patternLine(" c ")
				.key('c', basic)
				.key('i', Tags.Items.INGOTS_IRON)
				.addCriterion("has_conveyor", hasItem(basic))
				.build(out, toRL(toPath(splitter)));
		ShapedRecipeBuilder.shapedRecipe(vertical, 3)
				.patternLine("ci")
				.patternLine("c ")
				.patternLine("ci")
				.key('c', basic)
				.key('i', Tags.Items.INGOTS_IRON)
				.addCriterion("has_conveyor", hasItem(basic))
				.build(out, toRL(toPath(vertical)));
	}

	private void addCoveyorCoveringRecipe(IItemProvider covered, IItemProvider base, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(covered)
				.patternLine("s")
				.patternLine("c")
				.key('s', IETags.getItemTag(IETags.scaffoldingSteel))
				.key('c', base)
				.addCriterion("has_vertical_conveyor", hasItem(base))
				.build(out, toRL(toPath(covered)));
	}

	private void recipesCloth(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Cloth.balloon, 2)
				.patternLine(" f ")
				.patternLine("ftf")
				.patternLine(" s ")
				.key('f', IEItems.Ingredients.hempFabric)
				.key('t', Items.TORCH)
				.key('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
				.build(out, toRL(toPath(Cloth.balloon)));
		ShapedRecipeBuilder.shapedRecipe(Cloth.cushion, 3)
				.patternLine("fff")
				.patternLine("f f")
				.patternLine("fff")
				.key('f', IEItems.Ingredients.hempFabric)
				.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
				.build(out, toRL(toPath(Cloth.cushion)));
		ShapedRecipeBuilder.shapedRecipe(Cloth.curtain, 3)
				.patternLine("sss")
				.patternLine("fff")
				.patternLine("fff")
				.key('s', IETags.metalRods)
				.key('f', IEItems.Ingredients.hempFabric)
				.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
				.addCriterion("has_metal_rod", hasItem(IETags.metalRods))
				.build(out, toRL(toPath(Cloth.curtain)));
	}

	private void recipesTools(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Tools.hammer)
				.patternLine(" if")
				.patternLine(" si")
				.patternLine("s  ")
				.key('s', Tags.Items.RODS_WOODEN)
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('f', Tags.Items.STRING)
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.build(out, toRL(toPath(Tools.hammer)));
		ShapedRecipeBuilder.shapedRecipe(Tools.wirecutter)
				.patternLine("si")
				.patternLine(" s")
				.key('s', Tags.Items.RODS_WOODEN)
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.build(out, toRL(toPath(Tools.wirecutter)));
		ShapedRecipeBuilder.shapedRecipe(Tools.screwdriver)
				.patternLine(" i")
				.patternLine("s ")
				.key('s', Tags.Items.RODS_WOODEN)
				.key('i', IETags.ironRod)
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.build(out, toRL(toPath(Tools.screwdriver)));
		ShapelessRecipeBuilder.shapelessRecipe(Tools.manual)
				.addIngredient(Items.BOOK)
				.addIngredient(Items.LEVER)
				.addCriterion("has_book", hasItem(Items.BOOK))
				.build(out, toRL(toPath(Tools.manual)));
		ShapedRecipeBuilder.shapedRecipe(Tools.steelAxe)
				.patternLine("ii")
				.patternLine("is")
				.patternLine(" s")
				.key('s', IETags.treatedStick)
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Tools.steelAxe)));
		ShapedRecipeBuilder.shapedRecipe(Tools.steelPick)
				.patternLine("iii")
				.patternLine(" s ")
				.patternLine(" s ")
				.key('s', IETags.treatedStick)
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Tools.steelPick)));
		ShapedRecipeBuilder.shapedRecipe(Tools.steelShovel)
				.patternLine("i")
				.patternLine("s")
				.patternLine("s")
				.key('s', IETags.treatedStick)
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Tools.steelShovel)));
		ShapedRecipeBuilder.shapedRecipe(Tools.steelHoe)
				.patternLine("ii")
				.patternLine(" s")
				.patternLine(" s")
				.key('s', IETags.treatedStick)
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Tools.steelHoe)));
		ShapedRecipeBuilder.shapedRecipe(Tools.steelSword)
				.patternLine("i")
				.patternLine("i")
				.patternLine("s")
				.key('s', IETags.treatedStick)
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Tools.steelSword)));

		addArmor(IETags.getTagsFor(EnumMetals.STEEL).plate, Tools.steelArmor, "steel_plate", out);

		ShapedRecipeBuilder.shapedRecipe(Tools.toolbox)
				.patternLine("ppp")
				.patternLine("rcr")
				.key('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.key('r', Tags.Items.DYES_RED)
				.key('c', IEBlocks.WoodenDevices.crate)
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.addCriterion("has_red_dye", hasItem(Items.RED_DYE))
				.addCriterion("has_"+toPath(IEBlocks.WoodenDevices.crate), hasItem(IEBlocks.WoodenDevices.crate))
				.build(out, toRL(toPath(Tools.toolbox)));
		ShapedRecipeBuilder.shapedRecipe(Tools.voltmeter)
				.patternLine(" p ")
				.patternLine("scs")
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('p', Items.COMPASS)
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.addCriterion("has_compass", hasItem(Items.COMPASS))
				.addCriterion("has_treated_sticks", hasItem(IETags.treatedStick))
				.build(out, toRL(toPath(Tools.voltmeter)));

		ShapedRecipeBuilder.shapedRecipe(Tools.drill)
				.patternLine("  g")
				.patternLine(" hg")
				.patternLine("c  ")
				.key('g', Ingredients.woodenGrip)
				.key('h', MetalDecoration.engineeringHeavy)
				.key('c', Ingredients.componentIron)
				.addCriterion("has_"+toPath(MetalDecoration.engineeringHeavy), hasItem(MetalDecoration.engineeringHeavy))
				.build(out, toRL(toPath(Tools.drill)));
		ShapedRecipeBuilder.shapedRecipe(Tools.drillheadIron)
				.patternLine("  i")
				.patternLine("ii ")
				.patternLine("bi ")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('b', makeIngredient(Tags.Items.STORAGE_BLOCKS_IRON))
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.build(out, toRL(toPath(Tools.drillheadIron)));
		ShapedRecipeBuilder.shapedRecipe(Tools.drillheadSteel)
				.patternLine("  i")
				.patternLine("ii ")
				.patternLine("bi ")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('b', makeIngredientFromBlock(IETags.getTagsFor(EnumMetals.STEEL).storage))
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Tools.drillheadSteel)));

		ShapedRecipeBuilder.shapedRecipe(Tools.buzzsaw)
				.patternLine("  g")
				.patternLine("rcg")
				.patternLine("r  ")
				.key('g', Ingredients.woodenGrip)
				.key('c', Ingredients.componentSteel)
				.key('r', IETags.steelRod)
				.addCriterion("has_"+toPath(Ingredients.componentSteel), hasItem(Ingredients.componentSteel))
				.build(out, toRL(toPath(Tools.buzzsaw)));
		ShapedRecipeBuilder.shapedRecipe(Tools.sawblade)
				.patternLine("ipi")
				.patternLine("p p")
				.patternLine("ipi")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Tools.sawblade)));
		ShapedRecipeBuilder.shapedRecipe(Tools.rockcutter)
				.patternLine("ipi")
				.patternLine("p p")
				.patternLine("ipi")
				.key('i', Tags.Items.GEMS_DIAMOND)
				.key('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Tools.rockcutter)));

		ShapedRecipeBuilder.shapedRecipe(Tools.surveyTools)
				.patternLine("cbh")
				.patternLine("fff")
				.key('b', Items.GLASS_BOTTLE)
				.key('h', Tools.hammer)
				.key('c', Items.WRITABLE_BOOK)
				.key('f', IETags.fabricHemp)
				.addCriterion("has_"+toPath(Tools.hammer), hasItem(Tools.hammer))
				.build(out, toRL(toPath(Tools.surveyTools)));
	}

	private void recipesIngredients(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickTreated, 4)
				.patternLine("w")
				.patternLine("w")
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.setGroup("sticks")
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(Ingredients.stickTreated)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickIron, 4)
				.patternLine("i")
				.patternLine("i")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.setGroup("sticks")
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.build(out, toRL(toPath(Ingredients.stickIron)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickSteel, 4)
				.patternLine("i")
				.patternLine("i")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.setGroup("sticks")
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Ingredients.stickSteel)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickAluminum, 4)
				.patternLine("i")
				.patternLine("i")
				.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.setGroup("sticks")
				.addCriterion("has_alu_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.build(out, toRL(toPath(Ingredients.stickAluminum)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.hempFabric)
				.patternLine("fff")
				.patternLine("fsf")
				.patternLine("fff")
				.key('f', IETags.fiberHemp)
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_hemp_fiber", hasItem(IETags.fiberHemp))
				.build(out, toRL(toPath(Ingredients.hempFabric)));

		ShapedRecipeBuilder.shapedRecipe(Ingredients.componentIron)
				.patternLine("i i")
				.patternLine(" c ")
				.patternLine("i i")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.build(out, toRL(toPath(Ingredients.componentIron)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.componentSteel)
				.patternLine("i i")
				.patternLine(" c ")
				.patternLine("i i")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Ingredients.componentSteel)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.waterwheelSegment)
				.patternLine(" s ")
				.patternLine("sbs")
				.patternLine("bsb")
				.key('s', IETags.treatedStick)
				.key('b', IETags.getItemTag(IETags.treatedWood))
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(Ingredients.waterwheelSegment)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.windmillBlade)
				.patternLine("bb ")
				.patternLine("ssb")
				.patternLine("ss ")
				.key('s', IETags.treatedStick)
				.key('b', IETags.getItemTag(IETags.treatedWood))
				.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
				.build(out, toRL(toPath(Ingredients.windmillBlade)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.windmillSail)
				.patternLine(" cc")
				.patternLine("ccc")
				.patternLine(" c ")
				.key('c', IETags.fabricHemp)
				.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
				.build(out, toRL(toPath(Ingredients.windmillSail)));

		ShapedRecipeBuilder.shapedRecipe(Ingredients.woodenGrip)
				.patternLine("ss")
				.patternLine("cs")
				.patternLine("ss")
				.key('s', IETags.treatedStick)
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).nugget)
				.addCriterion("has_treated_sticks", hasItem(IETags.treatedStick))
				.build(out, toRL(toPath(Ingredients.woodenGrip)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartBarrel)
				.patternLine("ss")
				.key('s', IETags.steelRod)
				.addCriterion("has_"+toPath(Ingredients.stickSteel), hasItem(IETags.steelRod))
				.build(out, toRL(toPath(Ingredients.gunpartBarrel)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartDrum)
				.patternLine(" i ")
				.patternLine("ici")
				.patternLine(" i ")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('c', Ingredients.componentSteel)
				.addCriterion("has_ingot_steel", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Ingredients.gunpartDrum)));
		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartHammer)
				.patternLine("i  ")
				.patternLine("ii ")
				.patternLine(" ii")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_ingot_steel", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Ingredients.gunpartHammer)));

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.DRILL_WATERPROOF))
				.patternLine("bl ")
				.patternLine("lbl")
				.patternLine(" lc")
				.key('b', Items.BUCKET)
				.key('l', Tags.Items.GEMS_LAPIS)
				.key('c', Ingredients.componentIron)
				.addCriterion("has_drill", hasItem(Tools.drill))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_WATERPROOF))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.DRILL_LUBE))
				.patternLine("bi ")
				.patternLine("ibi")
				.patternLine(" ic")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('c', Ingredients.componentIron)
				.key('b', new IngredientFluidStack(IETags.fluidPlantoil, 1000))
				.addCriterion("has_drill", hasItem(Tools.drill))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_LUBE))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.DRILL_DAMAGE))
				.patternLine("iii")
				.patternLine(" c ")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('c', Ingredients.componentIron)
				.addCriterion("has_drill", hasItem(Tools.drill))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_DAMAGE))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY))
				.patternLine("ci ")
				.patternLine("ibr")
				.patternLine(" rb")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('c', Ingredients.componentIron)
				.key('r', Tags.Items.DYES_RED)
				.key('b', Items.BUCKET)
				.addCriterion("has_drill", hasItem(Tools.drill))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY))));

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_BAYONET))
				.patternLine("si")
				.patternLine("iw")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('s', Items.IRON_SWORD)
				.addCriterion("has_revolver", hasItem(Weapons.revolver))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_BAYONET))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))
				.patternLine(" ai")
				.patternLine("a a")
				.patternLine("ca ")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('a', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('c', Ingredients.componentIron)
				.addCriterion("has_revolver", hasItem(Weapons.revolver))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_ELECTRO))
				.patternLine("eee")
				.patternLine("rwr")
				.key('e', Ingredients.electronTube)
				.key('r', IETags.steelRod)
				.key('w', Ingredients.wireCopper)
				.addCriterion("has_revolver", hasItem(Weapons.revolver))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_ELECTRO))));

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_FOCUS))
				.patternLine(" ii")
				.patternLine("pph")
				.patternLine(" ii")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('p', MetalDevices.fluidPipe)
				.key('h', Items.HOPPER)
				.addCriterion("has_chemthrower", hasItem(Weapons.chemthrower))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_FOCUS))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_MULTITANK))
				.patternLine(" p ")
				.patternLine("tct")
				.key('p', MetalDevices.fluidPipe)
				.key('c', Ingredients.componentIron)
				.key('t', Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY))
				.addCriterion("has_chemthrower", hasItem(Weapons.chemthrower))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_MULTITANK))));

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
				.patternLine("pi ")
				.patternLine("c i")
				.patternLine(" cp")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('p', Tags.Items.GLASS_PANES)
				.addCriterion("has_railgun", hasItem(Weapons.railgun))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_CAPACITORS))
				.patternLine("p  ")
				.patternLine("ip ")
				.patternLine(" ip")
				.key('p', IETags.getTagsFor(EnumMetals.CONSTANTAN).plate)
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_railgun", hasItem(Weapons.railgun))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_CAPACITORS))));

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_FLASH))
				.patternLine("ipi")
				.patternLine("pep")
				.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.key('p', Tags.Items.GLASS_PANES)
				.key('e', Ingredients.electronTube)
				.addCriterion("has_shield", hasItem(Misc.shield))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_FLASH))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_SHOCK))
				.patternLine("crc")
				.patternLine("crc")
				.patternLine("crc")
				.key('r', IETags.ironRod)
				.key('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.addCriterion("has_shield", hasItem(Misc.shield))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_SHOCK))));
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_MAGNET))
				.patternLine("  l")
				.patternLine("lc ")
				.patternLine("lil")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('l', Tags.Items.LEATHER)
				.key('c', MetalDecoration.lvCoil)
				.addCriterion("has_shield", hasItem(Misc.shield))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_MAGNET))));

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.BUZZSAW_SPAREBLADES))
				.patternLine("rht")
				.patternLine("rt ")
				.key('r', IETags.ironRod)
				.key('h', IETags.fiberHemp)
				.key('t', IETags.getItemTag(IETags.treatedWood))
				.addCriterion("has_buzzsaw", hasItem(Tools.buzzsaw))
				.build(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.BUZZSAW_SPAREBLADES))));

		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireCopper)
				.addIngredient(IETags.getTagsFor(EnumMetals.COPPER).plate)
				.addIngredient(Tools.wirecutter)
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL(toPath(Ingredients.wireCopper)));
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireElectrum)
				.addIngredient(IETags.getTagsFor(EnumMetals.ELECTRUM).plate)
				.addIngredient(Tools.wirecutter)
				.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.build(out, toRL(toPath(Ingredients.wireElectrum)));
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireAluminum)
				.addIngredient(IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.addIngredient(Tools.wirecutter)
				.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.build(out, toRL(toPath(Ingredients.wireAluminum)));
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireSteel)
				.addIngredient(IETags.getTagsFor(EnumMetals.STEEL).plate)
				.addIngredient(Tools.wirecutter)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Ingredients.wireSteel)));

		ShapelessRecipeBuilder.shapelessRecipe(Metals.dusts.get(EnumMetals.ELECTRUM))
				.addIngredient(IETags.getTagsFor(EnumMetals.GOLD).dust)
				.addIngredient(IETags.getTagsFor(EnumMetals.SILVER).dust)
				.addCriterion("has_gold_dust", hasItem(IETags.getTagsFor(EnumMetals.GOLD).dust))
				.build(out, toRL("electrum_mix"));
		ShapelessRecipeBuilder.shapelessRecipe(Metals.dusts.get(EnumMetals.CONSTANTAN))
				.addIngredient(IETags.getTagsFor(EnumMetals.COPPER).dust)
				.addIngredient(IETags.getTagsFor(EnumMetals.NICKEL).dust)
				.addCriterion("has_nickel_dust", hasItem(IETags.getTagsFor(EnumMetals.NICKEL).dust))
				.build(out, toRL("constantan_mix"));

		ShapedRecipeBuilder.shapedRecipe(Misc.blueprint)
				.patternLine("jkl")
				.patternLine("ddd")
				.patternLine("ppp")
				.key('j', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.key('l', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('k', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.key('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.key('p', Items.PAPER)
				.addCriterion("has_"+toPath(Items.PAPER), hasItem(Items.PAPER))
				.build(buildBlueprint(out, "components"), toRL("blueprint_components"));
		ShapedRecipeBuilder.shapedRecipe(Misc.blueprint)
				.patternLine(" P ")
				.patternLine("ddd")
				.patternLine("ppp")
				.key('P', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.key('p', Items.PAPER)
				.addCriterion("has_"+toPath(Items.PAPER), hasItem(Items.PAPER))
				.build(buildBlueprint(out, "molds"), toRL("blueprint_molds"));
		ShapedRecipeBuilder.shapedRecipe(Misc.blueprint)
				.patternLine("gcg")
				.patternLine("ddd")
				.patternLine("ppp")
				.key('g', Tags.Items.GUNPOWDER)
				.key('c', Ingredients.emptyCasing)
				.key('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.key('p', Items.PAPER)
				.addCriterion("has_"+toPath(Items.PAPER), hasItem(Items.PAPER))
				.build(buildBlueprint(out, "bullet"), toRL("blueprint_bullets"));

		ShapedRecipeBuilder.shapedRecipe(Misc.blueprint)
				.patternLine("ggg")
				.patternLine("ddd")
				.patternLine("ppp")
				.key('g', IETags.hopGraphiteIngot)
				.key('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.key('p', Items.PAPER)
				.addCriterion("has_"+toPath(Items.PAPER), hasItem(Items.PAPER))
				.build(buildBlueprint(out, "electrode", new ConditionIEConfig(true, "machines.arcfurnace_electrodeCrafting")),
						toRL("blueprint_electrode"));
	}

	private void recipesVanilla(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Items.TORCH, 12)
				.patternLine("wc ")
				.patternLine("sss")
				.key('w', ItemTags.WOOL)
				.key('c', new IngredientFluidStack(IETags.fluidCreosote, 1000))
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_wool", hasItem(ItemTags.WOOL))
				.addCriterion("has_stick", hasItem(Tags.Items.RODS_WOODEN))
				.addCriterion("has_creosote", hasItem(IEContent.fluidCreosote.getFilledBucket()))
				.build(out, toRL(toPath(Items.TORCH)));
		ShapelessRecipeBuilder.shapelessRecipe(Items.STRING)
				.addIngredient(Ingredient.fromTag(IETags.fiberHemp), 3)
				.addCriterion("has_hemp_fiber", hasItem(Ingredients.hempFiber))
				.build(out, toRL(toPath(Items.STRING)));
		ShapelessRecipeBuilder.shapelessRecipe(Items.GUNPOWDER)
				.addIngredient(Ingredient.fromTag(IETags.saltpeterDust), 4)
				.addIngredient(IETags.sulfurDust)
				.addIngredient(Items.CHARCOAL)
				.addCriterion("has_sulfur", hasItem(IETags.sulfurDust))
				.build(out, toRL("gunpowder_from_dusts"));
	}

	private Consumer<IFinishedRecipe> buildBlueprint(Consumer<IFinishedRecipe> out, String blueprint, ICondition... conditions)
	{
		return recipe -> {
			out.accept(new IFinishedRecipe()
			{
				@Override
				public void serialize(@Nonnull JsonObject json)
				{
					if(conditions.length > 0)
					{
						JsonArray conditionArray = new JsonArray();
						for(ICondition condition : conditions)
							conditionArray.add(CraftingHelper.serialize(condition));
						json.add("conditions", conditionArray);
					}

					recipe.serialize(json);
					JsonObject output = json.getAsJsonObject("result");
					JsonObject nbt = new JsonObject();
					nbt.addProperty("blueprint", blueprint);
					output.add("nbt", nbt);
				}

				@Nonnull
				@Override
				public ResourceLocation getID()
				{
					return recipe.getID();
				}

				@Nonnull
				@Override
				public IRecipeSerializer<?> getSerializer()
				{
					return recipe.getSerializer();
				}

				@Nullable
				@Override
				public JsonObject getAdvancementJson()
				{
					return recipe.getAdvancementJson();
				}

				@Nullable
				@Override
				public ResourceLocation getAdvancementID()
				{
					return recipe.getAdvancementID();
				}
			});
		};
	}

	private void recipesWeapons(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Weapons.chemthrower)
				.patternLine(" tg")
				.patternLine(" hg")
				.patternLine("pb ")
				.key('g', Ingredients.woodenGrip)
				.key('p', MetalDevices.fluidPipe)
				.key('h', MetalDecoration.engineeringHeavy)
				.key('t', Misc.toolUpgrades.get(ToolUpgrade.DRILL_WATERPROOF))
				.key('b', Items.BUCKET)
				.addCriterion("has_"+toPath(Ingredients.woodenGrip), hasItem(Ingredients.woodenGrip))
				.build(out, toRL(toPath(Weapons.chemthrower)));
		ShapedRecipeBuilder.shapedRecipe(Weapons.railgun)
				.patternLine(" vg")
				.patternLine("icp")
				.patternLine("ci ")
				.key('g', Ingredients.woodenGrip)
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('v', MetalDevices.capacitorHV)
				.key('c', MetalDecoration.mvCoil)
				.key('p', Ingredients.circuitBoard)
				.addCriterion("has_"+toPath(MetalDevices.capacitorHV), hasItem(MetalDevices.capacitorHV))
				.build(out, toRL(toPath(Weapons.railgun)));
		ShapedRecipeBuilder.shapedRecipe(Misc.skyhook)
				.patternLine("ii ")
				.patternLine("ic ")
				.patternLine(" gg")
				.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.key('c', Ingredients.componentIron)
				.key('g', Ingredients.woodenGrip)
				.addCriterion("has_"+toPath(Ingredients.woodenGrip), hasItem(Ingredients.woodenGrip))
				.build(out, toRL(toPath(Misc.skyhook)));
		RevolverAssemblyRecipeBuilder.builder(Weapons.revolver)
				.setNBTCopyTargetRecipe(3, 4, 5)
				.patternLine(" i ")
				.patternLine("bdh")
				.patternLine("gig")
				.key('b', Ingredients.gunpartBarrel)
				.key('d', Ingredients.gunpartDrum)
				.key('h', Ingredients.gunpartHammer)
				.key('g', Ingredients.woodenGrip)
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.addCriterion("has_"+toPath(Ingredients.woodenGrip), hasItem(Ingredients.woodenGrip))
				.build(out, toRL(toPath(Weapons.revolver)));
		ShapedRecipeBuilder.shapedRecipe(Weapons.speedloader)
				.patternLine("  i")
				.patternLine("iis")
				.patternLine("  i")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.key('s', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(Weapons.speedloader)));

		ShapedRecipeBuilder.shapedRecipe(BulletHandler.emptyShell.getItem(), 5)
				.patternLine("prp")
				.patternLine("prp")
				.patternLine(" c ")
				.key('p', Items.PAPER)
				.key('r', Tags.Items.DYES_RED)
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.addCriterion("has_coppper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL(toPath(BulletHandler.emptyShell.getItem())));

		ShapedRecipeBuilder.shapedRecipe(BulletHandler.emptyCasing.getItem(), 5)
				.patternLine("c c")
				.patternLine("c c")
				.patternLine(" c ")
				.key('c', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.addCriterion("has_coppper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL(toPath(BulletHandler.emptyCasing.getItem())));

		BulletHandler.getBulletStack(BulletItem.FLARE);
		TurnAndCopyRecipeBuilder.builder(BulletHandler.getBulletItem(BulletItem.FIREWORK))
				.setNBTCopyTargetRecipe(0, 1, 2, 3, 4, 5, 6) //Since this isn't relative positioning, we have to account for the top 6 slots >_>
				.patternLine("f")
				.patternLine("c")
				.key('f', Items.FIREWORK_ROCKET)
				.key('c', Ingredients.emptyShell)
				.addCriterion("has_firework", hasItem(Items.FIREWORK_ROCKET))
				.build(out, toRL(toPath(BulletHandler.getBulletItem(BulletItem.FIREWORK))));
	}

	private void recipesMisc(@Nonnull Consumer<IFinishedRecipe> out)
	{
		Item wireCoilCopper = Misc.wireCoils.get(WireType.COPPER);
		ShapedRecipeBuilder.shapedRecipe(wireCoilCopper, 4)
				.patternLine(" w ")
				.patternLine("wsw")
				.patternLine(" w ")
				.key('w', IETags.copperWire)
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.build(out, toRL(toPath(wireCoilCopper)));
		Item wireCoilElectrum = Misc.wireCoils.get(WireType.ELECTRUM);
		ShapedRecipeBuilder.shapedRecipe(wireCoilElectrum, 4)
				.patternLine(" w ")
				.patternLine("wsw")
				.patternLine(" w ")
				.key('w', IETags.electrumWire)
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.build(out, toRL(toPath(wireCoilElectrum)));
		Item wireCoilSteel = Misc.wireCoils.get(WireType.STEEL);
		TurnAndCopyRecipeBuilder.builder(wireCoilSteel, 4)
				.allowQuarterTurn()
				.patternLine(" w ")
				.patternLine("asa")
				.patternLine(" w ")
				.key('w', IETags.steelWire)
				.key('a', IETags.aluminumWire)
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(wireCoilSteel)));

		Item wireCoilRope = Misc.wireCoils.get(WireType.STRUCTURE_ROPE);
		ShapedRecipeBuilder.shapedRecipe(wireCoilRope, 4)
				.patternLine(" w ")
				.patternLine("wsw")
				.patternLine(" w ")
				.key('w', Ingredients.hempFiber)
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_hemp_fiber", hasItem(Ingredients.hempFiber))
				.build(out, toRL(toPath(wireCoilRope)));
		Item wireCoilStructure = Misc.wireCoils.get(WireType.STRUCTURE_STEEL);
		ShapedRecipeBuilder.shapedRecipe(wireCoilStructure, 4)
				.patternLine(" w ")
				.patternLine("wsw")
				.patternLine(" w ")
				.key('w', Ingredients.wireSteel)
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.build(out, toRL(toPath(wireCoilStructure)));
		addCornerStraightMiddle(Misc.wireCoils.get(WireType.COPPER_INSULATED), 4,
				makeIngredient(IETags.fabricHemp),
				makeIngredient(Misc.wireCoils.get(WireType.COPPER)),
				makeIngredient(IETags.fabricHemp), out);
		addCornerStraightMiddle(Misc.wireCoils.get(WireType.ELECTRUM_INSULATED), 4,
				makeIngredient(IETags.fabricHemp),
				makeIngredient(Misc.wireCoils.get(WireType.ELECTRUM)),
				makeIngredient(IETags.fabricHemp), out);
		Item wireCoilRedstone = Misc.wireCoils.get(WireType.REDSTONE);
		TurnAndCopyRecipeBuilder.builder(wireCoilRedstone, 4)
				.allowQuarterTurn()
				.patternLine(" w ")
				.patternLine("asa")
				.patternLine(" w ")
				.key('w', IETags.aluminumWire)
				.key('a', Tags.Items.DUSTS_REDSTONE)
				.key('s', Tags.Items.RODS_WOODEN)
				.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.build(out, toRL(toPath(wireCoilRedstone)));

		ShapedRecipeBuilder.shapedRecipe(Misc.jerrycan)
				.patternLine(" ii")
				.patternLine("ibb")
				.patternLine("ibb")
				.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('b', Items.BUCKET)
				.addCriterion("has_bucket", hasItem(Items.BUCKET))
				.build(out, toRL("jerrycan"));
		ShapedRecipeBuilder.shapedRecipe(Misc.powerpack)
				.patternLine("lbl")
				.patternLine("wcw")
				.key('l', Tags.Items.LEATHER)
				.key('w', Misc.wireCoils.get(WireType.COPPER))
				.key('b', MetalDevices.capacitorLV)
				.key('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.addCriterion("has_leather", hasItem(Items.LEATHER))
				.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.addCriterion("has_"+toPath(MetalDevices.capacitorLV), hasItem(MetalDevices.capacitorLV))
				.addCriterion("has_"+toPath(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)), hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)))
				.build(out, toRL(toPath(Misc.powerpack)));
		ShapedRecipeBuilder.shapedRecipe(Misc.maintenanceKit)
				.patternLine("sc ")
				.patternLine("fff")
				.key('c', Tools.wirecutter)
				.key('s', Tools.screwdriver)
				.key('f', IETags.fabricHemp)
				.addCriterion("has_"+toPath(Tools.wirecutter), hasItem(Tools.wirecutter))
				.build(out, toRL(toPath(Misc.maintenanceKit)));
		ShapedRecipeBuilder.shapedRecipe(Misc.shield)
				.patternLine("sws")
				.patternLine("scs")
				.patternLine("sws")
				.key('s', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.key('w', IETags.getItemTag(IETags.treatedWood))
				.key('c', Items.SHIELD)
				.addCriterion("has_shield", hasItem(Items.SHIELD))
				.build(out, toRL(toPath(Misc.shield)));
		ShapedRecipeBuilder.shapedRecipe(Misc.fluorescentTube)
				.patternLine("GeG")
				.patternLine("GgG")
				.patternLine("GgG")
				.key('g', Tags.Items.DUSTS_GLOWSTONE)
				.key('e', Misc.graphiteElectrode)
				.key('G', Tags.Items.GLASS)
				.addCriterion("has_electrode", hasItem(Misc.graphiteElectrode))
				.build(out, toRL(toPath(Misc.fluorescentTube)));
		addArmor(IETags.getTagsFor(EnumMetals.ALUMINUM).plate, Misc.faradaySuit, "alu_plate", out);
		ShapedRecipeBuilder.shapedRecipe(Misc.earmuffs)
				.patternLine(" S ")
				.patternLine("S S")
				.patternLine("W W")
				.key('S', IETags.ironRod)
				.key('W', ItemTags.WOOL)
				.addCriterion("has_iron_rod", hasItem(IETags.ironRod))
				.build(out, toRL(toPath(Misc.earmuffs)));
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.lantern)
				.patternLine(" I ")
				.patternLine("PGP")
				.patternLine(" I ")
				.key('I', IETags.getTagsFor(EnumMetals.IRON).plate)
				.key('G', Tags.Items.DUSTS_GLOWSTONE)
				.key('P', Items.GLASS_PANE)
				.addCriterion("has_glowstone", hasItem(Tags.Items.DUSTS_GLOWSTONE))
				.build(out, toRL(toPath(MetalDecoration.lantern)));

		ShapedRecipeBuilder.shapedRecipe(Misc.cartWoodenCrate)
				.patternLine("B")
				.patternLine("C")
				.key('B', WoodenDevices.crate)
				.key('C', Items.MINECART)
				.addCriterion("has_minecart", hasItem(Items.MINECART))
				.build(out, toRL(toPath(Misc.cartWoodenCrate)));
		ShapedRecipeBuilder.shapedRecipe(Misc.cartReinforcedCrate)
				.patternLine("B")
				.patternLine("C")
				.key('B', WoodenDevices.reinforcedCrate)
				.key('C', Items.MINECART)
				.addCriterion("has_minecart", hasItem(Items.MINECART))
				.build(out, toRL(toPath(Misc.cartReinforcedCrate)));
		ShapedRecipeBuilder.shapedRecipe(Misc.cartWoodenBarrel)
				.patternLine("B")
				.patternLine("C")
				.key('B', WoodenDevices.woodenBarrel)
				.key('C', Items.MINECART)
				.addCriterion("has_minecart", hasItem(Items.MINECART))
				.build(out, toRL(toPath(Misc.cartWoodenBarrel)));
		ShapedRecipeBuilder.shapedRecipe(Misc.cartMetalBarrel)
				.patternLine("B")
				.patternLine("C")
				.key('B', MetalDevices.barrel)
				.key('C', Items.MINECART)
				.addCriterion("has_minecart", hasItem(Items.MINECART))
				.build(out, toRL(toPath(Misc.cartMetalBarrel)));
	}

	//TODO tag convention?
	private void addArmor(Tag<Item> input, Map<EquipmentSlotType, Item> items, String name, Consumer<IFinishedRecipe> out)
	{
		Item head = items.get(EquipmentSlotType.HEAD);
		Item chest = items.get(EquipmentSlotType.CHEST);
		Item legs = items.get(EquipmentSlotType.LEGS);
		Item feet = items.get(EquipmentSlotType.FEET);
		ShapedRecipeBuilder.shapedRecipe(head)
				.patternLine("xxx")
				.patternLine("x x")
				.key('x', input)
				.addCriterion("has_"+name, hasItem(input))
				.build(out, toRL(toPath(head)));
		ShapedRecipeBuilder.shapedRecipe(chest)
				.patternLine("x x")
				.patternLine("xxx")
				.patternLine("xxx")
				.key('x', input)
				.addCriterion("has_"+name, hasItem(input))
				.build(out, toRL(toPath(chest)));
		ShapedRecipeBuilder.shapedRecipe(legs)
				.patternLine("xxx")
				.patternLine("x x")
				.patternLine("x x")
				.key('x', input)
				.addCriterion("has_"+name, hasItem(input))
				.build(out, toRL(toPath(legs)));
		ShapedRecipeBuilder.shapedRecipe(feet)
				.patternLine("x x")
				.patternLine("x x")
				.key('x', input)
				.addCriterion("has_"+name, hasItem(input))
				.build(out, toRL(toPath(feet)));
	}

	private void add3x3Conversion(IItemProvider bigItem, IItemProvider smallItem, Tag<Item> smallTag, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(bigItem)
				.key('s', smallTag)
				.key('i', smallItem)
				.patternLine("sss")
				.patternLine("sis")
				.patternLine("sss")
				.addCriterion("has_"+toPath(smallItem), hasItem(smallItem))
				.build(out, toRL(toPath(smallItem)+"_to_")+toPath(bigItem));
		ShapelessRecipeBuilder.shapelessRecipe(smallItem, 9)
				.addIngredient(bigItem)
				.addCriterion("has_"+toPath(bigItem), hasItem(smallItem))
				.build(out, toRL(toPath(bigItem)+"_to_"+toPath(smallItem)));
	}

	private void addSlab(IItemProvider block, IItemProvider slab, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(slab, 6)
				.key('s', block)
				.patternLine("sss")
				.addCriterion("has_"+toPath(block), hasItem(block))
				.build(out, toRL(toPath(block)+"_to_slab"));
		ShapedRecipeBuilder.shapedRecipe(block)
				.key('s', slab)
				.patternLine("s")
				.patternLine("s")
				.addCriterion("has_"+toPath(block), hasItem(block))
				.build(out, toRL(toPath(block)+"_from_slab"));
	}

	private void addStairs(IItemProvider block, IItemProvider stairs, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(stairs, 4)
				.key('s', block)
				.patternLine("s  ")
				.patternLine("ss ")
				.patternLine("sss")
				.addCriterion("has_"+toPath(block), hasItem(block))
				.build(out, toRL(toPath(stairs)));
	}

	/**
	 * For Recipes like Coke or Blast Bricks, which use the same item in all corners, one for the sides and one for the middle
	 * Also work for shapes like TNT
	 *
	 * @param output the recipe's output
	 * @param corner the item in the corners
	 * @param side   the item on the sides
	 * @param middle the item in the middle
	 */
	@ParametersAreNonnullByDefault
	private void addCornerStraightMiddle(IItemProvider output, int count, Ingredient corner, Ingredient side, Ingredient middle, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(output, count)
				.key('c', corner)
				.key('s', side)
				.key('m', middle)
				.patternLine("csc")
				.patternLine("sms")
				.patternLine("csc")
				.addCriterion("has_"+toPath(output), hasItem(output))
				.build(out, toRL(toPath(output)));
	}

	/**
	 * For Recipes consisting of layers
	 *
	 * @param output the recipe's output
	 * @param top    the item on the top
	 * @param middle the item in the middle
	 * @param bottom the item on the bottom
	 */
	@ParametersAreNonnullByDefault
	private void addSandwich(IItemProvider output, int count, Ingredient top, Ingredient middle, Ingredient bottom, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(output, count)
				.key('t', top)
				.key('m', middle)
				.key('b', bottom)
				.patternLine("ttt")
				.patternLine("mmm")
				.patternLine("bbb")
				.addCriterion("has_"+toPath(output), hasItem(output))
				.build(out, toRL(toPath(output)));
	}

	private String toPath(IItemProvider src)
	{
		return src.asItem().getRegistryName().getPath();
	}

	private ResourceLocation toRL(String s)
	{
		if(!s.contains("/"))
			s = "crafting/"+s;
		if(PATH_COUNT.containsKey(s))
		{
			int count = PATH_COUNT.get(s)+1;
			PATH_COUNT.put(s, count);
			return new ResourceLocation(ImmersiveEngineering.MODID, s+count);
		}
		PATH_COUNT.put(s, 1);
		return new ResourceLocation(ImmersiveEngineering.MODID, s);
	}

	@Nonnull
	private Ingredient makeIngredient(IItemProvider in)
	{
		return Ingredient.fromItems(in);
	}

	@Nonnull
	private Ingredient makeIngredient(Tag<Item> in)
	{
		return Ingredient.fromTag(in);
	}

	@Nonnull
	private Ingredient makeIngredientFromBlock(Tag<Block> in)
	{
		Tag<Item> itemTag = IETags.getItemTag(in);
		if(itemTag==null)
			//TODO this currently does not work, the tag collection is not initialized in data gen mode
			itemTag = ItemTags.getCollection().getTagMap().get(in.getId());
		Preconditions.checkNotNull(itemTag, "Failed to convert block tag "+in.getId()+" to item tag");
		return makeIngredient(itemTag);
	}

	public static ICondition getTagCondition(Tag<?> tag)
	{
		return new NotCondition(new TagEmptyCondition(tag.getId()));
	}

	public static ICondition getTagCondition(ResourceLocation tag)
	{
		return getTagCondition(new ItemTags.Wrapper(tag));
	}

	/**
	 * For smelting recipes that also have a blasting recipe, like ores
	 * keep the smelting postfix in mind when using this for non-ores or weird cases where the primary recipe for the ingot is not occupied by the smelting recipe
	 * has an overloaded method for regular use
	 *
	 * @param input        the recipe's input
	 * @param output       the recipe's output
	 * @param xp           experience awarded per smelted item
	 * @param smeltingTime smelting time in ticks
	 * @param extraPostfix adds an additional postfix before the smelting/blasting postfix when needed (for example used by dusts)
	 * @param smeltPostfix allows adding the smelting postfix to the smelting (non-blasting) recipe
	 */
	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, int smeltingTime, Consumer<IFinishedRecipe> out, String extraPostfix, boolean smeltPostfix)
	{
		CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(input), output, xp, smeltingTime).addCriterion("has_"+toPath(input), hasItem(input)).build(out, toRL(toPath(output)+extraPostfix+(smeltPostfix?"_from_smelting": "")));
		CookingRecipeBuilder.blastingRecipe(Ingredient.fromItems(input), output, xp, smeltingTime/blastDivider).addCriterion("has_"+toPath(input), hasItem(input)).build(out, toRL(toPath(output)+extraPostfix+"_from_blasting"));
	}

	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, Consumer<IFinishedRecipe> out)
	{
		addStandardSmeltingBlastingRecipe(input, output, xp, out, "");
	}

	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, Consumer<IFinishedRecipe> out, String extraPostfix)
	{
		addStandardSmeltingBlastingRecipe(input, output, xp, standardSmeltingTime, out, extraPostfix, false);
	}

	private void addRGBRecipe(Consumer<IFinishedRecipe> out, ResourceLocation recipeName, Ingredient target, String nbtKey)
	{
		out.accept(new IFinishedRecipe()
		{

			@Override
			public void serialize(JsonObject json)
			{
				json.add("target", target.serialize());
				json.addProperty("key", nbtKey);
			}

			@Override
			public ResourceLocation getID()
			{
				return recipeName;
			}

			@Override
			public IRecipeSerializer<?> getSerializer()
			{
				return RecipeSerializers.RGB_SERIALIZER.get();
			}

			@Nullable
			@Override
			public JsonObject getAdvancementJson()
			{
				return null;
			}

			@Nullable
			@Override
			public ResourceLocation getAdvancementID()
			{
				return null;
			}
		});
	}
}
