/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.model;

import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.data.DirectoryCache;
import net.minecraft.util.ResourceLocation;

import java.nio.file.Path;

public abstract class ModelFile
{
	protected ResourceLocation location;

	protected ModelFile(ResourceLocation location)
	{
		this.location = location;
	}

	protected abstract boolean exists();

	public ResourceLocation getLocation()
	{
		Preconditions.checkState(exists(), "Model at "+location+" does not exist!");
		return location;
	}

	public ResourceLocation getUncheckedLocation()
	{
		return location;
	}

	public static class ExistingModelFile extends ModelFile
	{

		public ExistingModelFile(ResourceLocation location)
		{
			super(location);
		}

		@Override
		protected boolean exists()
		{
			//TODO proper check for non-generated model file
			return true;
		}
	}

	public static class GeneratedModelFile extends ModelFile
	{
		private boolean generated = false;
		private final JsonObject model;

		public GeneratedModelFile(ResourceLocation location, JsonObject model)
		{
			super(location);
			this.model = model;
		}

		@Override
		protected boolean exists()
		{
			return generated;
		}

		public void generate(Path basePath, DirectoryCache cache)
		{
			String suffix = "assets/"+location.getNamespace()+"/models/"+location.getPath()+".json";
			Path target = basePath.resolve(suffix);
			BlockstateGenerator.saveJSON(cache, model, target);
			generated = true;
		}

		public GeneratedModelFile withLoc(ResourceLocation loc)
		{
			return new GeneratedModelFile(loc, model);
		}
	}
}
