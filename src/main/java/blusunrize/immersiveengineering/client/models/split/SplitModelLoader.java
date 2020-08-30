/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.ArrayList;
import java.util.List;

public class SplitModelLoader implements IModelLoader<UnbakedSplitModel>
{
	public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "basic_split");
	public static final String PARTS = "split_parts";
	public static final String BASE_LOADER = "base_loader";
	public static final String DYNAMIC = "dynamic";

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{

	}

	@Override
	public UnbakedSplitModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		ResourceLocation subloader;
		if(modelContents.has(BASE_LOADER))
			subloader = new ResourceLocation(modelContents.get(BASE_LOADER).getAsString());
		else
			subloader = new ResourceLocation("minecraft", "elements");
		JsonArray partsJson = modelContents.getAsJsonArray(PARTS);
		List<Vector3i> parts = new ArrayList<>(partsJson.size());
		for(JsonElement e : partsJson)
		{
			JsonArray a = e.getAsJsonArray();
			parts.add(new Vector3i(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()));
		}
		IModelGeometry<?> baseModel = ModelLoaderRegistry.getModel(
				subloader,
				deserializationContext,
				modelContents
		);
		return new UnbakedSplitModel(baseModel, parts, modelContents.get(DYNAMIC).getAsBoolean());
	}
}
