/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public class IEOBJCallbacks
{
	private static final BiMap<ResourceLocation, IEOBJCallback<?>> CALLBACKS = HashBiMap.create();

	public static void register(ResourceLocation name, IEOBJCallback<?> callback)
	{
		CALLBACKS.put(name, callback);
	}

	@Nullable
	public static IEOBJCallback<?> getCallback(ResourceLocation name)
	{
		return CALLBACKS.get(name);
	}

	public static ResourceLocation getName(IEOBJCallback<?> callback)
	{
		return Objects.requireNonNull(CALLBACKS.inverse().get(callback));
	}
}
