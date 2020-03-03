/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.loadermodels;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelBuilder;

import java.util.Map.Entry;

public class LoadedModelBuilder extends ModelBuilder<LoadedModelBuilder>
{
	private ResourceLocation loader;
	private final JsonObject additional = new JsonObject();
	protected LoadedModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper)
	{
		super(outputLocation, existingFileHelper);
	}

	public LoadedModelBuilder loader(ResourceLocation loader) {
		this.loader = loader;
		return this;
	}

	public LoadedModelBuilder additional(String name, ResourceLocation value) {
		return additional(name, value.toString());
	}

	public LoadedModelBuilder additional(String name, boolean value) {
		additional.addProperty(name, value);
		return this;
	}

	public LoadedModelBuilder additional(String name, String value) {
		additional.addProperty(name, value);
		return this;
	}

	@Override
	public JsonObject toJson()
	{
		Preconditions.checkNotNull(loader);
		JsonObject ret = super.toJson();
		ret.addProperty("loader", loader.toString());
		for (Entry<String, JsonElement> entry : additional.entrySet()) {
			Preconditions.checkState(!ret.has(entry.getKey()));
			ret.add(entry.getKey(), entry.getValue());
		}
		return ret;
	}
}
