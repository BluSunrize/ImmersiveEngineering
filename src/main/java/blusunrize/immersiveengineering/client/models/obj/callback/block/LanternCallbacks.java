/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.common.blocks.metal.LanternBlock;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.stream.Collectors;

public class LanternCallbacks implements BlockCallback<Direction>
{
	public static final LanternCallbacks INSTANCE = new LanternCallbacks();

	private static final Map<Direction, IEObjState> DISPLAY_LISTS = Util.make(() -> {
		Map<Direction, VisibilityList> visibilityMap = ImmutableMap.<Direction, VisibilityList>builder()
				.put(Direction.DOWN, VisibilityList.show("base", "attach_t"))
				.put(Direction.UP, VisibilityList.show("base", "attach_b"))
				.put(Direction.NORTH, VisibilityList.show("base", "attach_n"))
				.put(Direction.SOUTH, VisibilityList.show("base", "attach_s"))
				.put(Direction.WEST, VisibilityList.show("base", "attach_w"))
				.put(Direction.EAST, VisibilityList.show("base", "attach_e"))
				.build();
		return visibilityMap.entrySet().stream()
				.map(e -> Pair.of(e.getKey(), new IEObjState(e.getValue())))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	});

	@Override
	public Direction extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return state.getValue(LanternBlock.FACING);
	}

	@Override
	public IEObjState getIEOBJState(Direction direction)
	{
		return DISPLAY_LISTS.get(direction);
	}

}
