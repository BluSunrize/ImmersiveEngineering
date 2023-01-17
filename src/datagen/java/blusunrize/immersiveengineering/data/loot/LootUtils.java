/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import blusunrize.immersiveengineering.common.util.loot.MultiblockDropsLootContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public class LootUtils
{
	public static LootPoolEntryContainer.Builder<?> getMultiblockDropBuilder()
	{
		return MultiblockDropsLootContainer.builder();
	}
}
