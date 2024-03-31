/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BiomeTags extends BiomeTagsProvider
{

	public BiomeTags(PackOutput output, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(output, lookupProvider, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider p_255894_)
	{
		// swamps
		tag(IETags.is_swamp)
				.add(Biomes.SWAMP)
				.add(Biomes.MANGROVE_SWAMP);
		// deserts, plains, mesas, savannas, ice plains
		tag(IETags.generateClaypan)
				.add(Biomes.DESERT)
				.add(Biomes.PLAINS)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.BADLANDS)
				.add(Biomes.SAVANNA)
				.add(Biomes.SNOWY_PLAINS);
		// swamps, beaches, coral reefs, stony shore
		tag(IETags.generateSeabed)
				.addTag(IETags.is_swamp)
				.addTag(net.minecraft.tags.BiomeTags.IS_BEACH)
				.add(Biomes.WARM_OCEAN)
				.add(Biomes.STONY_SHORE);
	}


	@Nonnull
	@Override
	public String getName()
	{
		return "IE biome tags";
	}
}
