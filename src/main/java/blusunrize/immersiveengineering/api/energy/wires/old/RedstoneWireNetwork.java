/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires.old;

import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
	}

	public void removeFromNetwork(IRedstoneConnector removedConnector)
	{
	}

	public static void updateConnectors(BlockPos start, World world, RedstoneWireNetwork network)
	{
	}

	public void updateValues()
	{
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
