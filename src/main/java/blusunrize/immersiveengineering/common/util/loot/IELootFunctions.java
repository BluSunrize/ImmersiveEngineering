/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import net.minecraft.loot.LootEntryManager;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.functions.LootFunctionManager;

/**
 * @author BluSunrize - 16.08.2018
 */
public class IELootFunctions
{
	public static LootFunctionType bluprintz;
	public static LootFunctionType windmill;

	public static LootPoolEntryType dropInventory;
	public static LootPoolEntryType tileDrop;
	public static LootPoolEntryType multiblockOrigBlock;

	public static void preInit()
	{
		bluprintz = LootFunctionManager.func_237451_a_(BluprintzLootFunction.ID.toString(), new BluprintzLootFunction.Serializer());
		windmill = LootFunctionManager.func_237451_a_(WindmillLootFunction.ID.toString(), new WindmillLootFunction.Serializer());

		dropInventory = LootEntryManager.register(DropInventoryLootEntry.ID.toString(), new DropInventoryLootEntry.Serializer());
		tileDrop = LootEntryManager.register(TileDropLootEntry.ID.toString(), new TileDropLootEntry.Serializer());
		multiblockOrigBlock = LootEntryManager.register(MBOriginalBlockLootEntry.ID.toString(), new MBOriginalBlockLootEntry.Serializer());
	}

}
