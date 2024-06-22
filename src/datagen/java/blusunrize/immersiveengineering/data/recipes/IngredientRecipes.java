/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.crafting.NoContainersRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.BasicShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems.*;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.concurrent.CompletableFuture;

public class IngredientRecipes extends IERecipeProvider
{
	public IngredientRecipes(PackOutput p_248933_, CompletableFuture<Provider> provider)
	{
		super(p_248933_, provider);
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		shapedMisc(Ingredients.STICK_TREATED, 4)
				.pattern("w")
				.pattern("w")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.group("sticks")
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(Ingredients.STICK_TREATED)));
		shapedMisc(Ingredients.STICK_IRON, 4)
				.pattern("i")
				.pattern("i")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.group("sticks")
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Ingredients.STICK_IRON)));
		shapedMisc(Ingredients.STICK_STEEL, 4)
				.pattern("i")
				.pattern("i")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.group("sticks")
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.STICK_STEEL)));
		shapedMisc(Ingredients.STICK_ALUMINUM, 4)
				.pattern("i")
				.pattern("i")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.group("sticks")
				.unlockedBy("has_alu_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.save(out, toRL(toPath(Ingredients.STICK_ALUMINUM)));
		shapedMisc(Ingredients.HEMP_FABRIC)
				.pattern("fff")
				.pattern("fsf")
				.pattern("fff")
				.define('f', IETags.fiberHemp)
				.define('s', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_hemp_fiber", has(IETags.fiberHemp))
				.save(out, toRL(toPath(Ingredients.HEMP_FABRIC)));
		shapedMisc(Ingredients.ERSATZ_LEATHER, 8)
				.pattern("fff")
				.pattern("fcf")
				.pattern("fff")
				.define('f', IETags.fabricHemp)
				.define('c', Items.HONEYCOMB)
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.save(out, toRL(toPath(Ingredients.ERSATZ_LEATHER)));

		shapedMisc(Ingredients.COMPONENT_IRON)
				.pattern("i i")
				.pattern(" c ")
				.pattern("i i")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL(toPath(Ingredients.COMPONENT_IRON)));
		shapedMisc(Ingredients.COMPONENT_STEEL)
				.pattern("i i")
				.pattern(" c ")
				.pattern("i i")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.COMPONENT_STEEL)));
		shapedMisc(Ingredients.WATERWHEEL_SEGMENT)
				.pattern(" s ")
				.pattern("sbs")
				.pattern("bsb")
				.define('s', IETags.treatedStick)
				.define('b', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(Ingredients.WATERWHEEL_SEGMENT)));
		shapedMisc(Ingredients.WINDMILL_BLADE)
				.pattern("bb ")
				.pattern("ssb")
				.pattern("ss ")
				.define('s', IETags.treatedStick)
				.define('b', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(Ingredients.WINDMILL_BLADE)));
		shapedMisc(Ingredients.WINDMILL_SAIL)
				.pattern(" cc")
				.pattern("ccc")
				.pattern(" c ")
				.define('c', IETags.fabricHemp)
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.save(out, toRL(toPath(Ingredients.WINDMILL_SAIL)));

		shapedMisc(Ingredients.WOODEN_GRIP)
				.pattern("ss")
				.pattern("cs")
				.pattern("ss")
				.define('s', IETags.treatedStick)
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).nugget)
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(Ingredients.WOODEN_GRIP)));
		shapedMisc(Ingredients.GUNPART_BARREL)
				.pattern("  h")
				.pattern(" s ")
				.pattern("i  ")
				.define('h', IETags.hammers)
				.define('s', IETags.steelRod)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_"+toPath(Ingredients.STICK_STEEL), has(IETags.steelRod))
				.save(out, toRL(toPath(Ingredients.GUNPART_BARREL)));
		shapedMisc(Ingredients.GUNPART_DRUM)
				.pattern(" i ")
				.pattern("isi")
				.pattern(" i ")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('s', IETags.steelRod)
				.unlockedBy("has_ingot_steel", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.GUNPART_DRUM)));
		shapedMisc(Ingredients.GUNPART_HAMMER)
				.pattern("iif")
				.pattern(" s ")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.define('f', Items.FLINT)
				.unlockedBy("has_ingot_steel", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.GUNPART_HAMMER)));

		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_WATERPROOF))
				.pattern("di ")
				.pattern("idi")
				.pattern(" ip")
				.define('d', Tags.Items.DYES_BLUE)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('p', MetalDevices.FLUID_PIPE)
				.unlockedBy("has_drill", has(Tools.DRILL))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_WATERPROOF))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_LUBE))
				.pattern(" i ")
				.pattern("ioi")
				.pattern(" ip")
				.define('o', new Ingredient(new IngredientFluidStack(IETags.fluidPlantoil, FluidType.BUCKET_VOLUME)))
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('p', MetalDevices.FLUID_PIPE)
				.unlockedBy("has_drill", has(Tools.DRILL))
				.save(
						new WrappingRecipeOutput<>(out, BasicShapedRecipe::new),
						toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_LUBE)))
				);
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_DAMAGE))
				.pattern(" i")
				.pattern("ic")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_drill", has(Tools.DRILL))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_DAMAGE))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_FORTUNE))
				.pattern("ai ")
				.pattern("iai")
				.pattern("ppc")
				.define('a', new Ingredient(new IngredientFluidStack(IETags.fluidRedstoneAcid, FluidType.BUCKET_VOLUME)))
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('p', MetalDevices.FLUID_PIPE)
				.define('c', Ingredients.COMPONENT_STEEL)
				.unlockedBy("has_drill", has(Tools.DRILL))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_FORTUNE))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_CAPACITY))
				.pattern("pi ")
				.pattern("idi")
				.pattern(" id")
				.define('d', Tags.Items.DYES_RED)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('p', MetalDevices.FLUID_PIPE)
				.unlockedBy("has_drill", has(Tools.DRILL))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_CAPACITY))));

		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_BAYONET))
				.pattern("ws")
				.pattern("iw")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('w', IETags.copperWire)
				.define('s', Tools.STEEL_SWORD)
				.unlockedBy("has_revolver", has(Weapons.REVOLVER))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_BAYONET))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_MAGAZINE))
				.pattern(" wi")
				.pattern("wiw")
				.pattern("cw ")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('w', IETags.copperWire)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_revolver", has(Weapons.REVOLVER))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_MAGAZINE))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_ELECTRO))
				.pattern("eee")
				.pattern("rwr")
				.define('e', Ingredients.ELECTRON_TUBE)
				.define('r', IETags.steelRod)
				.define('w', Ingredients.WIRE_COPPER)
				.unlockedBy("has_revolver", has(Weapons.REVOLVER))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_ELECTRO))));

		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.CHEMTHROWER_FOCUS))
				.pattern("p  ")
				.pattern(" pi")
				.pattern(" ic")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('p', MetalDevices.FLUID_PIPE)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_chemthrower", has(Weapons.CHEMTHROWER))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.CHEMTHROWER_FOCUS))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.CHEMTHROWER_MULTITANK))
				.pattern(" p ")
				.pattern("tct")
				.define('p', MetalDevices.FLUID_PIPE)
				.define('c', Ingredients.COMPONENT_IRON)
				.define('t', Misc.TOOL_UPGRADES.get(ToolUpgrade.DRILL_CAPACITY))
				.unlockedBy("has_chemthrower", has(Weapons.CHEMTHROWER))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.CHEMTHROWER_MULTITANK))));

		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.RAILGUN_SCOPE))
				.pattern("  p")
				.pattern(" si")
				.pattern("pi ")
				.define('i', IETags.steelRod)
				.define('p', Tags.Items.GLASS_PANES)
				.define('s', Items.SPYGLASS)
				.unlockedBy("has_railgun", has(Weapons.RAILGUN))
				.unlockedBy("has_revolver", has(Weapons.REVOLVER))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.RAILGUN_SCOPE))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.RAILGUN_CAPACITORS))
				.pattern("p  ")
				.pattern("ip ")
				.pattern(" ip")
				.define('p', IETags.getTagsFor(EnumMetals.CONSTANTAN).plate)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_railgun", has(Weapons.RAILGUN))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.RAILGUN_CAPACITORS))));

		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.SHIELD_FLASH))
				.pattern(" p ")
				.pattern("pep")
				.define('p', IETags.getTagsFor(EnumMetals.SILVER).plate)
				.define('e', Ingredients.LIGHT_BULB)
				.unlockedBy("has_shield", has(Misc.SHIELD))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.SHIELD_FLASH))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.SHIELD_SHOCK))
				.pattern("crc")
				.pattern("crc")
				.pattern("crc")
				.define('r', IETags.ironRod)
				.define('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.unlockedBy("has_shield", has(Misc.SHIELD))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.SHIELD_SHOCK))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.SHIELD_MAGNET))
				.pattern("  l")
				.pattern("lc ")
				.pattern("lil")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('l', Tags.Items.LEATHERS)
				.define('c', MetalDecoration.LV_COIL)
				.unlockedBy("has_shield", has(Misc.SHIELD))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.SHIELD_MAGNET))));

		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.BUZZSAW_SPAREBLADES))
				.pattern("rht")
				.pattern("rt ")
				.define('r', IETags.ironRod)
				.define('h', IETags.fiberHemp)
				.define('t', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_buzzsaw", has(Tools.BUZZSAW))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.BUZZSAW_SPAREBLADES))));

		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.POWERPACK_ANTENNA))
				.pattern("www")
				.pattern("rww")
				.pattern("c  ")
				.define('w', IETags.aluminumWire)
				.define('r', IETags.aluminumRod)
				.define('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.unlockedBy("has_powerpack", has(Misc.POWERPACK))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.POWERPACK_ANTENNA))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.POWERPACK_INDUCTION))
				.pattern("ttt")
				.pattern("wew")
				.define('t', Ingredients.ELECTRON_TUBE)
				.define('w', Misc.WIRE_COILS.get(WireType.ELECTRUM_INSULATED))
				.define('e', Ingredients.COMPONENT_ELECTRONIC)
				.unlockedBy("has_powerpack", has(Misc.POWERPACK))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.POWERPACK_INDUCTION))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.POWERPACK_TESLA))
				.pattern("iii")
				.pattern("wfw")
				.pattern("wew")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.define('f', WoodenDecoration.TREATED_FENCE)
				.define('w', Misc.WIRE_COILS.get(WireType.ELECTRUM))
				.define('e', Ingredients.COMPONENT_ELECTRONIC_ADV)
				.unlockedBy("has_powerpack", has(Misc.POWERPACK))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.POWERPACK_TESLA))));
		shapedMisc(Misc.TOOL_UPGRADES.get(ToolUpgrade.POWERPACK_MAGNET))
				.pattern("rer")
				.pattern("wiw")
				.pattern(" w ")
				.define('r', IETags.steelRod)
				.define('w', Misc.WIRE_COILS.get(WireType.COPPER))
				.define('e', Ingredients.COMPONENT_ELECTRONIC_ADV)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.unlockedBy("has_powerpack", has(Misc.POWERPACK))
				.save(out, toRL(toPath(Misc.TOOL_UPGRADES.get(ToolUpgrade.POWERPACK_MAGNET))));

		shapelessMisc(Ingredients.WIRE_COPPER)
				.requires(IETags.getTagsFor(EnumMetals.COPPER).plate)
				.requires(Tools.WIRECUTTER)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL(toPath(Ingredients.WIRE_COPPER)));
		shapelessMisc(Ingredients.WIRE_ELECTRUM)
				.requires(IETags.getTagsFor(EnumMetals.ELECTRUM).plate)
				.requires(Tools.WIRECUTTER)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.save(out, toRL(toPath(Ingredients.WIRE_ELECTRUM)));
		shapelessMisc(Ingredients.WIRE_ALUMINUM)
				.requires(IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.requires(Tools.WIRECUTTER)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.save(out, toRL(toPath(Ingredients.WIRE_ALUMINUM)));
		shapelessMisc(Ingredients.WIRE_STEEL)
				.requires(IETags.getTagsFor(EnumMetals.STEEL).plate)
				.requires(Tools.WIRECUTTER)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Ingredients.WIRE_STEEL)));
		shapelessMisc(Ingredients.WIRE_LEAD)
				.requires(IETags.getTagsFor(EnumMetals.LEAD).plate)
				.requires(Tools.WIRECUTTER)
				.unlockedBy("has_lead_ingot", has(IETags.getTagsFor(EnumMetals.LEAD).ingot))
				.save(out, toRL(toPath(Ingredients.WIRE_LEAD)));

		shapelessMisc(Metals.DUSTS.get(EnumMetals.ELECTRUM), 2)
				.requires(IETags.getTagsFor(EnumMetals.GOLD).dust)
				.requires(IETags.getTagsFor(EnumMetals.SILVER).dust)
				.unlockedBy("has_gold_dust", has(IETags.getTagsFor(EnumMetals.GOLD).dust))
				.save(out, toRL("electrum_mix"));
		shapelessMisc(Metals.DUSTS.get(EnumMetals.CONSTANTAN), 2)
				.requires(IETags.getTagsFor(EnumMetals.COPPER).dust)
				.requires(IETags.getTagsFor(EnumMetals.NICKEL).dust)
				.unlockedBy("has_nickel_dust", has(IETags.getTagsFor(EnumMetals.NICKEL).dust))
				.save(out, toRL("constantan_mix"));

		shapelessMisc(IEFluids.REDSTONE_ACID.getBucket())
				.requires(Tags.Items.DUSTS_REDSTONE)
				.requires(Tags.Items.DUSTS_REDSTONE)
				.requires(Tags.Items.DUSTS_REDSTONE)
				.requires(Tags.Items.DUSTS_REDSTONE)
				.requires(Items.WATER_BUCKET)
				.unlockedBy("has_redstone_dust", has(Tags.Items.DUSTS_REDSTONE))
				.save(new WrappingRecipeOutput<CraftingRecipe>(out, NoContainersRecipe::new), toRL("redstone_acid"));

		shapedMisc(Misc.BLUEPRINT)
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
		shapedMisc(Misc.BLUEPRINT)
				.pattern(" P ")
				.pattern("ddd")
				.pattern("ppp")
				.define('P', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.define('p', Items.PAPER)
				.unlockedBy("has_"+toPath(Items.PAPER), has(Items.PAPER))
				.save(buildBlueprint(out, "molds"), toRL("blueprint_molds"));
		shapedMisc(Misc.BLUEPRINT)
				.pattern("gcg")
				.pattern("ddd")
				.pattern("ppp")
				.define('g', Tags.Items.GUNPOWDERS)
				.define('c', Ingredients.EMPTY_CASING)
				.define('d', Tags.Items.DYES_BLUE)
				//TODO tag?
				.define('p', Items.PAPER)
				.unlockedBy("has_"+toPath(Items.PAPER), has(Items.PAPER))
				.save(buildBlueprint(out, "bullet"), toRL("blueprint_bullets"));
		shapedMisc(Misc.BLUEPRINT)
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

	private RecipeOutput buildBlueprint(RecipeOutput out, String blueprint)
	{
		ItemStack blueprintItem = new ItemStack(Misc.BLUEPRINT);
		blueprintItem.set(IEApiDataComponents.BLUEPRINT_TYPE, blueprint);
		return WrappingRecipeOutput.replaceShapedOutput(out, blueprintItem);
	}
}
