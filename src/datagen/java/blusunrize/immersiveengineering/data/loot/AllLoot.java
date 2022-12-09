package blusunrize.immersiveengineering.data.loot;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllLoot extends LootTableProvider
{
	public AllLoot(PackOutput output)
	{
		super(output, Set.of(), List.of());
	}

	@Override
	public List<SubProviderEntry> getTables()
	{
		return ImmutableList.of(
				new SubProviderEntry(GeneralLoot::new, LootContextParamSets.EMPTY),
				new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)
		);
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker)
	{
		map.forEach((p_218436_2_, p_218436_3_) -> {
			LootTables.validate(validationtracker, p_218436_2_, p_218436_3_);
		});
	}
}
