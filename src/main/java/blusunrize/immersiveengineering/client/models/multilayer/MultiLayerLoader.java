/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.multilayer;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MultiLayerLoader implements IModelLoader<MultiLayerModel>
{
	public static final ResourceLocation LOCATION = new ResourceLocation(
			ImmersiveEngineering.MODID, "models/multilayer");
	private static final Set<String> KNOWN_SPECIAL = ImmutableSet.of(
			"loader", "textures", "transform", "parent"
	);

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
	}

	@Nonnull
	@Override
	public MultiLayerModel read(@Nonnull JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		Map<String, IModelGeometry<?>> subModels = new HashMap<>();
		for(Entry<String, JsonElement> entry : modelContents.entrySet())
			if(!KNOWN_SPECIAL.contains(entry.getKey()))
			{
				JsonObject subModel = entry.getValue().getAsJsonObject();
				if(subModel!=null)
				{
					IModelGeometry<?> model = ModelLoaderRegistry.deserializeGeometry(deserializationContext, subModel);
					subModels.put(entry.getKey(), Preconditions.checkNotNull(model));
				}
			}
		return new MultiLayerModel(subModels);
	}
}
