/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static blusunrize.immersiveengineering.client.models.obj.IEOBJLoader.*;

public class IEOBJBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>> IEOBJBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new IEOBJBuilder<>(parent, existingFileHelper);
	}

	private boolean dynamic = false;
	private ResourceLocation modelLocation = null;
	private IEOBJCallback<?> callback;
	private List<RenderType> layers;

	protected IEOBJBuilder(T parent, ExistingFileHelper existingFileHelper)
	{
		super(IEOBJLoader.LOADER_NAME, parent, existingFileHelper);
	}

	public IEOBJBuilder<T> modelLocation(ResourceLocation modelLocation)
	{
		this.modelLocation = modelLocation;
		return this;
	}

	public IEOBJBuilder<T> layer(RenderType... layers)
	{
		if(this.layers==null)
			this.layers = new ArrayList<>();
		this.layers.addAll(Arrays.asList(layers));
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		Preconditions.checkNotNull(callback);
		Preconditions.checkNotNull(modelLocation);
		json = super.toJson(json);
		json.addProperty(MODEL_KEY, modelLocation.toString());
		if(dynamic)
			json.addProperty(DYNAMIC_KEY, true);
		if(this.layers!=null)
		{
			JsonArray types = new JsonArray();
			for(final RenderType layer : this.layers)
				types.add(ModelProviderUtils.getName(layer));
			json.add(LAYERS_KEY, types);
		}
		json.addProperty(CALLBACKS_KEY, IEOBJCallbacks.getName(callback).toString());
		return json;
	}

	public IEOBJBuilder<T> dynamic(boolean dynamic)
	{
		this.dynamic = dynamic;
		return this;
	}

	public IEOBJBuilder<T> callback(IEOBJCallback<?> callback)
	{
		this.callback = callback;
		return this;
	}
}
