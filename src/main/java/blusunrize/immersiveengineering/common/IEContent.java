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
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.IExternalHeatable;
import blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.assembler.FluidStackRecipeQuery;
import blusunrize.immersiveengineering.api.tool.assembler.FluidTagRecipeQuery;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.utils.TemplateWorldCreator;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import blusunrize.immersiveengineering.client.utils.ClocheRenderFunctions;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBeltBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.StaticTemplateManager;
import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.DefaultAssemblerAdapter;
import blusunrize.immersiveengineering.common.crafting.IngredientWithSizeSerializer;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SkyhookUserData;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.ChemthrowerEffects;
import blusunrize.immersiveengineering.common.items.RailgunProjectiles;
import blusunrize.immersiveengineering.common.items.WireCoilItem;
import blusunrize.immersiveengineering.common.register.*;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.ItemRegObject;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IEShaders;
import blusunrize.immersiveengineering.common.util.ServerLevelDuck;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.fakeworld.TemplateWorld;
import blusunrize.immersiveengineering.common.util.loot.IELootFunctions;
import blusunrize.immersiveengineering.common.wires.IEWireTypes;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.OreRetrogenFeature;
import blusunrize.immersiveengineering.mixin.accessors.ConcretePowderBlockAccess;
import blusunrize.immersiveengineering.mixin.accessors.ItemEntityAccess;
import blusunrize.immersiveengineering.mixin.accessors.TemplateAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler.defaultAdapter;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class IEContent
{
	public static final Feature<OreConfiguration> ORE_RETROGEN = new OreRetrogenFeature(OreConfiguration.CODEC);

	public static void modConstruction(Consumer<Runnable> runLater)
	{
		/*BULLETS*/
		BulletItem.initBullets();
		/*WIRES*/
		IEWireTypes.modConstruction();
		/*CONVEYORS*/
		ConveyorHandler.registerMagnetSuppression((entity, iConveyorTile) -> {
			CompoundTag data = entity.getPersistentData();
			if(!data.getBoolean(Lib.MAGNET_PREVENT_NBT))
				data.putBoolean(Lib.MAGNET_PREVENT_NBT, true);
		}, (entity, iConveyorTile) -> {
			entity.getPersistentData().remove(Lib.MAGNET_PREVENT_NBT);
		});
		ConveyorHandler.registerConveyorType(BasicConveyor.TYPE);
		ConveyorHandler.registerConveyorType(RedstoneConveyor.TYPE);
		ConveyorHandler.registerConveyorType(DropConveyor.TYPE);
		ConveyorHandler.registerConveyorType(VerticalConveyor.TYPE);
		ConveyorHandler.registerConveyorType(SplitConveyor.TYPE);
		ConveyorHandler.registerConveyorType(ExtractConveyor.TYPE);
		/*SHADERS*/
		ShaderRegistry.rarityWeightMap.put(Rarity.COMMON, 9);
		ShaderRegistry.rarityWeightMap.put(Rarity.UNCOMMON, 7);
		ShaderRegistry.rarityWeightMap.put(Rarity.RARE, 5);
		ShaderRegistry.rarityWeightMap.put(Rarity.EPIC, 3);
		ShaderRegistry.rarityWeightMap.put(Lib.RARITY_MASTERWORK, 1);

		IEFluids.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		IEPotions.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		IEParticles.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		IEBlockEntities.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		IEEntityTypes.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		IEContainerTypes.REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		IEBlocks.init();
		IEItems.init();

		BulletHandler.emptyCasing = Ingredients.EMPTY_CASING;
		BulletHandler.emptyShell = Ingredients.EMPTY_SHELL;
		IEWireTypes.setup();
		EntityDataSerializers.registerSerializer(IEFluid.OPTIONAL_FLUID_STACK);

		ClocheRenderFunctions.init();

		runLater.accept(IELootFunctions::register);
		IEShaders.commonConstruction();
		IEMultiblocks.init();
		BlueprintCraftingRecipe.registerDefaultCategories();
		populateAPI();
	}

	@SubscribeEvent
	public static void registerCaps(RegisterCapabilitiesEvent ev)
	{
		ev.register(ShaderWrapper.class);
		ev.register(GlobalWireNetwork.class);
		ev.register(SkyhookUserData.class);
		ev.register(RedstoneBundleConnection.class);
		ev.register(IExternalHeatable.class);
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

	public static void init(ParallelDispatchEvent ev)
	{
		IEFluids.fixFluidFields();
		/*WORLDGEN*/
		ev.enqueueWork(
				() -> {
					IEWorldGen.addOreGen(Metals.ORES.get(EnumMetals.COPPER), "copper", IEServerConfig.ORES.ore_copper);
					IEWorldGen.addOreGen(Metals.ORES.get(EnumMetals.ALUMINUM), "bauxite", IEServerConfig.ORES.ore_bauxite);
					IEWorldGen.addOreGen(Metals.ORES.get(EnumMetals.LEAD), "lead", IEServerConfig.ORES.ore_lead);
					IEWorldGen.addOreGen(Metals.ORES.get(EnumMetals.SILVER), "silver", IEServerConfig.ORES.ore_silver);
					IEWorldGen.addOreGen(Metals.ORES.get(EnumMetals.NICKEL), "nickel", IEServerConfig.ORES.ore_nickel);
					IEWorldGen.addOreGen(Metals.ORES.get(EnumMetals.URANIUM), "uranium", IEServerConfig.ORES.ore_uranium);
					IEWorldGen.registerMineralVeinGen();
				}
		);

		ShaderRegistry.itemShader = IEItems.Misc.SHADER.get();
		ShaderRegistry.itemShaderBag = IEItems.Misc.SHADER_BAG;
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.REVOLVER));
		ShaderRegistry.itemExamples.add(new ItemStack(Tools.DRILL));
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.CHEMTHROWER));
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.RAILGUN));
		ShaderRegistry.itemExamples.add(new ItemStack(IEItems.Misc.SHIELD));

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
			final ItemStack[] matching = o.getItems();
			if(!o.isVanilla()||matching.length!=1)
				return null;
			final Item potentialBucket = matching[0].getItem();
			if(!(potentialBucket instanceof BucketItem))
				return null;
			//Explicitly check for vanilla-style non-dynamic container items
			//noinspection deprecation
			if(!potentialBucket.hasCraftingRemainingItem()||potentialBucket.getCraftingRemainingItem()!=Items.BUCKET)
				return null;
			final Fluid contained = ((BucketItem)potentialBucket).getFluid();
			return new FluidStackRecipeQuery(new FluidStack(contained, FluidAttributes.BUCKET_VOLUME));
		});
		// Milk is a weird special case
		AssemblerHandler.registerSpecialIngredientConverter(o -> {
			final ItemStack[] matching = o.getItems();
			if(!o.isVanilla()||matching.length!=1||matching[0].getItem()!=Items.MILK_BUCKET||!ForgeMod.MILK.isPresent())
				return null;
			return new FluidStackRecipeQuery(new FluidStack(ForgeMod.MILK.get(), FluidAttributes.BUCKET_VOLUME));
		});

		DieselHandler.registerFuel(IETags.fluidBiodiesel, 250);
		DieselHandler.registerDrillFuel(IETags.fluidBiodiesel);
		DieselHandler.registerFuel(IETags.fluidCreosote, 20);

		// TODO move to IEFluids/constructors?
		IEFluids.CREOSOTE.getBlock().setEffect(IEPotions.FLAMMABLE.get(), 100, 0);
		IEFluids.ETHANOL.getBlock().setEffect(MobEffects.CONFUSION, 70, 0);
		IEFluids.BIODIESEL.getBlock().setEffect(IEPotions.FLAMMABLE.get(), 100, 1);
		IEFluids.CONCRETE.getBlock().setEffect(MobEffects.MOVEMENT_SLOWDOWN, 20, 3);

		ChemthrowerEffects.register();

		RailgunProjectiles.register();

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
				stack -> IETags.forbiddenInCrates.contains(stack.getItem())||
						Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock
		);

		FluidPipeBlockEntity.initCovers();
		LocalNetworkHandler.register(EnergyTransferHandler.ID, EnergyTransferHandler::new);
		LocalNetworkHandler.register(RedstoneNetworkHandler.ID, RedstoneNetworkHandler::new);
		LocalNetworkHandler.register(WireDamageHandler.ID, WireDamageHandler::new);
	}

	public static void populateAPI()
	{
		SetRestrictedField.startInitializing(false);
		IngredientWithSize.SERIALIZER.setValue(IngredientWithSizeSerializer.INSTANCE);
		BlueprintCraftingRecipe.blueprintItem.setValue(IEItems.Misc.BLUEPRINT);
		ExcavatorHandler.setSetDirtyCallback(IESaveData::markInstanceDirty);
		TemplateMultiblock.setCallbacks(
				Utils::getPickBlock,
				(loc, server) -> {
					try
					{
						return StaticTemplateManager.loadStaticTemplate(loc, server);
					} catch(IOException e)
					{
						throw new RuntimeException(e);
					}
				},
				template -> ((TemplateAccess)template).getPalettes()
		);
		defaultAdapter = new DefaultAssemblerAdapter();
		WirecoilUtils.COIL_USE.setValue(WireCoilItem::doCoilUse);
		AssemblerHandler.registerRecipeAdapter(Recipe.class, defaultAdapter);
		BulletHandler.GET_BULLET_ITEM.setValue(b -> {
			ItemRegObject<BulletItem> regObject = Weapons.BULLETS.get(b);
			if(regObject!=null)
				return regObject.asItem();
			else
				return null;
		});
		ChemthrowerHandler.SOLIDIFY_CONCRETE_POWDER.setValue(
				(world, pos) -> {
					Block b = world.getBlockState(pos).getBlock();
					if(b instanceof ConcretePowderBlock)
						world.setBlock(pos, ((ConcretePowderBlockAccess)b).getConcrete(), 3);
				}
		);
		WireDamageHandler.GET_WIRE_DAMAGE.setValue(IEDamageSources::causeWireDamage);
		GlobalWireNetwork.SANITIZE_CONNECTIONS.setValue(IEServerConfig.WIRES.sanitizeConnections::get);
		GlobalWireNetwork.VALIDATE_CONNECTIONS.setValue(IECommonConfig.validateNet::get);
		ConveyorHandler.ITEM_AGE_ACCESS.setValue((entity, newAge) -> ((ItemEntityAccess)entity).setAge(newAge));
		TemplateWorldCreator.CREATOR.setValue(TemplateWorld::new);
		ConveyorHandler.CONVEYOR_BLOCKS.setValue(rl -> MetalDevices.CONVEYORS.get(rl).get());
		ConveyorHandler.BLOCK_ENTITY_TYPES.setValue(rl -> ConveyorBeltBlockEntity.BE_TYPES.get(rl).get());
		ApiUtils.IS_UNLOADING_BLOCK_ENTITIES.setValue(
				l -> l instanceof ServerLevelDuck duck&&duck.immersiveengineering$isUnloadingBlockEntities()
		);
		SetRestrictedField.lock(false);
	}
}
