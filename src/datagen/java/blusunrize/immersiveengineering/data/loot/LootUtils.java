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
