/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.ModelCoresample.CoresampleLoader;
import blusunrize.immersiveengineering.client.models.PotionBucketModel.Loader;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader;
import blusunrize.immersiveengineering.client.models.obj.callback.item.*;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.metal.ChuteBlock;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.WarningSignBlock.WarningSignIcon;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns.BannerEntry;
import blusunrize.immersiveengineering.common.register.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.*;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.data.blockstates.MultiblockStates;
import blusunrize.immersiveengineering.data.models.*;
import blusunrize.immersiveengineering.mixin.accessors.ItemModelGeneratorsAccess;
import blusunrize.immersiveengineering.mixin.accessors.TrimModelDataAccess;
import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.api.IEApi.ieLoc;
import static net.minecraft.client.renderer.RenderType.translucent;

public class ItemModels extends TRSRItemModelProvider
{
	private final MultiblockStates blockStates;

	public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper, MultiblockStates blockStates)
	{
		super(output, existingFileHelper);
		this.blockStates = blockStates;
	}

	private ResourceLocation forgeLoc(String s)
	{
		return ResourceLocation.fromNamespaceAndPath("neoforge", s);
	}

	@Override
	protected void registerModels()
	{
		for(EnumMetals m : EnumMetals.values())
			createMetalModels(m);
		createItemModels();
		createMetalModels();
		createWoodenModels();
		createStoneModels();
		createClothModels();
		createConnectorModels();
	}

	private void createMetalModels()
	{
		cubeBottomTop(name(MetalDevices.BARREL),
				rl("block/metal_device/barrel_side"),
				rl("block/metal_device/barrel_up_none"),
				rl("block/metal_device/barrel_up_none"));

		obj(MetalDecoration.STEEL_POST, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/metal_decoration/steel_post"))
				.transforms(modLoc("item/post"));
		obj(MetalDecoration.ALU_POST, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/metal_decoration/aluminum_post"))
				.transforms(modLoc("item/post"));
		addItemModel("door_steel", MetalDecoration.STEEL_DOOR);
		addItemModel("sign_steel", MetalDecoration.STEEL_SIGN.sign());
		addItemModel("sign_steel_hanging", MetalDecoration.STEEL_SIGN.hanging());
		addItemModel("sign_aluminum", MetalDecoration.ALU_SIGN.sign());
		addItemModel("sign_aluminum_hanging", MetalDecoration.ALU_SIGN.hanging());

		for(Entry<WarningSignIcon, BlockEntry<IEBaseBlock>> warningSign : MetalDecoration.WARNING_SIGNS.entrySet())
		{
			addLayeredItemModel(warningSign.getValue().asItem(),
					rl("block/metal_decoration/sign/base_front"),
					rl("block/metal_decoration/sign/icon_"+warningSign.getKey().getSerializedName())
			);
		}

		obj(MetalDevices.CLOCHE, rl("block/metal_device/cloche.obj.ie"))
				.transforms(rl("item/cloche"))
				.renderType(ModelProviderUtils.getName(translucent()));
		obj(MetalDevices.TESLA_COIL, rl("block/metal_device/teslacoil.obj"))
				.transforms(rl("item/teslacoil"));
		for(Entry<EnumMetals, BlockEntry<ChuteBlock>> chute : MetalDevices.CHUTES.entrySet())
			obj(chute.getValue(), rl("block/metal_device/chute_inv.obj"))
					.texture("texture", modLoc("block/metal/sheetmetal_"+chute.getKey().tagName()))
					.transforms(rl("item/block"));
		for(Entry<DyeColor, BlockEntry<ChuteBlock>> chute : MetalDevices.DYED_CHUTES.entrySet())
			obj(chute.getValue(), rl("block/metal_device/chute_inv.obj"))
					.texture("texture", modLoc("block/metal/sheetmetal_"+chute.getKey().getName()))
					.transforms(rl("item/block"));


		obj(MetalDevices.TURRET_CHEM, rl("block/metal_device/chem_turret_inv.obj"))
				.transforms(rl("item/turret"));
		obj(MetalDevices.TURRET_GUN, rl("block/metal_device/gun_turret_inv.obj"))
				.transforms(rl("item/turret"));
		obj(MetalDevices.FLUID_PIPE, rl("block/metal_device/fluid_pipe.obj.ie"))
				.transforms(rl("item/block"));
		obj(MetalDevices.FLUID_PUMP, rl("block/metal_device/fluid_pump_inv.obj"))
				.transforms(rl("item/fluid_pump"));


		obj(MetalDevices.BLAST_FURNACE_PREHEATER, rl("block/metal_device/blastfurnace_preheater.obj"))
				.transforms(rl("item/blastfurnace_preheater"));
		obj(MetalDevices.SAMPLE_DRILL, rl("block/metal_device/core_drill.obj"))
				.transforms(rl("item/sampledrill"));
		obj(IEMultiblockLogic.METAL_PRESS.blockItem().get(), rl("block/metal_multiblock/metal_press.obj"))
				.transforms(rl("item/multiblock"));
		obj(IEMultiblockLogic.CRUSHER.blockItem().get(), rl("block/metal_multiblock/crusher.obj"))
				.transforms(rl("item/crusher"));
		obj(IEMultiblockLogic.SAWMILL.blockItem().get(), rl("block/metal_multiblock/sawmill.obj"))
				.transforms(rl("item/crusher"));
		obj(IEMultiblockLogic.TANK.blockItem().get(), rl("block/metal_multiblock/tank.obj"))
				.transforms(rl("item/tank"));
		obj(IEMultiblockLogic.SILO.blockItem().get(), rl("block/metal_multiblock/silo.obj"))
				.transforms(rl("item/silo"));
		obj(IEMultiblockLogic.ASSEMBLER.blockItem().get(), rl("block/metal_multiblock/assembler.obj"))
				.transforms(rl("item/multiblock"));
		obj(IEMultiblockLogic.AUTO_WORKBENCH.blockItem().get(), rl("block/metal_multiblock/auto_workbench.obj"))
				.transforms(rl("item/multiblock"));
		obj(IEMultiblockLogic.BOTTLING_MACHINE.blockItem().get(), rl("block/metal_multiblock/bottling_machine.obj.ie"))
				.transforms(rl("item/bottling_machine"));
		obj(IEMultiblockLogic.SQUEEZER.blockItem().get(), rl("block/metal_multiblock/squeezer.obj"))
				.transforms(rl("item/multiblock"));
		obj(IEMultiblockLogic.FERMENTER.blockItem().get(), rl("block/metal_multiblock/fermenter.obj"))
				.transforms(rl("item/multiblock"));
		obj(IEMultiblockLogic.REFINERY.blockItem().get(), rl("block/metal_multiblock/refinery.obj"))
				.transforms(rl("item/refinery"));
		obj(IEMultiblockLogic.DIESEL_GENERATOR.blockItem().get(), rl("block/metal_multiblock/diesel_generator.obj"))
				.transforms(rl("item/crusher"));
		obj(IEMultiblockLogic.EXCAVATOR.blockItem().get(), rl("block/metal_multiblock/excavator.obj"))
				.transforms(rl("item/excavator"));
		obj(IEMultiblockLogic.BUCKET_WHEEL.blockItem().get(), rl("block/metal_multiblock/bucket_wheel.obj.ie"))
				.transforms(rl("item/bucket_wheel"));
		obj(IEMultiblockLogic.ARC_FURNACE.blockItem().get(), rl("block/metal_multiblock/arc_furnace.obj"))
				.transforms(rl("item/arc_furnace"));
		obj(IEMultiblockLogic.LIGHTNING_ROD.blockItem().get(), rl("block/metal_multiblock/lightningrod.obj"))
				.transforms(rl("item/multiblock"));
		obj(IEMultiblockLogic.MIXER.blockItem().get(), rl("block/metal_multiblock/mixer.obj"))
				.transforms(rl("item/multiblock"));
		obj(IEMultiblockLogic.RADIO_TOWER.blockItem().get(), rl("block/metal_multiblock/radio_tower.obj"))
				.transforms(rl("item/radio_tower"));
		obj(IEMultiblockLogic.CHUNK_LOADER.blockItem().get(), rl("block/metal_multiblock/chunk_loader.obj.ie"))
				.transforms(rl("item/chunk_loader"))
				.renderType(ModelProviderUtils.getName(translucent()));

		obj(MetalDecoration.ALU_WALLMOUNT, modLoc("block/wooden_device/wallmount.obj"))
				.texture("texture", modLoc("block/metal_decoration/aluminum_wallmount"))
				.transforms(modLoc("item/wallmount"));
		obj(MetalDecoration.STEEL_WALLMOUNT, modLoc("block/wooden_device/wallmount.obj"))
				.texture("texture", modLoc("block/metal_decoration/steel_wallmount"))
				.transforms(modLoc("item/wallmount"));

		for(BlockEntry<ConveyorBlock> b : MetalDevices.CONVEYORS.values())
			getBuilder(b).customLoader(ConveyorModelBuilder::begin)
					.type(b.get().getType())
					.end();

		obj(MetalDecoration.LANTERN, modLoc("block/lantern_inventory.obj"))
				.transforms(modLoc("item/block"));
		obj(MetalDecoration.CAGELAMP, rl("block/cagelamp.obj"))
				.texture("texture", modLoc("block/metal_decoration/cagelamp"))
				.transforms(rl("item/block"));
		addLayeredItemModel(
				MetalDecoration.METAL_LADDER.get(CoverType.NONE).asItem(),
				rl("block/metal_decoration/metal_ladder")
		);
	}

	private void createWoodenModels()
	{
		obj(WoodenDevices.CRAFTING_TABLE, rl("block/wooden_device/craftingtable.obj"))
				.transforms(rl("item/block"));

		cubeBottomTop(name(WoodenDevices.WOODEN_BARREL),
				rl("block/wooden_device/barrel_side"),
				rl("block/wooden_device/barrel_up_none"),
				rl("block/wooden_device/barrel_up_none"));

		obj(WoodenDecoration.TREATED_POST, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/wooden_decoration/post"))
				.transforms(modLoc("item/post"));
		addItemModel("door_treated", WoodenDecoration.DOOR);
		addItemModel("door_treated_framed", WoodenDecoration.DOOR_FRAMED);
		addItemModel("sign_treated", WoodenDecoration.SIGN.sign());
		addItemModel("sign_treated_hanging", WoodenDecoration.SIGN.hanging());

		obj(WoodenDevices.WORKBENCH, rl("block/wooden_device/workbench.obj.ie"))
				.transforms(rl("item/workbench"));
		obj(WoodenDevices.CIRCUIT_TABLE, rl("block/wooden_device/circuit_table.obj"))
				.transforms(rl("item/workbench"));
		obj(WoodenDevices.LOGIC_UNIT, rl("block/wooden_device/logic_unit.obj.ie"))
				.transforms(rl("item/block"));

		obj(WoodenDevices.TREATED_WALLMOUNT, modLoc("block/wooden_device/wallmount.obj"))
				.texture("texture", modLoc("block/wooden_device/wallmount"))
				.transforms(modLoc("item/wallmount"));

		obj(WoodenDevices.WATERMILL, modLoc("block/wooden_device/watermill.obj"))
				.transforms(modLoc("item/watermill"));
		obj(WoodenDevices.WINDMILL, modLoc("block/wooden_device/windmill.obj.ie"))
				.transforms(modLoc("item/windmill"));
	}

	private void createClothModels()
	{
		withExistingParent(name(Cloth.STRIP_CURTAIN), rl("block/stripcurtain"))
				.transforms(rl("item/stripcurtain"));
		obj(Cloth.BALLOON, rl("block/balloon.obj.ie"))
				.transforms(rl("item/block"));
	}

	private void createItemModels()
	{
		addItemModels("metal_", IEItems.Metals.INGOTS.values().stream().filter(i -> ImmersiveEngineering.MODID.equals(i.getId().getNamespace())).toArray(ItemLike[]::new));
		addItemModels("metal_", IEItems.Metals.NUGGETS.values().stream().filter(i -> ImmersiveEngineering.MODID.equals(i.getId().getNamespace())).toArray(ItemLike[]::new));
		addItemModels("metal_", IEItems.Metals.RAW_ORES.values().stream().filter(i -> ImmersiveEngineering.MODID.equals(i.getId().getNamespace())).toArray(ItemLike[]::new));
		addItemModels("metal_", IEItems.Metals.DUSTS.values().toArray(new ItemLike[0]));
		addItemModels("metal_", IEItems.Metals.PLATES.values().toArray(new ItemLike[0]));
		for(ItemLike bag : IEItems.Misc.SHADER_BAG.values())
			addItemModel("shader_bag", bag);
		for(ItemLike shader : Misc.SHADERS.values())
			withExistingParent(name(shader), ieLoc("item/ie_item_base"))
					.texture("layer0", ieLoc("item/shader_0"))
					.texture("layer1", ieLoc("item/shader_1"))
					.texture("layer2", ieLoc("item/shader_2"));

		addItemModels("material_", Ingredients.STICK_TREATED, Ingredients.STICK_IRON, Ingredients.STICK_STEEL, Ingredients.STICK_ALUMINUM, Ingredients.STICK_NETHERITE,
				Ingredients.HEMP_FIBER, Ingredients.HEMP_FABRIC, Ingredients.ERSATZ_LEATHER, Ingredients.COAL_COKE, Ingredients.SLAG,
				Ingredients.COMPONENT_IRON, Ingredients.COMPONENT_STEEL, Ingredients.WATERWHEEL_SEGMENT, Ingredients.WINDMILL_BLADE, Ingredients.WINDMILL_SAIL,
				Ingredients.WOODEN_GRIP, Ingredients.GUNPART_BARREL, Ingredients.GUNPART_DRUM, Ingredients.GUNPART_HAMMER,
				Ingredients.DUST_COKE, Ingredients.DUST_HOP_GRAPHITE, Ingredients.INGOT_HOP_GRAPHITE, Ingredients.PLATE_HOP_GRAPHITE,
				Ingredients.WIRE_COPPER, Ingredients.WIRE_ELECTRUM, Ingredients.WIRE_ALUMINUM, Ingredients.WIRE_STEEL, Ingredients.WIRE_LEAD,
				Ingredients.DUST_SALTPETER, Ingredients.DUST_SULFUR, Ingredients.DUST_WOOD,
				Ingredients.LIGHT_BULB, Ingredients.ELECTRON_TUBE, Ingredients.CIRCUIT_BOARD,
				Ingredients.DUROPLAST_PLATE, Ingredients.COMPONENT_ELECTRONIC, Ingredients.COMPONENT_ELECTRONIC_ADV
		);
		addItemModels("metal_", Ingredients.NUGGET_NETHERITE);

		addItemModels(
				"tool_", mcLoc("item/handheld"), Tools.HAMMER, Tools.WIRECUTTER, Tools.SCREWDRIVER,
				Tools.MANUAL, Tools.STEEL_PICK, Tools.STEEL_SHOVEL, Tools.STEEL_AXE, Tools.STEEL_HOE, Tools.STEEL_SWORD
		);
		addItemModels("", Tools.SURVEY_TOOLS);
		addItemModels("", Tools.GLIDER);
		addItemModels("", IEItems.Misc.WIRE_COILS.values().toArray(new ItemLike[0]));
		addItemModels("", IEItems.Misc.GRAPHITE_ELECTRODE);
		addItemModels("", IEItems.Misc.TOOL_UPGRADES.values().toArray(new ItemLike[0]));
		addItemModels("", Molds.MOLD_PLATE, Molds.MOLD_GEAR, Molds.MOLD_ROD, Molds.MOLD_BULLET_CASING, Molds.MOLD_WIRE, Molds.MOLD_PACKING_4, Molds.MOLD_PACKING_9, Molds.MOLD_UNPACKING);
		addItemModels("bullet_", Ingredients.EMPTY_CASING, Ingredients.EMPTY_SHELL);
		for(Entry<IBullet<?>, ItemRegObject<BulletItem<?>>> bullet : Weapons.BULLETS.entrySet())
			addLayeredItemModel(bullet.getValue().asItem(), bullet.getKey().getTextures());
		addItemModels("", IEItems.Misc.FARADAY_SUIT.values());
//		addItemModels("", IEItems.Tools.STEEL_ARMOR.values());
		for(Entry<Type, ItemRegObject<ArmorItem>> armorPiece : IEItems.Tools.STEEL_ARMOR.entrySet())
			addTrimmedArmorModel(armorPiece.getValue().get());

		addItemModel("blueprint", IEItems.Misc.BLUEPRINT);
		addItemModel("seed_hemp", IEItems.Misc.HEMP_SEEDS);
		addItemModel("drillhead_iron", Tools.DRILLHEAD_IRON);
		addItemModel("drillhead_steel", Tools.DRILLHEAD_STEEL);
		addItemModels("", Tools.SAWBLADE, Tools.ROCKCUTTER, Tools.GRINDINGDISK);
		addItemModels("", IEItems.Misc.MAINTENANCE_KIT);
		addItemModels("", IEItems.Minecarts.CART_WOODEN_CRATE, IEItems.Minecarts.CART_REINFORCED_CRATE, IEItems.Minecarts.CART_WOODEN_BARREL, IEItems.Minecarts.CART_METAL_BARREL);
		addItemModels("", IEItems.Misc.LOGIC_CIRCUIT_BOARD);
		addItemModels("", IEItems.Misc.FERTILIZER);
		for(BannerEntry holder : IEBannerPatterns.ALL_BANNERS)
			addItemModel("banner_pattern_"+holder.name(), holder.item());
		addItemModels("", IEItems.Misc.ICON_BIRTHDAY, IEItems.Misc.ICON_LUCKY, IEItems.Misc.ICON_ACHTUNG,
				IEItems.Misc.ICON_DRILLBREAK, IEItems.Misc.ICON_RAVENHOLM, IEItems.Misc.ICON_FRIED, IEItems.Misc.ICON_BTTF);

		withExistingParent(name(SpawnEggs.EGG_FUSILIER), ResourceLocation.withDefaultNamespace("item/template_spawn_egg"));
		withExistingParent(name(SpawnEggs.EGG_COMMANDO), ResourceLocation.withDefaultNamespace("item/template_spawn_egg"));
		withExistingParent(name(SpawnEggs.EGG_BULWARK), ResourceLocation.withDefaultNamespace("item/template_spawn_egg"));
		addItemModels("", IEItems.SpawnEggs.ROBOT_WOLF);

		obj(Tools.VOLTMETER, rl("item/voltmeter.obj"))
				.transforms(rl("item/voltmeter"));
		obj(Tools.TOOLBOX, rl("item/toolbox.obj"))
				.transforms(rl("item/toolbox"));
		ieObjBuilder(IEItems.Misc.SHIELD, rl("item/shield.obj.ie"))
				.dynamic(true)
				.callback(ShieldCallbacks.INSTANCE)
				.layer(RenderType.translucent())
				.end()
				.transforms(rl("item/shield"));
		ieObjBuilder(Weapons.REVOLVER, modLoc("item/revolver.obj.ie"))
				.dynamic(true)
				.callback(RevolverCallbacks.INSTANCE)
				.layer(RenderType.translucent())
				.end()
				.transforms(modLoc("item/revolver"));
		ieObjBuilder(Tools.DRILL, modLoc("item/drill/drill_diesel.obj.ie"))
				.dynamic(true)
				.callback(DrillCallbacks.INSTANCE)
				.layer(RenderType.translucent())
				.end()
				.transforms(modLoc("item/drill"));
		ieObjBuilder(Tools.BUZZSAW, modLoc("item/buzzsaw_diesel.obj.ie"))
				.dynamic(true)
				.callback(BuzzsawCallbacks.INSTANCE)
				.layer(RenderType.translucent())
				.end()
				.transforms(modLoc("item/buzzsaw"));
		ieObjBuilder(Weapons.RAILGUN, modLoc("item/railgun.obj.ie"))
				.dynamic(true)
				.callback(RailgunCallbacks.INSTANCE)
				.layer(RenderType.translucent())
				.end()
				.transforms(modLoc("item/railgun"));
		ieObjBuilder(Weapons.CHEMTHROWER, modLoc("item/chemthrower.obj.ie"))
				.dynamic(true)
				.callback(ChemthrowerCallbacks.INSTANCE)
				.layer(RenderType.cutout())
				.end()
				.transforms(modLoc("item/chemthrower"));
		ieObjBuilder(Misc.POWERPACK, rl("item/powerpack.obj"))
				.callback(PowerpackCallbacks.INSTANCE)
				.layer(RenderType.translucent())
				.end()
				.transforms(rl("item/powerpack"));

		IEFluids.ALL_ENTRIES.forEach(this::createBucket);
		withExistingParent(name(Misc.POTION_BUCKET), forgeLoc("item/bucket"))
				.customLoader(SpecialModelBuilder.forLoader(Loader.LOADER_NAME))
				.end();

		ieObjBuilder(IEItems.Misc.FLUORESCENT_TUBE, rl("item/fluorescent_tube.obj.ie"))
				.callback(FluorescentTubeCallbacks.INSTANCE)
				.dynamic(true)
				.end()
				.transforms(modLoc("item/fluorescent_tube"));
		getBuilder(IEItems.Misc.CORESAMPLE)
				.customLoader(SpecialModelBuilder.forLoader(CoresampleLoader.LOCATION));
	}

	private void createBucket(IEFluids.FluidEntry entry)
	{
		withExistingParent(name(entry.getBucket()), forgeLoc("item/bucket"))
				.customLoader(DynamicFluidContainerModelBuilder::begin)
				.fluid(entry.getStill());
	}

	private void createStoneModels()
	{
		obj(StoneDecoration.CONCRETE_SPRAYED, rl("block/sprayed_concrete.obj"))
				.transforms(rl("item/block"));
		getBuilder(IEMultiblockLogic.ALLOY_SMELTER.blockItem().get())
				.parent(blockStates.alloySmelterOn)
				.transforms(rl("item/alloysmelter"));
		getBuilder(IEMultiblockLogic.BLAST_FURNACE.blockItem().get())
				.parent(blockStates.blastFurnaceOn)
				.transforms(rl("item/blastfurnace"));
		getBuilder(IEMultiblockLogic.COKE_OVEN.blockItem().get())
				.parent(blockStates.cokeOvenOn)
				.transforms(rl("item/blastfurnace"));
		obj(IEMultiblockLogic.ADV_BLAST_FURNACE.blockItem().get(), rl("block/blastfurnace_advanced.obj"))
				.transforms(rl("item/multiblock"));
	}

	private void createConnectorModels()
	{
		obj(Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), rl("block/connector/connector_lv.obj"))
				.texture("texture", modLoc("block/connector/connector_lv"))
				.transforms(rl("item/connector"));
		obj(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), rl("block/connector/connector_lv.obj"))
				.texture("texture", modLoc("block/connector/relay_lv"))
				.transforms(rl("item/connector"));

		obj(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), rl("block/connector/connector_mv.obj"))
				.texture("texture", modLoc("block/connector/connector_mv"))
				.transforms(rl("item/connector"));
		obj(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), rl("block/connector/connector_mv.obj"))
				.texture("texture", modLoc("block/connector/relay_mv"))
				.transforms(rl("item/connector"));

		obj(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), rl("block/connector/connector_hv.obj"))
				.transforms(rl("item/connector"));
		obj(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), rl("block/connector/relay_hv.obj"))
				.transforms(rl("item/connector"));

		obj(Connectors.CONNECTOR_REDSTONE, rl("block/connector/connector_redstone.obj.ie"))
				.transforms(rl("item/connector"));
		obj(Connectors.CONNECTOR_PROBE, rl("block/connector/connector_probe.obj.ie"))
				.transforms(rl("item/connector"))
				.renderType(ModelProviderUtils.getName(translucent()));
		obj(Connectors.CONNECTOR_BUNDLED, rl("block/connector/connector_bundled.obj"))
				.transforms(rl("item/connector"));
		obj(Connectors.REDSTONE_STATE_CELL, rl("block/connector/redstone_state_cell.obj"))
				.transforms(rl("item/connector"))
				.renderType(ModelProviderUtils.getName(translucent()));
		obj(Connectors.REDSTONE_TIMER, rl("block/connector/redstone_timer.obj.ie"))
				.transforms(rl("item/block"))
				.renderType(ModelProviderUtils.getName(translucent()));
		obj(Connectors.REDSTONE_SWITCHBOARD, rl("block/connector/switchboard.obj"))
				.transforms(rl("item/switchboard"));
		obj(Connectors.SIREN, rl("block/connector/siren.obj.ie"))
				.transforms(rl("item/block"));
		obj(Connectors.CONNECTOR_STRUCTURAL, rl("block/connector/connector_structural.obj.ie"))
				.transforms(rl("item/connector"));
		obj(Connectors.TRANSFORMER, rl("block/connector/transformer_mv_left.obj"))
				.transforms(rl("item/transformer"));
		obj(Connectors.TRANSFORMER_HV, rl("block/connector/transformer_hv_left.obj"))
				.transforms(rl("item/transformer"));
		obj(Connectors.REDSTONE_BREAKER, rl("block/connector/redstone_breaker.obj.ie"))
				.transforms(rl("item/redstone_breaker"));
		obj(Connectors.CURRENT_TRANSFORMER, rl("block/connector/e_meter.obj"))
				.transforms(rl("item/current_transformer"));
		obj(Connectors.BREAKER_SWITCH, rl("block/connector/breaker_switch_off.obj.ie"))
				.transforms(rl("item/breaker_switch"));
		obj(MetalDevices.RAZOR_WIRE, rl("block/razor_wire.obj.ie"))
				.transforms(rl("item/block"));

		obj(MetalDevices.ELECTRIC_LANTERN, rl("block/metal_device/e_lantern.obj"))
				.texture("texture", modLoc("block/metal_device/electric_lantern"))
				.transforms(rl("item/block"));
		obj(MetalDevices.FLOODLIGHT, rl("block/metal_device/floodlight.obj.ie"))
				.texture("texture", modLoc("block/metal_device/floodlight"))
				.transforms(rl("item/floodlight"));
		getBuilder(Connectors.FEEDTHROUGH)
				.customLoader(SpecialModelBuilder.forLoader(FeedthroughLoader.LOCATION));
	}

	private TRSRModelBuilder obj(ItemLike item, ResourceLocation model)
	{
		Preconditions.checkArgument(existingFileHelper.exists(model, PackType.CLIENT_RESOURCES, "", "models"));
		return getBuilder(item)
				.customLoader(ObjModelBuilder::begin)
				.flipV(true)
				.modelLocation(model.withPath("models/"+model.getPath()))
				.end();
	}

	private IEOBJBuilder<TRSRModelBuilder> ieObjBuilder(ItemLike item, ResourceLocation model)
	{
		Preconditions.checkArgument(existingFileHelper.exists(model, PackType.CLIENT_RESOURCES, "", "models"));
		return getBuilder(item)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(model.withPath("models/"+model.getPath()));
	}

	private TRSRModelBuilder getBuilder(ItemLike item)
	{
		return getBuilder(name(item));
	}

	private String name(ItemLike item)
	{
		return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Item models";
	}

	private void createMetalModels(EnumMetals metal)
	{
		String name = metal.tagName();
		if(metal.shouldAddOre())
		{
			cubeAll(name(Metals.ORES.get(metal)), rl("block/metal/ore_"+name));
			cubeAll(name(Metals.DEEPSLATE_ORES.get(metal)), rl("block/metal/deepslate_ore_"+name));
		}
		if(!metal.isVanillaMetal())
		{
			ResourceLocation defaultName = rl("block/metal/storage_"+name);
			if(metal==EnumMetals.URANIUM)
			{
				ResourceLocation side = rl("block/metal/storage_"+name+"_side");
				ResourceLocation top = rl("block/metal/storage_"+name+"_top");
				cubeBottomTop(name(Metals.STORAGE.get(metal)), side, top, top);
			}
			else
				cubeAll(name(Metals.STORAGE.get(metal)), defaultName);
		}
		ResourceLocation sheetmetalName = rl("block/metal/sheetmetal_"+name);
		cubeAll(name(Metals.SHEETMETAL.get(metal)), sheetmetalName);

	}

	private void addItemModels(String texturePrefix, ItemLike... items)
	{
		addItemModels(texturePrefix, Arrays.asList(items));
	}

	private void addItemModels(String texturePrefix, ResourceLocation parent, ItemLike... items)
	{
		addItemModels(texturePrefix, parent, Arrays.asList(items));
	}

	private void addItemModels(String texturePrefix, Collection<? extends ItemLike> items)
	{
		addItemModels(texturePrefix, mcLoc("item/generated"), items);
	}

	private void addItemModels(String texturePrefix, ResourceLocation parent, Collection<? extends ItemLike> items)
	{
		for(ItemLike item : items)
			addItemModel(texturePrefix==null?null: (texturePrefix+BuiltInRegistries.ITEM.getKey(item.asItem()).getPath()), item, parent);
	}

	private void addItemModel(String texture, ItemLike item)
	{
		addItemModel(texture, item, mcLoc("item/generated"));
	}

	private void addItemModel(String texture, ItemLike item, ResourceLocation parent)
	{
		String path = name(item);
		String textureLoc = texture==null?path: ("item/"+texture);
		withExistingParent(path, parent)
				.texture("layer0", modLoc(textureLoc));
	}

	private void addLayeredItemModel(Item item, ResourceLocation... layers)
	{
		String path = name(item);
		TRSRModelBuilder modelBuilder = withExistingParent(path, mcLoc("item/generated"));
		int layerIdx = 0;
		for(ResourceLocation layer : layers)
			modelBuilder.texture("layer"+(layerIdx++), layer);
	}

	private void addTrimmedArmorModel(ArmorItem item)
	{
		String path = name(item);
		ResourceLocation baseTexture = modLoc("item/"+path);
		TRSRModelBuilder modelBuilder = withExistingParent(path, mcLoc("item/generated"))
				.texture("layer0", baseTexture);
		for(TrimModelDataAccess trim : ItemModelGeneratorsAccess.getGeneratedTrimModels())
		{
			String material = trim.getName();
			String name = path+"_"+material+"_trim";
			ResourceLocation trimTexture = mcLoc("trims/items/"+item.getType().getName()+"_trim_"+material);
			// hacky workaround to avoid complaints about missing textures
			existingFileHelper.trackGenerated(trimTexture, ModelProvider.TEXTURE);
			TRSRModelBuilder trimModel = this.withExistingParent(name, mcLoc("item/generated"))
					.texture("layer0", baseTexture)
					.texture("layer1", trimTexture);
			modelBuilder.override(trimModel, ResourceLocation.withDefaultNamespace("trim_type"), trim.getItemModelIndex());
		}
	}
}
