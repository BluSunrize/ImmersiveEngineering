/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.model_old;

import blusunrize.immersiveengineering.common.data.model_old.ModelFile.GeneratedModelFile;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ModelGenerator implements IDataProvider
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
	private final DataGenerator gen;

	public ModelGenerator(DataGenerator gen)
	{
		this.gen = gen;
	}

	@Override
	public void act(@Nonnull DirectoryCache cache) throws IOException
	{
		Set<ResourceLocation> generatedModels = new HashSet<>();
		registerModels(modelFile -> {
			Preconditions.checkArgument(generatedModels.add(modelFile.getUncheckedLocation()));
			modelFile.generate(gen.getOutputFolder(), cache);
		});
	}

	protected abstract void registerModels(Consumer<GeneratedModelFile> out);

	@Override
	public String getName()
	{
		return "Models";
	}
}
