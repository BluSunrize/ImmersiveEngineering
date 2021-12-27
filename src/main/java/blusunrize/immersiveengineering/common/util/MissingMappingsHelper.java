package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MissingMappingsHelper
{
	private static final Map<ResourceKey<? extends IForgeRegistryEntry<?>>, Supplier<? extends IForgeRegistryEntry<?>>> REMAPPERS = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends IForgeRegistryEntry<T>> void handleRemapping(RegistryEvent.MissingMappings<T> event)
	{
		ImmutableList<RegistryEvent.MissingMappings.Mapping<T>> mappings = event.getMappings(Lib.MODID);
		if(!(event.getRegistry() instanceof ForgeRegistry<T> registry))
			return;
		mappings.forEach(mapping -> {
			Supplier<T> supplier = (Supplier<T>)REMAPPERS.get(ResourceKey.create(registry.getRegistryKey(), mapping.key));
			if(supplier!=null)
				mapping.remap(supplier.get());
		});
	}

	public static <T extends IForgeRegistryEntry<T>> void addRemapping(IForgeRegistry<T> registry, ResourceLocation oldKey, Supplier<T> newSupplier)
	{
		// there is no other subtype in Forge, but getRegistryKey does not exist in the interface
		if(registry instanceof ForgeRegistry<T> forgeRegistry)
			REMAPPERS.put(ResourceKey.create(forgeRegistry.getRegistryKey(), oldKey), newSupplier);
	}

}
