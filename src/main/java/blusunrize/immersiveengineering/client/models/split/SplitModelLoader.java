/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.UnbakedModelGeometry;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SplitModelLoader implements IModelLoader<UnbakedSplitModel>
{
	public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "basic_split");
	public static final String PARTS = "split_parts";
	public static final String BASE_MODEL = "base_model";
	public static final String BASE_LOADER = "base_loader";
	public static final String DYNAMIC = "dynamic";

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
	}

	@Nonnull
	@Override
	public UnbakedSplitModel read(@Nonnull JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		IModelGeometry<?> baseModel;
		if(modelContents.has(BASE_MODEL))
			baseModel = new UnbakedModelGeometry(ModelLoader.defaultModelGetter().apply(
					new ResourceLocation(modelContents.get(BASE_MODEL).getAsString())
			));
		else
		{
			ResourceLocation subloader;
			if(modelContents.has(BASE_LOADER))
				subloader = new ResourceLocation(modelContents.get(BASE_LOADER).getAsString());
			else
				subloader = new ResourceLocation("minecraft", "elements");
			baseModel = ModelLoaderRegistry.getModel(subloader, deserializationContext, modelContents);
		}
		JsonArray partsJson = modelContents.getAsJsonArray(PARTS);
		List<Vector3i> parts = new ArrayList<>(partsJson.size());
		for(JsonElement e : partsJson)
			parts.add(fromJson(e.getAsJsonArray()));
		MutableBoundingBox box = pointBB(parts.get(0));
		for(Vector3i v : parts)
			box.expandTo(pointBB(v));
		Vector3i size = new Vector3i(box.getXSize(), box.getYSize(), box.getZSize());
		return new UnbakedSplitModel(baseModel, parts, modelContents.get(DYNAMIC).getAsBoolean(), size);
	}

	private Vector3i fromJson(JsonArray a)
	{
		return new Vector3i(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt());
	}

	private MutableBoundingBox pointBB(Vector3i point)
	{
		return new MutableBoundingBox(point, point);
	}
}
