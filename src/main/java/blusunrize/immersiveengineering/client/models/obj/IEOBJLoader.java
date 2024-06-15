/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.client.ieobj.DefaultCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.OBJModel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class IEOBJLoader implements IGeometryLoader<IEOBJModel>
{
	public static final ResourceLocation LOADER_NAME = IEApi.ieLoc("ie_obj");
	public static final String MODEL_KEY = "model";
	public static final String CALLBACKS_KEY = "callbacks";
	public static final String DYNAMIC_KEY = "dynamic";
	public static final String LAYERS_KEY = "layers";
	public static final IEOBJLoader instance = new IEOBJLoader();

	@Override
	public IEOBJModel read(
			JsonObject modelContents, JsonDeserializationContext deserializationContext
	) throws JsonParseException
	{
		ResourceLocation modelLoc = toRL(modelContents.get(MODEL_KEY).getAsString(), null);
		try(InputStream input = getStream(modelLoc))
		{
			OBJModel<OBJMaterial> model = OBJModel.readFromStream(input, s -> getStream(toRL(s, modelLoc)))
					.quadify()
					.recomputeZeroNormals();
			IEOBJCallback<?> callback;
			if(modelContents.has(CALLBACKS_KEY))
			{
				String key = modelContents.get(CALLBACKS_KEY).getAsString();
				callback = IEOBJCallbacks.getCallback(ResourceLocation.parse(key));
			}
			else
				callback = DefaultCallback.INSTANCE;
			final boolean dynamic = modelContents.has(DYNAMIC_KEY)&&modelContents.get(DYNAMIC_KEY).getAsBoolean();
			List<ResourceLocation> layers = null;
			if(modelContents.has(LAYERS_KEY))
			{
				layers = new ArrayList<>();
				for(final JsonElement entry : modelContents.getAsJsonArray(LAYERS_KEY))
					layers.add(ResourceLocation.parse(entry.getAsString()));
			}
			return new IEOBJModel(model, dynamic, callback, layers);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static ResourceLocation toRL(String name, @Nullable ResourceLocation basePath)
	{
		if(name.contains(":"))
			return ResourceLocation.parse(name);
		else if(basePath!=null)
		{
			String baseDir = basePath.getPath().substring(0, basePath.getPath().lastIndexOf('/')+1);
			return basePath.withPath(baseDir+name);
		}
		else
			return ImmersiveEngineering.rl(name);
	}

	private InputStream getStream(ResourceLocation path)
	{
		try
		{
			return Minecraft.getInstance().getResourceManager().getResource(path).orElseThrow().open();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
