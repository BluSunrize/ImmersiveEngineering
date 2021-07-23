/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.shapes;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CachedShapesWithTransform<ShapeKey, TransformKey> extends CachedVoxelShapes<Pair<ShapeKey, TransformKey>>
{
	public CachedShapesWithTransform(
			Function<ShapeKey, List<AABB>> creator,
			BiFunction<TransformKey, AABB, AABB> transform)
	{
		super(p -> {
			List<AABB> base = creator.apply(p.getLeft());
			if(base==null)
				return ImmutableList.of();
			List<AABB> ret = new ArrayList<>(base.size());
			for(AABB aabb : base)
				ret.add(transform.apply(p.getRight(), aabb));
			return ret;
		});
	}

	public VoxelShape get(ShapeKey shapeKey, TransformKey transformKey)
	{
		return get(Pair.of(shapeKey, transformKey));
	}

	public static CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>>
	createForMultiblock(Function<BlockPos, List<AABB>> create)
	{
		return new CachedShapesWithTransform<>(create,
				(key, box) -> withFacingAndMirror(box, key.getLeft(), key.getRight())
		);
	}

	public static AABB withFacingAndMirror(AABB in, Direction d, boolean mirror)
	{
		AABB mirrored = in;
		if(mirror)
			mirrored = new AABB(1-in.minX, in.minY, in.minZ, 1-in.maxX, in.maxY, in.maxZ);
		return ShapeUtils.transformAABB(mirrored, d);
	}

	public static <T> CachedShapesWithTransform<T, Direction> createDirectional(Function<T, List<AABB>> create)
	{
		return new CachedShapesWithTransform<>(
				create,
				(key, box) -> ShapeUtils.transformAABB(box, key)
		);
	}
}
