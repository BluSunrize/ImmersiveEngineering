package blusunrize.immersiveengineering.api.wires.tile;

import blusunrize.immersiveengineering.api.IEProperties.ConnectionModelData;
import blusunrize.immersiveengineering.api.wires.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConnectorTileCalls
{
	public static ConnectionModelData getModelData(World world, IImmersiveConnectable connector)
	{
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
		Set<Connection> ret = new HashSet<>();
		for(ConnectionPoint cp : connector.getConnectionPoints())
		{
			LocalWireNetwork local = globalNet.getLocalNet(cp);
			Collection<Connection> conns = local.getConnections(cp);
			if(conns==null)
			{
				WireLogger.logger.warn("Aborting and returning empty data: null connections at {}", cp);
				return new ConnectionModelData(ImmutableSet.of(), connector.getPosition());
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
		return new ConnectionModelData(ret, connector.getPosition());
	}

	public static void onLoad(GlobalWireNetwork globalNet, IImmersiveConnectable iic, World world)
	{
		globalNet.onConnectorLoad(iic, world);
	}

	public static void onChunkUnloaded(GlobalWireNetwork globalNet, IImmersiveConnectable iic)
	{
		globalNet.onConnectorUnload(iic);
	}

	public static void remove(GlobalWireNetwork globalNet, IImmersiveConnectable iic)
	{
		globalNet.removeConnector(iic);
	}
}
