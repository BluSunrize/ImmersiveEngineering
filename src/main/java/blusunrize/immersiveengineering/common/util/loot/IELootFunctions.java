/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author BluSunrize - 16.08.2018
 */
public class IELootFunctions
{
	private static final DeferredRegister<LootItemFunctionType<?>> FUNCTION_REGISTER = DeferredRegister.create(
			Registries.LOOT_FUNCTION_TYPE, ImmersiveEngineering.MODID
	);
	public static final Holder<LootItemFunctionType<?>> BLUPRINTZ = registerFunction("secret_bluprintz", BluprintzLootFunction.CODEC);
	public static final Holder<LootItemFunctionType<?>> REVOLVERPERK = registerFunction("revolverperk", RevolverperkLootFunction.CODEC);
	public static final Holder<LootItemFunctionType<?>> WINDMILL = registerFunction("windmill", WindmillLootFunction.CODEC);
	public static final Holder<LootItemFunctionType<?>> CONVEYOR_COVER = registerFunction("conveyor_cover", ConveyorCoverLootFunction.CODEC);
	public static final Holder<LootItemFunctionType<?>> PROPERTY_COUNT = registerFunction("property_count", PropertyCountLootFunction.CODEC);

	private static final DeferredRegister<LootPoolEntryType> ENTRY_REGISTER = DeferredRegister.create(
			Registries.LOOT_POOL_ENTRY_TYPE, ImmersiveEngineering.MODID
	);
	public static final Holder<LootPoolEntryType> DROP_INVENTORY = registerEntry("drop_inv", DropInventoryLootEntry.CODEC);
	public static final Holder<LootPoolEntryType> TILE_DROP = registerEntry("tile_drop", BEDropLootEntry.CODEC);
	public static final Holder<LootPoolEntryType> MULTIBLOCK_DROPS = registerEntry("multiblock", MultiblockDropsLootContainer.CODEC);

	private static final DeferredRegister<LootItemConditionType> CONDITION_REGISTER = DeferredRegister.create(
			Registries.LOOT_CONDITION_TYPE, ImmersiveEngineering.MODID
	);
	public static final Holder<LootItemConditionType> BLOCKSTATE = registerCondition("blockstate", LootBlockStateFromLocationPredicate.CODEC);

	public static void init(IEventBus modBus)
	{
		FUNCTION_REGISTER.register(modBus);
		ENTRY_REGISTER.register(modBus);
		CONDITION_REGISTER.register(modBus);
	}

	private static Holder<LootPoolEntryType> registerEntry(
			String id, MapCodec<? extends LootPoolEntryContainer> serializer
	)
	{
		return ENTRY_REGISTER.register(id, () -> new LootPoolEntryType(serializer));
	}

	private static Holder<LootItemFunctionType<?>> registerFunction(
			String id, MapCodec<? extends LootItemFunction> serializer
	)
	{
		return FUNCTION_REGISTER.register(id, () -> new LootItemFunctionType<>(serializer));
	}

	private static Holder<LootItemConditionType> registerCondition(
			String id, MapCodec<? extends LootItemCondition> serializer
	)
	{
		return CONDITION_REGISTER.register(id, () -> new LootItemConditionType(serializer));
	}
}
