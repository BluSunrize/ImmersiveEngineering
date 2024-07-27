/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.config;

import blusunrize.immersiveengineering.api.Lib;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Path;

/**
 * Allows specific configs to be loaded "earlier than usual" for specific sections of code. This should usually be
 * avoided, but may be necessary in some cases. This uses AutoCloseable as a "poor man's RAII", so it should be used
 * with try-with-resources even though this results in an unused-variable warning.
 */
public class EarlyConfigLoader implements AutoCloseable
{
	private final ModConfigSpec configSpec;
	private final CommentedFileConfig backingConfig;

	public EarlyConfigLoader(ModConfigSpec spec, ModConfig.Type type)
	{
		Preconditions.checkState(!spec.isLoaded());
		this.configSpec = spec;
		// It would be nice to have this available somewhere, but apparently getting access to ModConfig objects is
		// impossible
		final Path configPath = FMLPaths.CONFIGDIR.get().resolve(Lib.MODID+"-"+type.extension()+".toml");
		this.backingConfig = CommentedFileConfig.builder(configPath).build();
		this.backingConfig.load();
		// Temporarily load the IE common config so we can query
		this.configSpec.setConfig(this.backingConfig);
	}

	@Override
	public void close()
	{
		configSpec.setConfig(null);
		backingConfig.close();
	}
}
