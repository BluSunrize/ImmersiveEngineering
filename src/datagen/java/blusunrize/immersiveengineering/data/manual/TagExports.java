/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.manual;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record TagExports(PackOutput output, ExistingFileHelper helper, Path outPath) implements DataProvider
{
	private static final Gson GSON = new Gson();

	@Override
	public CompletableFuture<?> run(@Nonnull CachedOutput cache)
	{
		try
		{
			actuallyRun();
			return CompletableFuture.completedFuture(null);
		} catch(IOException x)
		{
			return CompletableFuture.failedFuture(x);
		}
	}

	private void actuallyRun() throws IOException
	{
		TagLoader<Item> loader = new TagLoader<>(
				rl -> Optional.ofNullable(ForgeRegistries.ITEMS.getValue(rl)),
				TagManager.getTagDir(Registries.ITEM)
		);
		try(ReloadableResourceManager resourceManager = ManualDataGenerator.makeFullResourceManager(
				PackType.SERVER_DATA, output, helper
		))
		{
			Map<ResourceLocation, Collection<Item>> tags = loader.loadAndBuild(resourceManager);
			for(Entry<ResourceLocation, Collection<Item>> entry : tags.entrySet())
			{
				JsonArray elements = new JsonArray();
				entry.getValue().stream()
						.map(item -> BuiltInRegistries.ITEM.getKey(item).toString())
						.sorted()
						.forEach(elements::add);
				ResourceLocation tagName = entry.getKey();
				Path tagPath = outPath.resolve(tagName.getNamespace()).resolve(tagName.getPath()+".json");
				Files.createDirectories(tagPath.getParent());
				try(BufferedWriter writer = Files.newBufferedWriter(tagPath))
				{
					writer.write(GSON.toJson(elements));
				}
			}
		}
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Tag export for online manual";
	}
}
