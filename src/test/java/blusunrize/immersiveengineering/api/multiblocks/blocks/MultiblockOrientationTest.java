package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import junit.framework.TestCase;
import net.minecraft.core.BlockPos;
import org.junit.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiblockOrientationTest extends TestCase
{
	public void testInverse()
	{
		final BlockPos testPosRelative = new BlockPos(1, 2, 3);
		Set<BlockPos> seenPositions = new HashSet<>();
		for(final var mirrored : List.of(false, true))
			for(final var front : DirectionUtils.BY_HORIZONTAL_INDEX)
			{
				final var orientation = new MultiblockOrientation(front, mirrored);
				final var absolute = orientation.getAbsoluteOffset(testPosRelative);
				Assert.assertTrue(seenPositions.add(absolute));
				Assert.assertEquals(mirrored+", "+front, testPosRelative, orientation.getPosInMB(absolute));
			}
	}
}