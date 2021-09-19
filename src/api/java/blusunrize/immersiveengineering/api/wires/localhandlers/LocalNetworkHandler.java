/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.localhandlers;

import blusunrize.immersiveengineering.api.wires.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public abstract class LocalNetworkHandler
{
	private static final Map<ResourceLocation, ILocalHandlerConstructor> TYPES = new HashMap<>();

	public static void register(ResourceLocation loc, ILocalHandlerConstructor constructor)
	{
		TYPES.put(loc, constructor);
	}

	//TODO make non-API?
	public static LocalNetworkHandler createHandler(ResourceLocation type, LocalWireNetwork local, GlobalWireNetwork global)
	{
		try
		{
			return TYPES.get(type).create(local, global);
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	protected LocalWireNetwork localNet;
	protected final GlobalWireNetwork globalNet;

	protected LocalNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global)
	{
		this.localNet = net;
		this.globalNet = global;
	}

	public void setLocalNet(LocalWireNetwork net)
	{
		this.localNet = net;
	}

	public abstract LocalNetworkHandler merge(LocalNetworkHandler other);

	public abstract void onConnectorLoaded(ConnectionPoint p, IImmersiveConnectable iic);

	public abstract void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic);

	public abstract void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic);

	public abstract void onConnectionAdded(Connection c);

	public abstract void onConnectionRemoved(Connection c);
}
