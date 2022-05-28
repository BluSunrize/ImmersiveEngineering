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
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.tags.Tag;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public record TagExports(DataGenerator gen, ExistingFileHelper helper, Path outPath) implements DataProvider
{
	private static final Gson GSON = new Gson();

	@Override
	public void run(@Nonnull HashCache cache) throws IOException
	{
		TagLoader<Item> loader = new TagLoader<>(
				rl -> Optional.ofNullable(ForgeRegistries.ITEMS.getValue(rl)),
				TagManager.getTagDir(Registry.ITEM_REGISTRY)
		);
		try(ReloadableResourceManager resourceManager = ManualDataGenerator.makeFullResourceManager(
				PackType.SERVER_DATA, gen, helper
		))
		{
			Map<ResourceLocation, Tag<Item>> tags = loader.loadAndBuild(resourceManager);
			for(Entry<ResourceLocation, Tag<Item>> entry : tags.entrySet())
			{
				JsonArray elements = new JsonArray();
				for(Item item : entry.getValue().getValues())
				{
					elements.add(item.getRegistryName().toString());
				}
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
