/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires.redstone;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.*;

import static blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.INSTANCE;
import static blusunrize.immersiveengineering.api.energy.wires.WireType.REDSTONE_CATEGORY;

public class RedstoneWireNetwork
{
	public byte[] channelValues = new byte[16];
	public List<WeakReference<IRedstoneConnector>> connectors = new ArrayList<>();

	public RedstoneWireNetwork add(IRedstoneConnector connector)
	{
		connectors.add(new WeakReference<>(connector));
		return this;
	}

	public void mergeNetwork(RedstoneWireNetwork wireNetwork)
	{
		List<WeakReference<IRedstoneConnector>> conns = null;
		if(connectors.size() > 0)
			conns = connectors;
		else if(wireNetwork.connectors.size() > 0)
			conns = wireNetwork.connectors;
		if(conns==null)//No connectors to merge
			return;
		IRedstoneConnector start = null;
		for(WeakReference<IRedstoneConnector> conn : conns)
			if(conn.get()!=null)
			{
				start = conn.get();
				break;
			}
		if(start!=null)
		{
			BlockPos startPos = Utils.toCC(start);
			updateConnectors(startPos, start.getConnectorWorld(), this);
			updateValues();
		}
	}

	public void removeFromNetwork(IRedstoneConnector removedConnector)
	{
		Iterator<WeakReference<IRedstoneConnector>> iterator = connectors.iterator();
		Set<RedstoneWireNetwork> knownNets = new HashSet<>();
		while(iterator.hasNext())
		{
			WeakReference<IRedstoneConnector> conn = iterator.next();
			IRedstoneConnector start = conn.get();
			if(start!=null&&!knownNets.contains(start.getNetwork()))
			{
				RedstoneWireNetwork newNet = new RedstoneWireNetwork();
				updateConnectors(Utils.toCC(start), start.getConnectorWorld(), newNet);
				knownNets.add(newNet);
			}
		}

	}

	public static void updateConnectors(BlockPos start, World world, RedstoneWireNetwork network)
	{
		int dimension = world.provider.getDimension();
		Set<BlockPos> open = new HashSet<>();
		open.add(start);
		Set<BlockPos> closed = new HashSet<>();
		network.connectors.clear();
		while(!open.isEmpty())
		{
			Iterator<BlockPos> it = open.iterator();
			BlockPos next = it.next();
			it.remove();
			IImmersiveConnectable iic = ApiUtils.toIIC(next, world);
			closed.add(next);
			Set<Connection> connsAtBlock = INSTANCE.getConnections(dimension, next);
			if(iic instanceof IRedstoneConnector)
			{
				((IRedstoneConnector)iic).setNetwork(network);
				network.connectors.add(new WeakReference<>((IRedstoneConnector)iic));
			}
			if(connsAtBlock!=null&&iic!=null)
				for(Connection c : connsAtBlock)
				{
					if(iic.allowEnergyToPass(c)&&
							REDSTONE_CATEGORY.equals(c.cableType.getCategory())&&
							!closed.contains(c.end))
						open.add(c.end);
				}
		}
		network.channelValues = null;
		network.updateValues();
	}

	public void updateValues()
	{
		byte[] oldValues = channelValues;
		channelValues = new byte[16];
		for(WeakReference<IRedstoneConnector> connectorRef : connectors)
		{
			IRedstoneConnector connector = connectorRef.get();
			if(connector!=null)
			{
//						if (ProjectRedAPI.transmissionAPI != null)
//						{
//							for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
//							{
//								byte[] values = ProjectRedAPI.transmissionAPI.getBundledInput(connector.getWorldObj(), connector.xCoord, connector.yCoord, connector.zCoord, direction.getOpposite().ordinal());
//								if (values != null)
//								{
//									for (int i = 0; i < values.length; i++)
//									{
//										channelValues[i] = (byte) Math.max((values[i] & 255) / 16f, channelValues[i]);
//									}
//								}
//							}
//						}
//						if (Loader.isModLoaded("ComputerCraft")) CCCompat.updateRedstoneValues(this, connector);
				connector.updateInput(channelValues);
			}
		}
		if(!Arrays.equals(oldValues, channelValues))
			for(WeakReference<IRedstoneConnector> connectorRef : connectors)
			{
				IRedstoneConnector connector = connectorRef.get();
				if(connector!=null)
					connector.onChange();
			}
	}

	public int getPowerOutput(int redstoneChannel)
	{
		return channelValues[redstoneChannel];
	}


	public byte[] getByteValues()
	{
		byte[] values = new byte[16];
		for(int i = 0; i < values.length; i++)
			values[i] = (byte)(channelValues[i]*16);
		return values;
	}
}
