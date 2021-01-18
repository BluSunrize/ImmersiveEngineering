/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.redstone;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class RedstoneNetworkHandler extends LocalNetworkHandler
{
	public static final ResourceLocation ID = new ResourceLocation(Lib.MODID, "redstone");
	private byte[] values = new byte[16];

	public RedstoneNetworkHandler(LocalWireNetwork local, GlobalWireNetwork global)
	{
		super(local, global);
	}

	@Override
	public LocalNetworkHandler merge(LocalNetworkHandler other)
	{
		if(!(other instanceof RedstoneNetworkHandler))
			return new RedstoneNetworkHandler(localNet, globalNet);
		RedstoneNetworkHandler otherRS = (RedstoneNetworkHandler)other;
		RedstoneNetworkHandler ret = new RedstoneNetworkHandler(localNet, globalNet);
		for(int i = 0; i < 16; ++i)
			ret.values[i] = (byte)Math.max(values[i], otherRS.values[i]);
		return ret;
	}

	@Override
	public void onConnectorLoaded(ConnectionPoint newCP, IImmersiveConnectable iic)
	{
		if(!(iic instanceof IRedstoneConnector))
			return;
		localNet.addAsFutureTask(() -> {
			IRedstoneConnector rsConn = (IRedstoneConnector)iic;
			rsConn.updateInput(values, newCP);
			for(ConnectionPoint cp : localNet.getConnectionPoints())
			{
				IImmersiveConnectable here = localNet.getConnector(cp);
				if(here instanceof IRedstoneConnector)
					((IRedstoneConnector)here).onChange(cp, this);
			}
		});
	}

	public void updateValues()
	{
		values = new byte[16];
		for(ConnectionPoint cp : localNet.getConnectionPoints())
		{
			IImmersiveConnectable here = localNet.getConnector(cp);
			if(here instanceof IRedstoneConnector)
				((IRedstoneConnector)here).updateInput(values, cp);
		}
		for(ConnectionPoint cp : localNet.getConnectionPoints())
		{
			IImmersiveConnectable here = localNet.getConnector(cp);
			if(here instanceof IRedstoneConnector)
				((IRedstoneConnector)here).onChange(cp, this);
		}
	}

	@Override
	public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic)
	{
		updateValues();
	}

	@Override
	public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic)
	{
		updateValues();
	}

	@Override
	public void onConnectionAdded(Connection c)
	{
	}

	@Override
	public void onConnectionRemoved(Connection c)
	{
	}

	@Override
	public void setLocalNet(LocalWireNetwork net)
	{
		super.setLocalNet(net);
		for(ConnectionPoint cp : net.getConnectionPoints())
		{
			IImmersiveConnectable here = net.getConnector(cp);
			if(here instanceof IRedstoneConnector)
				((IRedstoneConnector)here).onChange(cp, this);
		}
	}

	public byte getValue(int redstoneChannel)
	{
		return values[redstoneChannel];
	}
}
