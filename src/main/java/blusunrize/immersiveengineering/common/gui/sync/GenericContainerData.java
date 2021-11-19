/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.gui.sync;

import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers.DataPair;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers.DataSerializer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericContainerData<T>
{
	private final DataSerializer<T> serializer;
	private final Supplier<T> get;
	private final Consumer<T> set;
	private T current;

	public GenericContainerData(DataSerializer<T> serializer, Supplier<T> get, Consumer<T> set)
	{
		this.serializer = serializer;
		this.get = get;
		this.set = set;
	}

	public static GenericContainerData<Integer> int32(Supplier<Integer> get, Consumer<Integer> set)
	{
		return new GenericContainerData<>(GenericDataSerializers.INT32, get, set);
	}

	public static GenericContainerData<?> energy(IMutableEnergyStorage storage)
	{
		return int32(storage::getEnergyStored, storage::setStoredEnergy);
	}

	public boolean needsUpdate()
	{
		T newValue = get.get();
		if(Objects.equals(current, newValue))
			return false;
		current = newValue;
		return true;
	}

	public void processSync(Object receivedData)
	{
		current = (T)receivedData;
		set.accept(current);
	}

	public DataPair<T> dataPair()
	{
		return new DataPair<>(serializer, current);
	}
}
