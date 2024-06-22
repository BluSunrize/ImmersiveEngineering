/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter.Collector;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AllLoot extends LootTableProvider
{
	public AllLoot(PackOutput output, CompletableFuture<Provider> provider)
	{
		super(output, Set.of(), List.of(), provider);
	}

	@Override
	public List<SubProviderEntry> getTables()
	{
		return ImmutableList.of(
				new SubProviderEntry(GeneralLoot::new, LootContextParamSets.EMPTY),
				new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK),
				new SubProviderEntry(EntityLoot::new, LootContextParamSets.ENTITY)
		);
	}

	@Override
	protected void validate(WritableRegistry<LootTable> registry, ValidationContext tracker, Collector collector)
	{
		registry.forEach(table -> table.validate(tracker));
	}
}
