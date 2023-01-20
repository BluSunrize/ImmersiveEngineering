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
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomeTags extends ForgeRegistryTagsProvider<Biome>
{

	public BiomeTags(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(generatorIn, ForgeRegistries.BIOMES, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags()
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
