/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.conveyor;

import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public interface IConveyorModelRender<T extends IConveyorBelt>
{
	/**
	 * @return An Object (typically a record) uniquely identifying the model produced by the given context
	 */
	default Object getModelCacheKey(RenderContext<T> context)
	{
		return getDefaultData(this, context);
	}

	static <T extends IConveyorBelt>
	BasicConveyorCacheData getDefaultData(IConveyorModelRender<T> renderer, RenderContext<T> context)
	{
		T instance = context.instance();
		Block cover = context.getCover();
		if(instance==null)
			return new BasicConveyorCacheData(
					cover, Direction.NORTH, ConveyorDirection.HORIZONTAL, true, true, true, DyeColor.WHITE
			);
		Direction facing = instance.getFacing();
		return new BasicConveyorCacheData(
				cover, facing, instance.getConveyorDirection(), instance.isActive(),
				renderer.shouldRenderWall(facing, ConveyorWall.LEFT, context),
				renderer.shouldRenderWall(facing, ConveyorWall.RIGHT, context),
				instance.getDyeColour()
		);
	}

	default Transformation modifyBaseRotationMatrix(Transformation matrix)
	{
		return matrix;
	}

	ResourceLocation getActiveTexture();

	ResourceLocation getInactiveTexture();

	default ResourceLocation getColouredStripesTexture()
	{
		return ConveyorHandler.textureConveyorColour;
	}

	/**
	 * @param renderType is null for item render
	 */
	List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, RenderContext<T> context, @Nullable RenderType renderType);

	/**
	 * @return whether the wall should be drawn on the model. Also used for they cache key
	 */
	default boolean shouldRenderWall(Direction facing, ConveyorWall wall, RenderContext<T> context)
	{
		T instance = context.instance();
		if(instance==null||instance.getConveyorDirection()!=ConveyorDirection.HORIZONTAL)
			return true;
		Direction side = wall.getWallSide(facing);
		BlockPos pos = instance.getBlockEntity().getBlockPos().relative(side);
		return ConveyorHandler.connectsToConveyor(instance.getBlockEntity().getLevel(), pos, side);
	}

	default void updateCachedModels(ModelBaker baker, Function<Material, TextureAtlasSprite> getTexture)
	{
	}

	record RenderContext<T extends IConveyorBelt>(
			IConveyorType<T> type,
			@Nullable T instance,
			Block coverFallback
	)
	{
		public Block getCover()
		{
			return IConveyorBelt.getCoverOrDefault(instance, coverFallback);
		}

		public Direction getFacing()
		{
			return instance==null?Direction.NORTH: instance.getFacing();
		}

		public ConveyorDirection getConveyorDirection()
		{
			return instance==null?ConveyorDirection.HORIZONTAL: instance.getConveyorDirection();
		}

		public boolean isActiveOr(boolean fallback)
		{
			return instance==null?fallback: instance.isActive();
		}
	}
}
