/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.state.IProperty;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.LootTable.Builder;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraft.world.storage.loot.functions.ApplyBonus;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class BlockLoot implements IDataProvider
{
	private final DataGenerator dataGenerator;
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	private final Map<ResourceLocation, LootTable> tables = Maps.newHashMap();

	public BlockLoot(DataGenerator gen)
	{
		dataGenerator = gen;
	}

	public void act(@Nonnull DirectoryCache outCache)
	{
		tables.clear();
		Path outFolder = this.dataGenerator.getOutputFolder();

		registerTables();

		ValidationResults validator = new ValidationResults();
		tables.forEach((name, table) -> {
			LootTableManager.func_215302_a(validator, name, table, tables::get);
		});
		Multimap<String, String> problems = validator.getProblems();
		if(!problems.isEmpty())
		{
			problems.forEach((name, table) -> {
				IELogger.logger.warn("Found validation problem in "+name+": "+table);
			});
			throw new IllegalStateException("Failed to validate loot tables, see logs");
		}
		else
		{
			tables.forEach((name, table) -> {
				Path out = getPath(outFolder, name);

				try
				{
					IDataProvider.save(GSON, outCache, LootTableManager.toJson(table), out);
				} catch(IOException x)
				{
					IELogger.logger.error("Couldn't save loot table {}", out, x);
				}

			});
		}
	}

	private static Path getPath(Path p_218439_0_, ResourceLocation p_218439_1_)
	{
		return p_218439_0_.resolve("data/"+p_218439_1_.getNamespace()+"/loot_tables/blocks/"+p_218439_1_.getPath()+".json");
	}

	public String getName()
	{
		return "LootTablesBlock";
	}

	private void registerTables()
	{
		registerHemp();
		registerAllRemainingAsDefault();
	}

	private void registerAllRemainingAsDefault()
	{
		for(Block b : IEContent.registeredIEBlocks)
			if(!tables.containsKey(b.getRegistryName()))
				registerSelfDropping(b);
	}

	private void register(Block b, LootTable.Builder table)
	{
		register(b.getRegistryName(), table);
	}

	private void register(ResourceLocation name, LootTable.Builder table)
	{
		if(tables.put(name, table.setParameterSet(LootParameterSets.BLOCK).build())!=null)
		{
			throw new IllegalStateException("Duplicate loot table "+name);
		}
	}

	private void registerSelfDropping(Block b)
	{
		register(b.getRegistryName(), dropProvider(b));
	}

	private Builder dropProvider(IItemProvider in)
	{
		return LootTable
				.builder()
				.addLootPool(singleItemPool(in)
				);
	}

	private LootPool.Builder singleItemPool(IItemProvider in)
	{
		return createPoolBuilder()
				.rolls(ConstantRange.of(1))
				.addEntry(ItemLootEntry.builder(in));
	}

	private LootPool.Builder createPoolBuilder()
	{
		return LootPool.builder().acceptCondition(SurvivesExplosion.builder());
	}

	private void registerHemp()
	{
		LootTable.Builder ret = LootTable.builder()
				.addLootPool(singleItemPool(Misc.hempSeeds));
		for(EnumHempGrowth g : EnumHempGrowth.values())
			if(g==HempBlock.getMaxGrowth(g))
			{
				ret.addLootPool(
						binBonusLootPool(Ingredients.hempFiber, Enchantments.FORTUNE, g.ordinal()/8f, 3)
								.acceptCondition(propertyIs(IEBlocks.Misc.hempPlant, HempBlock.GROWTH, g))
				);
			}
		register(IEBlocks.Misc.hempPlant, ret);
	}

	private LootPool.Builder binBonusLootPool(IItemProvider item, Enchantment ench, float prob, int extra)
	{
		return createPoolBuilder()
				.addEntry(ItemLootEntry.builder(item))
				.acceptFunction(ApplyBonus.binomialWithBonusCount(ench, prob, extra));
	}

	private <T extends Comparable<T>> ILootCondition.IBuilder propertyIs(Block b, IProperty<T> prop, T value)
	{
		return BlockStateProperty.builder(b)
				.with(prop, value);
	}
}
