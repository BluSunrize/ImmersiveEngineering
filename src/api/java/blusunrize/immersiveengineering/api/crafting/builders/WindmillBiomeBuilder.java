/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.energy.WindmillBiome;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class WindmillBiomeBuilder extends IEFinishedRecipe<WindmillBiomeBuilder>
{
	public static final String SINGLE_BIOME_KEY = "singleBiome";
	public static final String BIOME_TAG_KEY = "biomeTag";
	public static final String MODIFIER_KEY = "modifier";

	private WindmillBiomeBuilder(ResourceKey<Biome> matching)
	{
		super(WindmillBiome.SERIALIZER.get());
		addWriter(obj -> obj.addProperty(SINGLE_BIOME_KEY, matching.toString()));
	}

	private WindmillBiomeBuilder(TagKey<Biome> matching)
	{
		super(WindmillBiome.SERIALIZER.get());
		addWriter(obj -> obj.addProperty(BIOME_TAG_KEY, matching.location().toString()));
	}

	public static WindmillBiomeBuilder builder(TagKey<Biome> tag)
	{
		return new WindmillBiomeBuilder(tag);
	}

	public static WindmillBiomeBuilder builder(ResourceKey<Biome> biome)
	{
		return new WindmillBiomeBuilder(biome);
	}

	public WindmillBiomeBuilder modifier(float v)
	{
		addWriter(obj -> obj.addProperty(MODIFIER_KEY, v));
		return this;
	}
}
