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
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class DynamicModel<T>
{
	private static final Random RAND = new Random();

	public abstract BakedModel get(T key);

	public static DynamicModel<Void> createSimple(ResourceLocation model, String key, ModelType type)
	{
		return new SimpleDynamicModel(model, key, type);
	}

	public static DynamicModel<Direction> createSided(ResourceLocation model, String key, ModelType type)
	{
		return new SidedDynamicModel(model, key, type);
	}

	public List<BakedQuad> getNullQuads(T key, BlockState state)
	{
		return getNullQuads(key, state, EmptyModelData.INSTANCE);
	}

	public List<BakedQuad> getNullQuads(T key, BlockState state, IModelData data)
	{
		return get(key).getQuads(state, null, RAND, data);
	}

	private static class SidedDynamicModel extends DynamicModel<Direction>
	{
		private final Map<Direction, ModelResourceLocation> names = new HashMap<>();

		private SidedDynamicModel(ResourceLocation name, String desc, ModelType type)
		{
			ResourceLocation baseLoc = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/"+desc);
			for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
			{
				names.put(d, new ModelResourceLocation(baseLoc, d.getSerializedName()));
				DynamicModelLoader.requestModel(
						DynamicModel.getRequest(type, name, (int)d.toYRot()+180),
						names.get(d));
			}
		}

		@Override
		public BakedModel get(Direction key)
		{
			final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
			return blockRenderer.getBlockModelShaper().getModelManager().getModel(names.get(key));
		}
	}

	private static class SimpleDynamicModel extends DynamicModel<Void>
	{
		private final ModelResourceLocation name;

		private SimpleDynamicModel(ResourceLocation name, String desc, ModelType type)
		{
			ResourceLocation baseLoc = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/"+desc);
			this.name = new ModelResourceLocation(baseLoc, "");
			DynamicModelLoader.requestModel( DynamicModel.getRequest(type, name, 0), this.name);
		}

		@Override
		public BakedModel get(Void key)
		{
			final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
			return blockRenderer.getBlockModelShaper().getModelManager().getModel(name);
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
