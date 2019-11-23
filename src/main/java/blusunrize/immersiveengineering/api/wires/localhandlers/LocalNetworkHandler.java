/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.localhandlers;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class LocalNetworkHandler
{
	private static final Map<ResourceLocation, Constructor<? extends LocalNetworkHandler>> TYPES = new HashMap<>();

	public static void register(ResourceLocation loc, Class<? extends LocalNetworkHandler> cl)
	{
		try
		{
			TYPES.put(loc, cl.getConstructor(LocalWireNetwork.class));
		} catch(NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static LocalNetworkHandler createHandler(ResourceLocation type, LocalWireNetwork net)
	{
		try
		{
			return TYPES.get(type).newInstance(net);
		} catch(InstantiationException|IllegalAccessException|InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected LocalWireNetwork net;

	protected LocalNetworkHandler(LocalWireNetwork net)
	{
		this.net = net;
	}

	public void setLocalNet(LocalWireNetwork net)
	{
		this.net = net;
	}

	public abstract LocalNetworkHandler merge(LocalNetworkHandler other);

	public abstract void onConnectorLoaded(ConnectionPoint p, IImmersiveConnectable iic);

	public abstract void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic);

	public abstract void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic);

	public abstract void onConnectionAdded(Connection c);

	public abstract void onConnectionRemoved(Connection c);
}
