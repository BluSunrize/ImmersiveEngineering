/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.redstone;

import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public class CapabilityRedstoneNetwork
{
	public static final BlockCapability<RedstoneBundleConnection, @Nullable Direction> REDSTONE_BUNDLE_CONNECTION = BlockCapability.createSided(
			IEApi.ieLoc("redstone_bundle_connection"), RedstoneBundleConnection.class
	);

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
		 * @param externalInputs the signals produced by all connectors in the RedstoneWireNetwork *except* for this
		 *                       one
		 * @param side           the side the redstone connector is attached to
		 */
		public void onChange(byte[] externalInputs, Direction side)
		{
		}

		/**
		 * Called when the RedstoneWireNetwork updates its RS input values.
		 * As a general rule only stronger signals should override weaker signals, so you should never decrease the value of
		 * a channel in this method.
		 *
		 * @param signals the values of the RS channels up to this point. Modify this array to change output values.
		 * @param side    the side the redstone connector is attached to
		 */
		public void updateInput(byte[] signals, Direction side)
		{
		}
	}
}
