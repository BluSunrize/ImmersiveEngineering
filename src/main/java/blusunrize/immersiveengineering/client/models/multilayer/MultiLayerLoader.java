/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.multilayer;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry2;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class MultiLayerLoader implements IModelLoader<MultiLayerModel>
{
	public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID,
			"models/multilayer");

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
	}

	@Override
	public MultiLayerModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		Map<BlockRenderLayer, IModelGeometry<?>> subModels = new HashMap<>();
		for(BlockRenderLayer l : BlockRenderLayer.values())
		{
			JsonObject subModel = modelContents.getAsJsonObject(l.toString().toLowerCase());
			if(subModel!=null)
				subModels.put(l, ModelLoaderRegistry2.deserializeGeometry(deserializationContext, subModel));
		}
		return new MultiLayerModel(subModels);
	}
}
