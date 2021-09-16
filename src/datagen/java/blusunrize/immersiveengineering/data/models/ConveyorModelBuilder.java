package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.client.models.ModelConveyor.ConveyorLoader;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ConveyorModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>>
	ConveyorModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new ConveyorModelBuilder<>(parent, existingFileHelper);
	}

	protected ConveyorModelBuilder(T parent, ExistingFileHelper existingFileHelper)
	{
		super(ConveyorLoader.LOCATION, parent, existingFileHelper);
	}

	private IConveyorType<?> type;

	public ConveyorModelBuilder<T> type(IConveyorType<?> type)
	{
		Preconditions.checkNotNull(type);
		Preconditions.checkState(this.type==null);
		this.type = type;
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		json = super.toJson(json);
		json.addProperty(ConveyorLoader.TYPE_KEY, type.getId().toString());
		return json;
	}
}
