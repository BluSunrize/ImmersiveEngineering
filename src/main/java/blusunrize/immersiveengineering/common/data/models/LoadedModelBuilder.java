/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.models;

import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResource;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map.Entry;

public class LoadedModelBuilder extends ModelBuilder<LoadedModelBuilder>
{
	private ResourceLocation loader;
	private final JsonObject additional = new JsonObject();
	private final TransformationMap transforms = new TransformationMap();

	protected LoadedModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper)
	{
		super(outputLocation, existingFileHelper);
	}

	public LoadedModelBuilder loader(ResourceLocation loader)
	{
		this.loader = loader;
		return this;
	}

	public LoadedModelBuilder additional(String name, ResourceLocation value)
	{
		return additional(name, value.toString());
	}

	public LoadedModelBuilder additional(String name, List<String> value)
	{
		JsonArray arr = new JsonArray();
		for(String s : value)
			arr.add(s);
		additional.add(name, arr);
		return this;
	}

	public LoadedModelBuilder additional(String name, boolean value)
	{
		additional.addProperty(name, value);
		return this;
	}

	public LoadedModelBuilder additional(String name, String value)
	{
		additional.addProperty(name, value);
		return this;
	}

	public LoadedModelBuilder additional(String name, JsonElement value)
	{
		additional.add(name, value);
		return this;
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject ret = super.toJson();
		if(loader!=null)
		{
			ret.addProperty("loader", loader.toString());
			for(Entry<String, JsonElement> entry : additional.entrySet())
			{
				Preconditions.checkState(!ret.has(entry.getKey()));
				ret.add(entry.getKey(), entry.getValue());
			}
		}
		JsonObject transformJson = transforms.toJson();
		if(!transformJson.entrySet().isEmpty())
			ret.add("transform", transformJson);
		return ret;
	}

	@Override
	public TransformsBuilder transforms()
	{
		throw new UnsupportedOperationException("Use transforms(ResourceLocation) or transformationMap()");
	}

	public LoadedModelBuilder transforms(ResourceLocation source)
	{
		IResource transformFile;
		try
		{
			transformFile = existingFileHelper.getResource(
					source, ResourcePackType.CLIENT_RESOURCES, ".json", "transformations"
			);
			String jsonString = CharStreams.toString(new InputStreamReader(transformFile.getInputStream()));
			transforms.addFromJson(jsonString);
			return this;
		} catch(IOException e)
		{
			throw new RuntimeException("While loading transforms from "+source, e);
		}
	}

	public TransformationMap transformationMap()
	{
		return transforms;
	}
}
