/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiblockOrientationTest
{
	@Test
	public void testInverse()
	{
		final BlockPos testPosRelative = new BlockPos(1, 2, 3);
		Set<BlockPos> seenPositions = new HashSet<>();
		for(final boolean mirrored : List.of(false, true))
			for(final Direction front : DirectionUtils.BY_HORIZONTAL_INDEX)
			{
				final MultiblockOrientation orientation = new MultiblockOrientation(front, mirrored);
				final BlockPos absolute = orientation.getAbsoluteOffset(testPosRelative);
				assertTrue(seenPositions.add(absolute));
				assertEquals(testPosRelative, orientation.getPosInMB(absolute), mirrored+", "+front);
			}
	}

	@Test
	public void testMatched()
	{
		final BlockPos testPos = new BlockPos(1, 2, 3);
		final Vec3 testVec = Vec3.atCenterOf(testPos);
		for(final boolean mirrored : List.of(false, true))
			for(final Direction front : DirectionUtils.BY_HORIZONTAL_INDEX)
			{
				final MultiblockOrientation orientation = new MultiblockOrientation(front, mirrored);
				final BlockPos transformedPos = orientation.getAbsoluteOffset(testPos);
				final Vec3 transformedVec = orientation.getAbsoluteOffset(testVec);
				final Vec3 delta = transformedVec.subtract(Vec3.atCenterOf(transformedPos));
				assertEquals(delta.lengthSqr(), 0, 1e-3);
			}
	}
}