package blusunrize.immersiveengineering.common.data.models;

import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

public class ConnectorBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>>
	ConnectorBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new ConnectorBuilder<>(parent, existingFileHelper);
	}

	protected ConnectorBuilder(T parent, ExistingFileHelper existingFileHelper)
	{
		super(ConnectionLoader.LOADER_NAME, parent, existingFileHelper);
	}

	private List<String> layers = ImmutableList.of("solid");
	private JsonObject baseModel;

	public ConnectorBuilder<T> layers(List<String> layers)
	{
		this.layers = layers;
		return this;
	}

	public ConnectorBuilder<T> baseModel(JsonObject baseModel)
	{
		Preconditions.checkNotNull(baseModel);
		Preconditions.checkState(this.baseModel==null);
		this.baseModel = baseModel;
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		json = super.toJson(json);
		json.add("base_model", baseModel);
		JsonArray layersJson = new JsonArray();
		for(String s : layers)
			layersJson.add(s);
		json.add("layers", layersJson);
		return json;
	}
}
