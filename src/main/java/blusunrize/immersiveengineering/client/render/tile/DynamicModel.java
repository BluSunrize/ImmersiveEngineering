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
import blusunrize.immersiveengineering.client.DynamicModelLoader.ModelRequest;
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

	public static DynamicModel<Void> createSimple(ResourceLocation model, String key, ModelType type)
	{
		return new SimpleDynamicModel(model, key, type);
	}

	public static DynamicModel<Direction> createSided(ResourceLocation model, String key, ModelType type)
	{
		return new SidedDynamicModel(model, key, type);
	}

	private static class SidedDynamicModel extends DynamicModel<Direction>
	{
		private final Map<Direction, ModelResourceLocation> names = new HashMap<>();
		private final ResourceLocation modelLocation;

		private SidedDynamicModel(ResourceLocation name, String desc, ModelType type)
		{
			this.modelLocation = name;
			ResourceLocation baseLoc = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/"+desc);
			for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			{
				names.put(d, new ModelResourceLocation(baseLoc, d.getString()));
				DynamicModelLoader.requestModel(
						DynamicModel.getRequest(type, name, (int)d.getHorizontalAngle()+180),
						names.get(d));
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

		private SimpleDynamicModel(ResourceLocation name, String desc, ModelType type)
		{
			this.modelLocation = name;
			ResourceLocation baseLoc = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/"+desc);
			this.name = new ModelResourceLocation(baseLoc, "");
			DynamicModelLoader.requestModel( DynamicModel.getRequest(type, name, 0), this.name);
		}

		@Override
		public IBakedModel get(Void key)
		{
			final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			return blockRenderer.getBlockModelShapes().getModelManager().getModel(name);
		}
	}

	private static ModelRequest getRequest(ModelType type, ResourceLocation loc, int rotY) {
		switch (type) {
			case OBJ:
				return ModelRequest.obj(loc, rotY);
			case IE_OBJ:
				return ModelRequest.ieObj(loc, rotY);
		}
		throw new UnsupportedOperationException();
	}

	public enum ModelType {
		OBJ,
		IE_OBJ
	}

}
