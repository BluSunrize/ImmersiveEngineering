/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeedthroughBlock extends MiscConnectorBlock
{
	public FeedthroughBlock()
	{
		super("feedthrough", () -> FeedthroughTileEntity.TYPE,
				//TODO maybe clean up a bit
				ImmutableList.of(IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED),
				Arrays.asList(BlockRenderLayer.values())
		);
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof FeedthroughTileEntity&&!world.isRemote&&newState.getBlock()!=state.getBlock())
		{
			FeedthroughTileEntity feedthrough = (FeedthroughTileEntity)tile;
			if(!feedthrough.currentlyDisassembling)
			{
				Direction dir = feedthrough.getFacing();
				//TODO sign is probably wrong somewhere
				BlockPos centerPos = pos.offset(dir, -feedthrough.offset);
				//Middle block last, since that has the actual connections
				for(int offset : new int[]{-1, 1, 0})
				{
					if(offset==feedthrough.offset)
						continue;
					BlockPos posForOffset = centerPos.offset(dir, offset);
					TileEntity tileAtOffset = world.getTileEntity(posForOffset);
					if(tileAtOffset instanceof FeedthroughTileEntity)
						((FeedthroughTileEntity)tileAtOffset).currentlyDisassembling = true;
					if(offset==0)
						world.setBlockState(posForOffset, feedthrough.stateForMiddle);
					else
					{
						BlockState connector = WireApi.INFOS.get(feedthrough.reference).conn.get()
								.with(IEProperties.FACING_ALL, offset < 0?dir: dir.getOpposite());
						ConnectionPoint cpOnFeedthrough = new ConnectionPoint(centerPos,
								FeedthroughTileEntity.getIndexForOffset(offset));
						GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
						List<Connection> removedConnections = new ArrayList<>();
						global.removeAllConnectionsAt(cpOnFeedthrough, removedConnections::add);
						world.setBlockState(posForOffset, connector);
						ConnectionPoint newEnd = new ConnectionPoint(posForOffset, 0);
						for(Connection c : removedConnections)
							global.addConnection(new Connection(c.type, newEnd, c.getOtherEnd(cpOnFeedthrough)));
					}
				}
			}
		}
		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
	{
		//NOP
	}
}
