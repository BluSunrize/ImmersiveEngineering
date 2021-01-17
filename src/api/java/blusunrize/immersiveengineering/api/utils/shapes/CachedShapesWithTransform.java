/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils.shapes;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CachedShapesWithTransform<ShapeKey, TransformKey> extends CachedVoxelShapes<Pair<ShapeKey, TransformKey>>
{
	public CachedShapesWithTransform(
			Function<ShapeKey, List<AxisAlignedBB>> creator,
			BiFunction<TransformKey, AxisAlignedBB, AxisAlignedBB> transform)
	{
		super(p -> {
			List<AxisAlignedBB> base = creator.apply(p.getLeft());
			if(base==null)
				return ImmutableList.of();
			List<AxisAlignedBB> ret = new ArrayList<>(base.size());
			for(AxisAlignedBB aabb : base)
				ret.add(transform.apply(p.getRight(), aabb));
			return ret;
		});
	}

	public VoxelShape get(ShapeKey shapeKey, TransformKey transformKey)
	{
		return get(Pair.of(shapeKey, transformKey));
	}

	public static CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>>
	createForMultiblock(Function<BlockPos, List<AxisAlignedBB>> create)
	{
		return new CachedShapesWithTransform<>(create,
				(key, box) -> withFacingAndMirror(box, key.getLeft(), key.getRight())
		);
	}

	public static AxisAlignedBB withFacingAndMirror(AxisAlignedBB in, Direction d, boolean mirror)
	{
		AxisAlignedBB mirrored = in;
		if(mirror)
			mirrored = new AxisAlignedBB(
					1-in.minX,
					in.minY,
					in.minZ,
					1-in.maxX,
					in.maxY,
					in.maxZ
			);
		return ShapeUtils.transformAABB(mirrored, d);
	}

	public static <T> CachedShapesWithTransform<T, Direction> createDirectional(Function<T, List<AxisAlignedBB>> create)
	{
		return new CachedShapesWithTransform<>(
				create,
				(key, box) -> ShapeUtils.transformAABB(box, key)
		);
	}
}
