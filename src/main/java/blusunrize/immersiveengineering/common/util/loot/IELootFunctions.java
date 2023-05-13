/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * @author BluSunrize - 16.08.2018
 */
@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class IELootFunctions
{
	private static final DeferredRegister<LootItemFunctionType> FUNCTION_REGISTER = DeferredRegister.create(
			Registries.LOOT_FUNCTION_TYPE, ImmersiveEngineering.MODID
	);
	public static final RegistryObject<LootItemFunctionType> BLUPRINTZ = registerFunction("secret_bluprintz", () -> new SimpleSerializer<>(BluprintzLootFunction::new));
	public static final RegistryObject<LootItemFunctionType> REVOLVERPERK = registerFunction("revolverperk", () -> new SimpleSerializer<>(RevolverperkLootFunction::new));
	public static final RegistryObject<LootItemFunctionType> WINDMILL = registerFunction("windmill", () -> new SimpleSerializer<>(WindmillLootFunction::new));
	public static final RegistryObject<LootItemFunctionType> CONVEYOR_COVER = registerFunction("conveyor_cover", () -> new SimpleSerializer<>(ConveyorCoverLootFunction::new));
	public static final RegistryObject<LootItemFunctionType> PROPERTY_COUNT = registerFunction("property_count", PropertyCountLootFunction.Serializer::new);

	private static final DeferredRegister<LootPoolEntryType> ENTRY_REGISTER = DeferredRegister.create(
			// TODO why isn't there a REGISTRY field for this one?
			BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.key(), ImmersiveEngineering.MODID
	);
	public static final RegistryObject<LootPoolEntryType> DROP_INVENTORY = registerEntry("drop_inv", DropInventoryLootEntry.Serializer::new);
	public static final RegistryObject<LootPoolEntryType> TILE_DROP = registerEntry("tile_drop", BEDropLootEntry.Serializer::new);
	public static final RegistryObject<LootPoolEntryType> MULTIBLOCK_DROPS = registerEntry("multiblock", MultiblockDropsLootContainer.Serializer::new);

	public static void init()
	{
		final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		FUNCTION_REGISTER.register(bus);
		ENTRY_REGISTER.register(bus);
	}

	private static RegistryObject<LootPoolEntryType> registerEntry(
			String id, Supplier<Serializer<? extends LootPoolEntryContainer>> serializer
	)
	{
		return ENTRY_REGISTER.register(id, () -> new LootPoolEntryType(serializer.get()));
	}

	private static RegistryObject<LootItemFunctionType> registerFunction(
			String id, Supplier<Serializer<? extends LootItemFunction>> serializer
	)
	{
		return FUNCTION_REGISTER.register(id, () -> new LootItemFunctionType(serializer.get()));
	}
}
