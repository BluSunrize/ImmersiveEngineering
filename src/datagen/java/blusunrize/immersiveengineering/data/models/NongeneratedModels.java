/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.data.models.NongeneratedModels.NongeneratedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * When wrapping models for splitting/mirroring/etc, we include the "inner" model JSON into the "outer" one. So there is
 * no need to generate the files for the inner model. This ModelProvider gives a way to use the standard model creation
 * API for such models, as well as a compile-time way to specify that a model should be "inner".
 */
public class NongeneratedModels extends ModelProvider<NongeneratedModel>
{
	public NongeneratedModels(PackOutput output, ExistingFileHelper existingFileHelper)
	{
		super(output, Lib.MODID, "block", NongeneratedModel::new, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
	}

	@Override
	public String getName()
	{
		return "Non-generated models";
	}

	public static class NongeneratedModel extends ModelBuilder<NongeneratedModel>
	{

		protected NongeneratedModel(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper)
		{
			super(outputLocation, existingFileHelper);
		}
	}
}
