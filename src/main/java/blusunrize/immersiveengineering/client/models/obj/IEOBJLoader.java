/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.client.models.obj.callback.DefaultCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallbacks;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

import javax.annotation.Nonnull;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class IEOBJLoader implements IModelLoader<IEOBJModel>
{
	public static final ResourceLocation LOADER_NAME = new ResourceLocation(MODID, "ie_obj");
	public static final String CALLBACKS_KEY = "callbacks";
	public static final String DYNAMIC_KEY = "dynamic";
	public static final IEOBJLoader instance = new IEOBJLoader();

	@Nonnull
	@Override
	public IEOBJModel read(
			@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
	)
	{
		OBJModel model = OBJLoader.INSTANCE.read(deserializationContext, modelContents);
		IEOBJCallback<?> callback;
		if(modelContents.has(CALLBACKS_KEY))
		{
			String key = modelContents.get(CALLBACKS_KEY).getAsString();
			callback = IEOBJCallbacks.getCallback(new ResourceLocation(key));
		}
		else
			callback = DefaultCallback.INSTANCE;
		return new IEOBJModel(
				model, modelContents.has(DYNAMIC_KEY)&&modelContents.get(DYNAMIC_KEY).getAsBoolean(), callback
		);
	}

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager pResourceManager)
	{
	}
}
