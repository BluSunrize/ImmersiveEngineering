/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BiomeTags extends TagsProvider<Biome>
{

	public BiomeTags(
			PackOutput output,
			CompletableFuture<HolderLookup.Provider> lookup,
			@Nullable ExistingFileHelper existingFileHelper
	)
	{
		super(output, Registries.BIOME, lookup, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider provider)
	{
		tag(IETags.hasMineralVeins)
				.addTag(net.minecraft.tags.BiomeTags.IS_OVERWORLD)
				.addTag(net.minecraft.tags.BiomeTags.IS_NETHER)
				.addTag(net.minecraft.tags.BiomeTags.IS_END)
		;
	}


	@Nonnull
	@Override
	public String getName()
	{
		return "IE biome tags";
	}
}
