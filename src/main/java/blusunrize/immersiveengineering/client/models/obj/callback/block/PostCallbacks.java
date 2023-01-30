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
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.block.PostCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.generic.PostBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static blusunrize.immersiveengineering.common.blocks.generic.PostBlock.HORIZONTAL_OFFSET;
import static blusunrize.immersiveengineering.common.blocks.generic.PostBlock.POST_SLAVE;

public class PostCallbacks implements BlockCallback<Key>
{
	public static final PostCallbacks INSTANCE = new PostCallbacks();

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		final int height = state.getValue(POST_SLAVE);
		BlockPos centerPos = pos.subtract(state.getValue(HORIZONTAL_OFFSET).getOffset());
		BlockState centerState = level.getBlockState(centerPos);
		EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
		EnumSet<Direction> downArms = EnumSet.noneOf(Direction.class);
		if(centerState.getBlock()==state.getBlock())
			for(Direction f : DirectionUtils.BY_HORIZONTAL_INDEX)
				if(PostBlock.hasConnection(centerState, f, level, centerPos))
				{
					connections.add(f);
					if(height==3)//Arms
					{
						BlockPos armPos = centerPos.relative(f);
						if(PostBlock.hasConnection(level.getBlockState(armPos), Direction.DOWN, level, armPos))
							downArms.add(f);
					}
				}
		return new Key(state, connections, downArms);
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key(
				WoodenDecoration.TREATED_POST.defaultBlockState(),
				EnumSet.noneOf(Direction.class), EnumSet.noneOf(Direction.class)
		);
	}

	@Override
	public IEObjState getIEOBJState(Key key)
	{
		List<String> visible = new ArrayList<>();
		visible.add("base");
		final int height = key.state().getValue(POST_SLAVE);
		// With model splitting it's enough to check the model state for the current layer, since none of the arm
		// models extend into other layers
		for(Direction f : key.connections())
		{
			String name = f.getOpposite().getSerializedName();
			if(height==3)//Arms
			{
				if(key.downArms().contains(f))
					visible.add("arm_"+name+"_down");
				else
					visible.add("arm_"+name+"_up");
			}
			else//Simple Connectors
				visible.add("con_"+(height-1)+"_"+name);
		}
		return new IEObjState(VisibilityList.show(visible));
	}

	public record Key(BlockState state, EnumSet<Direction> connections, EnumSet<Direction> downArms)
	{
	}
}
