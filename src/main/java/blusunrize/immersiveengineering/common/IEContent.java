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
import blusunrize.immersiveengineering.common.items.IEItems.ItemRegObject;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
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
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler.defaultAdapter;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class IEContent
{
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
		IEPotions.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		IEBlocks.init();
		IEItems.init();

		ImmersiveEngineering.proxy.registerContainersAndScreens();

		BulletHandler.emptyCasing = Ingredients.emptyCasing;
		BulletHandler.emptyShell = Ingredients.emptyShell;
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

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IEItems.Misc.registerShaderBags();
	}

	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event)
	{
		event.getRegistry().register(ORE_RETROGEN.setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, "ore_retro")));
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
		ShaderRegistry.itemShader = IEItems.Misc.shader.get();
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
		IEFluids.fluidCreosote.getBlock().setEffect(IEPotions.flammable.get(), 100, 0);
		IEFluids.fluidEthanol.getBlock().setEffect(Effects.NAUSEA, 70, 0);
		IEFluids.fluidBiodiesel.getBlock().setEffect(IEPotions.flammable.get(), 100, 1);
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
		BulletHandler.GET_BULLET_ITEM.setValue(b -> {
			ItemRegObject<BulletItem> regObject = Weapons.bullets.get(b);
			if(regObject!=null)
				return regObject.asItem();
			else
				return null;
		});
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
