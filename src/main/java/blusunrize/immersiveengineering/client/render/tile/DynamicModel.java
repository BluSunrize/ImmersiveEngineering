/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.DynamicModelLoader;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.ConfiguredModel;
import blusunrize.immersiveengineering.common.data.model.ModelFile.ExistingModelFile;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public abstract class DynamicModel<T>
{
	public abstract IBakedModel get(T key);

	public static DynamicModel<Void> createSimple(ResourceLocation model, String key)
	{
		return new SimpleDynamicModel(model, key);
	}

	public static DynamicModel<Direction> createSided(ResourceLocation model, String key)
	{
		return new SidedDynamicModel(model, key);
	}

	private static class SidedDynamicModel extends DynamicModel<Direction>
	{
		private final Map<Direction, ModelResourceLocation> names = new HashMap<>();
		private final ResourceLocation modelLocation;

		private SidedDynamicModel(ResourceLocation name, String desc)
		{
			this.modelLocation = name;
			ResourceLocation baseLoc = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/"+desc);
			for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			{
				names.put(d, new ModelResourceLocation(baseLoc, d.getName()));
				ConfiguredModel model = new ConfiguredModel(new ExistingModelFile(modelLocation), 0,
						(int)d.getHorizontalAngle()+180, false, ImmutableMap.of("flip-v", true));
				DynamicModelLoader.requestModel(model, names.get(d));
			}
		}

		@Override
		public IBakedModel get(Direction key)
		{
			final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			return blockRenderer.getBlockModelShapes().getModelManager().getModel(names.get(key));
		}
	}

	private static class SimpleDynamicModel extends DynamicModel<Void>
	{
		private final ModelResourceLocation name;
		private final ResourceLocation modelLocation;

		private SimpleDynamicModel(ResourceLocation name, String desc)
		{
			this.modelLocation = name;
			ResourceLocation baseLoc = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/"+desc);
			this.name = new ModelResourceLocation(baseLoc, "");
			ConfiguredModel model = new ConfiguredModel(new ExistingModelFile(modelLocation), 0,
					0, false, ImmutableMap.of("flip-v", true));
			DynamicModelLoader.requestModel(model, this.name);
		}

		@Override
		public IBakedModel get(Void key)
		{
			final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			return blockRenderer.getBlockModelShapes().getModelManager().getModel(name);
		}
	}
}
