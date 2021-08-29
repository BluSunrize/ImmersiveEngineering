package blusunrize.immersiveengineering.api.tool.conveyor;

import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.List;

public interface IConveyorClientData<T extends IConveyorBelt>
{
	/**
	 * @param context
	 * @return the string by which unique models would be cached. Override for additional appended information*
	 * The model class will also append to this key for rendered walls and facing
	 */
	default String getModelCacheKey(RenderContext<T> context)
	{
		T instance = context.instance();
		String key = "";
		Block cover = context.getCover();
		if(cover!=Blocks.AIR)
			key += "s"+cover.getRegistryName();
		if(instance==null)
			return key;
		//TODO return record instead?
		Direction facing = instance.getFacing();
		key += "f"+facing.ordinal();
		key += "d"+instance.getConveyorDirection().ordinal();
		key += "a"+(instance.isActive()?1: 0);
		key += "w0"+(shouldRenderWall(facing, 0, context)?1: 0);
		key += "w1"+(shouldRenderWall(facing, 1, context)?1: 0);
		key += "c"+instance.getDyeColour();
		return key;
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

	default List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, RenderContext<T> context)
	{
		return baseModel;
	}

	/**
	 * @param wall    0 is left, 1 is right
	 * @param context
	 * @return whether the wall should be drawn on the model. Also used for they cache key
	 */
	default boolean shouldRenderWall(Direction facing, int wall, RenderContext<T> context)
	{
		T instance = context.instance();
		if(instance==null||instance.getConveyorDirection()!=ConveyorDirection.HORIZONTAL)
			return true;
		Direction side = wall==0?facing.getCounterClockWise(): facing.getClockWise();
		BlockPos pos = instance.getBlockEntity().getBlockPos().relative(side);
		return ConveyorHandler.connectsToConveyor(instance.getBlockEntity().getLevel(), pos, side);
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
