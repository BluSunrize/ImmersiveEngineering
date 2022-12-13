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
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ManualDataGenerator
{
    private static final String README_CONTENTS = "Minecraft's item icons (as found in `icons/minecraft` are used in"+
        "accordance with Mojang's [Asset Usage Guidelines]"+
        "(https://www.minecraft.net/en-us/terms#terms-brand_guidelines)\n";

	public static void addProviders(DataGenerator gen, ExistingFileHelper exHelper)
	{
		String outputTo = System.getenv("ie_manual_datagen_path");
		if(outputTo==null)
		{
			IELogger.logger.info("Skipping manual exports since the output directory is not set");
			return;
		}
		try
		{
			Path mainOutput = Path.of(outputTo);
			Files.createDirectories(mainOutput);
			Path readme = mainOutput.resolve("README.md");
			try(BufferedWriter readmeWriter = Files.newBufferedWriter(readme, StandardCharsets.UTF_8))
			{
				readmeWriter.write(README_CONTENTS, 0, README_CONTENTS.length());
			}
			final PackOutput output = gen.getPackOutput();
			gen.addProvider(true, new RenderedItemModelDataProvider(output, exHelper, mainOutput.resolve("icons")));
			gen.addProvider(true, new TagExports(output, exHelper, mainOutput.resolve("tags")));
		} catch(IOException xcp)
		{
			throw new RuntimeException(xcp);
		}
	}


	public static ReloadableResourceManager makeFullResourceManager(
			PackType type, PackOutput output, ExistingFileHelper helper
	)
	{
		MultiPackResourceManager nonGeneratedManager = ObfuscationReflectionHelper.getPrivateValue(
				ExistingFileHelper.class,
				helper,
				type==PackType.CLIENT_RESOURCES?"clientResources": "serverData"
		);
		List<PackResources> allSources = nonGeneratedManager.listPacks().collect(Collectors.toList());
		allSources.add(new PathPackResources("generated", output.getOutputFolder(), true));
		ReloadableResourceManager resourceManager = new ReloadableResourceManager(type);
		resourceManager.createReload(
				Util.backgroundExecutor(), Minecraft.getInstance(), CompletableFuture.completedFuture(Unit.INSTANCE), allSources
		);
		return resourceManager;
	}
}
