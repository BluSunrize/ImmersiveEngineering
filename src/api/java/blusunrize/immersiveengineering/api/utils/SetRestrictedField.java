/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.utils;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a value that depends on non-API IE code and should only be written to by IE.
 * It also contains a mechanism to make sure that all fields are actually set.
 *
 * @param <T> type of the contained value
 */
public class SetRestrictedField<T> implements Supplier<T>
{
	private static final InitializationTracker CLIENT_FIELDS = new InitializationTracker();
	private static final InitializationTracker COMMON_FIELDS = new InitializationTracker();

	private final InitializationTracker tracker;
	private T value;

	private SetRestrictedField(InitializationTracker tracker)
	{
		this.tracker = tracker;
	}

	public static <T> SetRestrictedField<T> client()
	{
		return CLIENT_FIELDS.make();
	}

	public static <T> SetRestrictedField<T> common()
	{
		return COMMON_FIELDS.make();
	}

	public static void lock(boolean client)
	{
		if(client)
			CLIENT_FIELDS.lock();
		else
			COMMON_FIELDS.lock();
	}

	public static void startInitializing(boolean client)
	{
		if(client)
			CLIENT_FIELDS.startInitialization();
		else
			COMMON_FIELDS.startInitialization();
	}

	public void setValue(T value)
	{
		Preconditions.checkState(tracker.state==TrackerState.INITIALIZING);
		String currentMod = ModLoadingContext.get().getActiveNamespace();
		Preconditions.checkState(
				DatagenModLoader.isRunningDataGen()||Lib.MODID.equals(currentMod),
				"Restricted fields may only be set by Immersive Engineering, current mod is %s", currentMod
		);
		this.value = value;
	}

	@Override
	public T get()
	{
		return Preconditions.checkNotNull(value);
	}

	public boolean isInitialized()
	{
		return value!=null;
	}

	private static class InitializationTracker
	{
		private final List<Pair<Exception, SetRestrictedField<?>>> fields = new ArrayList<>();
		private TrackerState state = TrackerState.OPEN;

		synchronized <T> SetRestrictedField<T> make()
		{
			Preconditions.checkState(state!=TrackerState.LOCKED);
			SetRestrictedField<T> result = new SetRestrictedField<>(this);
			fields.add(Pair.of(new RuntimeException("Field created here"), result));
			return result;
		}

		public void startInitialization()
		{
			Preconditions.checkState(state==TrackerState.OPEN);
			state = TrackerState.INITIALIZING;
		}

		void lock()
		{
			Preconditions.checkState(state==TrackerState.INITIALIZING);
			for(Pair<Exception, SetRestrictedField<?>> field : fields)
				if(!field.getSecond().isInitialized())
					throw new RuntimeException(field.getFirst());
			state = TrackerState.LOCKED;
		}
	}

	private enum TrackerState
	{
		OPEN,
		INITIALIZING,
		LOCKED
	}
}
