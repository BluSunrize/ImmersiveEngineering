package blusunrize.immersiveengineering.api.tool.conveyor;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Map;

public class ClientConveyors
{
	private static final Map<IConveyorType<?>, IConveyorClientData<?>> CLIENT_DATA = new Reference2ObjectOpenHashMap<>();

	public static <T extends IConveyorBelt>
	IConveyorClientData<T> getData(IConveyorType<T> type)
	{
		IConveyorClientData<?> untypedClientData = CLIENT_DATA.computeIfAbsent(type, t -> {
			Mutable<IConveyorClientData<?>> data = new MutableObject<>();
			t.initClientData(data::setValue);
			return data.getValue();
		});
		return (IConveyorClientData<T>)untypedClientData;
	}
}
