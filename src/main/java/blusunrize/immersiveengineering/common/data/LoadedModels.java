/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import blusunrize.immersiveengineering.common.data.models.LoadedModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;

public class LoadedModels extends LoadedModelProvider
{
	final Map<ResourceLocation, LoadedModelBuilder> backupModels = new HashMap<>();
	public LoadedModels(DataGenerator gen, ExistingFileHelper exHelper)
	{
		super(gen, ImmersiveEngineering.MODID, "block", exHelper);
	}

	@Override
	protected void registerModels()
	{
		super.generatedModels.putAll(backupModels);
	}

	public void backupModels() {
		backupModels.putAll(super.generatedModels);
	}

	@Override
	public String getName()
	{
		return "Loaded models";
	}
}
