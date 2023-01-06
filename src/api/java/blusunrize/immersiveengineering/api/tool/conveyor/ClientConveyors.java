/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.conveyor;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Map;

public class ClientConveyors
{
	private static final Map<IConveyorType<?>, IConveyorModelRender<?>> CLIENT_DATA = new Reference2ObjectOpenHashMap<>();

	public static <T extends IConveyorBelt>
	IConveyorModelRender<T> getData(IConveyorType<T> type)
	{
		IConveyorModelRender<?> untypedClientData = CLIENT_DATA.computeIfAbsent(type, t -> {
			Mutable<IConveyorModelRender<?>> data = new MutableObject<>();
			t.initClientData(data::setValue);
			return data.getValue();
		});
		return (IConveyorModelRender<T>)untypedClientData;
	}
}
