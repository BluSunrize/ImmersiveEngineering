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
import junit.framework.TestCase;
import net.minecraft.core.Direction;
import org.junit.Assert;

public class RelativeBlockFaceTest extends TestCase
{
	public void testInverse()
	{
		for(Direction front : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			for(boolean mirror : new boolean[]{true, false})
			{
				for(Direction face : DirectionUtils.VALUES)
				{
					RelativeBlockFace relative = RelativeBlockFace.from(new MultiblockOrientation(front, mirror), face);
					Assert.assertEquals(face, relative.forFront(new MultiblockOrientation(front, mirror)));
				}
			}
		}
	}
}