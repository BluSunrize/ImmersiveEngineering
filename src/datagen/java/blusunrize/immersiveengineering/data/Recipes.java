/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction.ClocheRenderReference;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.*;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.ChuteBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.fluids.IEFluids;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.*;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.data.recipebuilder.*;
import blusunrize.immersiveengineering.data.resources.RecipeMetals;
import blusunrize.immersiveengineering.data.resources.RecipeMetals.AlloyProperties;
import blusunrize.immersiveengineering.data.resources.RecipeOres;
import blusunrize.immersiveengineering.data.resources.RecipeWoods;
import blusunrize.immersiveengineering.data.resources.SecondaryOutput;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.api.IETags.getStorageBlock;
import static blusunrize.immersiveengineering.api.utils.TagUtils.createItemWrapper;

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
	protected void saveAdvancement(HashCache cache, JsonObject json, Path path)
	{
		if(path.equals(ADV_ROOT)) return; //We NEVER care about this.
		super.saveAdvancement(cache, json, path);
	}

	@Override
	protected void buildShapelessRecipes(@Nonnull Consumer<FinishedRecipe> out)
	{
		for(EnumMetals metal : EnumMetals.values())
		{
			IETags.MetalTags tags = IETags.getTagsFor(metal);

			ItemLike nugget = Metals.nuggets.get(metal);
			ItemLike ingot = Metals.ingots.get(metal);
			ItemLike plate = Metals.plates.get(metal);
			ItemLike dust = Metals.dusts.get(metal);
			BlockEntry<Block> block = IEBlocks.Metals.storage.get(metal);
			BlockEntry<IEBaseBlock> sheetMetal = IEBlocks.Metals.sheetmetal.get(metal);
			if(!metal.isVanillaMetal())
			{
				add3x3Conversion(ingot, nugget, tags.nugget, out);
				add3x3Conversion(block, ingot, tags.ingot, out);
				if(IEBlocks.Metals.ores.containsKey(metal))
				{
					BlockEntry<Block> ore = IEBlocks.Metals.ores.get(metal);
					addStandardSmeltingBlastingRecipe(ore, ingot, metal.smeltingXP, out);
				}
			}
			addStandardSmeltingBlastingRecipe(dust, ingot, 0, out, "_from_dust");
//			addStandardSmeltingBlastingRecipe(dust, ingot, metal.smeltingXP, out, "_from_dust"); //TODO: remove this, if 0 XP on dust is intentional. this bugs out because the alloys do not have metal.smeltingXP
			ShapelessRecipeBuilder.shapeless(plate)
					.requires(IETags.getTagsFor(metal).ingot)
					.requires(Tools.hammer)
					.unlockedBy("has_"+metal.tagName()+"_ingot", has(IETags.getTagsFor(metal).ingot))
					.save(out, toRL("plate_"+metal.tagName()+"_hammering"));
			ShapedRecipeBuilder.shaped(sheetMetal, 4)
					.pattern(" p ")
					.pattern("p p")
					.pattern(" p ")
					.define('p', IETags.getTagsFor(metal).plate)
					.unlockedBy("has_"+toPath(plate), has(plate))
					.save(out, toRL(toPath(sheetMetal)));
		}
		addStandardSmeltingBlastingRecipe(IEItems.Ingredients.dustHopGraphite, Ingredients.ingotHopGraphite, 0.5F, out);

		addStandardSmeltingBlastingRecipe(Tools.steelAxe, Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_axe");
		addStandardSmeltingBlastingRecipe(Tools.steelPick, Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_pick");
		addStandardSmeltingBlastingRecipe(Tools.steelShovel, Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_shovel");
		addStandardSmeltingBlastingRecipe(Tools.steelSword, Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_sword");

		for(EquipmentSlot slot : EquipmentSlot.values())
			if(slot.getType()==Type.ARMOR)
			{
				addStandardSmeltingBlastingRecipe(Tools.steelArmor.get(slot), Metals.nuggets.get(EnumMetals.STEEL), 0.1F, out, "_recycle_steel_"+slot.getName());
				addStandardSmeltingBlastingRecipe(Misc.faradaySuit.get(slot), Metals.nuggets.get(EnumMetals.ALUMINUM), 0.1F, out, "_recycle_faraday_"+slot.getName());
			}

		for(Entry<ResourceLocation, BlockEntry<SlabBlock>> blockSlab : IEBlocks.toSlab.entrySet())
			addSlab(ForgeRegistries.BLOCKS.getValue(blockSlab.getKey()), blockSlab.getValue(), out);

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

		SpecialRecipeBuilder.special(RecipeSerializers.SPEEDLOADER_LOAD.get())
				.save(out, ImmersiveEngineering.MODID+":speedloader_load");
		SpecialRecipeBuilder.special(RecipeSerializers.FLARE_BULLET_COLOR.get())
				.save(out, ImmersiveEngineering.MODID+":flare_bullet_color");
		SpecialRecipeBuilder.special(RecipeSerializers.POTION_BULLET_FILL.get())
				.save(out, ImmersiveEngineering.MODID+":potion_bullet_fill");
		SpecialRecipeBuilder.special(RecipeSerializers.POWERPACK_SERIALIZER.get())
				.save(out, ImmersiveEngineering.MODID+":powerpack_attach");
		SpecialRecipeBuilder.special(RecipeSerializers.EARMUFF_SERIALIZER.get())
				.save(out, ImmersiveEngineering.MODID+":earmuffs_attach");
		SpecialRecipeBuilder.special(RecipeSerializers.JERRYCAN_REFILL.get())
				.save(out, ImmersiveEngineering.MODID+":jerrycan_refill");
		SpecialRecipeBuilder.special(RecipeSerializers.REVOLVER_CYCLE_SERIALIZER.get())
				.save(out, ImmersiveEngineering.MODID+":revolver_cycle");
		SpecialRecipeBuilder.special(RecipeSerializers.IE_REPAIR_SERIALIZER.get())
				.save(out, ImmersiveEngineering.MODID+":ie_item_repair");
		SpecialRecipeBuilder.special(RecipeSerializers.SHADER_BAG_SERIALIZER.get())
				.save(out, ImmersiveEngineering.MODID+":shaderbag_downgrading");
		GeneratedListRecipeBuilder.build(out, rl("arc_recycling_list"));
		GeneratedListRecipeBuilder.build(out, rl("mixer_potion_list"));
		GeneratedListRecipeBuilder.build(out, rl("potion_bottling_list"));
		addRGBRecipe(out, toRL("curtain_colour"), Ingredient.of(Cloth.curtain), "colour");

		recipesBlast(out);
		recipesCoke(out);
		recipesCloche(out);
		recipesBlueprint(out);
		recipesMultiblockMachines(out);

		mineralMixes(out);
	}

	private void recipesBlast(@Nonnull Consumer<FinishedRecipe> out)
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
		Named<Item> charCoalBlocks = createItemWrapper(getStorageBlock("charcoal"));
		BlastFurnaceFuelBuilder.builder(charCoalBlocks)
				.addCondition(new NotCondition(new TagEmptyCondition(charCoalBlocks.getName())))
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

	private void recipesCoke(@Nonnull Consumer<FinishedRecipe> out)
	{
		CokeOvenRecipeBuilder.builder(IETags.coalCoke, 1)
				.addInput(Items.COAL)
				.setOil(FluidAttributes.BUCKET_VOLUME/2)
				.setTime(1800)
				.build(out, toRL("cokeoven/coke"));
		CokeOvenRecipeBuilder.builder(IETags.getItemTag(IETags.coalCokeBlock), 1)
				.addInput(Blocks.COAL_BLOCK)
				.setOil(FluidAttributes.BUCKET_VOLUME*5)
				.setTime(9*1800)
				.build(out, toRL("cokeoven/coke_block"));
		CokeOvenRecipeBuilder.builder(Items.CHARCOAL)
				.addInput(ItemTags.LOGS)
				.setOil(FluidAttributes.BUCKET_VOLUME/4)
				.setTime(900)
				.build(out, toRL("cokeoven/charcoal"));
	}

	private void recipesCloche(@Nonnull Consumer<FinishedRecipe> out)
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
				.addSoil(Tags.Items.SAND)
				.setTime(560)
				.setRender(new ClocheRenderReference("stacking", Blocks.SUGAR_CANE))
				.build(out, toRL("cloche/sugar_cane"));
		ClocheRecipeBuilder.builder(Items.CACTUS)
				.addInput(Items.CACTUS)
				.addSoil(Tags.Items.SAND)
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
				.setRender(new ClocheRenderReference("hemp", IEBlocks.Misc.hempPlant.get()))
				.build(out, toRL("cloche/hemp"));

		Ingredient shroomSoil = Ingredient.of(Blocks.MYCELIUM, Blocks.PODZOL);
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

	private void recipesBlueprint(@Nonnull Consumer<FinishedRecipe> out)
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
				.build(out, toRL("blueprint/circuit_board"));

		ItemLike[] molds = {Molds.moldPlate, Molds.moldGear, Molds.moldRod, Molds.moldBulletCasing, Molds.moldWire, Molds.moldPacking4, Molds.moldPacking9, Molds.moldUnpacking};
		for(ItemLike mold : molds)
			BlueprintCraftingRecipeBuilder.builder("molds", mold.asItem())
					.addInput(new IngredientWithSize(IETags.getTagsFor(EnumMetals.STEEL).plate, 3))
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
				.addInput(new IngredientWithSize(Ingredient.of(BulletHandler.getBulletItem(BulletItem.HOMING)), 4))
				.build(out, toRL("blueprint/bullet_wolfpack"));

		BlueprintCraftingRecipeBuilder.builder("electrode", Misc.graphiteElectrode)
				.addInput(new IngredientWithSize(IETags.hopGraphiteIngot, 4))
				.build(out, toRL("blueprint/electrode"));

		BlueprintCraftingRecipeBuilder.builder("bannerpatterns", BannerPatterns.bannerPatternHammer)
				.addInput(Items.PAPER)
				.addInput(Tools.hammer)
				.build(out, toRL("blueprint/banner_hammer"));
		BlueprintCraftingRecipeBuilder.builder("bannerpatterns", BannerPatterns.bannerPatternBevels)
				.addInput(Items.PAPER)
				.addInput(IETags.plates)
				.build(out, toRL("blueprint/banner_bevels"));
		BlueprintCraftingRecipeBuilder.builder("bannerpatterns", BannerPatterns.bannerPatternOrnate)
				.addInput(Items.PAPER)
				.addInput(IETags.getTagsFor(EnumMetals.SILVER).dust)
				.build(out, toRL("blueprint/banner_ornate"));
		BlueprintCraftingRecipeBuilder.builder("bannerpatterns", BannerPatterns.bannerPatternTreatedWood)
				.addInput(Items.PAPER)
				.addInput(IETags.getItemTag(IETags.treatedWood))
				.build(out, toRL("blueprint/banner_treatedwood"));
		BlueprintCraftingRecipeBuilder.builder("bannerpatterns", BannerPatterns.bannerPatternWindmill)
				.addInput(Items.PAPER)
				.addInput(WoodenDevices.windmill)
				.build(out, toRL("blueprint/banner_windmill"));
		BlueprintCraftingRecipeBuilder.builder("bannerpatterns", BannerPatterns.bannerPatternWolfR)
				.addInput(Items.PAPER)
				.addInput(BulletHandler.getBulletItem(BulletItem.WOLFPACK))
				.build(out, toRL("blueprint/banner_wolf_r"));
		BlueprintCraftingRecipeBuilder.builder("bannerpatterns", BannerPatterns.bannerPatternWolfL)
				.addInput(Items.PAPER)
				.addInput(BulletHandler.getBulletItem(BulletItem.WOLFPACK))
				.build(out, toRL("blueprint/banner_wolf_l"));
		BlueprintCraftingRecipeBuilder.builder("bannerpatterns", BannerPatterns.bannerPatternWolf)
				.addInput(Items.PAPER)
				.addInput(BulletHandler.getBulletItem(BulletItem.WOLFPACK))
				.build(out, toRL("blueprint/banner_wolf"));
	}

	private void recipesMultiblockMachines(@Nonnull Consumer<FinishedRecipe> out)
	{
		HammerCrushingRecipeBuilder hammerBuilder;
		CrusherRecipeBuilder crusherBuilder;
		ArcFurnaceRecipeBuilder arcBuilder;
		MetalPressRecipeBuilder pressBuilder;
		AlloyRecipeBuilder alloyBuilder;
		SawmillRecipeBuilder sawmillBuilder;

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

				// Arcfurnace ore
				arcBuilder = ArcFurnaceRecipeBuilder.builder(metal.getIngot(), 2);
				if(!metal.isNative())
					arcBuilder.addCondition(getTagCondition(metal.getIngot())).addCondition(getTagCondition(metal.getOre()));
				arcBuilder.addIngredient("input", metal.getOre())
						.addSlag(IETags.slag, 1)
						.setTime(200)
						.setEnergy(102400)
						.build(out, toRL("arcfurnace/ore_"+metal.getName()));
			}

			// Crush ingot
			crusherBuilder = CrusherRecipeBuilder.builder(metal.getDust(), 1);
			if(!metal.isNative())
				crusherBuilder.addCondition(getTagCondition(metal.getDust())).addCondition(getTagCondition(metal.getIngot()));
			crusherBuilder.addInput(metal.getIngot())
					.setEnergy(3000)
					.build(out, toRL("crusher/ingot_"+metal.getName()));

			// Arcfurnace dust
			arcBuilder = ArcFurnaceRecipeBuilder.builder(metal.getIngot(), 1);
			if(!metal.isNative())
				arcBuilder.addCondition(getTagCondition(metal.getIngot())).addCondition(getTagCondition(metal.getDust()));
			arcBuilder.addIngredient("input", metal.getDust())
					.setTime(100)
					.setEnergy(51200)
					.build(out, toRL("arcfurnace/dust_"+metal.getName()));

			// Plate
			Named<Item> plate = createItemWrapper(IETags.getPlate(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder(Molds.moldPlate, plate, 1);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot())).addCondition(getTagCondition(plate));
			pressBuilder.addInput(metal.getIngot())
					.setEnergy(2400)
					.build(out, toRL("metalpress/plate_"+metal.getName()));

			// Gear
			Named<Item> gear = createItemWrapper(IETags.getGear(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder(Molds.moldGear, gear, 1);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()));
			pressBuilder.addCondition(getTagCondition(gear))
					.addInput(new IngredientWithSize(metal.getIngot(), 4))
					.setEnergy(2400)
					.build(out, toRL("metalpress/gear_"+metal.getName()));

			// Rod
			Named<Item> rods = createItemWrapper(IETags.getRod(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder(Molds.moldRod, rods, 2);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()));
			pressBuilder.addCondition(getTagCondition(rods))
					.addInput(metal.getIngot())
					.setEnergy(2400)
					.build(out, toRL("metalpress/rod_"+metal.getName()));

			// Wire
			Named<Item> wire = createItemWrapper(IETags.getWire(metal.getName()));
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
		electrode.setDamageValue(IEServerConfig.MACHINES.arcfurnace_electrodeDamage.getOrDefault()/2);
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

		ArcFurnaceRecipeBuilder.builder(new ItemStack(Items.NETHERITE_SCRAP, 2))
				.addIngredient("input", Items.ANCIENT_DEBRIS)
				.addSlag(IETags.slag, 1)
				.setTime(100)
				.setEnergy(512000)
				.build(out, toRL("arcfurnace/netherite_scrap"));

		/* BOTTLING */
		BottlingMachineRecipeBuilder.builder(Items.WET_SPONGE)
				.addInput(Items.SPONGE)
				.addFluidTag(FluidTags.WATER, FluidAttributes.BUCKET_VOLUME)
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
				.addInput(IETags.getItemTag(IETags.colorlessSandstoneBlocks))
				.setEnergy(3200)
				.build(out, toRL("crusher/sandstone"));
		CrusherRecipeBuilder.builder(new ItemStack(Items.RED_SAND, 2))
				.addSecondary(IETags.saltpeterDust, .5f)
				.addInput(IETags.getItemTag(IETags.redSandstoneBlocks))
				.setEnergy(3200)
				.build(out, toRL("crusher/red_sandstone"));
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
				.build(out, toRL("crusher/glowstone"));
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

		Named<Item> coal_dust = createItemWrapper(IETags.getDust("coal"));
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

		CrusherRecipeBuilder.builder(IETags.getTagsFor(EnumMetals.GOLD).dust, 2)
				.addInput(Items.NETHER_GOLD_ORE)
				.setEnergy(4200)
				.build(out, toRL("crusher/nether_gold"));

		CrusherRecipeBuilder.builder(new ItemStack(Items.NETHER_WART, 9))
				.addInput(Items.NETHER_WART_BLOCK)
				.setEnergy(1600)
				.build(out, toRL("crusher/nether_wart"));

		/* SAWMILL */
		for(RecipeWoods wood : RecipeWoods.values())
		{
			// Basic log
			if(wood.getLog()!=null)
			{
				sawmillBuilder = SawmillRecipeBuilder.builder(new ItemStack(wood.getPlank(), 6))
						.setEnergy(1600);
				// If there is an all-bark block
				if(wood.getWood()!=null)
					sawmillBuilder.addInput(wood.getLog(), wood.getWood());
				else
					sawmillBuilder.addInput(wood.getLog());
				if(wood.getStripped()!=null)
				{
					sawmillBuilder.addStripped(wood.getStripped());
					if(wood.produceSawdust())
						sawmillBuilder.addSecondary(IETags.sawdust, true);
				}
				if(wood.produceSawdust())
					sawmillBuilder.addSecondary(IETags.sawdust, false);
				sawmillBuilder.build(out, toRL("sawmill/"+wood.getName()+"_log"));
			}
			// Already stripped log
			if(wood.getStripped()!=null)
			{
				sawmillBuilder = SawmillRecipeBuilder.builder(new ItemStack(wood.getPlank(), 6))
						.addInput(wood.getStripped())
						.setEnergy(800);
				if(wood.produceSawdust())
					sawmillBuilder.addSecondary(IETags.sawdust, false);
				sawmillBuilder.build(out, toRL("sawmill/stripped_"+wood.getName()+"_log"));
			}
			// Door
			if(wood.getDoor()!=null)
			{
				sawmillBuilder = SawmillRecipeBuilder.builder(new ItemStack(wood.getPlank(), 1))
						.addInput(wood.getDoor())
						.setEnergy(800);
				if(wood.produceSawdust())
					sawmillBuilder.addSecondary(IETags.sawdust, false);
				sawmillBuilder.build(out, toRL("sawmill/"+wood.getName()+"_door"));
			}
			// Stairs
			if(wood.getStairs()!=null)
			{
				sawmillBuilder = SawmillRecipeBuilder.builder(new ItemStack(wood.getPlank(), 1))
						.addInput(wood.getStairs())
						.setEnergy(1600);
				if(wood.produceSawdust())
					sawmillBuilder.addSecondary(IETags.sawdust, false);
				sawmillBuilder.build(out, toRL("sawmill/"+wood.getName()+"_stairs"));
			}
		}
		SawmillRecipeBuilder.builder(new ItemStack(Items.OAK_PLANKS, 4))
				.addInput(Items.BOOKSHELF)
				.addSecondary(IETags.sawdust, false)
				.addSecondary(new ItemStack(Items.BOOK, 3), false)
				.setEnergy(1600)
				.build(out, toRL("sawmill/bookshelf"));

		/* SQUEEZER */
		Fluid plantOil = IEFluids.fluidPlantoil.getStill();
		SqueezerRecipeBuilder.builder(plantOil, 80)
				.addInput(Items.WHEAT_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/wheat_seeds"));
		SqueezerRecipeBuilder.builder(plantOil, 60)
				.addInput(Items.BEETROOT_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/beetroot_seeds"));
		SqueezerRecipeBuilder.builder(plantOil, 40)
				.addInput(Items.PUMPKIN_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/pumpkin_seeds"));
		SqueezerRecipeBuilder.builder(plantOil, 20)
				.addInput(Items.MELON_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/melon_seeds"));
		SqueezerRecipeBuilder.builder(plantOil, 120)
				.addInput(Misc.hempSeeds)
				.setEnergy(6400)
				.build(out, toRL("squeezer/hemp_seeds"));
		SqueezerRecipeBuilder.builder()
				.addResult(new IngredientWithSize(IETags.hopGraphiteDust))
				.addInput(new IngredientWithSize(IETags.coalCokeDust, 8))
				.setEnergy(19200)
				.build(out, toRL("squeezer/graphite_dust"));
		/* FERMENTER */
		Fluid ethanol = IEFluids.fluidEthanol.getStill();
		FermenterRecipeBuilder.builder(ethanol, 80)
				.addInput(Items.SUGAR_CANE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/sugar_cane"));
		FermenterRecipeBuilder.builder(ethanol, 20)
				.addInput(Items.MELON_SLICE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/melon_slice"));
		FermenterRecipeBuilder.builder(ethanol, 80)
				.addInput(Items.APPLE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/apple"));
		FermenterRecipeBuilder.builder(ethanol, 80)
				.addInput(Tags.Items.CROPS_POTATO)
				.setEnergy(6400)
				.build(out, toRL("fermenter/potato"));
		/* REFINERY */
		RefineryRecipeBuilder.builder(IEFluids.fluidBiodiesel.getStill(), 16)
				.addInput(IETags.fluidPlantoil, 8)
				.addInput(IETags.fluidEthanol, 8)
				.setEnergy(80)
				.build(out, toRL("refinery/biodiesel"));
		/* MIXER */
		int half_bucket = FluidAttributes.BUCKET_VOLUME/2;
		Fluid concrete = IEFluids.fluidConcrete.getStill();
		MixerRecipeBuilder.builder(concrete, half_bucket)
				.addFluidTag(FluidTags.WATER, half_bucket)
				.addInput(new IngredientWithSize(Tags.Items.SAND, 2))
				.addInput(Tags.Items.GRAVEL)
				.addInput(IETags.clay)
				.setEnergy(3200)
				.build(out, toRL("mixer/concrete"));
		MixerRecipeBuilder.builder(concrete, half_bucket)
				.addFluidTag(FluidTags.WATER, half_bucket)
				.addInput(new IngredientWithSize(IETags.slag, 2))
				.addInput(Tags.Items.GRAVEL)
				.addInput(IETags.clay)
				.setEnergy(3200)
				.build(out, toRL("mixer/concrete"));
		MixerRecipeBuilder.builder(IEFluids.fluidHerbicide.getStill(), half_bucket)
				.addFluidTag(IETags.fluidEthanol, half_bucket)
				.addInput(IETags.saltpeterDust)
				.addInput(IETags.getTagsFor(EnumMetals.COPPER).dust)
				.setEnergy(3200)
				.build(out, toRL("mixer/herbicide"));
	}

	private void mineralMixes(@Nonnull Consumer<FinishedRecipe> out)
	{
		// Metals
		Named<Item> iron = Tags.Items.ORES_IRON;
		Named<Item> gold = Tags.Items.ORES_GOLD;
		Named<Item> copper = IETags.getItemTag(IETags.getTagsFor(EnumMetals.COPPER).ore);
		Named<Item> aluminum = IETags.getItemTag(IETags.getTagsFor(EnumMetals.ALUMINUM).ore);
		Named<Item> lead = IETags.getItemTag(IETags.getTagsFor(EnumMetals.LEAD).ore);
		Named<Item> silver = IETags.getItemTag(IETags.getTagsFor(EnumMetals.SILVER).ore);
		Named<Item> nickel = IETags.getItemTag(IETags.getTagsFor(EnumMetals.NICKEL).ore);
		Named<Item> uranium = IETags.getItemTag(IETags.getTagsFor(EnumMetals.URANIUM).ore);
		Named<Item> tin = createItemWrapper(IETags.getOre("tin"));
		Named<Item> titanium = createItemWrapper(IETags.getOre("titanium"));
		Named<Item> thorium = createItemWrapper(IETags.getOre("thorium"));
		Named<Item> tungsten = createItemWrapper(IETags.getOre("tungsten"));
		Named<Item> manganese = createItemWrapper(IETags.getOre("manganese"));
		Named<Item> platinum = createItemWrapper(IETags.getOre("platinum"));
		Named<Item> paladium = createItemWrapper(IETags.getOre("paladium"));
		Named<Item> mercury = createItemWrapper(IETags.getOre("mercury"));
		// Gems & Dusts
		Named<Item> sulfur = IETags.sulfurDust;
		Named<Item> phosphorus = createItemWrapper(IETags.getDust("phosphorus"));
		Named<Item> redstone = Tags.Items.ORES_REDSTONE;
		Named<Item> emerald = Tags.Items.ORES_EMERALD;
		Block prismarine = Blocks.PRISMARINE;
		Named<Item> aquamarine = createItemWrapper(IETags.getGem("aquamarine"));

		// Common things
		ResourceKey<DimensionType> overworld = DimensionType.OVERWORLD_LOCATION;
		ResourceKey<DimensionType> nether = DimensionType.NETHER_LOCATION;
		MineralMixBuilder.builder(overworld)
				.addOre(Tags.Items.ORES_COAL, .8f)
				.addOre(sulfur, .2f)
				.addOre(phosphorus, .2f, getTagCondition(phosphorus))
				.setWeight(25)
				.setFailchance(.05f)
				.build(out, toRL("mineral/bituminous_coal"));
		MineralMixBuilder.builder(overworld)
				.addOre(Items.CLAY, .5f)
				.addOre(Items.SAND, .3f)
				.addOre(Items.GRAVEL, .2f)
				.setWeight(25)
				.setFailchance(.05f)
				.build(out, toRL("mineral/silt"));
		MineralMixBuilder.builder(overworld)
				.addOre(Blocks.GRANITE, .3f)
				.addOre(Blocks.DIORITE, .3f)
				.addOre(Blocks.ANDESITE, .3f)
				.addOre(Blocks.OBSIDIAN, .1f)
				.setWeight(25)
				.setFailchance(.05f)
				.build(out, toRL("mineral/igneous_rock"));
		// Metals
		MineralMixBuilder.builder(overworld)
				.addOre(iron, .35f)
				.addOre(nickel, .35f)
				.addOre(sulfur, .3f)
				.setWeight(25)
				.setFailchance(.05f)
				.build(out, toRL("mineral/pentlandite"));
		MineralMixBuilder.builder(overworld)
				.addOre(iron, .35f)
				.addOre(copper, .35f)
				.addOre(sulfur, .3f)
				.setWeight(20)
				.setFailchance(.05f)
				.build(out, toRL("mineral/chalcopyrite"));
		MineralMixBuilder.builder(overworld)
				.addOre(aluminum, .7f)
				.addOre(iron, .2f)
				.addOre(titanium, .1f, getTagCondition(titanium))
				.setWeight(20)
				.setFailchance(.05f)
				.build(out, toRL("mineral/laterite"));
		MineralMixBuilder.builder(overworld)
				.addOre(copper, .75f)
				.addOre(gold, .25f)
				.setWeight(30)
				.setFailchance(.1f)
				.build(out, toRL("mineral/auricupride"));
		MineralMixBuilder.builder(overworld)
				.addOre(lead, .4f)
				.addOre(sulfur, .4f)
				.addOre(silver, .2f)
				.setWeight(15)
				.setFailchance(.05f)
				.build(out, toRL("mineral/galena"));
		MineralMixBuilder.builder(overworld)
				.addOre(redstone, .6f)
				.addOre(sulfur, .4f)
				.addOre(mercury, .3f, getTagCondition(mercury))
				.setWeight(15)
				.setFailchance(.1f)
				.build(out, toRL("mineral/cinnabar"));
		// Rare
		MineralMixBuilder.builder(overworld)
				.addOre(uranium, .7f)
				.addOre(lead, .3f)
				.addOre(thorium, .1f, getTagCondition(thorium))
				.setWeight(10)
				.setFailchance(.15f)
				.build(out, toRL("mineral/uraninite"));
		MineralMixBuilder.builder(overworld)
				.addOre(emerald, .3f)
				.addOre(prismarine, .7f)
				.addOre(aquamarine, .3f, getTagCondition(aquamarine))
				.setWeight(5)
				.setFailchance(.2f)
				.build(out, toRL("mineral/beryl"));
		// Nether
		MineralMixBuilder.builder(nether)
				.addOre(Blocks.NETHER_QUARTZ_ORE, .6f)
				.addOre(Blocks.NETHER_GOLD_ORE, .2f)
				.addOre(sulfur, .2f)
				.setWeight(20)
				.setFailchance(.15f)
				.setBackground(ForgeRegistries.BLOCKS.getKey(Blocks.NETHERRACK))
				.build(out, toRL("mineral/mephitic_quarzite"));
		MineralMixBuilder.builder(nether)
				.addOre(Blocks.GRAVEL, .6f)
				.addOre(Blocks.ANCIENT_DEBRIS, .4f)
				.setWeight(8)
				.setFailchance(.7f)
				.setBackground(ForgeRegistries.BLOCKS.getKey(Blocks.NETHERRACK))
				.build(out, toRL("mineral/ancient_debris"));

		// Compat
		MineralMixBuilder.builder(overworld)
				.addCondition(getTagCondition(tin))
				.addOre(tin, 1)
				.setWeight(20)
				.setFailchance(.05f)
				.build(out, toRL("mineral/cassiterite"));
		MineralMixBuilder.builder(overworld)
				.addCondition(getTagCondition(platinum))
				.addOre(platinum, .5f)
				.addOre(paladium, .5f, getTagCondition(paladium))
				.addOre(nickel, .5f)
				.setWeight(5)
				.setFailchance(.1f)
				.build(out, toRL("mineral/cooperite"));
		MineralMixBuilder.builder(overworld)
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

	private void recipesStoneDecorations(@Nonnull Consumer<FinishedRecipe> out)
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

		addStairs(StoneDecoration.hempcrete, out);
		addStairs(StoneDecoration.concrete, out);
		addStairs(StoneDecoration.concreteTile, out);
		addStairs(StoneDecoration.concreteLeaded, out);

		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.hempcrete), IEBlocks.toSlab.get(StoneDecoration.hempcrete.getId()), 2)
				.unlocks("has_hempcrete", has(StoneDecoration.hempcrete))
				.save(out, toRL("hempcrete_slab_from_hempcrete_stonecutting"));
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.hempcrete), IEBlocks.toStairs.get(StoneDecoration.hempcrete.getId()))
				.unlocks("has_hempcrete", has(StoneDecoration.hempcrete))
				.save(out, toRL("hempcrete_stairs_from_hempcrete_stonecutting"));
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.concrete), IEBlocks.toSlab.get(StoneDecoration.concrete.getId()), 2)
				.unlocks("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL("concrete_slab_from_concrete_stonecutting"));
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.concrete), IEBlocks.toStairs.get(StoneDecoration.concrete.getId()))
				.unlocks("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL("concrete_stairs_from_concrete_stonecutting"));
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.concreteTile), IEBlocks.toSlab.get(StoneDecoration.concreteTile.getId()), 2)
				.unlocks("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL("concrete_tile_slab_from_concrete_tile_stonecutting"));
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.concreteTile), IEBlocks.toStairs.get(StoneDecoration.concreteTile.getId()))
				.unlocks("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL("concrete_tile_stairs_from_concrete_tile_stonecutting"));
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.concreteLeaded), IEBlocks.toSlab.get(StoneDecoration.concreteLeaded.getId()), 2)
				.unlocks("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL("concrete_leaded_slab_from_concrete_leaded_stonecutting"));
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.concreteLeaded), IEBlocks.toStairs.get(StoneDecoration.concreteLeaded.getId()))
				.unlocks("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL("concrete_leaded_stairs_from_concrete_leaded_stonecutting"));
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(StoneDecoration.concrete), StoneDecoration.concreteTile)
				.unlocks("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL("concrete_tile_from_concrete_stonecutting"));

		ShapedRecipeBuilder.shaped(StoneDecoration.alloybrick, 2)
				.pattern("sb")
				.pattern("bs")
				.define('s', Tags.Items.SANDSTONE)
				.define('b', Tags.Items.INGOTS_BRICK)
				.unlockedBy("has_brick", has(Tags.Items.INGOTS_BRICK))
				.save(out, toRL(toPath(StoneDecoration.alloybrick)));
		ShapelessRecipeBuilder.shapeless(StoneDecoration.blastbrickReinforced)
				.requires(StoneDecoration.blastbrick)
				.requires(IETags.getTagsFor(EnumMetals.STEEL).plate)
				.unlockedBy("has_blastbrick", has(StoneDecoration.blastbrick))
				.save(out, toRL(toPath(StoneDecoration.blastbrickReinforced)));

		TurnAndCopyRecipeBuilder.builder(StoneDecoration.concrete, 8)
				.allowQuarterTurn()
				.group("ie_concrete")
				.pattern("scs")
				.pattern("gbg")
				.pattern("scs")
				.define('s', Tags.Items.SAND)
				.define('c', IETags.clay)
				.define('g', Tags.Items.GRAVEL)
				.define('b', new IngredientFluidStack(FluidTags.WATER, FluidAttributes.BUCKET_VOLUME))
				.unlockedBy("has_clay", has(IETags.clay))
				.save(out, toRL("concrete"));
		TurnAndCopyRecipeBuilder.builder(StoneDecoration.concrete, 12)
				.allowQuarterTurn()
				.group("ie_concrete")
				.pattern("scs")
				.pattern("gbg")
				.pattern("scs")
				.define('s', IEItems.Ingredients.slag)
				.define('c', IETags.clay)
				.define('g', Tags.Items.GRAVEL)
				.define('b', new IngredientFluidStack(FluidTags.WATER, FluidAttributes.BUCKET_VOLUME))
				.unlockedBy("has_slag", has(IEItems.Ingredients.slag))
				.save(out, toRL("concrete"));
		ShapedRecipeBuilder.shaped(StoneDecoration.concreteTile, 4)
				.group("ie_concrete")
				.pattern("cc")
				.pattern("cc")
				.define('c', StoneDecoration.concrete)
				.unlockedBy("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL(toPath(StoneDecoration.concreteTile)));
		ShapelessRecipeBuilder.shapeless(StoneDecoration.concreteLeaded)
				.requires(StoneDecoration.concrete)
				.requires(IETags.getTagsFor(EnumMetals.LEAD).plate)
				.unlockedBy("has_concrete", has(StoneDecoration.concrete))
				.save(out, toRL(toPath(StoneDecoration.concreteLeaded)));

		TurnAndCopyRecipeBuilder.builder(StoneDecoration.insulatingGlass, 2)
				.allowQuarterTurn()
				.pattern(" g ")
				.pattern("idi")
				.pattern(" g ")
				.define('g', Tags.Items.GLASS)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).dust)
				.define('d', Tags.Items.DYES_GREEN)
				.unlockedBy("has_glass", has(Tags.Items.GLASS))
				.save(out, toRL(toPath(StoneDecoration.insulatingGlass)));
	}

	private void recipesWoodenDecorations(@Nonnull Consumer<FinishedRecipe> out)
	{
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			addStairs(WoodenDecoration.treatedWood.get(style), out);

		int numTreatedStyles = TreatedWoodStyles.values().length;
		for(TreatedWoodStyles from : TreatedWoodStyles.values())
		{
			TreatedWoodStyles to = TreatedWoodStyles.values()[(from.ordinal()+1)%numTreatedStyles];
			ShapelessRecipeBuilder.shapeless(WoodenDecoration.treatedWood.get(to))
					.requires(WoodenDecoration.treatedWood.get(from))
					.unlockedBy("has_"+toPath(WoodenDecoration.treatedWood.get(from)), has(WoodenDecoration.treatedWood.get(from)))
					.save(out, toRL(toPath(WoodenDecoration.treatedWood.get(to))+"_from_"+from.toString().toLowerCase(Locale.US)));
		}
		ShapedRecipeBuilder.shaped(WoodenDecoration.treatedScaffolding, 6)
				.pattern("iii")
				.pattern(" s ")
				.pattern("s s")
				.define('i', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.treatedStick)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(WoodenDecoration.treatedScaffolding)));

		ShapedRecipeBuilder.shaped(WoodenDecoration.treatedFence, 3)
				.pattern("isi")
				.pattern("isi")
				.define('i', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.treatedStick)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(WoodenDecoration.treatedFence)));

		ShapedRecipeBuilder.shaped(WoodenDecoration.treatedPost)
				.pattern("f")
				.pattern("f")
				.pattern("s")
				.define('f', WoodenDecoration.treatedFence)
				.define('s', Blocks.STONE_BRICKS)
				.unlockedBy("has_"+toPath(WoodenDecoration.treatedFence), has(WoodenDecoration.treatedFence))
				.save(out, toRL(toPath(WoodenDecoration.treatedPost)));

		FluidAwareShapedRecipeBuilder.builder(WoodenDecoration.treatedWood.get(TreatedWoodStyles.HORIZONTAL), 8)
				.pattern("www")
				.pattern("wbw")
				.pattern("www")
				.define('w', ItemTags.PLANKS)
				.define('b', new IngredientFluidStack(IETags.fluidCreosote, FluidAttributes.BUCKET_VOLUME))
				.unlockedBy("has_creosote", has(IEFluids.fluidCreosote.getBucket()))
				.save(out, toRL(toPath(WoodenDecoration.treatedWood.get(TreatedWoodStyles.HORIZONTAL))));

		ShapedRecipeBuilder.shaped(WoodenDecoration.sawdust, 9)
				.pattern("sss")
				.pattern("sss")
				.pattern("sss")
				.define('s', IETags.sawdust)
				.unlockedBy("has_sawdust", has(IETags.sawdust))
				.save(out, toRL(toPath(WoodenDecoration.sawdust)));
	}

	private void recipesWoodenDevices(@Nonnull Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(WoodenDevices.craftingTable)
				.pattern("sss")
				.pattern("rcr")
				.pattern("r r")
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.define('r', IETags.treatedStick)
				.define('c', Blocks.CRAFTING_TABLE)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.craftingTable)));
		ShapedRecipeBuilder.shaped(WoodenDevices.crate)
				.pattern("ppp")
				.pattern("p p")
				.pattern("ppp")
				.define('p', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.crate)));
		TurnAndCopyRecipeBuilder.builder(WoodenDevices.reinforcedCrate)
				.setNBTCopyTargetRecipe(4)
				.allowQuarterTurn()
				.pattern("wpw")
				.pattern("rcr")
				.pattern("wpw")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('p', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('r', IETags.ironRod)
				.define('c', IEBlocks.WoodenDevices.crate)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.reinforcedCrate)));

		ShapedRecipeBuilder.shaped(WoodenDevices.treatedWallmount, 4)
				.pattern("ww")
				.pattern("ws")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.treatedStick)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.treatedWallmount)));

		ShapedRecipeBuilder.shaped(WoodenDevices.sorter)
				.pattern("wrw")
				.pattern("ici")
				.pattern("wbw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('b', ConveyorHandler.getBlock(BasicConveyor.NAME))
				.define('c', Ingredients.componentIron)
				.unlockedBy("has_"+toPath(ConveyorHandler.getBlock(BasicConveyor.NAME)), has(ConveyorHandler.getBlock(BasicConveyor.NAME)))
				.save(out, toRL(toPath(WoodenDevices.sorter)));
		ShapedRecipeBuilder.shaped(WoodenDevices.itemBatcher)
				.pattern("wrw")
				.pattern("ici")
				.pattern("wpw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('p', Ingredients.circuitBoard)
				.define('c', Ingredients.componentIron)
				.unlockedBy("has_"+toPath(ConveyorHandler.getBlock(BasicConveyor.NAME)), has(ConveyorHandler.getBlock(BasicConveyor.NAME)))
				.save(out, toRL(toPath(WoodenDevices.itemBatcher)));
		ShapedRecipeBuilder.shaped(WoodenDevices.fluidSorter)
				.pattern("wrw")
				.pattern("ici")
				.pattern("wbw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('b', MetalDevices.fluidPipe)
				.define('c', Ingredients.componentIron)
				.unlockedBy("has_"+toPath(MetalDevices.fluidPipe), has(MetalDevices.fluidPipe))
				.save(out, toRL(toPath(WoodenDevices.fluidSorter)));
		ShapedRecipeBuilder.shaped(WoodenDevices.logicUnit)
				.pattern("wtw")
				.pattern("tct")
				.pattern("wtw")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('t', Ingredients.electronTube)
				.define('c', Ingredients.circuitBoard)
				.unlockedBy("has_"+toPath(Ingredients.circuitBoard), has(Ingredients.circuitBoard))
				.save(out, toRL(toPath(WoodenDevices.logicUnit)));

		ShapedRecipeBuilder.shaped(WoodenDevices.turntable)
				.pattern("iwi")
				.pattern("rcr")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('c', MetalDecoration.lvCoil)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_"+toPath(MetalDecoration.lvCoil), has(MetalDecoration.lvCoil))
				.save(out, toRL(toPath(WoodenDevices.turntable)));

		ShapedRecipeBuilder.shaped(WoodenDevices.windmill)
				.pattern("ppp")
				.pattern("pip")
				.pattern("ppp")
				.define('p', Ingredients.windmillBlade)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.unlockedBy("has_"+toPath(Ingredients.windmillBlade), has(Ingredients.windmillBlade))
				.save(out, toRL(toPath(WoodenDevices.windmill)));
		ShapedRecipeBuilder.shaped(WoodenDevices.watermill)
				.pattern(" p ")
				.pattern("pip")
				.pattern(" p ")
				.define('p', Ingredients.waterwheelSegment)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_"+toPath(Ingredients.waterwheelSegment), has(Ingredients.waterwheelSegment))
				.save(out, toRL(toPath(WoodenDevices.watermill)));

		ShapedRecipeBuilder.shaped(WoodenDevices.gunpowderBarrel)
				.pattern(" f ")
				.pattern("gbg")
				.pattern("ggg")
				.define('f', Ingredients.hempFiber)
				.define('g', Tags.Items.GUNPOWDER)
				.define('b', WoodenDevices.woodenBarrel)
				.unlockedBy("has_"+toPath(WoodenDevices.woodenBarrel), has(WoodenDevices.woodenBarrel))
				.save(out, toRL(toPath(WoodenDevices.gunpowderBarrel)));

		ShapedRecipeBuilder.shaped(WoodenDevices.workbench)
				.pattern("iss")
				.pattern("c f")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.define('c', WoodenDevices.craftingTable)
				.define('f', WoodenDecoration.treatedFence)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.workbench)));
		ShapedRecipeBuilder.shaped(WoodenDevices.circuitTable)
				.pattern("sst")
				.pattern("c e")
				.define('t', Tools.screwdriver)
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.define('c', WoodenDevices.craftingTable)
				.define('e', MetalDecoration.engineeringLight)
				.unlockedBy("has_"+toPath(Ingredients.circuitBoard), has(Ingredients.circuitBoard))
				.save(out, toRL(toPath(WoodenDevices.circuitTable)));

		ShapedRecipeBuilder.shaped(WoodenDevices.woodenBarrel)
				.pattern("sss")
				.pattern("w w")
				.pattern("www")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.woodenBarrel)));
	}

	private void recipesMetalDecorations(@Nonnull Consumer<FinishedRecipe> out)
	{
		for(DyeColor dye : DyeColor.values())
		{
			Tag<Item> dyeTag = createItemWrapper(new ResourceLocation("forge", "dyes/"+dye.getName()));
			Block coloredSheetmetal = MetalDecoration.coloredSheetmetal.get(dye).get();
			ShapedRecipeBuilder.shaped(coloredSheetmetal, 8)
					.pattern("sss")
					.pattern("sds")
					.pattern("sss")
					.define('s', IETags.getItemTag(IETags.sheetmetals))
					.define('d', dyeTag)
					.unlockedBy("has_sheetmetal", has(IETags.getItemTag(IETags.sheetmetals)))
					.save(out, toRL(toPath(coloredSheetmetal)));
		}

		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			addStairs(MetalDecoration.steelScaffolding.get(type), out);
			addStairs(MetalDecoration.aluScaffolding.get(type), out);
		}

		int numScaffoldingTypes = MetalScaffoldingType.values().length;
		for(MetalScaffoldingType from : MetalScaffoldingType.values())
		{
			MetalScaffoldingType to = MetalScaffoldingType.values()[(from.ordinal()+1)%numScaffoldingTypes];
			ShapelessRecipeBuilder.shapeless(MetalDecoration.aluScaffolding.get(to))
					.requires(MetalDecoration.aluScaffolding.get(from))
					.unlockedBy("has_"+toPath(MetalDecoration.aluScaffolding.get(from)), has(MetalDecoration.aluScaffolding.get(from)))
					.save(out, toRL("alu_scaffolding_"+to.name().toLowerCase(Locale.US)+"_from_"+from.name().toLowerCase(Locale.US)));
			ShapelessRecipeBuilder.shapeless(MetalDecoration.steelScaffolding.get(to))
					.requires(MetalDecoration.steelScaffolding.get(from))
					.unlockedBy("has_"+toPath(MetalDecoration.steelScaffolding.get(from)), has(MetalDecoration.steelScaffolding.get(from)))
					.save(out, toRL("steel_scaffolding_"+to.name().toLowerCase(Locale.US)+"_from_"+from.name().toLowerCase(Locale.US)));
		}

		ShapedRecipeBuilder.shaped(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD), 6)
				.pattern("iii")
				.pattern(" s ")
				.pattern("s s")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('s', IETags.aluminumRod)
				.unlockedBy("has_alu_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.unlockedBy("has_alu_sticks", has(IETags.aluminumRod))
				.save(out, toRL(toPath(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD))));
		ShapedRecipeBuilder.shaped(MetalDecoration.slopeAlu, 4)
				.pattern("sss")
				.pattern("ss ")
				.pattern("s  ")
				.define('s', MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD))
				.unlockedBy("has_"+toPath(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD)), has(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD)))
				.save(out, toRL(toPath(MetalDecoration.slopeAlu)));

		ShapedRecipeBuilder.shaped(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD), 6)
				.pattern("iii")
				.pattern(" s ")
				.pattern("s s")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_steel_sticks", has(IETags.steelRod))
				.save(out, toRL(toPath(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))));
		ShapedRecipeBuilder.shaped(MetalDecoration.slopeSteel, 4)
				.pattern("sss")
				.pattern("ss ")
				.pattern("s  ")
				.define('s', MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))
				.unlockedBy("has_"+toPath(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)), has(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)))
				.save(out, toRL(toPath(MetalDecoration.slopeSteel)));

		ShapedRecipeBuilder.shaped(MetalDecoration.aluFence, 3)
				.pattern("isi")
				.pattern("isi")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('s', IETags.aluminumRod)
				.unlockedBy("has_alu_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.unlockedBy("has_alu_sticks", has(IETags.aluminumRod))
				.save(out, toRL(toPath(MetalDecoration.aluFence)));
		ShapedRecipeBuilder.shaped(MetalDecoration.steelFence, 3)
				.pattern("isi")
				.pattern("isi")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_steel_sticks", has(IETags.steelRod))
				.save(out, toRL(toPath(MetalDecoration.steelFence)));

		ShapedRecipeBuilder.shaped(MetalDecoration.lvCoil)
				.pattern("www")
				.pattern("wiw")
				.pattern("www")
				.define('i', Tags.Items.INGOTS_IRON)
				.define('w', Misc.wireCoils.get(WireType.COPPER))
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.unlockedBy("has_"+toPath(Misc.wireCoils.get(WireType.COPPER)), has(Misc.wireCoils.get(WireType.COPPER)))
				.save(out, toRL(toPath(MetalDecoration.lvCoil)));
		ShapedRecipeBuilder.shaped(MetalDecoration.mvCoil)
				.pattern("www")
				.pattern("wiw")
				.pattern("www")
				.define('i', Tags.Items.INGOTS_IRON)
				.define('w', Misc.wireCoils.get(WireType.ELECTRUM))
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.unlockedBy("has_"+toPath(Misc.wireCoils.get(WireType.ELECTRUM)), has(Misc.wireCoils.get(WireType.ELECTRUM)))
				.save(out, toRL(toPath(MetalDecoration.mvCoil)));
		ShapedRecipeBuilder.shaped(MetalDecoration.hvCoil)
				.pattern("www")
				.pattern("wiw")
				.pattern("www")
				.define('i', Tags.Items.INGOTS_IRON)
				.define('w', Misc.wireCoils.get(WireType.STEEL))
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_"+toPath(Misc.wireCoils.get(WireType.STEEL)), has(Misc.wireCoils.get(WireType.STEEL)))
				.save(out, toRL(toPath(MetalDecoration.hvCoil)));

		TurnAndCopyRecipeBuilder.builder(MetalDecoration.engineeringRS, 4)
				.allowQuarterTurn()
				.pattern("iri")
				.pattern("rcr")
				.pattern("iri")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_iron_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)))
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_redstone", has(Items.REDSTONE))
				.save(out, toRL(toPath(MetalDecoration.engineeringRS)));
		ShapedRecipeBuilder.shaped(MetalDecoration.engineeringLight, 4)
				.pattern("igi")
				.pattern("gcg")
				.pattern("igi")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('g', Ingredients.componentIron)
				.unlockedBy("has_iron_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)))
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_component_iron", has(Ingredients.componentIron))
				.save(out, toRL(toPath(MetalDecoration.engineeringLight)));
		ShapedRecipeBuilder.shaped(MetalDecoration.engineeringHeavy, 4)
				.pattern("igi")
				.pattern("geg")
				.pattern("igi")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.define('e', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.define('g', Ingredients.componentSteel)
				.unlockedBy("has_steel_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.unlockedBy("has_component_steel", has(Ingredients.componentSteel))
				.save(out, toRL(toPath(MetalDecoration.engineeringHeavy)));
		ShapedRecipeBuilder.shaped(MetalDecoration.generator, 4)
				.pattern("iei")
				.pattern("ede")
				.pattern("iei")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.define('e', IETags.getTagsFor(EnumMetals.ELECTRUM).plate)
				.define('d', MetalDevices.dynamo)
				.unlockedBy("has_steel_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.unlockedBy("has_"+toPath(MetalDevices.dynamo), has(MetalDevices.dynamo))
				.save(out, toRL(toPath(MetalDecoration.generator)));
		TurnAndCopyRecipeBuilder.builder(MetalDecoration.radiator, 4)
				.allowQuarterTurn()
				.pattern("ici")
				.pattern("cbc")
				.pattern("ici")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.define('b', new IngredientFluidStack(FluidTags.WATER, FluidAttributes.BUCKET_VOLUME))
				.unlockedBy("has_steel_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.unlockedBy("has_water_bucket", has(Items.WATER_BUCKET))
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(MetalDecoration.radiator)));

		ShapedRecipeBuilder.shaped(MetalDecoration.aluPost)
				.pattern("f")
				.pattern("f")
				.pattern("s")
				.define('f', MetalDecoration.aluFence)
				.define('s', Blocks.STONE_BRICKS)
				.unlockedBy("has_"+toPath(MetalDecoration.aluFence), has(MetalDecoration.aluFence))
				.save(out, toRL(toPath(MetalDecoration.aluPost)));
		ShapedRecipeBuilder.shaped(MetalDecoration.steelPost)
				.pattern("f")
				.pattern("f")
				.pattern("s")
				.define('f', MetalDecoration.steelFence)
				.define('s', Blocks.STONE_BRICKS)
				.unlockedBy("has_"+toPath(MetalDecoration.steelFence), has(MetalDecoration.steelFence))
				.save(out, toRL(toPath(MetalDecoration.steelPost)));

		ShapedRecipeBuilder.shaped(MetalDecoration.aluWallmount)
				.pattern("ii")
				.pattern("is")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('s', IETags.aluminumRod)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.save(out, toRL(toPath(MetalDecoration.aluWallmount)));
		ShapedRecipeBuilder.shaped(MetalDecoration.steelWallmount)
				.pattern("ii")
				.pattern("is")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(MetalDecoration.steelWallmount)));

		ShapedRecipeBuilder.shaped(MetalDecoration.metalLadder.get(CoverType.NONE), 3)
				.pattern("s s")
				.pattern("sss")
				.pattern("s s")
				.define('s', IETags.metalRods)
				.unlockedBy("has_metal_rod", has(IETags.metalRods))
				.save(out, toRL(toPath(MetalDecoration.metalLadder.get(CoverType.NONE))));
		ShapedRecipeBuilder.shaped(MetalDecoration.metalLadder.get(CoverType.ALU), 3)
				.pattern("s")
				.pattern("l")
				.define('s', IETags.getItemTag(IETags.scaffoldingAlu))
				.define('l', MetalDecoration.metalLadder.get(CoverType.NONE))
				.unlockedBy("has_metal_ladder", has(MetalDecoration.metalLadder.get(CoverType.NONE)))
				.save(out, toRL(toPath(MetalDecoration.metalLadder.get(CoverType.ALU))));
		ShapedRecipeBuilder.shaped(MetalDecoration.metalLadder.get(CoverType.STEEL), 3)
				.pattern("s")
				.pattern("l")
				.define('s', IETags.getItemTag(IETags.scaffoldingSteel))
				.define('l', MetalDecoration.metalLadder.get(CoverType.NONE))
				.unlockedBy("has_metal_ladder", has(MetalDecoration.metalLadder.get(CoverType.NONE)))
				.save(out, toRL(toPath(MetalDecoration.metalLadder.get(CoverType.STEEL))));
	}

	private void recipesMetalDevices(@Nonnull Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(MetalDevices.razorWire, 3)
				.pattern("psp")
				.pattern("sss")
				.pattern("psp")
				.define('s', Ingredients.wireSteel)
				.define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.unlockedBy("has_"+toPath(Ingredients.wireSteel), has(Ingredients.wireSteel))
				.save(out, toRL(toPath(MetalDevices.razorWire)));
		ShapedRecipeBuilder.shaped(MetalDevices.capacitorLV)
				.pattern("iii")
				.pattern("clc")
				.pattern("wrw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('l', IETags.getTagsFor(EnumMetals.LEAD).ingot)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_lead_ingot", has(IETags.getTagsFor(EnumMetals.LEAD).ingot))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(MetalDevices.capacitorLV)));
		ShapedRecipeBuilder.shaped(MetalDevices.capacitorMV)
				.pattern("iii")
				.pattern("ele")
				.pattern("wrw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('l', IETags.getTagsFor(EnumMetals.LEAD).ingot)
				.define('e', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('r', Tags.Items.STORAGE_BLOCKS_REDSTONE)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.unlockedBy("has_lead_ingot", has(IETags.getTagsFor(EnumMetals.LEAD).ingot))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(MetalDevices.capacitorMV)));
		ShapedRecipeBuilder.shaped(MetalDevices.capacitorHV)
				.pattern("sss")
				.pattern("ala")
				.pattern("wrw")
				.define('s', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('a', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('l', IETags.getItemTag(IETags.getTagsFor(EnumMetals.LEAD).storage))
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('r', Tags.Items.STORAGE_BLOCKS_REDSTONE)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(MetalDevices.capacitorHV)));
		ShapedRecipeBuilder.shaped(MetalDevices.barrel)
				.pattern("sss")
				.pattern("w w")
				.pattern("www")
				.define('w', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.define('s', IEBlocks.toSlab.get(IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON).getId()))
				.unlockedBy("has_iron_sheet_slab", has(IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON)))
				.save(out, toRL(toPath(MetalDevices.barrel)));
		ShapedRecipeBuilder.shaped(MetalDevices.fluidPump)
				.pattern(" i ")
				.pattern("ici")
				.pattern("ppp")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('c', Ingredients.componentIron)
				.define('p', IEBlocks.MetalDevices.fluidPipe)
				.unlockedBy("has_"+toPath(IEBlocks.MetalDevices.fluidPipe), has(IEBlocks.MetalDevices.fluidPipe))
				.save(out, toRL(toPath(MetalDevices.fluidPump)));
		ShapedRecipeBuilder.shaped(MetalDevices.blastFurnacePreheater)
				.pattern("sss")
				.pattern("s s")
				.pattern("shs")
				.define('s', IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON))
				.define('h', MetalDevices.furnaceHeater)
				.unlockedBy("has_"+toPath(MetalDevices.furnaceHeater), has(MetalDevices.furnaceHeater))
				.save(out, toRL(toPath(MetalDevices.blastFurnacePreheater)));
		ShapedRecipeBuilder.shaped(MetalDevices.furnaceHeater)
				.pattern("ici")
				.pattern("clc")
				.pattern("iri")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('l', MetalDecoration.lvCoil)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_"+toPath(MetalDecoration.lvCoil), has(MetalDecoration.lvCoil))
				.save(out, toRL(toPath(MetalDevices.furnaceHeater)));
		ShapedRecipeBuilder.shaped(MetalDevices.dynamo)
				.pattern("rlr")
				.pattern("iii")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('l', MetalDecoration.lvCoil)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_"+toPath(MetalDecoration.lvCoil), has(MetalDecoration.lvCoil))
				.save(out, toRL(toPath(MetalDevices.dynamo)));
		ShapedRecipeBuilder.shaped(MetalDevices.thermoelectricGen)
				.pattern("iii")
				.pattern("ele")
				.pattern("eee")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('l', MetalDecoration.lvCoil)
				.define('e', IETags.getTagsFor(EnumMetals.CONSTANTAN).plate)
				.unlockedBy("has_"+toPath(MetalDecoration.lvCoil), has(MetalDecoration.lvCoil))
				.save(out, toRL(toPath(MetalDevices.thermoelectricGen)));
		ShapedRecipeBuilder.shaped(MetalDevices.electricLantern)
				.pattern(" i ")
				.pattern("pep")
				.pattern("iri")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('e', Ingredients.electronTube)
				.define('p', Tags.Items.GLASS_PANES)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_"+toPath(Ingredients.electronTube), has(Ingredients.electronTube))
				.save(out, toRL(toPath(MetalDevices.electricLantern)));
		ShapedRecipeBuilder.shaped(MetalDevices.chargingStation)
				.pattern("ici")
				.pattern("ggg")
				.pattern("wlw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('l', MetalDecoration.lvCoil)
				.define('g', Tags.Items.GLASS)
				.unlockedBy("has_"+toPath(MetalDecoration.lvCoil), has(MetalDecoration.lvCoil))
				.save(out, toRL(toPath(MetalDevices.chargingStation)));
		ShapedRecipeBuilder.shaped(MetalDevices.fluidPipe, 8)
				.pattern("ppp")
				.pattern("   ")
				.pattern("ppp")
				.define('p', IETags.getTagsFor(EnumMetals.IRON).plate)
				.unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate))
				.save(out, toRL(toPath(MetalDevices.fluidPipe)));
		ShapedRecipeBuilder.shaped(MetalDevices.sampleDrill)
				.pattern("sfs")
				.pattern("sfs")
				.pattern("efe")
				.define('s', MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))
				.define('f', MetalDecoration.steelFence)
				.define('e', MetalDecoration.engineeringLight)
				.unlockedBy("has_"+toPath(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)), has(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)))
				.unlockedBy("has_"+toPath(MetalDecoration.steelFence), has(MetalDecoration.steelFence))
				.unlockedBy("has_"+toPath(MetalDecoration.engineeringLight), has(MetalDecoration.engineeringLight))
				.save(out, toRL(toPath(MetalDevices.sampleDrill)));
		ShapedRecipeBuilder.shaped(MetalDevices.teslaCoil)
				.pattern("iii")
				.pattern(" l ")
				.pattern("hlh")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('l', MetalDecoration.lvCoil)
				.define('h', MetalDevices.capacitorHV)
				.unlockedBy("has_"+toPath(MetalDevices.capacitorHV), has(MetalDevices.capacitorHV))
				.save(out, toRL(toPath(MetalDevices.teslaCoil)));
		ShapedRecipeBuilder.shaped(MetalDevices.floodlight)
				.pattern("iii")
				.pattern("pel")
				.pattern("ici")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('l', MetalDecoration.lvCoil)
				.define('e', Ingredients.electronTube)
				.define('c', Ingredients.componentIron)
				.define('p', Tags.Items.GLASS_PANES)
				.unlockedBy("has_"+toPath(Ingredients.electronTube), has(Ingredients.electronTube))
				.save(out, toRL(toPath(MetalDevices.floodlight)));
		ShapedRecipeBuilder.shaped(MetalDevices.turretChem)
				.pattern(" s ")
				.pattern(" gc")
				.pattern("bte")
				.define('s', Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
				.define('g', Weapons.chemthrower)
				.define('c', Ingredients.circuitBoard)
				.define('b', MetalDevices.barrel)
				.define('t', WoodenDevices.turntable)
				.define('e', MetalDecoration.engineeringRS)
				.unlockedBy("has_"+toPath(Weapons.chemthrower), has(Weapons.chemthrower))
				.save(out, toRL(toPath(MetalDevices.turretChem)));
		ShapedRecipeBuilder.shaped(MetalDevices.turretGun)
				.pattern(" s ")
				.pattern(" gc")
				.pattern("bte")
				.define('s', Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
				.define('g', Weapons.revolver)
				.define('c', Ingredients.circuitBoard)
				.define('b', Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))
				.define('t', WoodenDevices.turntable)
				.define('e', MetalDecoration.engineeringRS)
				.unlockedBy("has_"+toPath(Weapons.revolver), has(Weapons.revolver))
				.save(out, toRL(toPath(MetalDevices.turretGun)));
		ShapedRecipeBuilder.shaped(MetalDevices.cloche)
				.pattern("geg")
				.pattern("g g")
				.pattern("wcw")
				.define('g', Tags.Items.GLASS)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('e', Ingredients.electronTube)
				.define('c', Ingredients.componentIron)
				.unlockedBy("has_"+toPath(Ingredients.electronTube), has(Ingredients.electronTube))
				.save(out, toRL(toPath(MetalDevices.cloche)));
		ShapedRecipeBuilder.shaped(MetalDevices.fluidPlacer)
				.pattern("ibi")
				.pattern("b b")
				.pattern("ibi")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('b', Items.IRON_BARS)
				.unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate))
				.save(out, toRL(toPath(MetalDevices.fluidPlacer)));
		for(Entry<EnumMetals, BlockEntry<ChuteBlock>> chute : MetalDevices.chutes.entrySet())
			ShapedRecipeBuilder.shaped(chute.getValue(), 12)
					.pattern("s s")
					.pattern("s s")
					.pattern("s s")
					.define('s', IETags.getItemTag(IETags.getTagsFor(chute.getKey()).sheetmetal))
					.unlockedBy("has_plate", has(IETags.getTagsFor(chute.getKey()).plate))
					.save(out, toRL(toPath(chute.getValue())));
	}

	private void recipesConnectors(@Nonnull Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.breakerswitch)
				.pattern(" l ")
				.pattern("cic")
				.define('l', Items.LEVER)
				.define('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('c', Blocks.TERRACOTTA)
				.unlockedBy("has_"+toPath(Misc.wireCoils.get(WireType.COPPER)), has(Misc.wireCoils.get(WireType.COPPER)))
				.save(out, toRL(toPath(Connectors.breakerswitch)));
		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.redstoneBreaker)
				.pattern("h h")
				.pattern("ici")
				.pattern("iri")
				.define('h', IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false))
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('c', Items.REPEATER)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_hv_connector", has(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)))
				.save(out, toRL(toPath(Connectors.redstoneBreaker)));

		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.currentTransformer)
				.pattern(" m ")
				.pattern("bcb")
				.pattern("ici")
				.define('m', IEItems.Tools.voltmeter)
				.define('b', Blocks.TERRACOTTA)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('c', MetalDecoration.lvCoil)
				.unlockedBy("has_voltmeter", has(IEItems.Tools.voltmeter))
				.save(out, toRL(toPath(Connectors.currentTransformer)));

		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.transformer)
				.pattern("l m")
				.pattern("ibi")
				.pattern("iii")
				.define('l', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.define('m', IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false))
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('b', MetalDecoration.mvCoil)
				.unlockedBy("has_mv_connector", has(IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false)))
				.save(out, toRL(toPath(Connectors.transformer)));
		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.transformerHV)
				.pattern("m h")
				.pattern("ibi")
				.pattern("iii")
				.define('m', IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false))
				.define('h', IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false))
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('b', MetalDecoration.hvCoil)
				.unlockedBy("has_hv_connector", has(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)))
				.save(out, toRL(toPath(Connectors.transformerHV)));

		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.connectorStructural, 8)
				.pattern("isi")
				.pattern("i i")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Connectors.connectorStructural)));

		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.connectorRedstone, 4)
				.pattern("iii")
				.pattern("brb")
				.define('i', IETags.getTagsFor(EnumMetals.ELECTRUM).nugget)
				.define('b', Blocks.TERRACOTTA)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_electrum_nugget", has(IETags.getTagsFor(EnumMetals.ELECTRUM).nugget))
				.save(out, toRL(toPath(Connectors.connectorRedstone)));
		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.connectorProbe)
				.pattern(" c ")
				.pattern("gpg")
				.pattern(" q ")
				.define('c', Connectors.connectorRedstone)
				.define('g', Tags.Items.GLASS_PANES)
				.define('p', Ingredients.circuitBoard)
				.define('q', Tags.Items.GEMS_QUARTZ)
				.unlockedBy("has_connector", has(Connectors.connectorRedstone))
				.save(out, toRL(toPath(Connectors.connectorProbe)));
		ShapedRecipeBuilder.shaped(IEBlocks.Connectors.connectorBundled)
				.pattern(" w ")
				.pattern("wcw")
				.pattern(" w ")
				.define('c', Connectors.connectorRedstone)
				.define('w', IETags.aluminumWire)
				.unlockedBy("has_connector", has(Connectors.connectorRedstone))
				.save(out, toRL(toPath(Connectors.connectorBundled)));

		// Connectors and Relays
		ShapedRecipeBuilder.shaped(Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), 4)
				.pattern(" i ")
				.pattern("cic")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('c', Blocks.TERRACOTTA)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL("connector_lv"));
		ShapedRecipeBuilder.shaped(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), 8)
				.pattern(" i ")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('c', Blocks.TERRACOTTA)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL("connector_lv_relay"));
		ShapedRecipeBuilder.shaped(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), 4)
				.pattern(" i ")
				.pattern("cic")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.define('c', Blocks.TERRACOTTA)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.save(out, toRL("connector_mv"));
		ShapedRecipeBuilder.shaped(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), 8)
				.pattern(" i ")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.define('c', Blocks.TERRACOTTA)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.save(out, toRL("connector_mv_relay"));
		ShapedRecipeBuilder.shaped(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), 4)
				.pattern(" i ")
				.pattern("cic")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('c', Blocks.TERRACOTTA)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL("connector_hv"));
		ShapedRecipeBuilder.shaped(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), 8)
				.pattern(" i ")
				.pattern("cic")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('c', StoneDecoration.insulatingGlass)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL("connector_hv_relay"));
	}

	private void recipesConveyors(@Nonnull Consumer<FinishedRecipe> out)
	{
		ItemLike basic = ConveyorHandler.getBlock(BasicConveyor.NAME);
		ItemLike redstone = ConveyorHandler.getBlock(RedstoneConveyor.NAME);
		ItemLike covered = ConveyorHandler.getBlock(CoveredConveyor.NAME);
		ItemLike dropper = ConveyorHandler.getBlock(DropConveyor.NAME);
		ItemLike dropperCovered = ConveyorHandler.getBlock(DropCoveredConveyor.NAME);
		ItemLike extract = ConveyorHandler.getBlock(ExtractConveyor.NAME);
		ItemLike extractCovered = ConveyorHandler.getBlock(ExtractCoveredConveyor.NAME);
		ItemLike splitter = ConveyorHandler.getBlock(SplitConveyor.NAME);
		ItemLike splitterCovered = ConveyorHandler.getBlock(SplitCoveredConveyor.NAME);
		ItemLike vertical = ConveyorHandler.getBlock(VerticalConveyor.NAME);
		ItemLike verticalCovered = ConveyorHandler.getBlock(VerticalCoveredConveyor.NAME);
		addCoveyorCoveringRecipe(covered, basic, out);
		addCoveyorCoveringRecipe(dropperCovered, dropper, out);
		addCoveyorCoveringRecipe(extractCovered, extract, out);
		addCoveyorCoveringRecipe(splitterCovered, splitter, out);
		addCoveyorCoveringRecipe(verticalCovered, vertical, out);
		ShapedRecipeBuilder.shaped(basic, 8)
				.pattern("lll")
				.pattern("iri")
				.define('l', Tags.Items.LEATHER)
				.define('i', Tags.Items.INGOTS_IRON)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_leather", has(Items.LEATHER))
				.save(out, toRL(toPath(basic)));
		//TODO
		//ShapedRecipeBuilder.shapedRecipe(basic, 8)
		//		.patternLine("rrr")
		//		.patternLine("iri")
		//		.key('r', RUBBER)
		//		.key('i', Tags.Items.INGOTS_IRON)
		//		.key('r', Tags.Items.DUSTS_REDSTONE)
		//		.build(out);
		ShapedRecipeBuilder.shaped(redstone)
				.pattern("c")
				.pattern("r")
				.define('c', basic)
				.define('r', Blocks.REDSTONE_TORCH)
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(redstone)));
		ShapedRecipeBuilder.shaped(dropper)
				.pattern("c")
				.pattern("t")
				.define('c', basic)
				.define('t', Blocks.IRON_TRAPDOOR)
				.unlockedBy("has_trapdoor", has(Blocks.IRON_TRAPDOOR))
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(dropper)));
		ShapedRecipeBuilder.shaped(extract)
				.pattern("ws")
				.pattern("mc")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('s', Cloth.curtain)
				.define('m', Ingredients.componentIron)
				.define('c', basic)
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(extract)));
		ShapedRecipeBuilder.shaped(splitter, 3)
				.pattern("cic")
				.pattern(" c ")
				.define('c', basic)
				.define('i', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(splitter)));
		ShapedRecipeBuilder.shaped(vertical, 3)
				.pattern("ci")
				.pattern("c ")
				.pattern("ci")
				.define('c', basic)
				.define('i', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(vertical)));
	}

	private void addCoveyorCoveringRecipe(ItemLike covered, ItemLike base, Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(covered)
				.pattern("s")
				.pattern("c")
				.define('s', IETags.getItemTag(IETags.scaffoldingSteel))
				.define('c', base)
				.unlockedBy("has_vertical_conveyor", has(base))
				.save(out, toRL(toPath(covered)));
	}

	private void recipesCloth(@Nonnull Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(Cloth.balloon, 2)
				.pattern(" f ")
				.pattern("ftf")
				.pattern(" s ")
				.define('f', IEItems.Ingredients.hempFabric)
				.define('t', Items.TORCH)
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.save(out, toRL(toPath(Cloth.balloon)));
		ShapedRecipeBuilder.shaped(Cloth.cushion, 3)
				.pattern("fff")
				.pattern("f f")
				.pattern("fff")
				.define('f', IEItems.Ingredients.hempFabric)
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.save(out, toRL(toPath(Cloth.cushion)));
		ShapedRecipeBuilder.shaped(Cloth.curtain, 3)
				.pattern("sss")
				.pattern("fff")
				.pattern("fff")
				.define('s', IETags.metalRods)
				.define('f', IEItems.Ingredients.hempFabric)
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.unlockedBy("has_metal_rod", has(IETags.metalRods))
				.save(out, toRL(toPath(Cloth.curtain)));
	}

	private void recipesTools(@Nonnull Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(Tools.hammer)
				.pattern(" if")
				.pattern(" si")
				.pattern("s  ")
				.define('s', Tags.Items.RODS_WOODEN)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('f', Tags.Items.STRING)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Tools.hammer)));
		ShapedRecipeBuilder.shaped(Tools.wirecutter)
				.pattern("si")
				.pattern(" s")
				.define('s', Tags.Items.RODS_WOODEN)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Tools.wirecutter)));
		ShapedRecipeBuilder.shaped(Tools.screwdriver)
				.pattern(" i")
				.pattern("s ")
				.define('s', Tags.Items.RODS_WOODEN)
				.define('i', IETags.ironRod)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Tools.screwdriver)));
		ShapelessRecipeBuilder.shapeless(Tools.manual)
				.requires(Items.BOOK)
				.requires(Items.LEVER)
				.unlockedBy("has_book", has(Items.BOOK))
				.save(out, toRL(toPath(Tools.manual)));
		ShapedRecipeBuilder.shaped(Tools.steelAxe)
				.pattern("ii")
				.pattern("is")
				.pattern(" s")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.steelAxe)));
		ShapedRecipeBuilder.shaped(Tools.steelPick)
				.pattern("iii")
				.pattern(" s ")
				.pattern(" s ")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.steelPick)));
		ShapedRecipeBuilder.shaped(Tools.steelShovel)
				.pattern("i")
				.pattern("s")
				.pattern("s")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.steelShovel)));
		ShapedRecipeBuilder.shaped(Tools.steelHoe)
				.pattern("ii")
				.pattern(" s")
				.pattern(" s")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.steelHoe)));
		ShapedRecipeBuilder.shaped(Tools.steelSword)
				.pattern("i")
				.pattern("i")
				.pattern("s")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.steelSword)));

		addArmor(IETags.getTagsFor(EnumMetals.STEEL).plate, Tools.steelArmor, "steel_plate", out);

		ShapedRecipeBuilder.shaped(Tools.toolbox)
				.pattern("ppp")
				.pattern("rcr")
				.define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.define('r', Tags.Items.DYES_RED)
				.define('c', IEBlocks.WoodenDevices.crate)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.unlockedBy("has_red_dye", has(Items.RED_DYE))
				.unlockedBy("has_"+toPath(IEBlocks.WoodenDevices.crate), has(IEBlocks.WoodenDevices.crate))
				.save(out, toRL(toPath(Tools.toolbox)));
		ShapedRecipeBuilder.shaped(Tools.voltmeter)
				.pattern(" p ")
				.pattern("scs")
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('p', Items.COMPASS)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_compass", has(Items.COMPASS))
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(Tools.voltmeter)));

		ShapedRecipeBuilder.shaped(Tools.drill)
				.pattern("  g")
				.pattern(" hg")
				.pattern("c  ")
				.define('g', Ingredients.woodenGrip)
				.define('h', MetalDecoration.engineeringHeavy)
				.define('c', Ingredients.componentIron)
				.unlockedBy("has_"+toPath(MetalDecoration.engineeringHeavy), has(MetalDecoration.engineeringHeavy))
				.save(out, toRL(toPath(Tools.drill)));
		ShapedRecipeBuilder.shaped(Tools.drillheadIron)
				.pattern("  i")
				.pattern("ii ")
				.pattern("bi ")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('b', makeIngredient(Tags.Items.STORAGE_BLOCKS_IRON))
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Tools.drillheadIron)));
		ShapedRecipeBuilder.shaped(Tools.drillheadSteel)
				.pattern("  i")
				.pattern("ii ")
				.pattern("bi ")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('b', makeIngredientFromBlock(IETags.getTagsFor(EnumMetals.STEEL).storage))
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.drillheadSteel)));

		ShapedRecipeBuilder.shaped(Tools.buzzsaw)
				.pattern("  g")
				.pattern("rcg")
				.pattern("r  ")
				.define('g', Ingredients.woodenGrip)
				.define('c', Ingredients.componentSteel)
				.define('r', IETags.steelRod)
				.unlockedBy("has_"+toPath(Ingredients.componentSteel), has(Ingredients.componentSteel))
				.save(out, toRL(toPath(Tools.buzzsaw)));
		ShapedRecipeBuilder.shaped(Tools.sawblade)
				.pattern("ipi")
				.pattern("p p")
				.pattern("ipi")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.sawblade)));
		ShapedRecipeBuilder.shaped(Tools.rockcutter)
				.pattern("ipi")
				.pattern("p p")
				.pattern("ipi")
				.define('i', Tags.Items.GEMS_DIAMOND)
				.define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.rockcutter)));

		ShapedRecipeBuilder.shaped(Tools.surveyTools)
				.pattern("cbh")
				.pattern("fff")
				.define('b', Items.GLASS_BOTTLE)
				.define('h', Tools.hammer)
				.define('c', Items.WRITABLE_BOOK)
				.define('f', IETags.fabricHemp)
				.unlockedBy("has_"+toPath(Tools.hammer), has(Tools.hammer))
				.save(out, toRL(toPath(Tools.surveyTools)));
	}

	private void recipesIngredients(@Nonnull Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(Ingredients.stickTreated, 4)
				.pattern("w")
				.pattern("w")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.group("sticks")
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(Ingredients.stickTreated)));
		ShapedRecipeBuilder.shaped(Ingredients.stickIron, 4)
				.pattern("i")
				.pattern("i")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.group("sticks")
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Ingredients.stickIron)));
		ShapedRecipeBuilder.shaped(Ingredients.stickSteel, 4)
				.pattern("i")
				.pattern("i")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.group("sticks")
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.stickSteel)));
		ShapedRecipeBuilder.shaped(Ingredients.stickAluminum, 4)
				.pattern("i")
				.pattern("i")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.group("sticks")
				.unlockedBy("has_alu_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.save(out, toRL(toPath(Ingredients.stickAluminum)));
		ShapedRecipeBuilder.shaped(Ingredients.hempFabric)
				.pattern("fff")
				.pattern("fsf")
				.pattern("fff")
				.define('f', IETags.fiberHemp)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_hemp_fiber", has(IETags.fiberHemp))
				.save(out, toRL(toPath(Ingredients.hempFabric)));

		ShapedRecipeBuilder.shaped(Ingredients.componentIron)
				.pattern("i i")
				.pattern(" c ")
				.pattern("i i")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Ingredients.componentIron)));
		ShapedRecipeBuilder.shaped(Ingredients.componentSteel)
				.pattern("i i")
				.pattern(" c ")
				.pattern("i i")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.componentSteel)));
		ShapedRecipeBuilder.shaped(Ingredients.waterwheelSegment)
				.pattern(" s ")
				.pattern("sbs")
				.pattern("bsb")
				.define('s', IETags.treatedStick)
				.define('b', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(Ingredients.waterwheelSegment)));
		ShapedRecipeBuilder.shaped(Ingredients.windmillBlade)
				.pattern("bb ")
				.pattern("ssb")
				.pattern("ss ")
				.define('s', IETags.treatedStick)
				.define('b', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(Ingredients.windmillBlade)));
		ShapedRecipeBuilder.shaped(Ingredients.windmillSail)
				.pattern(" cc")
				.pattern("ccc")
				.pattern(" c ")
				.define('c', IETags.fabricHemp)
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.save(out, toRL(toPath(Ingredients.windmillSail)));

		ShapedRecipeBuilder.shaped(Ingredients.woodenGrip)
				.pattern("ss")
				.pattern("cs")
				.pattern("ss")
				.define('s', IETags.treatedStick)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).nugget)
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(Ingredients.woodenGrip)));
		ShapedRecipeBuilder.shaped(Ingredients.gunpartBarrel)
				.pattern("ss")
				.define('s', IETags.steelRod)
				.unlockedBy("has_"+toPath(Ingredients.stickSteel), has(IETags.steelRod))
				.save(out, toRL(toPath(Ingredients.gunpartBarrel)));
		ShapedRecipeBuilder.shaped(Ingredients.gunpartDrum)
				.pattern(" i ")
				.pattern("ici")
				.pattern(" i ")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('c', Ingredients.componentSteel)
				.unlockedBy("has_ingot_steel", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.gunpartDrum)));
		ShapedRecipeBuilder.shaped(Ingredients.gunpartHammer)
				.pattern("i  ")
				.pattern("ii ")
				.pattern(" ii")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_ingot_steel", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.gunpartHammer)));

		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.DRILL_WATERPROOF))
				.pattern("bl ")
				.pattern("lbl")
				.pattern(" lc")
				.define('b', Items.BUCKET)
				.define('l', Tags.Items.DYES_BLUE)
				.define('c', Ingredients.componentIron)
				.unlockedBy("has_drill", has(Tools.drill))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_WATERPROOF))));
		FluidAwareShapedRecipeBuilder.builder(Misc.toolUpgrades.get(ToolUpgrade.DRILL_LUBE))
				.pattern("bi ")
				.pattern("ibi")
				.pattern(" ic")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('c', Ingredients.componentIron)
				.define('b', new IngredientFluidStack(IETags.fluidPlantoil, FluidAttributes.BUCKET_VOLUME))
				.unlockedBy("has_drill", has(Tools.drill))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_LUBE))));
		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.DRILL_DAMAGE))
				.pattern("iii")
				.pattern(" c ")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('c', Ingredients.componentIron)
				.unlockedBy("has_drill", has(Tools.drill))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_DAMAGE))));
		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY))
				.pattern("ci ")
				.pattern("ibr")
				.pattern(" rb")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('c', Ingredients.componentIron)
				.define('r', Tags.Items.DYES_RED)
				.define('b', Items.BUCKET)
				.unlockedBy("has_drill", has(Tools.drill))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY))));

		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_BAYONET))
				.pattern("si")
				.pattern("iw")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('s', Items.IRON_SWORD)
				.unlockedBy("has_revolver", has(Weapons.revolver))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_BAYONET))));
		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))
				.pattern(" ai")
				.pattern("a a")
				.pattern("ca ")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('a', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('c', Ingredients.componentIron)
				.unlockedBy("has_revolver", has(Weapons.revolver))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))));
		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_ELECTRO))
				.pattern("eee")
				.pattern("rwr")
				.define('e', Ingredients.electronTube)
				.define('r', IETags.steelRod)
				.define('w', Ingredients.wireCopper)
				.unlockedBy("has_revolver", has(Weapons.revolver))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_ELECTRO))));

		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_FOCUS))
				.pattern(" ii")
				.pattern("pph")
				.pattern(" ii")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('p', MetalDevices.fluidPipe)
				.define('h', Items.HOPPER)
				.unlockedBy("has_chemthrower", has(Weapons.chemthrower))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_FOCUS))));
		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_MULTITANK))
				.pattern(" p ")
				.pattern("tct")
				.define('p', MetalDevices.fluidPipe)
				.define('c', Ingredients.componentIron)
				.define('t', Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY))
				.unlockedBy("has_chemthrower", has(Weapons.chemthrower))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_MULTITANK))));

		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
				.pattern("pi ")
				.pattern("c i")
				.pattern(" cp")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('p', Tags.Items.GLASS_PANES)
				.unlockedBy("has_railgun", has(Weapons.railgun))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))));
		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_CAPACITORS))
				.pattern("p  ")
				.pattern("ip ")
				.pattern(" ip")
				.define('p', IETags.getTagsFor(EnumMetals.CONSTANTAN).plate)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_railgun", has(Weapons.railgun))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_CAPACITORS))));

		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_FLASH))
				.pattern("ipi")
				.pattern("pep")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.define('p', Tags.Items.GLASS_PANES)
				.define('e', Ingredients.electronTube)
				.unlockedBy("has_shield", has(Misc.shield))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_FLASH))));
		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_SHOCK))
				.pattern("crc")
				.pattern("crc")
				.pattern("crc")
				.define('r', IETags.ironRod)
				.define('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.unlockedBy("has_shield", has(Misc.shield))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_SHOCK))));
		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_MAGNET))
				.pattern("  l")
				.pattern("lc ")
				.pattern("lil")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('l', Tags.Items.LEATHER)
				.define('c', MetalDecoration.lvCoil)
				.unlockedBy("has_shield", has(Misc.shield))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_MAGNET))));

		ShapedRecipeBuilder.shaped(Misc.toolUpgrades.get(ToolUpgrade.BUZZSAW_SPAREBLADES))
				.pattern("rht")
				.pattern("rt ")
				.define('r', IETags.ironRod)
				.define('h', IETags.fiberHemp)
				.define('t', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_buzzsaw", has(Tools.buzzsaw))
				.save(out, toRL(toPath(Misc.toolUpgrades.get(ToolUpgrade.BUZZSAW_SPAREBLADES))));

		ShapelessRecipeBuilder.shapeless(Ingredients.wireCopper)
				.requires(IETags.getTagsFor(EnumMetals.COPPER).plate)
				.requires(Tools.wirecutter)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(Ingredients.wireCopper)));
		ShapelessRecipeBuilder.shapeless(Ingredients.wireElectrum)
				.requires(IETags.getTagsFor(EnumMetals.ELECTRUM).plate)
				.requires(Tools.wirecutter)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.save(out, toRL(toPath(Ingredients.wireElectrum)));
		ShapelessRecipeBuilder.shapeless(Ingredients.wireAluminum)
				.requires(IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.requires(Tools.wirecutter)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.save(out, toRL(toPath(Ingredients.wireAluminum)));
		ShapelessRecipeBuilder.shapeless(Ingredients.wireSteel)
				.requires(IETags.getTagsFor(EnumMetals.STEEL).plate)
				.requires(Tools.wirecutter)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.wireSteel)));
		ShapelessRecipeBuilder.shapeless(Ingredients.wireLead)
				.requires(IETags.getTagsFor(EnumMetals.LEAD).plate)
				.requires(Tools.wirecutter)
				.unlockedBy("has_lead_ingot", has(IETags.getTagsFor(EnumMetals.LEAD).ingot))
				.save(out, toRL(toPath(Ingredients.wireLead)));

		ShapelessRecipeBuilder.shapeless(Metals.dusts.get(EnumMetals.ELECTRUM), 2)
				.requires(IETags.getTagsFor(EnumMetals.GOLD).dust)
				.requires(IETags.getTagsFor(EnumMetals.SILVER).dust)
				.unlockedBy("has_gold_dust", has(IETags.getTagsFor(EnumMetals.GOLD).dust))
				.save(out, toRL("electrum_mix"));
		ShapelessRecipeBuilder.shapeless(Metals.dusts.get(EnumMetals.CONSTANTAN), 2)
				.requires(IETags.getTagsFor(EnumMetals.COPPER).dust)
				.requires(IETags.getTagsFor(EnumMetals.NICKEL).dust)
				.unlockedBy("has_nickel_dust", has(IETags.getTagsFor(EnumMetals.NICKEL).dust))
				.save(out, toRL("constantan_mix"));

		ShapedRecipeBuilder.shaped(Misc.blueprint)
				.pattern("jkl")
				.pattern("ddd")
				.pattern("ppp")
				.define('j', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('l', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('k', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.define('p', Items.PAPER)
				.unlockedBy("has_"+toPath(Items.PAPER), has(Items.PAPER))
				.save(buildBlueprint(out, "components"), toRL("blueprint_components"));
		ShapedRecipeBuilder.shaped(Misc.blueprint)
				.pattern(" P ")
				.pattern("ddd")
				.pattern("ppp")
				.define('P', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.define('p', Items.PAPER)
				.unlockedBy("has_"+toPath(Items.PAPER), has(Items.PAPER))
				.save(buildBlueprint(out, "molds"), toRL("blueprint_molds"));
		ShapedRecipeBuilder.shaped(Misc.blueprint)
				.pattern("gcg")
				.pattern("ddd")
				.pattern("ppp")
				.define('g', Tags.Items.GUNPOWDER)
				.define('c', Ingredients.emptyCasing)
				.define('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.define('p', Items.PAPER)
				.unlockedBy("has_"+toPath(Items.PAPER), has(Items.PAPER))
				.save(buildBlueprint(out, "bullet"), toRL("blueprint_bullets"));
		ShapedRecipeBuilder.shaped(Misc.blueprint)
				.pattern(" b ")
				.pattern("ddd")
				.pattern("ppp")
				.define('b', ItemTags.BANNERS)
				.define('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.define('p', Items.PAPER)
				.unlockedBy("has_"+toPath(Items.PAPER), has(Items.PAPER))
				.save(buildBlueprint(out, "bannerpatterns"), toRL("blueprint_bannerpatterns"));
	}

	private void recipesVanilla(@Nonnull Consumer<FinishedRecipe> out)
	{
		FluidAwareShapedRecipeBuilder.builder(Items.TORCH, 12)
				.pattern("wc ")
				.pattern("sss")
				.define('w', ItemTags.WOOL)
				.define('c', new IngredientFluidStack(IETags.fluidCreosote, FluidAttributes.BUCKET_VOLUME))
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_wool", has(ItemTags.WOOL))
				.unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
				.unlockedBy("has_creosote", has(IEFluids.fluidCreosote.getBucket()))
				.save(out, toRL(toPath(Items.TORCH)));
		ShapelessRecipeBuilder.shapeless(Items.STRING)
				.requires(Ingredient.of(IETags.fiberHemp), 3)
				.unlockedBy("has_hemp_fiber", has(Ingredients.hempFiber))
				.save(out, toRL(toPath(Items.STRING)));
		ShapelessRecipeBuilder.shapeless(Items.GUNPOWDER, 6)
				.requires(Ingredient.of(IETags.saltpeterDust), 4)
				.requires(IETags.sulfurDust)
				.requires(Items.CHARCOAL)
				.unlockedBy("has_sulfur", has(IETags.sulfurDust))
				.save(out, toRL("gunpowder_from_dusts"));
		FluidAwareShapelessRecipeBuilder.builder(Items.PAPER, 2)
				.requires(Ingredient.of(IETags.sawdust), 4)
				.requires(new IngredientFluidStack(FluidTags.WATER, FluidAttributes.BUCKET_VOLUME))
				.unlockedBy("has_sawdust", has(IETags.sawdust))
				.save(out, toRL("paper_from_sawdust"));
	}

	private Consumer<FinishedRecipe> buildBlueprint(Consumer<FinishedRecipe> out, String blueprint, ICondition... conditions)
	{
		return recipe -> {
			out.accept(new FinishedRecipe()
			{
				@Override
				public void serializeRecipeData(@Nonnull JsonObject json)
				{
					if(conditions.length > 0)
					{
						JsonArray conditionArray = new JsonArray();
						for(ICondition condition : conditions)
							conditionArray.add(CraftingHelper.serialize(condition));
						json.add("conditions", conditionArray);
					}

					recipe.serializeRecipeData(json);
					JsonObject output = json.getAsJsonObject("result");
					JsonObject nbt = new JsonObject();
					nbt.addProperty("blueprint", blueprint);
					output.add("nbt", nbt);
				}

				@Nonnull
				@Override
				public ResourceLocation getId()
				{
					return recipe.getId();
				}

				@Nonnull
				@Override
				public RecipeSerializer<?> getType()
				{
					return recipe.getType();
				}

				@Nullable
				@Override
				public JsonObject serializeAdvancement()
				{
					return recipe.serializeAdvancement();
				}

				@Nullable
				@Override
				public ResourceLocation getAdvancementId()
				{
					return recipe.getAdvancementId();
				}
			});
		};
	}

	private void recipesWeapons(@Nonnull Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(Weapons.chemthrower)
				.pattern(" tg")
				.pattern(" hg")
				.pattern("pb ")
				.define('g', Ingredients.woodenGrip)
				.define('p', MetalDevices.fluidPipe)
				.define('h', MetalDecoration.engineeringHeavy)
				.define('t', Misc.toolUpgrades.get(ToolUpgrade.DRILL_WATERPROOF))
				.define('b', Items.BUCKET)
				.unlockedBy("has_"+toPath(Ingredients.woodenGrip), has(Ingredients.woodenGrip))
				.save(out, toRL(toPath(Weapons.chemthrower)));
		ShapedRecipeBuilder.shaped(Weapons.railgun)
				.pattern(" vg")
				.pattern("icp")
				.pattern("ci ")
				.define('g', Ingredients.woodenGrip)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('v', MetalDevices.capacitorHV)
				.define('c', MetalDecoration.mvCoil)
				.define('p', Ingredients.circuitBoard)
				.unlockedBy("has_"+toPath(MetalDevices.capacitorHV), has(MetalDevices.capacitorHV))
				.save(out, toRL(toPath(Weapons.railgun)));
		ShapedRecipeBuilder.shaped(Misc.skyhook)
				.pattern("ii ")
				.pattern("ic ")
				.pattern(" gg")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('c', Ingredients.componentIron)
				.define('g', Ingredients.woodenGrip)
				.unlockedBy("has_"+toPath(Ingredients.woodenGrip), has(Ingredients.woodenGrip))
				.save(out, toRL(toPath(Misc.skyhook)));
		RevolverAssemblyRecipeBuilder.builder(Weapons.revolver)
				.setNBTCopyTargetRecipe(3, 4, 5)
				.pattern(" i ")
				.pattern("bdh")
				.pattern("gig")
				.define('b', Ingredients.gunpartBarrel)
				.define('d', Ingredients.gunpartDrum)
				.define('h', Ingredients.gunpartHammer)
				.define('g', Ingredients.woodenGrip)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.unlockedBy("has_"+toPath(Ingredients.woodenGrip), has(Ingredients.woodenGrip))
				.save(out, toRL(toPath(Weapons.revolver)));
		ShapedRecipeBuilder.shaped(Weapons.speedloader)
				.pattern("  i")
				.pattern("iis")
				.pattern("  i")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('s', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Weapons.speedloader)));

		ShapedRecipeBuilder.shaped(BulletHandler.emptyShell, 5)
				.pattern("prp")
				.pattern("prp")
				.pattern(" c ")
				.define('p', Items.PAPER)
				.define('r', Tags.Items.DYES_RED)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.unlockedBy("has_coppper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(BulletHandler.emptyShell)));

		ShapedRecipeBuilder.shaped(BulletHandler.emptyCasing, 5)
				.pattern("c c")
				.pattern("c c")
				.pattern(" c ")
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.unlockedBy("has_coppper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(BulletHandler.emptyCasing)));

		BulletHandler.getBulletStack(BulletItem.FLARE);
		TurnAndCopyRecipeBuilder.builder(BulletHandler.getBulletItem(BulletItem.FIREWORK))
				.setNBTCopyTargetRecipe(0, 1, 2, 3, 4, 5, 6) //Since this isn't relative positioning, we have to account for the top 6 slots >_>
				.pattern("f")
				.pattern("c")
				.define('f', Items.FIREWORK_ROCKET)
				.define('c', Ingredients.emptyShell)
				.unlockedBy("has_firework", has(Items.FIREWORK_ROCKET))
				.save(out, toRL(toPath(BulletHandler.getBulletItem(BulletItem.FIREWORK))));
	}

	private void recipesMisc(@Nonnull Consumer<FinishedRecipe> out)
	{
		ItemLike wireCoilCopper = Misc.wireCoils.get(WireType.COPPER);
		ShapedRecipeBuilder.shaped(wireCoilCopper, 4)
				.pattern(" w ")
				.pattern("wsw")
				.pattern(" w ")
				.define('w', IETags.copperWire)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(wireCoilCopper)));
		ItemLike wireCoilElectrum = Misc.wireCoils.get(WireType.ELECTRUM);
		ShapedRecipeBuilder.shaped(wireCoilElectrum, 4)
				.pattern(" w ")
				.pattern("wsw")
				.pattern(" w ")
				.define('w', IETags.electrumWire)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.save(out, toRL(toPath(wireCoilElectrum)));
		ItemLike wireCoilSteel = Misc.wireCoils.get(WireType.STEEL);
		TurnAndCopyRecipeBuilder.builder(wireCoilSteel, 4)
				.allowQuarterTurn()
				.pattern(" w ")
				.pattern("asa")
				.pattern(" w ")
				.define('w', IETags.steelWire)
				.define('a', IETags.aluminumWire)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(wireCoilSteel)));

		ItemLike wireCoilRope = Misc.wireCoils.get(WireType.STRUCTURE_ROPE);
		ShapedRecipeBuilder.shaped(wireCoilRope, 4)
				.pattern(" w ")
				.pattern("wsw")
				.pattern(" w ")
				.define('w', Ingredients.hempFiber)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_hemp_fiber", has(Ingredients.hempFiber))
				.save(out, toRL(toPath(wireCoilRope)));
		ItemLike wireCoilStructure = Misc.wireCoils.get(WireType.STRUCTURE_STEEL);
		ShapedRecipeBuilder.shaped(wireCoilStructure, 4)
				.pattern(" w ")
				.pattern("wsw")
				.pattern(" w ")
				.define('w', Ingredients.wireSteel)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(wireCoilStructure)));
		addCornerStraightMiddle(Misc.wireCoils.get(WireType.COPPER_INSULATED), 4,
				makeIngredient(IETags.fabricHemp),
				makeIngredient(Misc.wireCoils.get(WireType.COPPER)),
				makeIngredient(IETags.fabricHemp), out);
		addCornerStraightMiddle(Misc.wireCoils.get(WireType.ELECTRUM_INSULATED), 4,
				makeIngredient(IETags.fabricHemp),
				makeIngredient(Misc.wireCoils.get(WireType.ELECTRUM)),
				makeIngredient(IETags.fabricHemp), out);
		ItemLike wireCoilRedstone = Misc.wireCoils.get(WireType.REDSTONE);
		TurnAndCopyRecipeBuilder.builder(wireCoilRedstone, 4)
				.allowQuarterTurn()
				.pattern(" w ")
				.pattern("asa")
				.pattern(" w ")
				.define('w', IETags.aluminumWire)
				.define('a', Tags.Items.DUSTS_REDSTONE)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.save(out, toRL(toPath(wireCoilRedstone)));

		ShapedRecipeBuilder.shaped(Misc.jerrycan)
				.pattern(" ii")
				.pattern("ibb")
				.pattern("ibb")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('b', Items.BUCKET)
				.unlockedBy("has_bucket", has(Items.BUCKET))
				.save(out, toRL("jerrycan"));
		ShapedRecipeBuilder.shaped(Misc.powerpack)
				.pattern("lbl")
				.pattern("wcw")
				.define('l', Tags.Items.LEATHER)
				.define('w', Misc.wireCoils.get(WireType.COPPER))
				.define('b', MetalDevices.capacitorLV)
				.define('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.unlockedBy("has_leather", has(Items.LEATHER))
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_"+toPath(MetalDevices.capacitorLV), has(MetalDevices.capacitorLV))
				.unlockedBy("has_"+toPath(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)), has(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)))
				.save(out, toRL(toPath(Misc.powerpack)));
		ShapedRecipeBuilder.shaped(Misc.maintenanceKit)
				.pattern("sc ")
				.pattern("fff")
				.define('c', Tools.wirecutter)
				.define('s', Tools.screwdriver)
				.define('f', IETags.fabricHemp)
				.unlockedBy("has_"+toPath(Tools.wirecutter), has(Tools.wirecutter))
				.save(out, toRL(toPath(Misc.maintenanceKit)));
		ShapedRecipeBuilder.shaped(Misc.shield)
				.pattern("sws")
				.pattern("scs")
				.pattern("sws")
				.define('s', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('c', Items.SHIELD)
				.unlockedBy("has_shield", has(Items.SHIELD))
				.save(out, toRL(toPath(Misc.shield)));
		ShapedRecipeBuilder.shaped(Misc.fluorescentTube)
				.pattern("GeG")
				.pattern("GgG")
				.pattern("GgG")
				.define('g', Tags.Items.DUSTS_GLOWSTONE)
				.define('e', Misc.graphiteElectrode)
				.define('G', Tags.Items.GLASS)
				.unlockedBy("has_electrode", has(Misc.graphiteElectrode))
				.save(out, toRL(toPath(Misc.fluorescentTube)));
		addArmor(IETags.getTagsFor(EnumMetals.ALUMINUM).plate, Misc.faradaySuit, "alu_plate", out);
		ShapedRecipeBuilder.shaped(Misc.earmuffs)
				.pattern(" S ")
				.pattern("S S")
				.pattern("W W")
				.define('S', IETags.ironRod)
				.define('W', ItemTags.WOOL)
				.unlockedBy("has_iron_rod", has(IETags.ironRod))
				.save(out, toRL(toPath(Misc.earmuffs)));
		ShapedRecipeBuilder.shaped(MetalDecoration.lantern)
				.pattern(" I ")
				.pattern("PGP")
				.pattern(" I ")
				.define('I', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('G', Tags.Items.DUSTS_GLOWSTONE)
				.define('P', Items.GLASS_PANE)
				.unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
				.save(out, toRL(toPath(MetalDecoration.lantern)));

		ShapedRecipeBuilder.shaped(Minecarts.cartWoodenCrate)
				.pattern("B")
				.pattern("C")
				.define('B', WoodenDevices.crate)
				.define('C', Items.MINECART)
				.unlockedBy("has_minecart", has(Items.MINECART))
				.save(out, toRL(toPath(Minecarts.cartWoodenCrate)));
		ShapedRecipeBuilder.shaped(Minecarts.cartReinforcedCrate)
				.pattern("B")
				.pattern("C")
				.define('B', WoodenDevices.reinforcedCrate)
				.define('C', Items.MINECART)
				.unlockedBy("has_minecart", has(Items.MINECART))
				.save(out, toRL(toPath(Minecarts.cartReinforcedCrate)));
		ShapedRecipeBuilder.shaped(Minecarts.cartWoodenBarrel)
				.pattern("B")
				.pattern("C")
				.define('B', WoodenDevices.woodenBarrel)
				.define('C', Items.MINECART)
				.unlockedBy("has_minecart", has(Items.MINECART))
				.save(out, toRL(toPath(Minecarts.cartWoodenBarrel)));
		ShapedRecipeBuilder.shaped(Minecarts.cartMetalBarrel)
				.pattern("B")
				.pattern("C")
				.define('B', MetalDevices.barrel)
				.define('C', Items.MINECART)
				.unlockedBy("has_minecart", has(Items.MINECART))
				.save(out, toRL(toPath(Minecarts.cartMetalBarrel)));
	}

	private void addArmor(Named<Item> input, Map<EquipmentSlot, ? extends ItemLike> items, String name, Consumer<FinishedRecipe> out)
	{
		ItemLike head = items.get(EquipmentSlot.HEAD);
		ItemLike chest = items.get(EquipmentSlot.CHEST);
		ItemLike legs = items.get(EquipmentSlot.LEGS);
		ItemLike feet = items.get(EquipmentSlot.FEET);
		ShapedRecipeBuilder.shaped(head)
				.pattern("xxx")
				.pattern("x x")
				.define('x', input)
				.unlockedBy("has_"+name, has(input))
				.save(out, toRL(toPath(head)));
		ShapedRecipeBuilder.shaped(chest)
				.pattern("x x")
				.pattern("xxx")
				.pattern("xxx")
				.define('x', input)
				.unlockedBy("has_"+name, has(input))
				.save(out, toRL(toPath(chest)));
		ShapedRecipeBuilder.shaped(legs)
				.pattern("xxx")
				.pattern("x x")
				.pattern("x x")
				.define('x', input)
				.unlockedBy("has_"+name, has(input))
				.save(out, toRL(toPath(legs)));
		ShapedRecipeBuilder.shaped(feet)
				.pattern("x x")
				.pattern("x x")
				.define('x', input)
				.unlockedBy("has_"+name, has(input))
				.save(out, toRL(toPath(feet)));
	}

	private void add3x3Conversion(ItemLike bigItem, ItemLike smallItem, Named<Item> smallTag, Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(bigItem)
				.define('s', smallTag)
				.define('i', smallItem)
				.pattern("sss")
				.pattern("sis")
				.pattern("sss")
				.unlockedBy("has_"+toPath(smallItem), has(smallItem))
				.save(out, toRL(toPath(smallItem)+"_to_")+toPath(bigItem));
		ShapelessRecipeBuilder.shapeless(smallItem, 9)
				.requires(bigItem)
				.unlockedBy("has_"+toPath(bigItem), has(smallItem))
				.save(out, toRL(toPath(bigItem)+"_to_"+toPath(smallItem)));
	}

	private void addSlab(ItemLike block, ItemLike slab, Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(slab, 6)
				.define('s', block)
				.pattern("sss")
				.unlockedBy("has_"+toPath(block), has(block))
				.save(out, toRL(toPath(block)+"_to_slab"));
		ShapedRecipeBuilder.shaped(block)
				.define('s', slab)
				.pattern("s")
				.pattern("s")
				.unlockedBy("has_"+toPath(block), has(block))
				.save(out, toRL(toPath(block)+"_from_slab"));
	}

	private void addStairs(ItemLike block, Consumer<FinishedRecipe> out)
	{
		ItemLike stairs = IEBlocks.toStairs.get(block.asItem().getRegistryName());
		ShapedRecipeBuilder.shaped(stairs, 4)
				.define('s', block)
				.pattern("s  ")
				.pattern("ss ")
				.pattern("sss")
				.unlockedBy("has_"+toPath(block), has(block))
				.save(out, toRL(toPath(stairs)));
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
	private void addCornerStraightMiddle(ItemLike output, int count, Ingredient corner, Ingredient side, Ingredient middle, Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(output, count)
				.define('c', corner)
				.define('s', side)
				.define('m', middle)
				.pattern("csc")
				.pattern("sms")
				.pattern("csc")
				.unlockedBy("has_"+toPath(output), has(output))
				.save(out, toRL(toPath(output)));
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
	private void addSandwich(ItemLike output, int count, Ingredient top, Ingredient middle, Ingredient bottom, Consumer<FinishedRecipe> out)
	{
		ShapedRecipeBuilder.shaped(output, count)
				.define('t', top)
				.define('m', middle)
				.define('b', bottom)
				.pattern("ttt")
				.pattern("mmm")
				.pattern("bbb")
				.unlockedBy("has_"+toPath(output), has(output))
				.save(out, toRL(toPath(output)));
	}

	private String toPath(ItemLike src)
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
	private Ingredient makeIngredient(ItemLike in)
	{
		return Ingredient.of(in);
	}

	@Nonnull
	private Ingredient makeIngredient(Tag<Item> in)
	{
		return Ingredient.of(in);
	}

	@Nonnull
	private Ingredient makeIngredientFromBlock(Named<Block> in)
	{
		Tag<Item> itemTag = IETags.getItemTag(in);
		if(itemTag==null)
			//TODO this currently does not work, the tag collection is not initialized in data gen mode
			itemTag = ItemTags.getAllTags().getTag(in.getName());
		Preconditions.checkNotNull(itemTag, "Failed to convert block tag "+in.getName()+" to item tag");
		return makeIngredient(itemTag);
	}

	public static ICondition getTagCondition(Named<?> tag)
	{
		return new NotCondition(new TagEmptyCondition(tag.getName()));
	}

	public static ICondition getTagCondition(ResourceLocation tag)
	{
		return getTagCondition(createItemWrapper(tag));
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
	private void addStandardSmeltingBlastingRecipe(ItemLike input, ItemLike output, float xp, int smeltingTime, Consumer<FinishedRecipe> out, String extraPostfix, boolean smeltPostfix)
	{
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), output, xp, smeltingTime).unlockedBy("has_"+toPath(input), has(input)).save(out, toRL(toPath(output)+extraPostfix+(smeltPostfix?"_from_smelting": "")));
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), output, xp, smeltingTime/blastDivider).unlockedBy("has_"+toPath(input), has(input)).save(out, toRL(toPath(output)+extraPostfix+"_from_blasting"));
	}

	private void addStandardSmeltingBlastingRecipe(ItemLike input, ItemLike output, float xp, Consumer<FinishedRecipe> out)
	{
		addStandardSmeltingBlastingRecipe(input, output, xp, out, "");
	}

	private void addStandardSmeltingBlastingRecipe(ItemLike input, ItemLike output, float xp, Consumer<FinishedRecipe> out, String extraPostfix)
	{
		addStandardSmeltingBlastingRecipe(input, output, xp, standardSmeltingTime, out, extraPostfix, false);
	}

	private void addRGBRecipe(Consumer<FinishedRecipe> out, ResourceLocation recipeName, Ingredient target, String nbtKey)
	{
		out.accept(new FinishedRecipe()
		{

			@Override
			public void serializeRecipeData(JsonObject json)
			{
				json.add("target", target.toJson());
				json.addProperty("key", nbtKey);
			}

			@Override
			public ResourceLocation getId()
			{
				return recipeName;
			}

			@Override
			public RecipeSerializer<?> getType()
			{
				return RecipeSerializers.RGB_SERIALIZER.get();
			}

			@Nullable
			@Override
			public JsonObject serializeAdvancement()
			{
				return null;
			}

			@Nullable
			@Override
			public ResourceLocation getAdvancementId()
			{
				return null;
			}
		});
	}
}
