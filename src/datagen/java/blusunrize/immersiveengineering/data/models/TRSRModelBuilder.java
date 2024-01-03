/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import com.google.common.io.CharStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

public class TRSRModelBuilder extends ModelBuilder<TRSRModelBuilder>
{
	private final TransformationMap transforms = new TransformationMap();

	private final List<SimpleOverride> overrides = new ArrayList<>();

	protected TRSRModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper)
	{
		super(outputLocation, existingFileHelper);
	}

	public TRSRModelBuilder transforms(ResourceLocation source)
	{
		Resource transformFile;
		try
		{
			transformFile = existingFileHelper.getResource(
					source, PackType.CLIENT_RESOURCES, ".json", "transformations"
			);
			String jsonString = CharStreams.toString(new InputStreamReader(transformFile.open()));
			transforms.addFromJson(jsonString);
			return this;
		} catch(IOException e)
		{
			throw new RuntimeException("While loading transforms from "+source, e);
		}
	}

	public TRSRModelBuilder override(ModelFile model, ResourceLocation predicateKey, float predicateValue)
	{
		this.overrides.add(new SimpleOverride(model, Map.of(predicateKey, predicateValue)));
		return this;
	}

	@Override
	public JsonObject toJson()
	{
		JsonObject ret = super.toJson();
		JsonObject transformJson = transforms.toJson();
		if(!transformJson.entrySet().isEmpty())
			ret.add("display", transformJson);
		if(!overrides.isEmpty())
			ret.add("overrides", overrides.stream().map(SimpleOverride::toJson).collect(
					Collector.of(JsonArray::new, JsonArray::add, (jsonElements, jsonElements2) -> {
						jsonElements.addAll(jsonElements2);
						return jsonElements;
					})
			));
		return ret;
	}

	record SimpleOverride(ModelFile model, Map<ResourceLocation, Float> predicates)
	{
		JsonObject toJson()
		{
			JsonObject ret = new JsonObject();
			JsonObject predicatesJson = new JsonObject();
			predicates.forEach((key, val) -> predicatesJson.addProperty(key.toString(), val));
			ret.add("predicate", predicatesJson);
			ret.addProperty("model", model.getLocation().toString());
			return ret;
		}
	}
}
