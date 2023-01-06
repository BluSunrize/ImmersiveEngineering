/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.BiFunction;

public class SpecialModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	private SpecialModelBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper)
	{
		super(loaderId, parent, existingFileHelper);
	}

	public static <T extends ModelBuilder<T>>
	BiFunction<T, ExistingFileHelper, SpecialModelBuilder<T>> forLoader(ResourceLocation loader)
	{
		return (t, h) -> new SpecialModelBuilder<>(loader, t, h);
	}
}
