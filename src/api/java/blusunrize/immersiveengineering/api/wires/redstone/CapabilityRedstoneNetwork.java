/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.redstone;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityRedstoneNetwork
{
	@CapabilityInject(RedstoneBundleConnection.class)
	public static Capability<RedstoneBundleConnection> REDSTONE_BUNDLE_CONNECTION = null;

	public static class RedstoneBundleConnection
	{
		private boolean dirty = false;

		/**
		 * Used by connectors to check if this object is dirty, resetting the flag in the process
		 */
		public boolean pollDirty()
		{
			if(dirty)
			{
				dirty = false;
				return true;
			}
			return false;
		}

		/**
		 * Marks this object as dirty, causing an attached connector to update the network
		 */
		public void markDirty()
		{
			this.dirty = true;
		}

		/**
		 * Called whenever the RedstoneWireNetwork is changed in some way (both adding/removing connectors and changes in RS values).
		 *
		 * @param cp      the connectionpoint within the network
		 * @param handler the redstone network to which the connector belongs
		 * @param side    the side the redstone connector is attached to
		 */
		public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler, Direction side)
		{
		}

		/**
		 * Called when the RedstoneWireNetwork updates its RS input values.
		 * As a general rule only stronger signals should override weaker signals, so you should never decrease the value of
		 * a channel in this method.
		 *
		 * @param signals the values of the RS channels up to this point. Modify this array to change output values.
		 * @param cp      the connectionpoint within the network
		 * @param side    the side the redstone connector is attached to
		 */
		public void updateInput(byte[] signals, ConnectionPoint cp, Direction side)
		{
		}
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(RedstoneBundleConnection.class, new Capability.IStorage<RedstoneBundleConnection>()
		{
			@Override
			public Tag writeNBT(Capability<RedstoneBundleConnection> capability, RedstoneBundleConnection instance, Direction side)
			{
				return null;
			}

			@Override
			public void readNBT(Capability<RedstoneBundleConnection> capability, RedstoneBundleConnection instance, Direction side, Tag nbt)
			{
			}
		}, RedstoneBundleConnection::new);
	}
}
