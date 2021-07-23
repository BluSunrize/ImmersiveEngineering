/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public abstract class LootGenerator implements DataProvider
{
	private final DataGenerator dataGenerator;
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	protected final Map<ResourceLocation, LootTable> tables = Maps.newHashMap();

	public LootGenerator(DataGenerator gen)
	{
		dataGenerator = gen;
	}

	public void run(@Nonnull HashCache outCache)
	{
		tables.clear();
		Path outFolder = this.dataGenerator.getOutputFolder();

		registerTables();

		ValidationContext validator = new ValidationContext(
				LootContextParamSets.ALL_PARAMS,
				(p_229442_0_) -> null,
				tables::get
		);
		tables.forEach((name, table) -> {
			LootTables.validate(validator, name, table);
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
					DataProvider.save(GSON, outCache, LootTables.serialize(table), out);
				} catch(IOException x)
				{
					IELogger.logger.error("Couldn't save loot table {}", out, x);
				}

			});
		}
	}

	private static Path getPath(Path pathIn, ResourceLocation id)
	{
		return pathIn.resolve("data/"+id.getNamespace()+"/loot_tables/"+id.getPath()+".json");
	}

	protected abstract void registerTables();
}
