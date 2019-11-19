/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireCollisionData;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.api.wires.localhandlers.ICollisionHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

//TODO IWorldEventListener seems to be gone/hardcoded now, so we need an ASM hook for notifyBlockUpdate as well now. Or
// a Forge PR.
public class WireCollisions
{
	public static void handleEntityCollision(BlockPos p, Entity e)
	{
		if(!e.world.isRemote&&IEConfig.WIRES.enableWireDamage.get()&&e instanceof LivingEntity&&
				!e.isInvulnerableTo(IEDamageSources.wireShock)&&
				!(e instanceof PlayerEntity&&((PlayerEntity)e).abilities.disableDamage))
		{
			GlobalWireNetwork global = GlobalWireNetwork.getNetwork(e.world);
			WireCollisionData wireData = global.getCollisionData();
			Collection<WireCollisionData.CollisionInfo> atBlock = wireData.getCollisionInfo(p);
			if(atBlock!=null)
				for(WireCollisionData.CollisionInfo info : atBlock)
				{
					LocalWireNetwork local = info.getLocalNet();
					for(LocalNetworkHandler h : local.getAllHandlers())
						if(h instanceof ICollisionHandler)
							((ICollisionHandler)h).onCollided((LivingEntity)e, p, info);
				}
		}
	}

	public void notifyBlockUpdate(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState oldState, @Nonnull BlockState newState, int flags)
	{
		if(!worldIn.isRemote&&(flags&1)!=0&&!newState.getCollisionShape(worldIn, pos).isEmpty())
		{
			GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(worldIn);
			Collection<CollisionInfo> data = globalNet.getCollisionData().getCollisionInfo(pos);
			if(data!=null)
			{
				Collection<Pair<Connection, BlockPos>> toBreak = new ArrayList<>();
				for(CollisionInfo info : data)
					if(info.isInBlock)
					{
						Vec3d vecA = info.conn.getPoint(0, info.conn.getEndA());
						if(Utils.isVecInBlock(vecA, pos, info.conn.getEndA().getPosition(), 1e-3))
							continue;
						Vec3d vecB = info.conn.getPoint(0, info.conn.getEndB());
						if(Utils.isVecInBlock(vecB, pos, info.conn.getEndB().getPosition(), 1e-3))
							continue;
						BlockPos dropPos = pos;
						if(ApiUtils.preventsConnection(worldIn, pos, newState, info.intersectA, info.intersectB))
						{
							for(Direction f : Direction.VALUES)
								if(worldIn.isAirBlock(pos.offset(f)))
								{
									dropPos = dropPos.offset(f);
									break;
								}
							toBreak.add(new ImmutablePair<>(info.conn, dropPos));
						}
					}
				for(Pair<Connection, BlockPos> b : toBreak)
					globalNet.removeAndDropConnection(b.getLeft(), b.getRight());
			}
		}
	}
}
