package blusunrize.immersiveengineering.common.util.shapes;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CachedVoxelShapes<Key>
{
	private final Map<Key, VoxelShape> calculatedShapes = new ConcurrentHashMap<>();
	private final Function<Key, List<AxisAlignedBB>> creator;

	public CachedVoxelShapes(Function<Key, List<AxisAlignedBB>> creator)
	{
		this.creator = creator;
	}

	public VoxelShape get(Key k)
	{
		return calculatedShapes.computeIfAbsent(k, this::calculateShape);
	}

	private VoxelShape calculateShape(Key k)
	{
		List<AxisAlignedBB> subshapes = creator.apply(k);
		VoxelShape ret = VoxelShapes.empty();
		if(subshapes!=null)
			for(AxisAlignedBB aabb : subshapes)
				ret = VoxelShapes.combine(ret, VoxelShapes.create(aabb), IBooleanFunction.OR);
		return ret.simplify();
	}
}
