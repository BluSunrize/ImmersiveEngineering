/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MissingMappingsHelper
{
	private static final Map<ResourceKey<?>, Supplier<?>> REMAPPERS = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static void handleRemapping(MissingMappingsEvent event)
	{
		handleRemapping(event, Registries.ITEM);
		handleRemapping(event, Registries.BLOCK);
		handleRemapping(event, Registries.FLUID);
	}

	private static <T>
	void handleRemapping(MissingMappingsEvent event, ResourceKey<Registry<T>> key)
	{
		var mappings = event.getMappings(key, Lib.MODID);
		mappings.forEach(mapping -> {
			var supplier = (Supplier<T>)REMAPPERS.get(ResourceKey.create(key, mapping.getKey()));
			if(supplier!=null)
				mapping.remap(supplier.get());
		});
	}

	public static <T> void addRemapping(
			Registry<T> registry, ResourceLocation oldKey, Supplier<T> newSupplier
	)
	{
		REMAPPERS.put(ResourceKey.create(registry.key(), oldKey), newSupplier);
	}

}
