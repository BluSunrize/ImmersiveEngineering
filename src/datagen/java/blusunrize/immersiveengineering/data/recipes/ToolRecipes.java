/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.crafting.*;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.items.upgrades.ToolUpgrade;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEItems.*;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ToolRecipes extends IERecipeProvider
{
	public ToolRecipes(PackOutput p_248933_, CompletableFuture<Provider> provider)
	{
		super(p_248933_, provider);
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		addStandardSmeltingBlastingRecipe(Tools.STEEL_AXE, Metals.NUGGETS.get(EnumMetals.STEEL), 0.1F, out, "_recycle_axe");
		addStandardSmeltingBlastingRecipe(Tools.STEEL_PICK, Metals.NUGGETS.get(EnumMetals.STEEL), 0.1F, out, "_recycle_pick");
		addStandardSmeltingBlastingRecipe(Tools.STEEL_SHOVEL, Metals.NUGGETS.get(EnumMetals.STEEL), 0.1F, out, "_recycle_shovel");
		addStandardSmeltingBlastingRecipe(Tools.STEEL_SWORD, Metals.NUGGETS.get(EnumMetals.STEEL), 0.1F, out, "_recycle_sword");

		for(ArmorItem.Type slot : ArmorItem.Type.values())
			if(slot!=Type.BODY)
			{
				addStandardSmeltingBlastingRecipe(Tools.STEEL_ARMOR.get(slot), Metals.NUGGETS.get(EnumMetals.STEEL), 0.1F, out, "_recycle_steel_"+slot.getName());
				addStandardSmeltingBlastingRecipe(Misc.FARADAY_SUIT.get(slot), Metals.NUGGETS.get(EnumMetals.ALUMINUM), 0.1F, out, "_recycle_faraday_"+slot.getName());
			}

		SpecialRecipeBuilder.special(SpeedloaderLoadRecipe::new)
				.save(out, ImmersiveEngineering.MODID+":speedloader_load");
		SpecialRecipeBuilder.special(FlareBulletColorRecipe::new)
				.save(out, ImmersiveEngineering.MODID+":flare_bullet_color");
		SpecialRecipeBuilder.special(PotionBulletFillRecipe::new)
				.save(out, ImmersiveEngineering.MODID+":potion_bullet_fill");
		SpecialRecipeBuilder.special($ -> new PowerpackRecipe())
				.save(out, ImmersiveEngineering.MODID+":powerpack_attach");
		SpecialRecipeBuilder.special($ -> new EarmuffsRecipe())
				.save(out, ImmersiveEngineering.MODID+":earmuffs_attach");
		out.accept(toRL("earmuffs_colour"), new RGBColourationRecipe(Ingredient.of(Misc.EARMUFFS)), null);
		SpecialRecipeBuilder.special(JerrycanRefillRecipe::new)
				.save(out, ImmersiveEngineering.MODID+":jerrycan_refill");
		SpecialRecipeBuilder.special(RevolverCycleRecipe::new)
				.save(out, ImmersiveEngineering.MODID+":revolver_cycle");
		SpecialRecipeBuilder.special(IERepairItemRecipe::new)
				.save(out, ImmersiveEngineering.MODID+":ie_item_repair");
		SpecialRecipeBuilder.special(ShaderBagRecipe::new)
				.save(out, ImmersiveEngineering.MODID+":shaderbag_downgrading");


		shapedMisc(Tools.HAMMER)
				.pattern(" if")
				.pattern(" si")
				.pattern("s  ")
				.define('s', Tags.Items.RODS_WOODEN)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('f', Tags.Items.STRINGS)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Tools.HAMMER)));
		shapedMisc(Tools.WIRECUTTER)
				.pattern("si")
				.pattern(" s")
				.define('s', Tags.Items.RODS_WOODEN)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Tools.WIRECUTTER)));
		shapedMisc(Tools.SCREWDRIVER)
				.pattern(" i")
				.pattern("s ")
				.define('s', Tags.Items.RODS_WOODEN)
				.define('i', IETags.ironRod)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Tools.SCREWDRIVER)));
		shapelessMisc(Tools.MANUAL)
				.requires(Items.BOOK)
				.requires(Items.LEVER)
				.unlockedBy("has_book", has(Items.BOOK))
				.save(out, toRL(toPath(Tools.MANUAL)));
		shapedMisc(Tools.STEEL_AXE)
				.pattern("ii")
				.pattern("is")
				.pattern(" s")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.STEEL_AXE)));
		shapedMisc(Tools.STEEL_PICK)
				.pattern("iii")
				.pattern(" s ")
				.pattern(" s ")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.STEEL_PICK)));
		shapedMisc(Tools.STEEL_SHOVEL)
				.pattern("i")
				.pattern("s")
				.pattern("s")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.STEEL_SHOVEL)));
		shapedMisc(Tools.STEEL_HOE)
				.pattern("ii")
				.pattern(" s")
				.pattern(" s")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.STEEL_HOE)));
		shapedMisc(Tools.STEEL_SWORD)
				.pattern("i")
				.pattern("i")
				.pattern("s")
				.define('s', IETags.treatedStick)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.STEEL_SWORD)));

		addArmor(IETags.getTagsFor(EnumMetals.STEEL).plate, Tools.STEEL_ARMOR, "steel_plate", out);
		addArmor(IETags.getTagsFor(EnumMetals.ALUMINUM).plate, Misc.FARADAY_SUIT, "alu_plate", out);

		shapedMisc(Tools.TOOLBOX)
				.pattern("ppp")
				.pattern("rcr")
				.define('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.define('r', Tags.Items.DYES_RED)
				.define('c', IEBlocks.WoodenDevices.CRATE)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.unlockedBy("has_red_dye", has(Items.RED_DYE))
				.unlockedBy("has_"+toPath(IEBlocks.WoodenDevices.CRATE), has(IEBlocks.WoodenDevices.CRATE))
				.save(out, toRL(toPath(Tools.TOOLBOX)));
		shapedMisc(Tools.VOLTMETER)
				.pattern(" p ")
				.pattern("scs")
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('p', Items.COMPASS)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_compass", has(Items.COMPASS))
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(Tools.VOLTMETER)));

		shapedMisc(Tools.DRILL)
				.pattern("  g")
				.pattern(" hg")
				.pattern("c  ")
				.define('g', Ingredients.WOODEN_GRIP)
				.define('h', MetalDecoration.ENGINEERING_HEAVY)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_"+toPath(MetalDecoration.ENGINEERING_HEAVY), has(MetalDecoration.ENGINEERING_HEAVY))
				.save(out, toRL(toPath(Tools.DRILL)));
		shapedMisc(Tools.DRILLHEAD_IRON)
				.pattern("  i")
				.pattern("ii ")
				.pattern("bi ")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('b', Tags.Items.STORAGE_BLOCKS_IRON)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Tools.DRILLHEAD_IRON)));
		shapedMisc(Tools.DRILLHEAD_STEEL)
				.pattern("  i")
				.pattern("ii ")
				.pattern("bi ")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('b', makeIngredientFromBlock(IETags.getTagsFor(EnumMetals.STEEL).storage))
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.DRILLHEAD_STEEL)));

		shapedMisc(Tools.BUZZSAW)
				.pattern("  g")
				.pattern("rhg")
				.pattern("r  ")
				.define('g', Ingredients.WOODEN_GRIP)
				.define('h', MetalDecoration.ENGINEERING_HEAVY)
				.define('r', IETags.steelRod)
				.unlockedBy("has_"+toPath(Ingredients.COMPONENT_STEEL), has(Ingredients.COMPONENT_STEEL))
				.save(out, toRL(toPath(Tools.BUZZSAW)));
		shapedMisc(Tools.SAWBLADE)
				.pattern("ipi")
				.pattern("p p")
				.pattern("ipi")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.SAWBLADE)));
		shapedMisc(Tools.ROCKCUTTER)
				.pattern("ipi")
				.pattern("p p")
				.pattern("ipi")
				.define('i', Tags.Items.GEMS_DIAMOND)
				.define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Tools.ROCKCUTTER)));

		shapedMisc(Tools.SURVEY_TOOLS)
				.pattern("cbh")
				.pattern("fff")
				.define('b', Items.GLASS_BOTTLE)
				.define('h', Tools.HAMMER)
				.define('c', Items.WRITABLE_BOOK)
				.define('f', IETags.fabricHemp)
				.unlockedBy("has_"+toPath(Tools.HAMMER), has(Tools.HAMMER))
				.save(
						new WrappingRecipeOutput<CraftingRecipe>(out, NoContainersRecipe::new),
						toRL(toPath(Tools.SURVEY_TOOLS))
				);

		shapedMisc(Tools.GLIDER)
				.pattern(" f ")
				.pattern("rcr")
				.pattern("frf")
				.define('f', IETags.fabricHemp)
				.define('r', IETags.aluminumRod)
				.define('c', Items.LEATHER_CHESTPLATE)
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.save(out, toRL(toPath(Tools.GLIDER)));

		weapons(out);
	}

	private void addArmor(TagKey<Item> input, Map<Type, ? extends ItemLike> items, String name, RecipeOutput out)
	{
		ItemLike head = items.get(ArmorItem.Type.HELMET);
		ItemLike chest = items.get(ArmorItem.Type.CHESTPLATE);
		ItemLike legs = items.get(ArmorItem.Type.LEGGINGS);
		ItemLike feet = items.get(ArmorItem.Type.BOOTS);
		shapedMisc(head)
				.pattern("xxx")
				.pattern("x x")
				.define('x', input)
				.unlockedBy("has_"+name, has(input))
				.save(out, toRL(toPath(head)));
		shapedMisc(chest)
				.pattern("x x")
				.pattern("xxx")
				.pattern("xxx")
				.define('x', input)
				.unlockedBy("has_"+name, has(input))
				.save(out, toRL(toPath(chest)));
		shapedMisc(legs)
				.pattern("xxx")
				.pattern("x x")
				.pattern("x x")
				.define('x', input)
				.unlockedBy("has_"+name, has(input))
				.save(out, toRL(toPath(legs)));
		shapedMisc(feet)
				.pattern("x x")
				.pattern("x x")
				.define('x', input)
				.unlockedBy("has_"+name, has(input))
				.save(out, toRL(toPath(feet)));
	}

	private void weapons(@Nonnull RecipeOutput out)
	{
		shapedMisc(Weapons.CHEMTHROWER)
				.pattern(" tg")
				.pattern(" hg")
				.pattern("pb ")
				.define('g', Ingredients.WOODEN_GRIP)
				.define('p', MetalDevices.FLUID_PIPE)
				.define('h', MetalDecoration.ENGINEERING_HEAVY)
				.define('t', Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_WATERPROOF))
				.define('b', Items.BUCKET)
				.unlockedBy("has_"+toPath(Ingredients.WOODEN_GRIP), has(Ingredients.WOODEN_GRIP))
				.save(out, toRL(toPath(Weapons.CHEMTHROWER)));
		shapedMisc(Weapons.RAILGUN)
				.pattern(" vg")
				.pattern("icp")
				.pattern("ci ")
				.define('g', Ingredients.WOODEN_GRIP)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('v', MetalDevices.CAPACITOR_HV)
				.define('c', MetalDecoration.MV_COIL)
				.define('p', Ingredients.COMPONENT_ELECTRONIC_ADV)
				.unlockedBy("has_"+toPath(MetalDevices.CAPACITOR_HV), has(MetalDevices.CAPACITOR_HV))
				.save(out, toRL(toPath(Weapons.RAILGUN)));
		shapedMisc(Misc.SKYHOOK)
				.pattern("ii ")
				.pattern("ic ")
				.pattern(" gg")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('c', Ingredients.COMPONENT_IRON)
				.define('g', Ingredients.WOODEN_GRIP)
				.unlockedBy("has_"+toPath(Ingredients.WOODEN_GRIP), has(Ingredients.WOODEN_GRIP))
				.save(out, toRL(toPath(Misc.SKYHOOK)));
		shapedMisc(Weapons.REVOLVER)
				.pattern(" hg")
				.pattern(" dc")
				.pattern("b  ")
				.define('b', Ingredients.GUNPART_BARREL)
				.define('d', Ingredients.GUNPART_DRUM)
				.define('h', Ingredients.GUNPART_HAMMER)
				.define('g', Ingredients.WOODEN_GRIP)
				.define('c', Ingredients.COMPONENT_STEEL)
				.unlockedBy("has_"+toPath(Ingredients.WOODEN_GRIP), has(Ingredients.WOODEN_GRIP))
				.save(new WrappingRecipeOutput<ShapedRecipe>(
						out, r -> new RevolverAssemblyRecipe(r, List.of(1, 4, 6))
				), toRL(toPath(Weapons.REVOLVER)));
		shapedMisc(Weapons.SPEEDLOADER)
				.pattern("sd")
				.pattern("dc")
				.define('s', IETags.ironRod)
				.define('d', IETags.plasticPlate)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_revolver", has(Weapons.REVOLVER))
				.save(out, toRL(toPath(Weapons.SPEEDLOADER)));

		shapedMisc(BulletHandler.emptyShell, 3)
				.pattern("prp")
				.pattern("prp")
				.pattern(" c ")
				.define('p', IETags.paper)
				.define('r', Tags.Items.DYES_RED)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.unlockedBy("has_coppper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(BulletHandler.emptyShell)));

		shapedMisc(BulletHandler.emptyCasing, 5)
				.pattern("c c")
				.pattern("c c")
				.pattern(" c ")
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.unlockedBy("has_coppper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(BulletHandler.emptyCasing)));

		shapedMisc(BulletHandler.getBulletItem(IEBullets.FIREWORK))
				.pattern("f")
				.pattern("c")
				.define('f', Items.FIREWORK_ROCKET)
				.define('c', Ingredients.EMPTY_SHELL)
				.unlockedBy("has_firework", has(Items.FIREWORK_ROCKET))
				.save(
						new WrappingRecipeOutput<ShapedRecipe>(
								out, r -> new TurnAndCopyRecipe(r, List.of(0))
						),
						toRL(toPath(BulletHandler.getBulletItem(IEBullets.FIREWORK)))
				);
	}

	@Nonnull
	private Ingredient makeIngredientFromBlock(TagKey<Block> in)
	{
		return Ingredient.of(IETags.getItemTag(in));
	}
}
