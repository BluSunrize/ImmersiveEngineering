/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.IExternalHeatable;
import blusunrize.immersiveengineering.api.tool.ShieldDisablingHandler;
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
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperDummy;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperMaster;
import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.DefaultAssemblerAdapter;
import blusunrize.immersiveengineering.common.crafting.IngredientWithSizeSerializer;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SkyhookUserData;
import blusunrize.immersiveengineering.common.entities.illager.Bulwark;
import blusunrize.immersiveengineering.common.entities.illager.Commando;
import blusunrize.immersiveengineering.common.entities.illager.EngineerIllager;
import blusunrize.immersiveengineering.common.entities.illager.Fusilier;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.register.*;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.Misc;
import blusunrize.immersiveengineering.common.register.IEFluids.FluidEntry;
import blusunrize.immersiveengineering.common.register.IEItems.*;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IEShaders;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.commands.IEArgumentTypes;
import blusunrize.immersiveengineering.common.util.fakeworld.TemplateWorld;
import blusunrize.immersiveengineering.common.util.loot.GrassDropModifier;
import blusunrize.immersiveengineering.common.util.loot.IELootFunctions;
import blusunrize.immersiveengineering.common.wires.IEWireTypes;
import blusunrize.immersiveengineering.common.world.Villages;
import blusunrize.immersiveengineering.mixin.accessors.ConcretePowderBlockAccess;
import blusunrize.immersiveengineering.mixin.accessors.ItemEntityAccess;
import blusunrize.immersiveengineering.mixin.accessors.TemplateAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler.defaultAdapter;
import static blusunrize.immersiveengineering.common.fluids.IEFluid.BUCKET_DISPENSE_BEHAVIOR;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class IEContent
{
	private static CompletableFuture<?> lastOnThreadFuture;

	public static void modConstruction()
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

		final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEFluids.REGISTER.register(modBus);
		IEFluids.TYPE_REGISTER.register(modBus);
		IEPotions.REGISTER.register(modBus);
		IEParticles.REGISTER.register(modBus);
		IEBlockEntities.REGISTER.register(modBus);
		IEEntityTypes.REGISTER.register(modBus);
		IEMenuTypes.REGISTER.register(modBus);
		IECreativeTabs.REGISTER.register(modBus);
		IEEntityDataSerializers.REGISTER.register(modBus);
		IEStats.modConstruction();
		IEItems.init();
		IESounds.init();
		IEBlocks.init();
		GrassDropModifier.init();
		IERecipeTypes.init();
		IELootFunctions.init();
		IEArgumentTypes.init();
		IEBannerPatterns.init();

		BulletHandler.emptyCasing = Ingredients.EMPTY_CASING;
		BulletHandler.emptyShell = Ingredients.EMPTY_SHELL;

		ClocheRenderFunctions.init();

		IEShaders.commonConstruction();
		IEMultiblocks.init();
		IEMultiblockLogic.init(modBus);
		populateAPI();
	}

	@SubscribeEvent
	public static void registerCaps(EntityAttributeCreationEvent ev)
	{
		ev.put(IEEntityTypes.FUSILIER.get(), Fusilier.createAttributes().build());
		ev.put(IEEntityTypes.COMMANDO.get(), Commando.createAttributes().build());
		ev.put(IEEntityTypes.BULWARK.get(), Bulwark.createAttributes().build());
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
	public static void addMissingRegistrations(NewRegistryEvent event)
	{
		IEItems.Misc.registerShaderBags();
	}

	public static void commonSetup(ParallelDispatchEvent ev)
	{
		IEWireTypes.setup();
		IEStats.setup();

		ShaderRegistry.itemShader = IEItems.Misc.SHADER.get();
		ShaderRegistry.itemShaderBag = IEItems.Misc.SHADER_BAG;
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.REVOLVER));
		ShaderRegistry.itemExamples.add(new ItemStack(Tools.DRILL));
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.CHEMTHROWER));
		ShaderRegistry.itemExamples.add(new ItemStack(Weapons.RAILGUN));
		ShaderRegistry.itemExamples.add(new ItemStack(IEItems.Misc.SHIELD));

		/*ASSEMBLER RECIPE ADAPTERS*/
		//Fluid Ingredients
		AssemblerHandler.registerSpecialIngredientConverter((o, remain) -> {
			if(o instanceof IngredientFluidStack)
				return new FluidTagRecipeQuery(((IngredientFluidStack)o).getFluidTagInput());
			else
				return null;
		});
		// Buckets
		// TODO add "duplicates" of the fluid-aware recipes that only use buckets, so that other mods using similar
		//  code don't need explicit compat?
		AssemblerHandler.registerSpecialIngredientConverter((o, remain) -> {
			// Must be a vanilla ingredient, which returns an empty bucket
			if(!o.isVanilla()||remain.getItem()!=Items.BUCKET)
				return null;
			// Find bucket out of available items
			Optional<ItemStack> potentialBucket = Arrays.stream(o.getItems())
					.filter(stack -> stack.getItem() instanceof BucketItem)
					.findFirst();
			if(potentialBucket.isEmpty())
				return null;
			final Item bucketItem = potentialBucket.get().getItem();
			//Explicitly check for vanilla-style non-dynamic container items
			//noinspection deprecation
			if(!bucketItem.hasCraftingRemainingItem()||bucketItem.getCraftingRemainingItem()!=Items.BUCKET)
				return null;
			final Fluid contained = ((BucketItem)bucketItem).getFluid();
			return new FluidStackRecipeQuery(new FluidStack(contained, FluidType.BUCKET_VOLUME));
		});
		// Milk is a weird special case
		AssemblerHandler.registerSpecialIngredientConverter((o, remain) -> {
			// Only works when the milk fluid is enabled
			if(!ForgeMod.MILK.isPresent())
				return null;
			// Must be a vanilla ingredient, which returns an empty bucket
			if(!o.isVanilla()||remain.getItem()!=Items.BUCKET)
				return null;
			// Find milk bucket out of available items
			Optional<ItemStack> potentialBucket = Arrays.stream(o.getItems())
					.filter(stack -> stack.getItem()==Items.MILK_BUCKET)
					.findFirst();
			if(potentialBucket.isEmpty())
				return null;
			return new FluidStackRecipeQuery(new FluidStack(ForgeMod.MILK.get(), FluidType.BUCKET_VOLUME));
		});

		// TODO move to IEFluids/constructors?
		IEFluids.CREOSOTE.getBlock().setEffect(IEPotions.FLAMMABLE.get(), 100, 0);
		IEFluids.ETHANOL.getBlock().setEffect(MobEffects.CONFUSION, 70, 0);
		IEFluids.BIODIESEL.getBlock().setEffect(IEPotions.FLAMMABLE.get(), 100, 1);
		IEFluids.CONCRETE.getBlock().setEffect(MobEffects.MOVEMENT_SLOWDOWN, 20, 3);
		IEFluids.REDSTONE_ACID.getBlock().setEffect(IEPotions.CONDUCTIVE.get(), 100, 1);
		IEFluids.ACETALDEHYDE.getBlock().setEffect(MobEffects.CONFUSION, 70, 0);
		IEFluids.PHENOLIC_RESIN.getBlock().setEffect(IEPotions.STICKY.get(), 40, 1);

		ChemthrowerEffects.register();

		RailgunProjectiles.register();

		FluidPipeBlockEntity.initCovers();
		LocalNetworkHandler.register(EnergyTransferHandler.ID, EnergyTransferHandler::new);
		LocalNetworkHandler.register(RedstoneNetworkHandler.ID, RedstoneNetworkHandler::new);
		LocalNetworkHandler.register(WireDamageHandler.ID, WireDamageHandler::new);

		setFuture(ev.enqueueWork(IEContent::onThreadCommonSetup));
	}

	private static void onThreadCommonSetup()
	{
		((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(Misc.HEMP_PLANT.getId(), Misc.POTTED_HEMP);

		DispenserBlock.registerBehavior(IEItems.Misc.SHIELD, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
		DispenserBlock.registerBehavior(Minecarts.CART_METAL_BARREL, IEMinecartItem.MINECART_DISPENSER_BEHAVIOR);
		DispenserBlock.registerBehavior(Minecarts.CART_WOODEN_BARREL, IEMinecartItem.MINECART_DISPENSER_BEHAVIOR);
		DispenserBlock.registerBehavior(Minecarts.CART_REINFORCED_CRATE, IEMinecartItem.MINECART_DISPENSER_BEHAVIOR);
		DispenserBlock.registerBehavior(Minecarts.CART_WOODEN_CRATE, IEMinecartItem.MINECART_DISPENSER_BEHAVIOR);
		for(FluidEntry entry : IEFluids.ALL_ENTRIES)
			DispenserBlock.registerBehavior(entry.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
		ComposterBlock.COMPOSTABLES.putIfAbsent(IEItems.Misc.HEMP_SEEDS.asItem(), 0.3f);
		ComposterBlock.COMPOSTABLES.putIfAbsent(IEItems.Ingredients.HEMP_FIBER.asItem(), 0.15f);
		Villages.init();
		ShaderRegistry.compileWeight();
	}

	public static void populateAPI()
	{
		SetRestrictedField.startInitializing(false);
		IngredientWithSize.SERIALIZER.setValue(IngredientWithSizeSerializer.INSTANCE);
		BlueprintCraftingRecipe.blueprintItem.setValue(IEItems.Misc.BLUEPRINT);
		ExcavatorHandler.setSetDirtyCallback(IESaveData::markInstanceDirty);
		TemplateMultiblock.setCallbacks(
				Utils::getPickBlock,
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
		IMultiblockBEHelperMaster.MAKE_HELPER.setValue(MultiblockBEHelperMaster::new);
		IMultiblockBEHelperDummy.MAKE_HELPER.setValue(MultiblockBEHelperDummy::new);
		SetRestrictedField.lock(false);
		ShieldDisablingHandler.registerDisablingFunction(Player.class, player -> player.disableShield(true));
		ShieldDisablingHandler.registerDisablingFunction(EngineerIllager.class, EngineerIllager::disableShield);
	}

	public static void clearLastFuture()
	{
		if(lastOnThreadFuture==null)
			return;
		try
		{
			lastOnThreadFuture.get();
		} catch(InterruptedException|ExecutionException e)
		{
			throw new RuntimeException(e);
		}
		lastOnThreadFuture = null;
	}

	public static void setFuture(CompletableFuture<?> next)
	{
		clearLastFuture();
		lastOnThreadFuture = next;
	}
}
