/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.IEProperties.ConnectionModelData;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ConnectorTileHelper
{
	public static ConnectionModelData genConnBlockState(
			GlobalWireNetwork globalNet, IImmersiveConnectable iic, World world
	)
	{
		final BlockPos pos = iic.getPosition();
		Set<Connection> ret = new HashSet<>();
		for(ConnectionPoint cp : iic.getConnectionPoints())
		{
			LocalWireNetwork local = globalNet.getLocalNet(cp);
			Collection<Connection> conns = local.getConnections(cp);
			if(conns==null)
			{
				WireLogger.logger.warn("Aborting and returning empty data: null connections at {}", cp);
				return new ConnectionModelData(ImmutableSet.of(), pos);
			}
			//TODO change model data to only include catenary (a, oX, oY) and number of vertices to render
			for(Connection c : conns)
			{
				ConnectionPoint other = c.getOtherEnd(cp);
				if(!c.isInternal())
				{
					IImmersiveConnectable otherConnector = globalNet.getLocalNet(other).getConnector(other);
					if(otherConnector!=null&&!otherConnector.isProxy())
					{
						// generate subvertices
						c.generateCatenaryData(world);
						ret.add(c);
					}
				}
			}
		}
		return new ConnectionModelData(ret, pos);
	}

	public static void onChunkUnload(GlobalWireNetwork globalNet, IImmersiveConnectable iic)
	{
		globalNet.onConnectorUnload(iic);
	}

	public static void onChunkLoad(GlobalWireNetwork globalNet, IImmersiveConnectable iic, World world)
	{
		globalNet.onConnectorLoad(iic, world);
	}

	public static void remove(World world, IImmersiveConnectable iic)
	{
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
		if(!world.isRemote)
		{
			BlockPos pos = iic.getPosition();
			Consumer<Connection> dropHandler;
			if(world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
				dropHandler = (c) -> {
					if(!c.isInternal())
					{
						BlockPos end = c.getOtherEnd(c.getEndFor(pos)).getPosition();
						double dx = pos.getX()+.5+Math.signum(end.getX()-pos.getX());
						double dy = pos.getY()+.5+Math.signum(end.getY()-pos.getY());
						double dz = pos.getZ()+.5+Math.signum(end.getZ()-pos.getZ());
						world.addEntity(new ItemEntity(world, dx, dy, dz, c.type.getWireCoil(c)));
					}
				};
			else
				dropHandler = c -> {
				};
			for(ConnectionPoint cp : iic.getConnectionPoints())
				globalNet.removeAllConnectionsAt(cp, dropHandler);
		}
		globalNet.removeConnector(iic);
	}

	public static LocalWireNetwork getLocalNetWithCache(
			GlobalWireNetwork globalNet, BlockPos pos, int cpIndex, Int2ObjectMap<LocalWireNetwork> cachedLocalNets
	)
	{
		LocalWireNetwork ret = cachedLocalNets.get(cpIndex);
		ConnectionPoint cp = new ConnectionPoint(pos, cpIndex);
		if(ret==null||!ret.isValid(cp))
		{
			ret = globalNet.getLocalNet(cp);
			cachedLocalNets.put(cpIndex, ret);
		}
		return ret;
	}
}
