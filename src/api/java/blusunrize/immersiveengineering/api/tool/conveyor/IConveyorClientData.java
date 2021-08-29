package blusunrize.immersiveengineering.api.tool.conveyor;

import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public interface IConveyorClientData<T extends IConveyorBelt>
{
	/**
	 * @return the string by which unique models would be cached. Override for additional appended information*
	 * The model class will also append to this key for rendered walls and facing
	 */
	default String getModelCacheKey(IConveyorType<T> type, @Nullable T instance)
	{
		if(instance==null)
			return "__default__";
		//TODO return record instead?
		String key = instance.getType().getId().toString();
		Direction facing = instance.getFacing();
		key += "f"+facing.ordinal();
		key += "d"+instance.getConveyorDirection().ordinal();
		key += "a"+(instance.isActive()?1: 0);
		key += "w0"+(renderWall(facing, 0, instance)?1: 0);
		key += "w1"+(renderWall(facing, 1, instance)?1: 0);
		key += "c"+instance.getDyeColour();
		return key;
	}

	default Transformation modifyBaseRotationMatrix(Transformation matrix, @Nullable T conveyor)
	{
		return matrix;
	}

	ResourceLocation getActiveTexture();

	ResourceLocation getInactiveTexture();

	default ResourceLocation getColouredStripesTexture()
	{
		return ConveyorHandler.textureConveyorColour;
	}

	default List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, IConveyorType<T> type, @Nullable T conveyor)
	{
		return baseModel;
	}

	/**
	 * @param wall 0 is left, 1 is right
	 * @return whether the wall should be drawn on the model. Also used for they cache key
	 */
	default boolean renderWall(Direction facing, int wall, @Nullable T instance)
	{
		if(instance==null||instance.getConveyorDirection()!=ConveyorDirection.HORIZONTAL)
			return true;
		Direction side = wall==0?facing.getCounterClockWise(): facing.getClockWise();
		BlockPos pos = instance.getBlockEntity().getBlockPos().relative(side);
		return ConveyorHandler.connectsToConveyor(instance.getBlockEntity().getLevel(), pos, side);
	}
}
