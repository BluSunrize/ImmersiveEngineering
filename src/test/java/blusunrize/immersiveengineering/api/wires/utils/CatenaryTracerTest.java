/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.testutils.DummyWireType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CatenaryTracerTest
{
	private static final BlockPos OFFSET = new BlockPos(1, 2, 3);

	private void testIntegerIntersections(Vec3 start, Vec3 end, double slack, double... expected)
	{
		CatenaryTracer ct = create(start, end, slack);
		ct.calculateIntegerIntersections();
		assertEquals(new DoubleAVLTreeSet(expected), ct.integerIntersections);
	}

	@Test
	public void calculateIntegerIntersections()
	{
		testIntegerIntersections(new Vec3(.5, .9, -.5), new Vec3(.5, .9, .5), 0.001, 0, 0.5, 1);
		//Diagonal
		testIntegerIntersections(new Vec3(-.5, .9, -.5), new Vec3(.5, .9, .5), 0.001, 0, 0.5, 1);
		//Vertical
		testIntegerIntersections(new Vec3(0.5, -0.5, 0.5), new Vec3(0.5, 0.5, 0.5), 0.001, 0, 0.5, 1);
		//Y intersections
		CatenaryTracer ct = create(new Vec3(0.1, 0.01, 0.5), new Vec3(0.9, 0.01, 0.5), 0.05);
		ct.calculateIntegerIntersections();
		assertTrue(ct.integerIntersections.contains(0));
		assertTrue(ct.integerIntersections.contains(1));
		assertEquals(4, ct.integerIntersections.size());
		double sum = 0;
		for(double d : ct.integerIntersections)
			sum += d;
		assertTrue(Math.abs(sum-2) < 1e-5);
		//Only one branch exists
		for(int i = 0; i < 2; ++i)
		{
			ct = create(new Vec3(0.1+0.8*i, 0.5, 0.5), new Vec3(0.9-0.8*i, 5.5, 0.5), 0.0005);
			ct.calculateIntegerIntersections();
			assertTrue(ct.integerIntersections.contains(0));
			assertTrue(ct.integerIntersections.contains(1));
			assertEquals(5+2, ct.integerIntersections.size());
		}
	}

	@Test
	public void forEachSegment()
	{
		//TODO more tests?
		{
			CatenaryTracer ct = create(new Vec3(0.5, 0.125, 0.75), new Vec3(1.5, 0.125, 0.75), 0.005);
			ct.calculateIntegerIntersections();
			ct.forEachSegment(segment -> {
				double[] minDist = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
				for(Vec3 p : new Vec3[]{segment.relativeSegmentStart, segment.relativeSegmentEnd})
				{
					assertEquals(0.75+OFFSET.getZ(), p.z+segment.mainPos.getZ(), 1e-3);
					for(int i = 0; i < 3; ++i)
					{
						double pos = ApiUtils.getDim(p, i);
						double projection = Mth.clamp(pos, 0, 1);
						minDist[i] = Math.min(minDist[i], Math.abs(projection-pos));
					}
				}
				double eps = segment.inBlock?1e-5: 0.3;
				for(int i = 0; i < 3; ++i)
					assertTrue(minDist[i] < eps);
			});
		}
		{
			Object2IntMap<BlockPos> inCounts = new Object2IntOpenHashMap<>();
			CatenaryTracer ct = create(new Vec3(0.5, 0.5, 0.5), new Vec3(-0.5, 0.5, -0.5), 0.005);
			ct.calculateIntegerIntersections();
			ct.forEachSegment(seg -> {
				if(seg.inBlock)
					inCounts.computeInt(seg.mainPos, (bp, count) -> count==null?1: count+1);
			});
			Map<BlockPos, Integer> expectedBase = ImmutableMap.of(
					BlockPos.ZERO, 2,
					new BlockPos(-1, 0, -1), 2,
					new BlockPos(-1, 0, 0), 2,
					new BlockPos(0, 0, -1), 2
			);
			Object2IntMap<BlockPos> expected = new Object2IntOpenHashMap<>();
			for(Entry<BlockPos, Integer> x : expectedBase.entrySet())
				expected.put(x.getKey().offset(OFFSET), (int)x.getValue());
			assertEquals(expected, inCounts);
		}
	}

	private void testForEachCloseCoord(Vec3 center, double eps, Set<BlockPos> expected)
	{
		CatenaryTracer ct = create(Vec3.ZERO, new Vec3(0, 0, 1), 0.01);
		Set<BlockPos> actual = new HashSet<>();
		ct.forEachCloseCoordinate(center, eps, actual::add);
		assertEquals(new HashSet<>(expected), actual);
	}

	@Test
	public void forEachCloseCoordinate()
	{
		Set<BlockPos> center = ImmutableSet.of(BlockPos.ZERO);
		Set<BlockPos> cube2 = new HashSet<>();
		for(int x = -1; x < 1; ++x)
			for(int y = -1; y < 1; ++y)
				for(int z = -1; z < 1; ++z)
					cube2.add(new BlockPos(x, y, z));
		Set<BlockPos> cube3 = new HashSet<>();
		for(int x = -1; x <= 1; ++x)
			for(int y = -1; y <= 1; ++y)
				for(int z = -1; z <= 1; ++z)
					cube3.add(new BlockPos(x, y, z));
		testForEachCloseCoord(new Vec3(0.5, 0.5, 0.5), 0.1, center);
		testForEachCloseCoord(Vec3.ZERO, 0.5, cube2);
		testForEachCloseCoord(new Vec3(0.5, 0.5, 0.5), 0.5, center);
		testForEachCloseCoord(new Vec3(0.5, 0.5, 0.5), 0.500001, cube3);
	}

	private CatenaryTracer create(Vec3 start, Vec3 end, double slack)
	{
		WireType type = new DummyWireType(slack);
		BlockPos posStart = BlockPos.containing(start);
		BlockPos posEnd = BlockPos.containing(end);
		Connection conn = new Connection(
				type,
				new ConnectionPoint(posStart, 0),
				new ConnectionPoint(posEnd, 0),
				start.subtract(Vec3.atLowerCornerOf(posStart)),
				end.subtract(Vec3.atLowerCornerOf(posEnd))
		);
		return new CatenaryTracer(conn.getCatenaryData(), OFFSET);
	}

}
