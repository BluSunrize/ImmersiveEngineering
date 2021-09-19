package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class IEOBJBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>> IEOBJBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new IEOBJBuilder<>(parent, existingFileHelper);
	}

	private final OBJLoaderBuilder<T> internal;
	private boolean dynamic = false;

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

	public IEOBJBuilder<T> detectCullableFaces(boolean detectCullableFaces)
	{
		internal.detectCullableFaces(detectCullableFaces);
		return this;
	}

	public IEOBJBuilder<T> diffuseLighting(boolean diffuseLighting)
	{
		internal.diffuseLighting(diffuseLighting);
		return this;
	}

	public IEOBJBuilder<T> flipV(boolean flipV)
	{
		internal.flipV(flipV);
		return this;
	}

	public IEOBJBuilder<T> ambientToFullbright(boolean ambientToFullbright)
	{
		internal.ambientToFullbright(ambientToFullbright);
		return this;
	}

	public IEOBJBuilder<T> overrideMaterialLibrary(ResourceLocation materialLibraryOverrideLocation)
	{
		internal.overrideMaterialLibrary(materialLibraryOverrideLocation);
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		json = internal.toJson(json);
		json = super.toJson(json);
		if(dynamic)
			json.addProperty("dynamic", true);
		return json;
	}

	public IEOBJBuilder<T> dynamic(boolean dynamic)
	{
		this.dynamic = dynamic;
		return this;
	}
}
