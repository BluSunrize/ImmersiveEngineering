/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.models;

import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class LoadedModelProvider extends ModelProvider<LoadedModelBuilder>
{
	public LoadedModelProvider(DataGenerator gen, String modid, String folder, ExistingFileHelper exHelper) {
		super(gen, modid, folder, LoadedModelBuilder::new, exHelper);
	}

	@Override
	public LoadedModelBuilder getBuilder(String path)
	{
		return super.getBuilder(path);
	}

	@Override
	public LoadedModelBuilder withExistingParent(String name, String parent)
	{
		return super.withExistingParent(name, parent);
	}

	@Override
	public LoadedModelBuilder withExistingParent(String name, ResourceLocation parent)
	{
		return super.withExistingParent(name, parent);
	}
}
