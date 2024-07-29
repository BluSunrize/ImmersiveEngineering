/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.orientation;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RelativeBlockFaceTest
{
	@Test
	public void testInverse()
	{
		for(Direction front : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			for(boolean mirror : new boolean[]{true, false})
			{
				for(Direction face : DirectionUtils.VALUES)
				{
					RelativeBlockFace relative = RelativeBlockFace.from(new MultiblockOrientation(front, mirror), face);
					assertEquals(face, relative.forFront(new MultiblockOrientation(front, mirror)));
				}
			}
		}
	}
}