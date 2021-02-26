package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.client.models.multilayer.MultiLayerLoader;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MultiLayerBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>>
	MultiLayerBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new MultiLayerBuilder<>(parent, existingFileHelper);
	}

	protected MultiLayerBuilder(T parent, ExistingFileHelper existingFileHelper)
	{
		super(MultiLayerLoader.LOCATION, parent, existingFileHelper);
	}

	private final Map<String, JsonObject> layerModels = new HashMap<>();

	public MultiLayerBuilder<T> addLayer(String layer, JsonObject model)
	{
		Preconditions.checkState(!layerModels.containsKey(layer));
		layerModels.put(layer, model);
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		json = super.toJson(json);
		for(Entry<String, JsonObject> entry : layerModels.entrySet())
			json.add(entry.getKey(), entry.getValue());
		return json;
	}
}
