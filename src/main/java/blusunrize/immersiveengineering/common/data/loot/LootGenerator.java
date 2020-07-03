/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.loot;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public abstract class LootGenerator implements IDataProvider
{
	private final DataGenerator dataGenerator;
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	protected final Map<ResourceLocation, LootTable> tables = Maps.newHashMap();

	public LootGenerator(DataGenerator gen)
	{
		dataGenerator = gen;
	}

	public void act(@Nonnull DirectoryCache outCache)
	{
		tables.clear();
		Path outFolder = this.dataGenerator.getOutputFolder();

		registerTables();

		ValidationTracker validator = new ValidationTracker(
				LootParameterSets.GENERIC,
				(p_229442_0_) -> null,
				tables::get
		);
		tables.forEach((name, table) -> {
			LootTableManager.func_227508_a_(validator, name, table);
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
		return p_218439_0_.resolve("data/"+p_218439_1_.getNamespace()+"/loot_tables/"+p_218439_1_.getPath()+".json");
	}

	protected abstract void registerTables();
}
