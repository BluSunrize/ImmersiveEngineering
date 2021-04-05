/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * @author BluSunrize - 16.08.2018
 */
public class IELootFunctions
{
	public static LootFunctionType bluprintz;
	public static LootFunctionType windmill;
	public static LootFunctionType propertyCount;

	public static LootPoolEntryType dropInventory;
	public static LootPoolEntryType tileDrop;
	public static LootPoolEntryType multiblockOrigBlock;

	public static void register()
	{
		bluprintz = registerFunction(BluprintzLootFunction.ID, new BluprintzLootFunction.Serializer());
		windmill = registerFunction(WindmillLootFunction.ID, new WindmillLootFunction.Serializer());
		propertyCount = registerFunction(PropertyCountLootFunction.ID, new PropertyCountLootFunction.Serializer());

		dropInventory = registerEntry(DropInventoryLootEntry.ID, new DropInventoryLootEntry.Serializer());
		tileDrop = registerEntry(TileDropLootEntry.ID, new TileDropLootEntry.Serializer());
		multiblockOrigBlock = registerEntry(MBOriginalBlockLootEntry.ID, new MBOriginalBlockLootEntry.Serializer());
	}

	private static LootPoolEntryType registerEntry(ResourceLocation id, ILootSerializer<? extends LootEntry> serializer)
	{
		return Registry.register(
				Registry.LOOT_POOL_ENTRY_TYPE, id, new LootPoolEntryType(serializer)
		);
	}

	private static LootFunctionType registerFunction(ResourceLocation id, ILootSerializer<? extends ILootFunction> serializer)
	{
		return Registry.register(
				Registry.LOOT_FUNCTION_TYPE, id, new LootFunctionType(serializer)
		);
	}
}
