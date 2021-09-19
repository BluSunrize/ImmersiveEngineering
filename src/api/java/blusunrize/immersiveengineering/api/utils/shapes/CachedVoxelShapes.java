/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.utils.shapes;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CachedVoxelShapes<Key>
{
	private final Map<Key, VoxelShape> calculatedShapes = new ConcurrentHashMap<>();
	private final Function<Key, List<AABB>> creator;

	public CachedVoxelShapes(Function<Key, List<AABB>> creator)
	{
		this.creator = creator;
	}

	public VoxelShape get(Key k)
	{
		return calculatedShapes.computeIfAbsent(k, this::calculateShape);
	}

	private VoxelShape calculateShape(Key k)
	{
		List<AABB> subshapes = creator.apply(k);
		VoxelShape ret = Shapes.empty();
		if(subshapes!=null)
			for(AABB aabb : subshapes)
				ret = Shapes.joinUnoptimized(ret, Shapes.create(aabb), BooleanOp.OR);
		return ret.optimize();
	}
}
