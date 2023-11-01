/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.client.ieobj;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class IEOBJCallbacks
{
	private static final BiMap<ResourceLocation, IEOBJCallback<?>> CALLBACKS = HashBiMap.create();
	private static final Map<IEOBJCallback<?>, ModelProperty<?>> MODEL_PROPERTIES = new IdentityHashMap<>();

	public static void register(ResourceLocation name, IEOBJCallback<?> callback)
	{
		CALLBACKS.put(name, callback);
		MODEL_PROPERTIES.put(callback, new ModelProperty<>());
	}

	@Nullable
	public static IEOBJCallback<?> getCallback(ResourceLocation name)
	{
		return CALLBACKS.get(name);
	}

	@SuppressWarnings("unchecked")
	public static <T> ModelProperty<T> getModelProperty(IEOBJCallback<?> callback)
	{
		return (ModelProperty<T>)MODEL_PROPERTIES.get(callback);
	}

	public static ResourceLocation getName(IEOBJCallback<?> callback)
	{
		return Objects.requireNonNull(CALLBACKS.inverse().get(callback));
	}
}
