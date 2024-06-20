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
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.ChuteBlock;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map.Entry;

public class DeviceRecipes extends IERecipeProvider
{
	public DeviceRecipes(PackOutput p_248933_)
	{
		super(p_248933_);
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		woodenDevices(out);
		metalDevices(out);
		connectors(out);
		conveyors(out);
		cloth(out);
	}

	private void woodenDevices(RecipeOutput out)
	{
		shapedMisc(WoodenDevices.CRAFTING_TABLE)
				.pattern("sss")
				.pattern("rcr")
				.pattern("r r")
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.define('r', IETags.treatedStick)
				.define('c', Blocks.CRAFTING_TABLE)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.CRAFTING_TABLE)));
		shapedMisc(WoodenDevices.CRATE)
				.pattern("ppp")
				.pattern("p p")
				.pattern("ppp")
				.define('p', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.CRATE)));
		shapedMisc(WoodenDevices.REINFORCED_CRATE)
				.pattern("wpw")
				.pattern("rcr")
				.pattern("wpw")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('p', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('r', IETags.ironRod)
				.define('c', IEBlocks.WoodenDevices.CRATE)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(new WrappingRecipeOutput<ShapedRecipe>(
						out, r -> new TurnAndCopyRecipe(r, List.of(4)).allowQuarterTurn()
				), toRL(toPath(WoodenDevices.REINFORCED_CRATE)));

		shapedMisc(WoodenDevices.TREATED_WALLMOUNT, 4)
				.pattern("ww")
				.pattern("ws")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.treatedStick)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.TREATED_WALLMOUNT)));

		shapedMisc(WoodenDevices.SORTER)
				.pattern("wrw")
				.pattern("ici")
				.pattern("wbw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('b', ConveyorHandler.getBlock(BasicConveyor.TYPE))
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_"+toPath(ConveyorHandler.getBlock(BasicConveyor.TYPE)), has(ConveyorHandler.getBlock(BasicConveyor.TYPE)))
				.save(out, toRL(toPath(WoodenDevices.SORTER)));
		shapedMisc(WoodenDevices.ITEM_BATCHER)
				.pattern("wrw")
				.pattern("ici")
				.pattern("wpw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('p', Ingredients.COMPONENT_ELECTRONIC)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_"+toPath(ConveyorHandler.getBlock(BasicConveyor.TYPE)), has(ConveyorHandler.getBlock(BasicConveyor.TYPE)))
				.save(out, toRL(toPath(WoodenDevices.ITEM_BATCHER)));
		shapedMisc(WoodenDevices.FLUID_SORTER)
				.pattern("wrw")
				.pattern("ici")
				.pattern("wbw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('b', MetalDevices.FLUID_PIPE)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_"+toPath(MetalDevices.FLUID_PIPE), has(MetalDevices.FLUID_PIPE))
				.save(out, toRL(toPath(WoodenDevices.FLUID_SORTER)));
		shapedMisc(WoodenDevices.LOGIC_UNIT)
				.pattern("wtw")
				.pattern("tct")
				.pattern("wtw")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('t', Ingredients.ELECTRON_TUBE)
				.define('c', Ingredients.CIRCUIT_BOARD)
				.unlockedBy("has_"+toPath(Ingredients.CIRCUIT_BOARD), has(Ingredients.CIRCUIT_BOARD))
				.save(out, toRL(toPath(WoodenDevices.LOGIC_UNIT)));
		shapedMisc(WoodenDevices.MACHINE_INTERFACE)
				.pattern("aea")
				.pattern("wcw")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('a', IETags.aluminumWire)
				.define('c', Ingredients.CIRCUIT_BOARD)
				.define('e', Ingredients.COMPONENT_ELECTRONIC_ADV)
				.unlockedBy("has_"+toPath(Ingredients.CIRCUIT_BOARD), has(Ingredients.CIRCUIT_BOARD))
				.save(out, toRL(toPath(WoodenDevices.MACHINE_INTERFACE)));

		shapedMisc(WoodenDevices.TURNTABLE)
				.pattern("iwi")
				.pattern("rcr")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('c', MetalDecoration.LV_COIL)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_"+toPath(MetalDecoration.LV_COIL), has(MetalDecoration.LV_COIL))
				.save(out, toRL(toPath(WoodenDevices.TURNTABLE)));

		shapedMisc(WoodenDevices.WINDMILL)
				.pattern("ppp")
				.pattern("pip")
				.pattern("ppp")
				.define('p', Ingredients.WINDMILL_BLADE)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.unlockedBy("has_"+toPath(Ingredients.WINDMILL_BLADE), has(Ingredients.WINDMILL_BLADE))
				.save(out, toRL(toPath(WoodenDevices.WINDMILL)));
		shapedMisc(WoodenDevices.WATERMILL)
				.pattern(" p ")
				.pattern("pip")
				.pattern(" p ")
				.define('p', Ingredients.WATERWHEEL_SEGMENT)
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_"+toPath(Ingredients.WATERWHEEL_SEGMENT), has(Ingredients.WATERWHEEL_SEGMENT))
				.save(out, toRL(toPath(WoodenDevices.WATERMILL)));

		shapedMisc(WoodenDevices.GUNPOWDER_BARREL)
				.pattern("gfg")
				.pattern("gbg")
				.define('f', Ingredients.HEMP_FIBER)
				.define('g', Tags.Items.GUNPOWDER)
				.define('b', WoodenDevices.WOODEN_BARREL)
				.unlockedBy("has_"+toPath(WoodenDevices.WOODEN_BARREL), has(WoodenDevices.WOODEN_BARREL))
				.save(out, toRL(toPath(WoodenDevices.GUNPOWDER_BARREL)));

		shapedMisc(WoodenDevices.WORKBENCH)
				.pattern("iss")
				.pattern("c f")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.define('c', WoodenDevices.CRAFTING_TABLE)
				.define('f', WoodenDecoration.TREATED_FENCE)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.WORKBENCH)));
		shapedMisc(WoodenDevices.BLUEPRINT_SHELF)
				.pattern("srs")
				.pattern("rsr")
				.pattern("www")
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.define('r', IETags.treatedStick)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.BLUEPRINT_SHELF)));
		shapedMisc(WoodenDevices.CIRCUIT_TABLE)
				.pattern("sst")
				.pattern("c e")
				.define('t', Tools.SCREWDRIVER)
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.define('c', WoodenDevices.CRAFTING_TABLE)
				.define('e', MetalDecoration.ENGINEERING_LIGHT)
				.unlockedBy("has_"+toPath(Ingredients.CIRCUIT_BOARD), has(Ingredients.CIRCUIT_BOARD))
				.save(out, toRL(toPath(WoodenDevices.CIRCUIT_TABLE)));

		shapedMisc(WoodenDevices.WOODEN_BARREL)
				.pattern("sss")
				.pattern("w w")
				.pattern("www")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(WoodenDevices.WOODEN_BARREL)));
	}

	private void connectors(@Nonnull RecipeOutput out)
	{
		shapedMisc(IEBlocks.Connectors.BREAKER_SWITCH)
				.pattern(" l ")
				.pattern("cic")
				.define('l', Items.LEVER)
				.define('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('c', IETags.connectorInsulator)
				.unlockedBy("has_"+toPath(Misc.WIRE_COILS.get(WireType.COPPER)), has(Misc.WIRE_COILS.get(WireType.COPPER)))
				.save(out, toRL(toPath(Connectors.BREAKER_SWITCH)));
		shapedMisc(IEBlocks.Connectors.REDSTONE_BREAKER)
				.pattern("hrh")
				.pattern("ici")
				.define('h', IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false))
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('r', Items.REPEATER)
				.define('c', Ingredients.COMPONENT_ELECTRONIC)
				.unlockedBy("has_hv_connector", has(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)))
				.save(out, toRL(toPath(Connectors.REDSTONE_BREAKER)));

		shapedMisc(IEBlocks.Connectors.CURRENT_TRANSFORMER)
				.pattern(" m ")
				.pattern(" b ")
				.pattern("iei")
				.define('m', IEItems.Tools.VOLTMETER)
				.define('b', IETags.connectorInsulator)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('e', Ingredients.COMPONENT_ELECTRONIC)
				.unlockedBy("has_voltmeter", has(IEItems.Tools.VOLTMETER))
				.save(out, toRL(toPath(Connectors.CURRENT_TRANSFORMER)));

		shapedMisc(IEBlocks.Connectors.TRANSFORMER)
				.pattern("lm")
				.pattern("eb")
				.pattern("ii")
				.define('l', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
				.define('m', IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false))
				.define('e', Ingredients.COMPONENT_ELECTRONIC)
				.define('b', MetalDecoration.MV_COIL)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.unlockedBy("has_mv_connector", has(IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false)))
				.save(out, toRL(toPath(Connectors.TRANSFORMER)));
		shapedMisc(IEBlocks.Connectors.TRANSFORMER_HV)
				.pattern("mh")
				.pattern("eb")
				.pattern("ii")
				.define('m', IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false))
				.define('h', IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false))
				.define('e', Ingredients.COMPONENT_ELECTRONIC)
				.define('b', MetalDecoration.HV_COIL)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.unlockedBy("has_hv_connector", has(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)))
				.save(out, toRL(toPath(Connectors.TRANSFORMER_HV)));

		shapedMisc(IEBlocks.Connectors.CONNECTOR_STRUCTURAL, 8)
				.pattern("isi")
				.pattern("i i")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(Connectors.CONNECTOR_STRUCTURAL)));

		shapedMisc(IEBlocks.Connectors.CONNECTOR_REDSTONE, 4)
				.pattern("iii")
				.pattern("brb")
				.define('i', IETags.getTagsFor(EnumMetals.ELECTRUM).nugget)
				.define('b', IETags.connectorInsulator)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_electrum_nugget", has(IETags.getTagsFor(EnumMetals.ELECTRUM).nugget))
				.save(out, toRL(toPath(Connectors.CONNECTOR_REDSTONE)));
		shapedMisc(IEBlocks.Connectors.CONNECTOR_PROBE)
				.pattern(" c ")
				.pattern("gpg")
				.define('c', Connectors.CONNECTOR_REDSTONE)
				.define('g', Tags.Items.GLASS_PANES)
				.define('p', Ingredients.COMPONENT_ELECTRONIC)
				.unlockedBy("has_connector", has(Connectors.CONNECTOR_REDSTONE))
				.save(out, toRL(toPath(Connectors.CONNECTOR_PROBE)));
		shapedMisc(IEBlocks.Connectors.CONNECTOR_BUNDLED)
				.pattern(" w ")
				.pattern("wcw")
				.pattern(" w ")
				.define('c', Connectors.CONNECTOR_REDSTONE)
				.define('w', IETags.aluminumWire)
				.unlockedBy("has_connector", has(Connectors.CONNECTOR_REDSTONE))
				.save(out, toRL(toPath(Connectors.CONNECTOR_BUNDLED)));

		// Connectors and Relays
		shapedMisc(Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), 4)
				.pattern(" i ")
				.pattern("cic")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('c', IETags.connectorInsulator)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL("connector_lv"));
		shapedMisc(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), 8)
				.pattern(" i ")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('c', IETags.connectorInsulator)
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.save(out, toRL("connector_lv_relay"));
		shapedMisc(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), 4)
				.pattern(" i ")
				.pattern("cic")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.define('c', IETags.connectorInsulator)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.save(out, toRL("connector_mv"));
		shapedMisc(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), 8)
				.pattern(" i ")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.define('c', IETags.connectorInsulator)
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.save(out, toRL("connector_mv_relay"));
		shapedMisc(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), 4)
				.pattern(" i ")
				.pattern("cic")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('c', IETags.connectorInsulator)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL("connector_hv"));
		shapedMisc(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), 8)
				.pattern(" i ")
				.pattern("cic")
				.pattern("cic")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('c', Ingredient.of(StoneDecoration.INSULATING_GLASS, StoneDecoration.SLAG_GLASS))
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.save(out, toRL("connector_hv_relay"));
	}

	private void cloth(@Nonnull RecipeOutput out)
	{
		shapedMisc(Cloth.BALLOON, 2)
				.pattern(" f ")
				.pattern("ftf")
				.pattern(" s ")
				.define('f', IEItems.Ingredients.HEMP_FABRIC)
				.define('t', Items.TORCH)
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.save(out, toRL(toPath(Cloth.BALLOON)));
		shapedMisc(Cloth.CUSHION, 3)
				.pattern("fff")
				.pattern("f f")
				.pattern("fff")
				.define('f', IEItems.Ingredients.HEMP_FABRIC)
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.save(out, toRL(toPath(Cloth.CUSHION)));
		shapedMisc(Cloth.STRIP_CURTAIN, 3)
				.pattern("sss")
				.pattern("fff")
				.pattern("fff")
				.define('s', IETags.metalRods)
				.define('f', IEItems.Ingredients.HEMP_FABRIC)
				.unlockedBy("has_hemp_fabric", has(IETags.fabricHemp))
				.unlockedBy("has_metal_rod", has(IETags.metalRods))
				.save(out, toRL(toPath(Cloth.STRIP_CURTAIN)));
	}

	private void conveyors(@Nonnull RecipeOutput out)
	{
		ItemLike basic = ConveyorHandler.getBlock(BasicConveyor.TYPE);
		ItemLike redstone = ConveyorHandler.getBlock(RedstoneConveyor.TYPE);
		ItemLike dropper = ConveyorHandler.getBlock(DropConveyor.TYPE);
		ItemLike extract = ConveyorHandler.getBlock(ExtractConveyor.TYPE);
		ItemLike splitter = ConveyorHandler.getBlock(SplitConveyor.TYPE);
		ItemLike vertical = ConveyorHandler.getBlock(VerticalConveyor.TYPE);
		addCoveyorCoveringRecipe(basic, out);
		addCoveyorCoveringRecipe(dropper, out);
		addCoveyorCoveringRecipe(extract, out);
		addCoveyorCoveringRecipe(splitter, out);
		addCoveyorCoveringRecipe(vertical, out);
		shapedMisc(basic, 8)
				.pattern("lll")
				.pattern("iri")
				.define('l', Tags.Items.LEATHER)
				.define('i', Tags.Items.INGOTS_IRON)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_leather", has(Items.LEATHER))
				.save(out, toRL(toPath(basic)));
		//TODO
		//shapedMiscRecipe(basic, 8)
		//		.patternLine("rrr")
		//		.patternLine("iri")
		//		.key('r', RUBBER)
		//		.key('i', Tags.Items.INGOTS_IRON)
		//		.key('r', Tags.Items.DUSTS_REDSTONE)
		//		.build(out);
		shapedMisc(redstone)
				.pattern("c")
				.pattern("r")
				.define('c', basic)
				.define('r', Blocks.REDSTONE_TORCH)
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(redstone)));
		shapedMisc(dropper)
				.pattern("c")
				.pattern("t")
				.define('c', basic)
				.define('t', Blocks.IRON_TRAPDOOR)
				.unlockedBy("has_trapdoor", has(Blocks.IRON_TRAPDOOR))
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(dropper)));
		shapedMisc(extract)
				.pattern("ws")
				.pattern("mc")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('s', Cloth.STRIP_CURTAIN)
				.define('m', Ingredients.COMPONENT_IRON)
				.define('c', basic)
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(extract)));
		shapedMisc(splitter, 3)
				.pattern("cic")
				.pattern(" c ")
				.define('c', basic)
				.define('i', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(splitter)));
		shapedMisc(vertical, 3)
				.pattern("ci")
				.pattern("c ")
				.pattern("ci")
				.define('c', basic)
				.define('i', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_conveyor", has(basic))
				.save(out, toRL(toPath(vertical)));
	}

	private void metalDevices(RecipeOutput out)
	{
		shapedMisc(MetalDevices.RAZOR_WIRE, 3)
				.pattern("sps")
				.pattern("fsf")
				.define('s', Ingredients.WIRE_STEEL)
				.define('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
				.define('f', WoodenDecoration.TREATED_FENCE)
				.unlockedBy("has_"+toPath(Ingredients.WIRE_STEEL), has(Ingredients.WIRE_STEEL))
				.save(out, toRL(toPath(MetalDevices.RAZOR_WIRE)));
		shapedMisc(MetalDevices.CAPACITOR_LV)
				.pattern("waw")
				.pattern("fef")
				.pattern("waw")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('f', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('a', IETags.getTagsFor(EnumMetals.LEAD).plate)
				.define('e', new IngredientFluidStack(IETags.fluidRedstoneAcid, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_lead_ingot", has(IETags.getTagsFor(EnumMetals.LEAD).ingot))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(MetalDevices.CAPACITOR_LV)));
		shapedMisc(MetalDevices.CAPACITOR_MV)
				.pattern("waw")
				.pattern("fef")
				.pattern("wcw")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('f', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('a', IETags.getTagsFor(EnumMetals.NICKEL).plate)
				.define('c', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('e', new IngredientFluidStack(IETags.fluidRedstoneAcid, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_nickel_ingot", has(IETags.getTagsFor(EnumMetals.NICKEL).ingot))
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(MetalDevices.CAPACITOR_MV)));
		shapedMisc(MetalDevices.CAPACITOR_HV)
				.pattern("waw")
				.pattern("fef")
				.pattern("wcw")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('f', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('a', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.define('c', IETags.hopGraphiteIngot)
				.define('e', new IngredientFluidStack(IETags.fluidRedstoneAcid, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_nickel_ingot", has(IETags.getTagsFor(EnumMetals.NICKEL).ingot))
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(out, toRL(toPath(MetalDevices.CAPACITOR_HV)));
		shapedMisc(MetalDevices.BARREL)
				.pattern("sss")
				.pattern("w w")
				.pattern("www")
				.define('w', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.define('s', IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).getId()))
				.unlockedBy("has_iron_sheet_slab", has(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON)))
				.save(out, toRL(toPath(MetalDevices.BARREL)));
		shapedMisc(MetalDevices.FLUID_PUMP)
				.pattern(" i ")
				.pattern("ici")
				.pattern("ppp")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('c', Ingredients.COMPONENT_IRON)
				.define('p', IEBlocks.MetalDevices.FLUID_PIPE)
				.unlockedBy("has_"+toPath(IEBlocks.MetalDevices.FLUID_PIPE), has(IEBlocks.MetalDevices.FLUID_PIPE))
				.save(out, toRL(toPath(MetalDevices.FLUID_PUMP)));
		shapedMisc(MetalDevices.BLAST_FURNACE_PREHEATER)
				.pattern("ss")
				.pattern("ss")
				.pattern("ph")
				.define('s', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.define('p', MetalDevices.FLUID_PIPE)
				.define('h', MetalDevices.FURNACE_HEATER)
				.unlockedBy("has_"+toPath(MetalDevices.FURNACE_HEATER), has(MetalDevices.FURNACE_HEATER))
				.save(out, toRL(toPath(MetalDevices.BLAST_FURNACE_PREHEATER)));
		shapedMisc(MetalDevices.FURNACE_HEATER)
				.pattern("pwp")
				.pattern("wsw")
				.pattern("prp")
				.define('w', Misc.WIRE_COILS.get(WireType.COPPER))
				.define('p', IETags.getTagsFor(EnumMetals.COPPER).plate)
				.define('s', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_"+toPath(MetalDecoration.LV_COIL), has(MetalDecoration.LV_COIL))
				.save(out, toRL(toPath(MetalDevices.FURNACE_HEATER)));
		shapedMisc(MetalDevices.DYNAMO)
				.pattern("rcr")
				.pattern("ili")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('l', MetalDecoration.LV_COIL)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_"+toPath(MetalDecoration.LV_COIL), has(MetalDecoration.LV_COIL))
				.save(out, toRL(toPath(MetalDevices.DYNAMO)));
		shapedMisc(MetalDevices.THERMOELECTRIC_GEN)
				.pattern("iii")
				.pattern("ele")
				.pattern("eee")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('l', MetalDecoration.LV_COIL)
				.define('e', IETags.getTagsFor(EnumMetals.CONSTANTAN).plate)
				.unlockedBy("has_"+toPath(MetalDecoration.LV_COIL), has(MetalDecoration.LV_COIL))
				.save(out, toRL(toPath(MetalDevices.THERMOELECTRIC_GEN)));
		shapedMisc(MetalDevices.ELECTRIC_LANTERN)
				.pattern(" i ")
				.pattern("pep")
				.pattern(" w ")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('e', Ingredients.LIGHT_BULB)
				.define('p', Tags.Items.GLASS_PANES)
				.define('w', IETags.copperWire)
				.unlockedBy("has_"+toPath(Ingredients.LIGHT_BULB), has(Ingredients.LIGHT_BULB))
				.save(out, toRL(toPath(MetalDevices.ELECTRIC_LANTERN)));
		shapedMisc(MetalDevices.CHARGING_STATION)
				.pattern("gig")
				.pattern("ttt")
				.pattern("wlw")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('t', Ingredients.ELECTRON_TUBE)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('l', MetalDecoration.LV_COIL)
				.define('g', Tags.Items.GLASS)
				.unlockedBy("has_"+toPath(MetalDecoration.LV_COIL), has(MetalDecoration.LV_COIL))
				.save(out, toRL(toPath(MetalDevices.CHARGING_STATION)));
		shapedMisc(MetalDevices.FLUID_PIPE, 8)
				.pattern("ppp")
				.pattern("   ")
				.pattern("ppp")
				.define('p', IETags.getTagsFor(EnumMetals.IRON).plate)
				.unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate))
				.save(out, toRL(toPath(MetalDevices.FLUID_PIPE)));
		shapedMisc(MetalDevices.SAMPLE_DRILL)
				.pattern("sfs")
				.pattern("sfs")
				.pattern("efe")
				.define('s', MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD))
				.define('f', MetalDecoration.STEEL_FENCE)
				.define('e', MetalDecoration.ENGINEERING_LIGHT)
				.unlockedBy("has_"+toPath(MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD)), has(MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD)))
				.unlockedBy("has_"+toPath(MetalDecoration.STEEL_FENCE), has(MetalDecoration.STEEL_FENCE))
				.unlockedBy("has_"+toPath(MetalDecoration.ENGINEERING_LIGHT), has(MetalDecoration.ENGINEERING_LIGHT))
				.save(out, toRL(toPath(MetalDevices.SAMPLE_DRILL)));
		shapedMisc(MetalDevices.TESLA_COIL)
				.pattern("iii")
				.pattern(" l ")
				.pattern("ehc")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
				.define('l', MetalDecoration.MV_COIL)
				.define('h', MetalDevices.CAPACITOR_HV)
				.define('e', Ingredients.COMPONENT_ELECTRONIC_ADV)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_"+toPath(MetalDevices.CAPACITOR_HV), has(MetalDevices.CAPACITOR_HV))
				.save(out, toRL(toPath(MetalDevices.TESLA_COIL)));
		shapedMisc(MetalDevices.FLOODLIGHT)
				.pattern("sii")
				.pattern("pes")
				.pattern("sci")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('s', IETags.getTagsFor(EnumMetals.SILVER).plate)
				.define('e', Ingredients.LIGHT_BULB)
				.define('c', Ingredients.COMPONENT_IRON)
				.define('p', Tags.Items.GLASS_PANES)
				.unlockedBy("has_"+toPath(Ingredients.LIGHT_BULB), has(Ingredients.LIGHT_BULB))
				.save(out, toRL(toPath(MetalDevices.FLOODLIGHT)));
		shapedMisc(MetalDevices.TURRET_CHEM)
				.pattern(" s ")
				.pattern(" gc")
				.pattern("bte")
				.define('s', Misc.TOOL_UPGRADES.get(ToolUpgrade.RAILGUN_SCOPE))
				.define('g', Weapons.CHEMTHROWER)
				.define('c', Ingredients.COMPONENT_ELECTRONIC_ADV)
				.define('b', MetalDevices.BARREL)
				.define('t', WoodenDevices.TURNTABLE)
				.define('e', MetalDecoration.ENGINEERING_RS)
				.unlockedBy("has_"+toPath(Weapons.CHEMTHROWER), has(Weapons.CHEMTHROWER))
				.save(out, toRL(toPath(MetalDevices.TURRET_CHEM)));
		shapedMisc(MetalDevices.TURRET_GUN)
				.pattern(" s ")
				.pattern(" gc")
				.pattern("bte")
				.define('s', Misc.TOOL_UPGRADES.get(ToolUpgrade.RAILGUN_SCOPE))
				.define('g', Weapons.REVOLVER)
				.define('c', Ingredients.COMPONENT_ELECTRONIC_ADV)
				.define('b', Misc.TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_MAGAZINE))
				.define('t', WoodenDevices.TURNTABLE)
				.define('e', MetalDecoration.ENGINEERING_RS)
				.unlockedBy("has_"+toPath(Weapons.REVOLVER), has(Weapons.REVOLVER))
				.save(out, toRL(toPath(MetalDevices.TURRET_GUN)));
		shapedMisc(MetalDevices.CLOCHE)
				.pattern("geg")
				.pattern("g g")
				.pattern("wcw")
				.define('g', Tags.Items.GLASS)
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.define('e', Ingredients.LIGHT_BULB)
				.define('c', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_"+toPath(Ingredients.LIGHT_BULB), has(Ingredients.LIGHT_BULB))
				.save(out, toRL(toPath(MetalDevices.CLOCHE)));
		shapedMisc(MetalDevices.FLUID_PLACER)
				.pattern("ibi")
				.pattern("b b")
				.pattern("ibi")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).plate)
				.define('b', Items.IRON_BARS)
				.unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate))
				.save(out, toRL(toPath(MetalDevices.FLUID_PLACER)));
		for(Entry<EnumMetals, BlockEntry<ChuteBlock>> chute : MetalDevices.CHUTES.entrySet())
			shapedMisc(chute.getValue(), 12)
					.pattern("s s")
					.pattern("s s")
					.pattern("s s")
					.define('s', IETags.getItemTag(IETags.getTagsFor(chute.getKey()).sheetmetal))
					.unlockedBy("has_plate", has(IETags.getTagsFor(chute.getKey()).plate))
					.save(out, toRL(toPath(chute.getValue())));
		shapedMisc(MetalDevices.ELECTROMAGNET)
				.pattern("pcp")
				.pattern("wiw")
				.pattern("pwp")
				.define('w', Misc.WIRE_COILS.get(WireType.COPPER))
				.define('p', IETags.steelRod)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
				.define('c', Ingredients.COMPONENT_ELECTRONIC)
				.unlockedBy("has_"+toPath(Ingredients.COMPONENT_ELECTRONIC), has(Ingredients.COMPONENT_ELECTRONIC))
				.save(out, toRL(toPath(MetalDevices.ELECTROMAGNET)));
		shapelessMisc(MetalDevices.PIPE_VALVE)
				.requires(Ingredient.of(MetalDevices.FLUID_PIPE))
				.requires(Ingredient.of(Ingredients.COMPONENT_STEEL))
				.requires(Ingredient.of(Connectors.CONNECTOR_REDSTONE))
				.unlockedBy("has_fluid_pipe", has(MetalDevices.FLUID_PIPE))
				.save(out, toRL(toPath(MetalDevices.PIPE_VALVE)));
	}

	private void addCoveyorCoveringRecipe(ItemLike basic, RecipeOutput out)
	{
		ItemStack result = ConveyorBlock.makeCovered(
				basic, MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD).get()
		);
		shapedMisc(result.getItem())
				.pattern("s")
				.pattern("c")
				.define('s', IETags.getItemTag(IETags.scaffoldingSteel))
				.define('c', basic)
				.unlockedBy("has_vertical_conveyor", has(basic))
				.save(WrappingRecipeOutput.replaceShapedOutput(out, result), toRL(toPath(basic)+"_covered"));
	}
}
