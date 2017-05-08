package blusunrize.immersiveengineering.api.energy.wires.redstone;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
		for(WeakReference<IRedstoneConnector> connectorRef : wireNetwork.connectors)
		{
			IRedstoneConnector connector = connectorRef.get();
			if(connector != null)
				connector.setNetwork(add(connector));
		}
		for(WeakReference<IRedstoneConnector> connectorRef : wireNetwork.connectors)
		{
			IRedstoneConnector connector = connectorRef.get();
			if(connector != null)
				connector.onChange();
		}
		updateValues();
	}

	public void removeFromNetwork(IRedstoneConnector removedConnector)
	{
		BlockPos removedCC = Utils.toCC(removedConnector);
		for(WeakReference<IRedstoneConnector> connectorRef : connectors)
		{
			IRedstoneConnector connector = connectorRef.get();
			if(connector != null)
				connector.setNetwork(new RedstoneWireNetwork().add(connector));
		}
		for(WeakReference<IRedstoneConnector> connectorRef : connectors)
		{
			IRedstoneConnector connector = connectorRef.get();
			if(connector != null)
			{
				BlockPos conCC = Utils.toCC(connector);
				Set<ImmersiveNetHandler.Connection> connections = ImmersiveNetHandler.INSTANCE.getConnections(connector.getConnectorWorld(), conCC);
				if(connections != null)
					for(ImmersiveNetHandler.Connection connection : connections)
					{
						BlockPos node = connection.start;
						if(node.equals(conCC))
							node = connection.end;
						if(!node.equals(removedCC))
						{
							TileEntity nodeTile = connector.getConnectorWorld().getTileEntity(node);
							if(nodeTile instanceof IRedstoneConnector)
								if(connector.getNetwork() != ((IRedstoneConnector) nodeTile).getNetwork())
									connector.getNetwork().mergeNetwork(((IRedstoneConnector) nodeTile).getNetwork());
						}
					}
				connector.onChange();
			}
		}
	}

	public void updateValues()
	{
		byte[] oldValues = channelValues;
		channelValues = new byte[16];
		for(WeakReference<IRedstoneConnector> connectorRef : connectors)
		{
			IRedstoneConnector connector = connectorRef.get();
			if(connector != null)
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
				if(connector != null)
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
			values[i] = (byte) (channelValues[i] * 16);
		return values;
	}
}
