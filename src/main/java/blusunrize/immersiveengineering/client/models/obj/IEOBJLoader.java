/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.obj.callback.DefaultCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallbacks;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.OBJModel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class IEOBJLoader implements IModelLoader<IEOBJModel>
{
	public static final ResourceLocation LOADER_NAME = new ResourceLocation(MODID, "ie_obj");
	public static final String MODEL_KEY = "model";
	public static final String CALLBACKS_KEY = "callbacks";
	public static final String DYNAMIC_KEY = "dynamic";
	public static final IEOBJLoader instance = new IEOBJLoader();

	private ResourceManager manager;

	@Nonnull
	@Override
	public IEOBJModel read(
			@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
	)
	{
		ResourceLocation modelLoc = toRL(modelContents.get(MODEL_KEY).getAsString(), null);
		try(InputStream input = getStream(modelLoc))
		{
			OBJModel<OBJMaterial> model = OBJModel.readFromStream(input, s -> getStream(toRL(s, modelLoc))).quadify();
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
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static ResourceLocation toRL(String name, @Nullable ResourceLocation basePath)
	{
		if(name.contains(":"))
			return new ResourceLocation(name);
		else if(basePath!=null)
		{
			String baseDir = basePath.getPath().substring(0, basePath.getPath().lastIndexOf('/')+1);
			return new ResourceLocation(basePath.getNamespace(), baseDir+name);
		}
		else
			return ImmersiveEngineering.rl(name);
	}

	private InputStream getStream(ResourceLocation path)
	{
		try
		{
			if(manager==null)
				manager = Minecraft.getInstance().getResourceManager();
			return manager.getResource(path).getInputStream();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager pResourceManager)
	{
		manager = pResourceManager;
	}
}
