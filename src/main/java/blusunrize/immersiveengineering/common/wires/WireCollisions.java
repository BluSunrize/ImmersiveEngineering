/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireCollisionData;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.api.wires.localhandlers.ICollisionHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WireCollisions
{
	public static void handleEntityCollision(BlockPos p, Entity e)
	{
		if(!e.world.isRemote&&IEConfig.CACHED.wireDamage&&e instanceof LivingEntity&&
				!e.isInvulnerableTo(IEDamageSources.wireShock)&&
				!(e instanceof PlayerEntity&&((PlayerEntity)e).abilities.disableDamage))
		{
			GlobalWireNetwork global = GlobalWireNetwork.getNetwork(e.world);
			WireCollisionData wireData = global.getCollisionData();
			Collection<WireCollisionData.CollisionInfo> atBlock = wireData.getCollisionInfo(p);
			for(CollisionInfo info : atBlock)
			{
				LocalWireNetwork local = info.getLocalNet();
				for(LocalNetworkHandler h : local.getAllHandlers())
					if(h instanceof ICollisionHandler)
						((ICollisionHandler)h).onCollided((LivingEntity)e, p, info);
			}
		}
	}

	public static void notifyBlockUpdate(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState oldState, @Nonnull BlockState newState, int flags)
	{
		if(IEConfig.CACHED.blocksBreakWires&&!worldIn.isRemote&&(flags&1)!=0&&!newState.getCollisionShape(worldIn, pos).isEmpty())
		{
			GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(worldIn);
			Collection<CollisionInfo> data = globalNet.getCollisionData().getCollisionInfo(pos);
			if(!data.isEmpty())
			{
				Map<Connection, BlockPos> toBreak = new HashMap<>();
				for(CollisionInfo info : data)
					if(info.isInBlock)
					{
						Vector3d vecA = info.conn.getPoint(0, info.conn.getEndA());
						if(Utils.isVecInBlock(vecA, pos, info.conn.getEndA().getPosition(), 1e-3))
							continue;
						Vector3d vecB = info.conn.getPoint(0, info.conn.getEndB());
						if(Utils.isVecInBlock(vecB, pos, info.conn.getEndB().getPosition(), 1e-3))
							continue;
						BlockPos dropPos = pos;
						if(WireUtils.preventsConnection(worldIn, pos, newState, info.intersectA, info.intersectB))
						{
							for(Direction f : Direction.VALUES)
								if(worldIn.isAirBlock(pos.offset(f)))
								{
									dropPos = dropPos.offset(f);
									break;
								}
							toBreak.put(info.conn, dropPos);
						}
					}
				for(Entry<Connection, BlockPos> b : toBreak.entrySet())
					globalNet.removeAndDropConnection(b.getKey(), b.getValue(), worldIn);
			}
		}
	}
}
