package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallbacks;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.client.models.obj.IEOBJLoader.CALLBACKS_KEY;
import static blusunrize.immersiveengineering.client.models.obj.IEOBJLoader.DYNAMIC_KEY;

public class IEOBJBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>> IEOBJBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new IEOBJBuilder<>(parent, existingFileHelper);
	}

	private final OBJLoaderBuilder<T> internal;
	private boolean dynamic = false;
	@Nullable
	private IEOBJCallback<?> callback;

	protected IEOBJBuilder(T parent, ExistingFileHelper existingFileHelper)
	{
		super(IEOBJLoader.LOADER_NAME, parent, existingFileHelper);
		this.internal = OBJLoaderBuilder.begin(parent, existingFileHelper);
	}

	public IEOBJBuilder<T> modelLocation(ResourceLocation modelLocation)
	{
		internal.modelLocation(modelLocation);
		return this;
	}

	public IEOBJBuilder<T> flipV(boolean flipV)
	{
		internal.flipV(flipV);
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		json = internal.toJson(json);
		json = super.toJson(json);
		if(dynamic)
			json.addProperty(DYNAMIC_KEY, true);
		if(callback!=null)
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
