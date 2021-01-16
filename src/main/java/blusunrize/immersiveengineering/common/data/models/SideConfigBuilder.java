package blusunrize.immersiveengineering.common.data.models;

import blusunrize.immersiveengineering.client.models.ModelConfigurableSides.Loader;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides.Type;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class SideConfigBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>>
	SideConfigBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new SideConfigBuilder<>(parent, existingFileHelper);
	}

	protected SideConfigBuilder(T parent, ExistingFileHelper existingFileHelper)
	{
		super(Loader.NAME, parent, existingFileHelper);
	}

	private Type type;
	private ResourceLocation baseName;

	public SideConfigBuilder<T> type(Type type)
	{
		Preconditions.checkNotNull(type);
		Preconditions.checkState(this.type==null);
		this.type = type;
		return this;
	}

	public SideConfigBuilder<T> baseName(ResourceLocation baseName)
	{
		Preconditions.checkNotNull(baseName);
		Preconditions.checkState(this.baseName==null);
		this.baseName = baseName;
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		json = super.toJson(json);
		json.addProperty("type", type.getName());
		json.addProperty("base_name", baseName.toString());
		return json;
	}
}
