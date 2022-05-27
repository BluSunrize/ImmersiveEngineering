/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.manual;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.data.manual.icon.RenderedItemModelDataProvider;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ManualDataGenerator
{
	public static void addProviders(DataGenerator gen, ExistingFileHelper exHelper)
	{
		String outputTo = System.getenv("ie_manual_datagen_path");
		if(outputTo==null)
		{
			IELogger.logger.info("Skipping manual exports since the output directory is not set");
			return;
		}
		gen.addProvider(new RenderedItemModelDataProvider(gen, exHelper, Path.of(outputTo, "icons")));
		gen.addProvider(new TagExports(gen, exHelper, Path.of(outputTo, "tags")));
	}


	public static ReloadableResourceManager makeFullResourceManager(
			PackType type, DataGenerator gen, ExistingFileHelper helper
	)
	{
		MultiPackResourceManager nonGeneratedManager = ObfuscationReflectionHelper.getPrivateValue(
				ExistingFileHelper.class,
				helper,
				type == PackType.CLIENT_RESOURCES ? "clientResources" : "serverData"
		);
		var allSources = nonGeneratedManager.listPacks().collect(Collectors.toList());
		allSources.add(new FolderPackResources(gen.getOutputFolder().toFile()));
		var resourceManager = new ReloadableResourceManager(type);
		resourceManager.createReload(
				Util.backgroundExecutor(), Minecraft.getInstance(), CompletableFuture.completedFuture(Unit.INSTANCE), allSources
		);
		return resourceManager;
	}
}
