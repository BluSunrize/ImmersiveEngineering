/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.model_old;

import blusunrize.immersiveengineering.common.data.blockstate_old.BlockstateGenerator;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.data.DirectoryCache;
import net.minecraft.resources.ResourcePackType;
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

	public static class UncheckedModelFile extends ModelFile
	{

		public UncheckedModelFile(ResourceLocation location)
		{
			super(location);
		}

		@Override
		protected boolean exists()
		{
			return true;
		}
	}

	public static class ExistingModelFileIE extends ModelFile
	{

		public ExistingModelFileIE(ResourceLocation location)
		{
			super(location);
		}

		@Override
		protected boolean exists()
		{
			if(ModelHelperOld.EXISTING_FILE_HELPER!=null)
			{
				String suffix = getUncheckedLocation().getPath().contains(".")?"": ".json";
				return ModelHelperOld.EXISTING_FILE_HELPER.exists(getUncheckedLocation(),
						ResourcePackType.CLIENT_RESOURCES, suffix, "models");
			}
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

		public GeneratedModelFile createChild(ResourceLocation loc)
		{
			JsonObject childModel = new JsonObject();
			childModel.addProperty("parent", this.location.toString());
			return new GeneratedModelFile(loc, childModel);
		}
	}
}
