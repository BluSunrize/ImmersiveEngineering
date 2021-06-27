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
import blusunrize.immersiveengineering.client.models.ModelConveyor.ConveyorLoader;
import blusunrize.immersiveengineering.client.models.ModelCoresample.CoresampleLoader;
import blusunrize.immersiveengineering.client.models.PotionBucketModel.Loader;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.ChuteBlock;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.fluids.IEFluids;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.*;
import blusunrize.immersiveengineering.data.blockstates.MultiblockStates;
import blusunrize.immersiveengineering.data.models.IEOBJBuilder;
import blusunrize.immersiveengineering.data.models.SpecialModelBuilder;
import blusunrize.immersiveengineering.data.models.TRSRItemModelProvider;
import blusunrize.immersiveengineering.data.models.TRSRModelBuilder;
import com.google.common.base.Preconditions;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.loaders.DynamicBucketModelBuilder;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class ItemModels extends TRSRItemModelProvider
{
	private final MultiblockStates blockStates;

	public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper, MultiblockStates blockStates)
	{
		super(generator, existingFileHelper);
		this.blockStates = blockStates;
	}

	private ResourceLocation forgeLoc(String s)
	{
		return new ResourceLocation("forge", s);
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
		cubeBottomTop(name(MetalDevices.barrel),
				rl("block/metal_device/barrel_side"),
				rl("block/metal_device/barrel_up_none"),
				rl("block/metal_device/barrel_up_none"));

		obj(MetalDecoration.steelPost, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/metal_decoration/steel_post"))
				.transforms(modLoc("item/post"));
		obj(MetalDecoration.aluPost, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/metal_decoration/aluminum_post"))
				.transforms(modLoc("item/post"));

		obj(MetalDevices.cloche, rl("block/metal_device/cloche.obj.ie"))
				.transforms(rl("item/cloche"));
		obj(MetalDevices.teslaCoil, rl("block/metal_device/teslacoil.obj"))
				.transforms(rl("item/teslacoil"));
		for(Entry<EnumMetals, BlockEntry<ChuteBlock>> chute : MetalDevices.chutes.entrySet())
			obj(chute.getValue(), rl("block/metal_device/chute_inv.obj"))
					.texture("texture", modLoc("block/metal/sheetmetal_"+chute.getKey().tagName()))
					.transforms(rl("item/block"));


		obj(MetalDevices.turretChem, rl("block/metal_device/chem_turret_inv.obj"))
				.transforms(rl("item/turret"));
		obj(MetalDevices.turretGun, rl("block/metal_device/gun_turret_inv.obj"))
				.transforms(rl("item/turret"));
		obj(MetalDevices.fluidPipe, rl("block/metal_device/fluid_pipe.obj.ie"))
				.transforms(rl("item/block"));
		obj(MetalDevices.fluidPump, rl("block/metal_device/fluid_pump_inv.obj"))
				.transforms(rl("item/fluid_pump"));


		obj(MetalDevices.blastFurnacePreheater, rl("block/metal_device/blastfurnace_preheater.obj"))
				.transforms(rl("item/blastfurnace_preheater"));
		obj(MetalDevices.sampleDrill, rl("block/metal_device/core_drill.obj"))
				.transforms(rl("item/sampledrill"));
		obj(Multiblocks.metalPress, rl("block/metal_multiblock/metal_press.obj"))
				.transforms(rl("item/multiblock"));
		obj(Multiblocks.crusher, rl("block/metal_multiblock/crusher.obj"))
				.transforms(rl("item/crusher"));
		obj(Multiblocks.sawmill, rl("block/metal_multiblock/sawmill.obj"))
				.transforms(rl("item/crusher"));
		obj(Multiblocks.tank, rl("block/metal_multiblock/tank.obj"))
				.transforms(rl("item/tank"));
		obj(Multiblocks.silo, rl("block/metal_multiblock/silo.obj"))
				.transforms(rl("item/silo"));
		obj(Multiblocks.assembler, rl("block/metal_multiblock/assembler.obj"))
				.transforms(rl("item/multiblock"));
		obj(Multiblocks.autoWorkbench, rl("block/metal_multiblock/auto_workbench.obj"))
				.transforms(rl("item/multiblock"));
		obj(Multiblocks.bottlingMachine, rl("block/metal_multiblock/bottling_machine.obj.ie"))
				.transforms(rl("item/bottling_machine"));
		obj(Multiblocks.squeezer, rl("block/metal_multiblock/squeezer.obj"))
				.transforms(rl("item/multiblock"));
		obj(Multiblocks.fermenter, rl("block/metal_multiblock/fermenter.obj"))
				.transforms(rl("item/multiblock"));
		obj(Multiblocks.refinery, rl("block/metal_multiblock/refinery.obj"))
				.transforms(rl("item/refinery"));
		obj(Multiblocks.dieselGenerator, rl("block/metal_multiblock/diesel_generator.obj"))
				.transforms(rl("item/crusher"));
		obj(Multiblocks.excavator, rl("block/metal_multiblock/excavator.obj"))
				.transforms(rl("item/excavator"));
		obj(Multiblocks.bucketWheel, rl("block/metal_multiblock/bucket_wheel.obj.ie"))
				.transforms(rl("item/bucket_wheel"));
		obj(Multiblocks.arcFurnace, rl("block/metal_multiblock/arc_furnace.obj"))
				.transforms(rl("item/arc_furnace"));
		obj(Multiblocks.lightningrod, rl("block/metal_multiblock/lightningrod.obj"))
				.transforms(rl("item/multiblock"));
		obj(Multiblocks.mixer, rl("block/metal_multiblock/mixer.obj"))
				.transforms(rl("item/multiblock"));

		obj(MetalDecoration.aluWallmount, modLoc("block/wooden_device/wallmount.obj"))
				.texture("texture", modLoc("block/metal_decoration/aluminum_wallmount"))
				.transforms(modLoc("item/wallmount"));
		obj(MetalDecoration.steelWallmount, modLoc("block/wooden_device/wallmount.obj"))
				.texture("texture", modLoc("block/metal_decoration/steel_wallmount"))
				.transforms(modLoc("item/wallmount"));

		for(BlockEntry<ConveyorBlock> b : MetalDevices.CONVEYORS.values())
			getBuilder(b).customLoader(SpecialModelBuilder.forLoader(ConveyorLoader.LOCATION));

		obj(MetalDecoration.lantern, modLoc("block/lantern_inventory.obj"))
				.transforms(modLoc("item/block"));
		addLayeredItemModel(
				MetalDecoration.metalLadder.get(CoverType.NONE).asItem(),
				rl("block/metal_decoration/metal_ladder")
		);
	}

	private void createWoodenModels()
	{
		obj(WoodenDevices.craftingTable, rl("block/wooden_device/craftingtable.obj"))
				.transforms(rl("item/block"));

		cubeBottomTop(name(WoodenDevices.woodenBarrel),
				rl("block/wooden_device/barrel_side"),
				rl("block/wooden_device/barrel_up_none"),
				rl("block/wooden_device/barrel_up_none"));

		obj(WoodenDecoration.treatedPost, modLoc("block/wooden_device/wooden_post_inv.obj"))
				.texture("post", modLoc("block/wooden_decoration/post"))
				.transforms(modLoc("item/post"));
		obj(WoodenDevices.workbench, rl("block/wooden_device/workbench.obj.ie"))
				.transforms(rl("item/workbench"));
		obj(WoodenDevices.circuitTable, rl("block/wooden_device/circuit_table.obj"))
				.transforms(rl("item/workbench"));
		obj(WoodenDevices.logicUnit, rl("block/wooden_device/logic_unit.obj.ie"))
				.transforms(rl("item/block"));

		obj(WoodenDevices.treatedWallmount, modLoc("block/wooden_device/wallmount.obj"))
				.texture("texture", modLoc("block/wooden_device/wallmount"))
				.transforms(modLoc("item/wallmount"));

		obj(WoodenDevices.watermill, modLoc("block/wooden_device/watermill.obj.ie"))
				.transforms(modLoc("item/watermill"));
		obj(WoodenDevices.windmill, modLoc("block/wooden_device/windmill.obj.ie"))
				.transforms(modLoc("item/windmill"));
	}

	private void createClothModels()
	{
		withExistingParent(name(Cloth.curtain), rl("block/stripcurtain"))
				.transforms(rl("item/stripcurtain"));
		obj(Cloth.balloon, rl("block/balloon.obj.ie"))
				.transforms(rl("item/block"));
	}

	private void createItemModels()
	{
		addItemModels("metal_", IEItems.Metals.ingots.values().stream().filter(i -> ImmersiveEngineering.MODID.equals(i.getId().getNamespace())).toArray(IItemProvider[]::new));
		addItemModels("metal_", IEItems.Metals.nuggets.values().stream().filter(i -> ImmersiveEngineering.MODID.equals(i.getId().getNamespace())).toArray(IItemProvider[]::new));
		addItemModels("metal_", IEItems.Metals.dusts.values().toArray(new IItemProvider[0]));
		addItemModels("metal_", IEItems.Metals.plates.values().toArray(new IItemProvider[0]));
		for(IItemProvider bag : IEItems.Misc.shaderBag.values())
			addItemModel("shader_bag", bag);

		addItemModels("material_", Ingredients.stickTreated, Ingredients.stickIron, Ingredients.stickSteel, Ingredients.stickAluminum,
				Ingredients.hempFiber, Ingredients.hempFabric, Ingredients.coalCoke, Ingredients.slag,
				Ingredients.componentIron, Ingredients.componentSteel, Ingredients.waterwheelSegment, Ingredients.windmillBlade, Ingredients.windmillSail,
				Ingredients.woodenGrip, Ingredients.gunpartBarrel, Ingredients.gunpartDrum, Ingredients.gunpartHammer,
				Ingredients.dustCoke, Ingredients.dustHopGraphite, Ingredients.ingotHopGraphite,
				Ingredients.wireCopper, Ingredients.wireElectrum, Ingredients.wireAluminum, Ingredients.wireSteel, Ingredients.wireLead,
				Ingredients.dustSaltpeter, Ingredients.dustSulfur, Ingredients.dustWood, Ingredients.electronTube, Ingredients.circuitBoard);

		addItemModels(
				"tool_", mcLoc("item/handheld"), Tools.hammer, Tools.wirecutter, Tools.screwdriver,
				Tools.manual, Tools.steelPick, Tools.steelShovel, Tools.steelAxe, Tools.steelHoe, Tools.steelSword
		);
		addItemModels("", Tools.surveyTools);
		addItemModels("", IEItems.Misc.wireCoils.values().toArray(new IItemProvider[0]));
		addItemModels("", IEItems.Misc.graphiteElectrode);
		addItemModels("", IEItems.Misc.toolUpgrades.values().toArray(new IItemProvider[0]));
		addItemModels("", Molds.moldPlate, Molds.moldGear, Molds.moldRod, Molds.moldBulletCasing, Molds.moldWire, Molds.moldPacking4, Molds.moldPacking9, Molds.moldUnpacking);
		addItemModels("bullet_", Ingredients.emptyCasing, Ingredients.emptyShell);
		for(Entry<IBullet, ItemRegObject<BulletItem>> bullet : Weapons.bullets.entrySet())
			addLayeredItemModel(bullet.getValue().asItem(), bullet.getKey().getTextures());
		addItemModels("", IEItems.Misc.faradaySuit.values());
		addItemModels("", IEItems.Tools.steelArmor.values());
		addItemModel("blueprint", IEItems.Misc.blueprint);
		addItemModel("seed_hemp", IEItems.Misc.hempSeeds);
		addItemModel("drillhead_iron", Tools.drillheadIron);
		addItemModel("drillhead_steel", Tools.drillheadSteel);
		addItemModels("", Tools.sawblade, Tools.rockcutter);
		addItemModels("", IEItems.Misc.maintenanceKit);
		addItemModels("", IEItems.Minecarts.cartWoodenCrate, IEItems.Minecarts.cartReinforcedCrate, IEItems.Minecarts.cartWoodenBarrel, IEItems.Minecarts.cartMetalBarrel);
		addItemModels("", IEItems.Misc.logicCircuitBoard);
		addItemModel("banner_pattern", IEItems.BannerPatterns.bannerPatternHammer);
		addItemModel("banner_pattern", IEItems.BannerPatterns.bannerPatternBevels);
		addItemModel("banner_pattern", IEItems.BannerPatterns.bannerPatternOrnate);
		addItemModel("banner_pattern", IEItems.BannerPatterns.bannerPatternTreatedWood);
		addItemModel("banner_pattern", IEItems.BannerPatterns.bannerPatternWindmill);
		addItemModel("banner_pattern", IEItems.BannerPatterns.bannerPatternWolfR);
		addItemModel("banner_pattern", IEItems.BannerPatterns.bannerPatternWolfL);
		addItemModel("banner_pattern", IEItems.BannerPatterns.bannerPatternWolf);
		addItemModels("", IEItems.Misc.iconBirthday, IEItems.Misc.iconLucky, IEItems.Misc.iconDrillbreak, IEItems.Misc.iconRavenholm);

		obj(Tools.voltmeter, rl("item/voltmeter.obj"))
				.transforms(rl("item/voltmeter"));
		obj(Tools.toolbox, rl("item/toolbox.obj"))
				.transforms(rl("item/toolbox"));
		ieObj(IEItems.Misc.shield, rl("item/shield.obj.ie"))
				.transforms(rl("item/shield"));
		ieObjBuilder(Weapons.revolver, modLoc("item/revolver.obj.ie"))
				.dynamic(true)
				.end()
				.transforms(modLoc("item/revolver"));
		ieObjBuilder(Tools.drill, modLoc("item/drill/drill_diesel.obj.ie"))
				.dynamic(true)
				.end()
				.transforms(modLoc("item/drill"));
		ieObjBuilder(Tools.buzzsaw, modLoc("item/buzzsaw_diesel.obj.ie"))
				.dynamic(true)
				.end()
				.transforms(modLoc("item/buzzsaw"));
		ieObj(Weapons.railgun, modLoc("item/railgun.obj.ie"))
				.transforms(modLoc("item/railgun"));
		ieObj(Weapons.chemthrower, modLoc("item/chemthrower.obj.ie"))
				.transforms(modLoc("item/chemthrower"));

		IEFluids.ALL_ENTRIES.forEach(this::createBucket);
		withExistingParent(name(Misc.potionBucket), forgeLoc("item/bucket"))
				.customLoader(SpecialModelBuilder.forLoader(Loader.LOADER_NAME))
				.end();

		ieObj(IEItems.Misc.fluorescentTube, rl("item/fluorescent_tube.obj.ie"))
				.transforms(modLoc("item/fluorescent_tube"));
		getBuilder(IEItems.Misc.coresample)
				.customLoader(SpecialModelBuilder.forLoader(CoresampleLoader.LOCATION));
	}

	private void createBucket(IEFluids.FluidEntry entry)
	{
		withExistingParent(name(entry.getBucket()), forgeLoc("item/bucket"))
				.customLoader(DynamicBucketModelBuilder::begin)
				.fluid(entry.getStill());
	}

	private void createStoneModels()
	{
		obj(StoneDecoration.concreteSprayed, rl("block/sprayed_concrete.obj"))
				.transforms(rl("item/block"));
		getBuilder(Multiblocks.alloySmelter)
				.parent(blockStates.alloySmelterOn)
				.transforms(rl("item/alloysmelter"));
		getBuilder(Multiblocks.blastFurnace)
				.parent(blockStates.blastFurnaceOn)
				.transforms(rl("item/blastfurnace"));
		getBuilder(Multiblocks.cokeOven)
				.parent(blockStates.cokeOvenOn)
				.transforms(rl("item/blastfurnace"));
		obj(Multiblocks.blastFurnaceAdv, rl("block/blastfurnace_advanced.obj"))
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

		obj(Connectors.connectorRedstone, rl("block/connector/connector_redstone.obj.ie"))
				.transforms(rl("item/connector"));
		obj(Connectors.connectorProbe, rl("block/connector/connector_probe.obj.ie"))
				.transforms(rl("item/connector"));
		obj(Connectors.connectorBundled, rl("block/connector/connector_bundled.obj"))
				.transforms(rl("item/connector"));
		obj(Connectors.connectorStructural, rl("block/connector/connector_structural.obj.ie"))
				.transforms(rl("item/connector"));
		obj(Connectors.transformer, rl("block/connector/transformer_mv_left.obj"))
				.transforms(rl("item/transformer"));
		obj(Connectors.transformerHV, rl("block/connector/transformer_hv_left.obj"))
				.transforms(rl("item/transformer"));
		obj(Connectors.redstoneBreaker, rl("block/connector/redstone_breaker.obj.ie"))
				.transforms(rl("item/redstone_breaker"));
		obj(Connectors.currentTransformer, rl("block/connector/e_meter.obj"))
				.transforms(rl("item/current_transformer"));
		obj(Connectors.breakerswitch, rl("block/connector/breaker_switch_off.obj.ie"))
				.transforms(rl("item/breaker_switch"));
		obj(MetalDevices.razorWire, rl("block/razor_wire.obj.ie"))
				.transforms(rl("item/block"));

		obj(MetalDevices.electricLantern, rl("block/metal_device/e_lantern.obj"))
				.texture("texture", modLoc("block/metal_device/electric_lantern"))
				.transforms(rl("item/block"));
		obj(MetalDevices.floodlight, rl("block/metal_device/floodlight.obj.ie"))
				.transforms(rl("item/floodlight"));
		getBuilder(Connectors.feedthrough)
				.customLoader(SpecialModelBuilder.forLoader(FeedthroughLoader.LOCATION));
	}

	private TRSRModelBuilder obj(IItemProvider item, ResourceLocation model)
	{
		Preconditions.checkArgument(existingFileHelper.exists(model, ResourcePackType.CLIENT_RESOURCES, "", "models"));
		return getBuilder(item)
				.customLoader(OBJLoaderBuilder::begin)
				.flipV(true)
				.modelLocation(new ResourceLocation(model.getNamespace(), "models/"+model.getPath()))
				.end();
	}

	private IEOBJBuilder<TRSRModelBuilder> ieObjBuilder(IItemProvider item, ResourceLocation model)
	{
		Preconditions.checkArgument(existingFileHelper.exists(model, ResourcePackType.CLIENT_RESOURCES, "", "models"));
		return getBuilder(item)
				.customLoader(IEOBJBuilder::begin)
				.flipV(true)
				.modelLocation(new ResourceLocation(model.getNamespace(), "models/"+model.getPath()));
	}

	private TRSRModelBuilder ieObj(IItemProvider item, ResourceLocation model)
	{
		return ieObjBuilder(item, model).end();
	}

	private TRSRModelBuilder getBuilder(IItemProvider item)
	{
		return getBuilder(name(item));
	}

	private String name(IItemProvider item)
	{
		return item.asItem().getRegistryName().getPath();
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
			cubeAll(name(Metals.ores.get(metal)), rl("block/metal/ore_"+name));
		if(!metal.isVanillaMetal())
		{
			ResourceLocation defaultName = rl("block/metal/storage_"+name);
			if(metal==EnumMetals.URANIUM)
			{
				ResourceLocation side = rl("block/metal/storage_"+name+"_side");
				ResourceLocation top = rl("block/metal/storage_"+name+"_top");
				cubeBottomTop(name(Metals.storage.get(metal)), side, top, top);
			}
			else
				cubeAll(name(Metals.storage.get(metal)), defaultName);
		}
		ResourceLocation sheetmetalName = rl("block/metal/sheetmetal_"+name);
		cubeAll(name(Metals.sheetmetal.get(metal)), sheetmetalName);

	}

	private void addItemModels(String texturePrefix, IItemProvider... items)
	{
		addItemModels(texturePrefix, Arrays.asList(items));
	}

	private void addItemModels(String texturePrefix, ResourceLocation parent, IItemProvider... items)
	{
		addItemModels(texturePrefix, parent, Arrays.asList(items));
	}

	private void addItemModels(String texturePrefix, Collection<? extends IItemProvider> items)
	{
		addItemModels(texturePrefix, mcLoc("item/generated"), items);
	}

	private void addItemModels(String texturePrefix, ResourceLocation parent, Collection<? extends IItemProvider> items)
	{
		for(IItemProvider item : items)
			addItemModel(texturePrefix==null?null: (texturePrefix+item.asItem().getRegistryName().getPath()), item, parent);
	}

	private void addItemModel(String texture, IItemProvider item)
	{
		addItemModel(texture, item, mcLoc("item/generated"));
	}

	private void addItemModel(String texture, IItemProvider item, ResourceLocation parent)
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
}
