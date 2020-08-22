/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public class BasicSplitLoader implements IModelLoader<BasicSplitModel>
{
	public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "basic_split");

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{

	}

	@Override
	public BasicSplitModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		ResourceLocation subloader;
		if(modelContents.has("base_loader"))
			subloader = new ResourceLocation(modelContents.get("base_loader").getAsString());
		else
			subloader = new ResourceLocation("minecraft", "elements");
		IModelGeometry<?> baseModel = ModelLoaderRegistry.getModel(
				subloader,
				deserializationContext,
				modelContents
		);
		return new BasicSplitModel(baseModel);
	}
}
