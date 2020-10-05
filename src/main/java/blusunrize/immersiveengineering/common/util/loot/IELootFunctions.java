/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import net.minecraft.world.storage.loot.LootEntryManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

/**
 * @author BluSunrize - 16.08.2018
 */
public class IELootFunctions
{
	public static void preInit()
	{
		LootFunctionManager.registerFunction(new BluprintzLootFunction.Serializer());
		LootFunctionManager.registerFunction(new WindmillLootFunction.Serializer());
		LootFunctionManager.registerFunction(new PropertyCountLootFunction.Serializer());

		LootEntryManager.func_216194_a(new DropInventoryLootEntry.Serializer());
		LootEntryManager.func_216194_a(new TileDropLootEntry.Serializer());
		LootEntryManager.func_216194_a(new MBOriginalBlockLootEntry.Serializer());
	}

}
