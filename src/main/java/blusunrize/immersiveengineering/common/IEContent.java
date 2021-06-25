/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ItemAgeAccessor;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.assembler.FluidStackRecipeQuery;
import blusunrize.immersiveengineering.api.tool.assembler.FluidTagRecipeQuery;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.utils.TemplateWorldCreator;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import blusunrize.immersiveengineering.client.utils.ClocheRenderFunctions;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Misc;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBeltTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.StaticTemplateManager;
import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.DefaultAssemblerAdapter;
import blusunrize.immersiveengineering.common.crafting.IngredientWithSizeSerializer;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import blusunrize.immersiveengineering.common.fluids.IEFluids;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Molds;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.fakeworld.TemplateWorld;
import blusunrize.immersiveengineering.common.util.loot.IELootFunctions;
import blusunrize.immersiveengineering.common.wires.CapabilityInit;
import blusunrize.immersiveengineering.common.wires.IEWireTypes;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.OreRetrogenFeature;
import blusunrize.immersiveengineering.mixin.accessors.ConcretePowderBlockAccess;
import blusunrize.immersiveengineering.mixin.accessors.ItemEntityAccess;
import blusunrize.immersiveengineering.mixin.accessors.TemplateAccess;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler.defaultAdapter;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class IEContent
{
	public static List<Item> registeredIEItems = new ArrayList<>();

	public static final Feature<OreFeatureConfig> ORE_RETROGEN = new OreRetrogenFeature(OreFeatureConfig.CODEC);

	public static void modConstruction()
	{
		/*BULLETS*/
		BulletItem.initBullets();
		/*WIRES*/
		IEWireTypes.modConstruction();
		/*CONVEYORS*/
		ConveyorHandler.registerMagnetSuppression((entity, iConveyorTile) -> {
			CompoundNBT data = entity.getPersistentData();
			if(!data.getBoolean(Lib.MAGNET_PREVENT_NBT))
				data.putBoolean(Lib.MAGNET_PREVENT_NBT, true);
		}, (entity, iConveyorTile) -> {
			entity.getPersistentData().remove(Lib.MAGNET_PREVENT_NBT);
		});
		ConveyorHandler.registerConveyorHandler(BasicConveyor.NAME, BasicConveyor.class, BasicConveyor::new);
		ConveyorHandler.registerConveyorHandler(RedstoneConveyor.NAME, RedstoneConveyor.class, RedstoneConveyor::new);
		ConveyorHandler.registerConveyorHandler(DropConveyor.NAME, DropConveyor.class, DropConveyor::new);
		ConveyorHandler.registerConveyorHandler(VerticalConveyor.NAME, VerticalConveyor.class, VerticalConveyor::new);
		ConveyorHandler.registerConveyorHandler(SplitConveyor.NAME, SplitConveyor.class, SplitConveyor::new);
		ConveyorHandler.registerConveyorHandler(ExtractConveyor.NAME, ExtractConveyor.class, ExtractConveyor::new);
		ConveyorHandler.registerConveyorHandler(CoveredConveyor.NAME, CoveredConveyor.class, CoveredConveyor::new);
		ConveyorHandler.registerConveyorHandler(DropCoveredConveyor.NAME, DropCoveredConveyor.class, DropCoveredConveyor::new);
		ConveyorHandler.registerConveyorHandler(VerticalCoveredConveyor.NAME, VerticalCoveredConveyor.class, VerticalCoveredConveyor::new);
		ConveyorHandler.registerConveyorHandler(ExtractCoveredConveyor.NAME, ExtractCoveredConveyor.class, ExtractCoveredConveyor::new);
		ConveyorHandler.registerConveyorHandler(SplitCoveredConveyor.NAME, SplitCoveredConveyor.class, SplitCoveredConveyor::new);
		ConveyorHandler.registerSubstitute(new ResourceLocation(MODID, "conveyor"), new ResourceLocation(MODID, "uncontrolled"));
		/*SHADERS*/
		ShaderRegistry.rarityWeightMap.put(Rarity.COMMON, 9);
		ShaderRegistry.rarityWeightMap.put(Rarity.UNCOMMON, 7);
		ShaderRegistry.rarityWeightMap.put(Rarity.RARE, 5);
		ShaderRegistry.rarityWeightMap.put(Rarity.EPIC, 3);
		ShaderRegistry.rarityWeightMap.put(Lib.RARITY_MASTERWORK, 1);

		IEFluids.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		IEBlocks.init();
		IEItems.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());

		ImmersiveEngineering.proxy.registerContainersAndScreens();


		for(EnumMetals m : EnumMetals.values())
		{
			String name = m.tagName();
			Item nugget;
			Item ingot;
			Item plate = new IEBaseItem("plate_"+name);
			Item dust = new IEBaseItem("dust_"+name);
			if(!m.isVanillaMetal())
			{
				nugget = new IEBaseItem("nugget_"+name);
				ingot = new IEBaseItem("ingot_"+name);
			}
			else if(m==EnumMetals.IRON)
			{
				nugget = Items.IRON_NUGGET;
				ingot = Items.IRON_INGOT;
			}
			else if(m==EnumMetals.GOLD)
			{
				nugget = Items.GOLD_NUGGET;
				ingot = Items.GOLD_INGOT;
			}
			else
				throw new RuntimeException("Unkown vanilla metal: "+m.name());
			IEItems.Metals.plates.put(m, plate);
			IEItems.Metals.nuggets.put(m, nugget);
			IEItems.Metals.ingots.put(m, ingot);
			IEItems.Metals.dusts.put(m, dust);
		}

		Tools.hammer = new HammerItem();
		Tools.wirecutter = new WirecutterItem();
		Tools.screwdriver = new ScrewdriverItem();
		Tools.voltmeter = new VoltmeterItem();
		Tools.manual = new ManualItem();
		IEItems.Tools.steelPick = IETools.createPickaxe(Lib.MATERIAL_Steel, "pickaxe_steel");
		IEItems.Tools.steelShovel = IETools.createShovel(Lib.MATERIAL_Steel, "shovel_steel");
		IEItems.Tools.steelAxe = IETools.createAxe(Lib.MATERIAL_Steel, "axe_steel");
		IEItems.Tools.steelHoe = IETools.createHoe(Lib.MATERIAL_Steel, "hoe_steel");
		IEItems.Tools.steelSword = IETools.createSword(Lib.MATERIAL_Steel, "sword_steel");
		for(EquipmentSlotType slot : EquipmentSlotType.values())
			if(slot.getSlotType()==Group.ARMOR)
				IEItems.Tools.steelArmor.put(slot, new SteelArmorItem(slot));
		Tools.toolbox = new ToolboxItem();
		IEItems.Misc.hempSeeds = new IESeedItem(Misc.hempPlant);
		IEItems.Ingredients.stickTreated = new IEBaseItem("stick_treated");
		IEItems.Ingredients.stickIron = new IEBaseItem("stick_iron");
		IEItems.Ingredients.stickSteel = new IEBaseItem("stick_steel");
		IEItems.Ingredients.stickAluminum = new IEBaseItem("stick_aluminum");
		IEItems.Ingredients.hempFiber = new IEBaseItem("hemp_fiber");
		IEItems.Ingredients.hempFabric = new IEBaseItem("hemp_fabric");
		IEItems.Ingredients.coalCoke = new IEBaseItem("coal_coke")
				.setBurnTime(3200);
		IEItems.Ingredients.slag = new IEBaseItem("slag");
		IEItems.Ingredients.componentIron = new IEBaseItem("component_iron");
		IEItems.Ingredients.componentSteel = new IEBaseItem("component_steel");
		IEItems.Ingredients.waterwheelSegment = new IEBaseItem("waterwheel_segment");
		IEItems.Ingredients.windmillBlade = new IEBaseItem("windmill_blade");
		IEItems.Ingredients.windmillSail = new IEBaseItem("windmill_sail");
		IEItems.Ingredients.woodenGrip = new IEBaseItem("wooden_grip");
		IEItems.Ingredients.gunpartBarrel = new RevolverpartItem("gunpart_barrel");
		IEItems.Ingredients.gunpartDrum = new RevolverpartItem("gunpart_drum");
		IEItems.Ingredients.gunpartHammer = new RevolverpartItem("gunpart_hammer");
		IEItems.Ingredients.dustCoke = new IEBaseItem("dust_coke");
		IEItems.Ingredients.dustHopGraphite = new IEBaseItem("dust_hop_graphite");
		IEItems.Ingredients.ingotHopGraphite = new IEBaseItem("ingot_hop_graphite");
		IEItems.Ingredients.wireCopper = new IEBaseItem("wire_copper");
		IEItems.Ingredients.wireElectrum = new IEBaseItem("wire_electrum");
		IEItems.Ingredients.wireAluminum = new IEBaseItem("wire_aluminum");
		IEItems.Ingredients.wireSteel = new IEBaseItem("wire_steel");
		IEItems.Ingredients.wireLead = new IEBaseItem("wire_lead");
		IEItems.Ingredients.dustSaltpeter = new IEBaseItem("dust_saltpeter");
		IEItems.Ingredients.dustSulfur = new IEBaseItem("dust_sulfur");
		IEItems.Ingredients.dustWood = new IEBaseItem("dust_wood")
				.setBurnTime(100);
		IEItems.Ingredients.electronTube = new IEBaseItem("electron_tube");
		IEItems.Ingredients.circuitBoard = new IEBaseItem("circuit_board");
		IEItems.Ingredients.emptyCasing = new IEBaseItem("empty_casing");
		IEItems.Ingredients.emptyShell = new IEBaseItem("empty_shell");
		for(WireType t : WireType.getIEWireTypes())
			IEItems.Misc.wireCoils.put(t, new WireCoilItem(t));
		Item.Properties moldProperties = new Item.Properties().maxStackSize(1);
		Molds.moldPlate = new IEBaseItem("mold_plate", moldProperties);
		Molds.moldGear = new IEBaseItem("mold_gear", moldProperties);
		Molds.moldRod = new IEBaseItem("mold_rod", moldProperties);
		Molds.moldBulletCasing = new IEBaseItem("mold_bullet_casing", moldProperties);
		Molds.moldWire = new IEBaseItem("mold_wire", moldProperties);
		Molds.moldPacking4 = new IEBaseItem("mold_packing_4", moldProperties);
		Molds.moldPacking9 = new IEBaseItem("mold_packing_9", moldProperties);
		Molds.moldUnpacking = new IEBaseItem("mold_unpacking", moldProperties);
		IEItems.Misc.graphiteElectrode = new GraphiteElectrodeItem();
		IEItems.Misc.coresample = new CoresampleItem();
		Tools.drill = new DrillItem();
		Tools.drillheadIron = new DrillheadItem(DrillheadItem.IRON);
		Tools.drillheadSteel = new DrillheadItem(DrillheadItem.STEEL);
		Tools.buzzsaw = new BuzzsawItem();
		Tools.sawblade = new SawbladeItem("sawblade", 10000, 8f, 9f);
		Tools.rockcutter = new RockcutterItem("rockcutter", 5000, 5f, 9f);
		Tools.surveyTools = new SurveyToolsItem();
		Weapons.revolver = new RevolverItem();
		Weapons.speedloader = new SpeedloaderItem();
		Weapons.chemthrower = new ChemthrowerItem();
		Weapons.railgun = new RailgunItem();
		for(ResourceLocation bulletType : BulletHandler.getAllKeys())
		{
			IBullet bullet = BulletHandler.getBullet(bulletType);
			if(bullet.isProperCartridge())
				Weapons.bullets.put(bullet, new BulletItem(bullet));
		}
		IEItems.Misc.powerpack = new PowerpackItem();
		for(ToolUpgrade upgrade : ToolUpgrade.values())
			IEItems.Misc.toolUpgrades.put(upgrade, new ToolUpgradeItem(upgrade));
		IEItems.Misc.jerrycan = new JerrycanItem();
		IEItems.Misc.shader = new ShaderItem();
		IEItems.Misc.blueprint = new EngineersBlueprintItem();
		IEItems.Misc.earmuffs = new EarmuffsItem();
		for(EquipmentSlotType slot : EquipmentSlotType.values())
			if(slot.getSlotType()==Group.ARMOR)
				IEItems.Misc.faradaySuit.put(slot, new FaradaySuitItem(slot));
		IEItems.Misc.fluorescentTube = new FluorescentTubeItem();
		IEItems.Misc.shield = new IEShieldItem();
		IEItems.Misc.skyhook = new SkyhookItem();
		IEItems.Misc.maintenanceKit = new MaintenanceKitItem();
		IEItems.Misc.cartWoodenCrate = new IEMinecartItem("woodencrate")
		{
			@Override
			public IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack)
			{
				return new CrateMinecartEntity(CrateMinecartEntity.TYPE, world, x, y, z);
			}
		};
		IEItems.Misc.cartReinforcedCrate = new IEMinecartItem("reinforcedcrate")
		{
			@Override
			public IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack)
			{
				return new ReinforcedCrateMinecartEntity(ReinforcedCrateMinecartEntity.TYPE, world, x, y, z);
			}
		};
		IEItems.Misc.cartWoodenBarrel = new IEMinecartItem("woodenbarrel")
		{
			@Override
			public IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack)
			{
				return new BarrelMinecartEntity(BarrelMinecartEntity.TYPE, world, x, y, z);
			}
		};
		IEItems.Misc.cartMetalBarrel = new IEMinecartItem("metalbarrel")
		{
			@Override
			public IEMinecartEntity createCart(World world, double x, double y, double z, ItemStack stack)
			{
				return new MetalBarrelMinecartEntity(MetalBarrelMinecartEntity.TYPE, world, x, y, z);
			}
		};
		IEItems.Misc.logicCircuitBoard = new LogicCircuitBoardItem();

		IEItems.Misc.bannerPatternHammer = addBanner("hammer", "hmr");
		IEItems.Misc.bannerPatternBevels = addBanner("bevels", "bvl");
		IEItems.Misc.bannerPatternOrnate = addBanner("ornate", "orn");
		IEItems.Misc.bannerPatternTreatedWood = addBanner("treated_wood", "twd");
		IEItems.Misc.bannerPatternWindmill = addBanner("windmill", "wnd");
		IEItems.Misc.bannerPatternWolfR = addBanner("wolf_r", "wlfr");
		IEItems.Misc.bannerPatternWolfL = addBanner("wolf_l", "wlfl");
		IEItems.Misc.bannerPatternWolf = addBanner("wolf", "wlf");

		IEItems.Misc.iconBirthday = new FakeIconItem("birthday");
		IEItems.Misc.iconLucky = new FakeIconItem("lucky");
		IEItems.Misc.iconDrillbreak = new FakeIconItem("drillbreak");
		IEItems.Misc.iconRavenholm = new FakeIconItem("ravenholm");

		BulletHandler.emptyCasing = new ItemStack(Ingredients.emptyCasing);
		BulletHandler.emptyShell = new ItemStack(Ingredients.emptyShell);
		IEWireTypes.setup();
		DataSerializers.registerSerializer(IEFluid.OPTIONAL_FLUID_STACK);

		ClocheRenderFunctions.init();

		DeferredWorkQueue.runLater(IELootFunctions::register);
		IEShaders.commonConstruction();
		IEMultiblocks.init();
		BlueprintCraftingRecipe.registerDefaultCategories();
		IETileTypes.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		populateAPI();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		for(Rarity r : ShaderRegistry.rarityWeightMap.keySet())
			IEItems.Misc.shaderBag.put(r, new ShaderBagItem(r));
		checkNonNullNames(registeredIEItems);
		for(Item item : registeredIEItems)
			event.getRegistry().register(item);
	}

	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event)
	{
		event.getRegistry().register(ORE_RETROGEN.setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, "ore_retro")));
	}

	private static <T extends IForgeRegistryEntry<T>> void checkNonNullNames(Collection<T> coll)
	{
		int numNull = 0;
		for(T b : coll)
			if(b.getRegistryName()==null)
			{
				IELogger.logger.info("Null name for {} (class {})", b, b.getClass());
				++numNull;
			}
		if(numNull > 0)
			System.exit(1);
	}

	@SubscribeEvent
	public static void registerPotions(RegistryEvent.Register<Effect> event)
	{
		/*POTIONS*/
		IEPotions.init();
	}

	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event)
	{
		event.getRegistry().registerAll(
				ChemthrowerShotEntity.TYPE,
				FluorescentTubeEntity.TYPE,
				IEExplosiveEntity.TYPE,
				RailgunShotEntity.TYPE,
				RevolvershotEntity.TYPE,
				RevolvershotFlareEntity.TYPE,
				RevolvershotHomingEntity.TYPE,
				SkylineHookEntity.TYPE,
				WolfpackShotEntity.TYPE,
				CrateMinecartEntity.TYPE,
				ReinforcedCrateMinecartEntity.TYPE,
				BarrelMinecartEntity.TYPE,
				MetalBarrelMinecartEntity.TYPE,
				SawbladeEntity.TYPE
		);
	}

	@SubscribeEvent
	public static void registerTEs(RegistryEvent.Register<TileEntityType<?>> event)
	{
		ConveyorBeltTileEntity.registerConveyorTEs(event);
	}

	public static void init(ParallelDispatchEvent ev)
	{
		/*WORLDGEN*/
		ev.enqueueWork(
				() -> {
					IEWorldGen.addOreGen(Metals.ores.get(EnumMetals.COPPER), "copper", IEServerConfig.ORES.ore_copper);
					IEWorldGen.addOreGen(Metals.ores.get(EnumMetals.ALUMINUM), "bauxite", IEServerConfig.ORES.ore_bauxite);
					IEWorldGen.addOreGen(Metals.ores.get(EnumMetals.LEAD), "lead", IEServerConfig.ORES.ore_lead);
					IEWorldGen.addOreGen(Metals.ores.get(EnumMetals.SILVER), "silver", IEServerConfig.ORES.ore_silver);
					IEWorldGen.addOreGen(Metals.ores.get(EnumMetals.NICKEL), "nickel", IEServerConfig.ORES.ore_nickel);
					IEWorldGen.addOreGen(Metals.ores.get(EnumMetals.URANIUM), "uranium", IEServerConfig.ORES.ore_uranium);
					IEWorldGen.registerMineralVeinGen();
				}
		);

		CapabilityShader.register();
		CapabilityInit.register();
		CapabilitySkyhookData.register();
		CapabilityRedstoneNetwork.register();
		ShaderRegistry.itemShader = IEItems.Misc.shader;
		ShaderRegistry.itemShaderBag = IEItems.Misc.shaderBag;
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.revolver));
		ShaderRegistry.itemExamples.add(new ItemStack(Tools.drill));
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.chemthrower));
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.railgun));
		ShaderRegistry.itemExamples.add(new ItemStack(IEItems.Misc.shield));

		/*ASSEMBLER RECIPE ADAPTERS*/
		//Fluid Ingredients
		AssemblerHandler.registerSpecialIngredientConverter((o) ->
		{
			if(o instanceof IngredientFluidStack)
				return new FluidTagRecipeQuery(((IngredientFluidStack)o).getFluidTagInput());
			else
				return null;
		});
		// Buckets
		// TODO add "duplicates" of the fluid-aware recipes that only use buckets, so that other mods using similar
		//  code don't need explicit compat?
		AssemblerHandler.registerSpecialIngredientConverter((o) ->
		{
			final ItemStack[] matching = o.getMatchingStacks();
			if(!o.isVanilla()||matching.length!=1)
				return null;
			final Item potentialBucket = matching[0].getItem();
			if(!(potentialBucket instanceof BucketItem))
				return null;
			//Explicitly check for vanilla-style non-dynamic container items
			//noinspection deprecation
			if(!potentialBucket.hasContainerItem()||potentialBucket.getContainerItem()!=Items.BUCKET)
				return null;
			final Fluid contained = ((BucketItem)potentialBucket).getFluid();
			return new FluidStackRecipeQuery(new FluidStack(contained, FluidAttributes.BUCKET_VOLUME));
		});
		// Milk is a weird special case
		AssemblerHandler.registerSpecialIngredientConverter(o -> {
			final ItemStack[] matching = o.getMatchingStacks();
			if(!o.isVanilla()||matching.length!=1||matching[0].getItem()!=Items.MILK_BUCKET||!ForgeMod.MILK.isPresent())
				return null;
			return new FluidStackRecipeQuery(new FluidStack(ForgeMod.MILK.get(), FluidAttributes.BUCKET_VOLUME));
		});

		DieselHandler.registerFuel(IETags.fluidBiodiesel, 250);
		DieselHandler.registerDrillFuel(IETags.fluidBiodiesel);
		DieselHandler.registerFuel(IETags.fluidCreosote, 20);

		// TODO move to IEFluids/constructors?
		IEFluids.fluidCreosote.getBlock().setEffect(IEPotions.flammable, 100, 0);
		IEFluids.fluidEthanol.getBlock().setEffect(Effects.NAUSEA, 70, 0);
		IEFluids.fluidBiodiesel.getBlock().setEffect(IEPotions.flammable, 100, 1);
		IEFluids.fluidConcrete.getBlock().setEffect(Effects.SLOWNESS, 20, 3);

		ChemthrowerEffects.register();

		RailgunProjectiles.register();

		ExternalHeaterHandler.registerHeatableAdapter(FurnaceTileEntity.class, new DefaultFurnaceAdapter());

		ThermoelectricHandler.registerSourceInKelvin(Blocks.MAGMA_BLOCK, 1300);
		//TODO tags?
		ThermoelectricHandler.registerSourceInKelvin(Blocks.ICE, 273);
		ThermoelectricHandler.registerSourceInKelvin(Blocks.PACKED_ICE, 240);
		ThermoelectricHandler.registerSourceInKelvin(Blocks.BLUE_ICE, 200);
		ThermoelectricHandler.registerSourceInKelvin(IETags.getTagsFor(EnumMetals.URANIUM).storage, 2000);
		//ThermoelectricHandler.registerSourceInKelvin(new ResourceLocation("forge:storage_blocks/yellorium"), 2000);
		//ThermoelectricHandler.registerSourceInKelvin(new ResourceLocation("forge:storage_blocks/plutonium"), 4000);
		//ThermoelectricHandler.registerSourceInKelvin(new ResourceLocation("forge:storage_blocks/blutonium"), 4000);

		/*MULTIBLOCKS*/
		MultiblockHandler.registerMultiblock(IEMultiblocks.FEEDTHROUGH);
		MultiblockHandler.registerMultiblock(IEMultiblocks.LIGHTNING_ROD);
		MultiblockHandler.registerMultiblock(IEMultiblocks.DIESEL_GENERATOR);
		MultiblockHandler.registerMultiblock(IEMultiblocks.REFINERY);
		MultiblockHandler.registerMultiblock(IEMultiblocks.MIXER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.SQUEEZER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.FERMENTER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.BOTTLING_MACHINE);
		MultiblockHandler.registerMultiblock(IEMultiblocks.COKE_OVEN);
		MultiblockHandler.registerMultiblock(IEMultiblocks.ALLOY_SMELTER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.BLAST_FURNACE);
		MultiblockHandler.registerMultiblock(IEMultiblocks.CRUSHER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.SAWMILL);
		MultiblockHandler.registerMultiblock(IEMultiblocks.ADVANCED_BLAST_FURNACE);
		MultiblockHandler.registerMultiblock(IEMultiblocks.METAL_PRESS);
		MultiblockHandler.registerMultiblock(IEMultiblocks.ASSEMBLER);
		MultiblockHandler.registerMultiblock(IEMultiblocks.AUTO_WORKBENCH);
		MultiblockHandler.registerMultiblock(IEMultiblocks.EXCAVATOR);
		MultiblockHandler.registerMultiblock(IEMultiblocks.BUCKET_WHEEL);
		MultiblockHandler.registerMultiblock(IEMultiblocks.ARC_FURNACE);
		MultiblockHandler.registerMultiblock(IEMultiblocks.SILO);
		MultiblockHandler.registerMultiblock(IEMultiblocks.SHEETMETAL_TANK);
		MultiblockHandler.registerMultiblock(IEMultiblocks.EXCAVATOR_DEMO);

		/*BLOCK ITEMS FROM CRATES*/
		IEApi.forbiddenInCrates.add(
				stack -> stack.getItem().isIn(IETags.forbiddenInCrates)||
						Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock
		);

		FluidPipeTileEntity.initCovers();
		LocalNetworkHandler.register(EnergyTransferHandler.ID, EnergyTransferHandler::new);
		LocalNetworkHandler.register(RedstoneNetworkHandler.ID, RedstoneNetworkHandler::new);
		LocalNetworkHandler.register(WireDamageHandler.ID, WireDamageHandler::new);
	}

	public static Item addBanner(String name, String id)
	{
		String enumName = MODID+"_"+name;
		id = "ie_"+id;
		BannerPattern pattern = BannerPattern.create(enumName.toUpperCase(), enumName, id, true);
		Item patternItem = new BannerPatternItem(pattern, new Item.Properties().group(ImmersiveEngineering.ITEM_GROUP));
		patternItem.setRegistryName(ImmersiveEngineering.MODID, "bannerpattern_"+name);
		IEContent.registeredIEItems.add(patternItem);
		return patternItem;
	}

	public static void populateAPI()
	{
		SetRestrictedField.startInitializing(false);
		ApiUtils.disableTicking.setValue(EventHandler.REMOVE_FROM_TICKING::add);
		IngredientWithSize.SERIALIZER.setValue(IngredientWithSizeSerializer.INSTANCE);
		BlueprintCraftingRecipe.blueprintItem.setValue(IEItems.Misc.blueprint);
		ExcavatorHandler.setSetDirtyCallback(IESaveData::markInstanceDirty);
		TemplateMultiblock.setCallbacks(
				bs -> Utils.getPickBlock(
						bs, new BlockRayTraceResult(Vector3d.ZERO, Direction.DOWN, BlockPos.ZERO, false),
						ImmersiveEngineering.proxy.getClientPlayer()
				),
				(loc, server) -> {
					try
					{
						return StaticTemplateManager.loadStaticTemplate(loc, server);
					} catch(IOException e)
					{
						throw new RuntimeException(e);
					}
				},
				template -> ((TemplateAccess)template).getBlocks()
		);
		defaultAdapter = new DefaultAssemblerAdapter();
		WirecoilUtils.COIL_USE.setValue(WireCoilItem::doCoilUse);
		AssemblerHandler.registerRecipeAdapter(IRecipe.class, defaultAdapter);
		BulletHandler.GET_BULLET_ITEM.setValue(Weapons.bullets::get);
		ChemthrowerHandler.SOLIDIFY_CONCRETE_POWDER.setValue(
				(world, pos) -> {
					Block b = world.getBlockState(pos).getBlock();
					if(b instanceof ConcretePowderBlock)
						world.setBlockState(pos, ((ConcretePowderBlockAccess)b).getSolidifiedState(), 3);
				}
		);
		WireDamageHandler.GET_WIRE_DAMAGE.setValue(IEDamageSources::causeWireDamage);
		GlobalWireNetwork.SANITIZE_CONNECTIONS.setValue(IEServerConfig.WIRES.sanitizeConnections::get);
		GlobalWireNetwork.VALIDATE_CONNECTIONS.setValue(IECommonConfig.validateNet::get);
		ConveyorHandler.ITEM_AGE_ACCESS.setValue(new ItemAgeAccessor()
		{
			@Override
			public int getAgeNonsided(ItemEntity entity)
			{
				return ((ItemEntityAccess)entity).getAgeNonsided();
			}

			@Override
			public void setAge(ItemEntity entity, int newAge)
			{
				((ItemEntityAccess)entity).setAge(newAge);
			}
		});
		TemplateWorldCreator.CREATOR.setValue(TemplateWorld::new);
		ConveyorHandler.conveyorBlocks.setValue(rl -> MetalDevices.CONVEYORS.get(rl).get());
		SetRestrictedField.lock(false);
	}
}
