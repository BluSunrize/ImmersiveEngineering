/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.redstone;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class RedstoneNetworkHandler extends LocalNetworkHandler implements IWorldTickable
{
	public static final ResourceLocation ID = IEApi.ieLoc("redstone");
	private byte[] totalValues = new byte[16];
	private final Map<ConnectionPoint, byte[]> emittedValues = new HashMap<>();
	private boolean updateNextTick = false;

	public RedstoneNetworkHandler(LocalWireNetwork local, GlobalWireNetwork global)
	{
		super(local, global);
	}


	@Override
	public void update(Level w)
	{
		if(updateNextTick)
		{
			updateValues();
			updateNextTick = false;
		}
	}

	@Override
	public LocalNetworkHandler merge(LocalNetworkHandler other)
	{
		if(!(other instanceof RedstoneNetworkHandler otherRS))
			return new RedstoneNetworkHandler(localNet, globalNet);
		RedstoneNetworkHandler ret = new RedstoneNetworkHandler(localNet, globalNet);
		fillWithMax(totalValues, otherRS.totalValues, ret.totalValues);
		ret.emittedValues.putAll(this.emittedValues);
		ret.emittedValues.putAll(otherRS.emittedValues);
		return ret;
	}

	@Override
	public void onConnectorLoaded(ConnectionPoint newCP, IImmersiveConnectable iic)
	{
		if(!(iic instanceof IRedstoneConnector rsConn))
			return;
		localNet.addAsFutureTask(() -> {
			byte[] emitted = getEmitted(rsConn, newCP);
			fillWithMax(emitted, totalValues, totalValues);
			emittedValues.put(newCP, emitted);
			for(ConnectionPoint cp : localNet.getConnectionPoints())
				if(localNet.getConnector(cp) instanceof IRedstoneConnector here)
					here.onChange(cp, this);
		});
	}

	public void updateValues()
	{
		totalValues = new byte[16];
		emittedValues.clear();
		for(ConnectionPoint cp : localNet.getConnectionPoints())
			if(localNet.getConnector(cp) instanceof IRedstoneConnector here)
			{
				byte[] output = getEmitted(here, cp);
				emittedValues.put(cp, output);
				fillWithMax(output, totalValues, totalValues);
			}
		for(ConnectionPoint cp : localNet.getConnectionPoints())
			if(localNet.getConnector(cp) instanceof IRedstoneConnector here)
				here.onChange(cp, this);
	}

	@Override
	public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic)
	{
		updateNextTick = true;
	}

	@Override
	public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic)
	{
		updateNextTick = true;
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
			if(net.getConnector(cp) instanceof IRedstoneConnector here)
				here.onChange(cp, this);
	}

	public byte getValue(int redstoneChannel)
	{
		return totalValues[redstoneChannel];
	}

	public byte[] getValuesExcluding(ConnectionPoint excluded)
	{
		byte[] ret = new byte[16];
		for(Map.Entry<ConnectionPoint, byte[]> entry : emittedValues.entrySet())
			if(!entry.getKey().equals(excluded))
				fillWithMax(entry.getValue(), ret, ret);
		return ret;
	}

	private static byte[] getEmitted(IRedstoneConnector connector, ConnectionPoint cp)
	{
		byte[] ret = new byte[16];
		connector.updateInput(ret, cp);
		return ret;
	}

	private static void fillWithMax(byte[] inA, byte[] inB, byte[] out)
	{
		Preconditions.checkArgument(inA.length==16);
		Preconditions.checkArgument(inB.length==16);
		Preconditions.checkArgument(out.length==16);
		for(int i = 0; i < 16; ++i)
			out[i] = (byte)Math.max(inA[i], inB[i]);
	}
}
