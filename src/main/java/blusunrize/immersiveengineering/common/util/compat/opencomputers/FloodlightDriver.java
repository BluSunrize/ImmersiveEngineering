
package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodlight;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class FloodlightDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityFloodlight)
		{
			TileEntityFloodlight floodlight = (TileEntityFloodlight)te;
			return new FermenterEnvironment(w, floodlight.xCoord, floodlight.yCoord, floodlight.zCoord);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityFloodlight.class;
	}


	public class FermenterEnvironment extends ManagedEnvironmentIE<TileEntityFloodlight>
	{

		public FermenterEnvironment(World w, int x, int y, int z)
		{
			super(w, x, y, z, TileEntityFloodlight.class);
		}

		@Override
		public String preferredName()
		{
			return "ie_floodlight";
		}

		@Override
		public int priority()
		{
			return 1000;
		}
		// "override" what gets injected by OC's class transformer
		public void onConnect(Node node)
		{
			TileEntityFloodlight te = getTileEntity();
			te.computerControlled = true;
			te.computerOn = true;
		}

		// "override" what gets injected by OC's class transformer
		public void onDisconnect(Node node)
		{
			TileEntityFloodlight te = getTileEntity();
			if (te!=null)
				te.computerControlled = false;
		}

		@Callback(doc = "function():int -- gets the maximum amount of energy stored")
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{80};
		}

		@Callback(doc = "function():int -- gets the amount of energy stored")
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage};
		}

		@Callback(doc = "function(up:boolean) -- turns the floodlight")
		public Object[] turnAroundXZ(Context context, Arguments args)
		{
			boolean up = args.checkBoolean(0);
			getTileEntity().turnX(up, true);
			return null;
		}

		@Callback(doc = "function(direction:boolean) -- turns the floodlight")
		public Object[] turnAroundY(Context context, Arguments args)
		{
			boolean dir = args.checkBoolean(0);
			getTileEntity().turnY(dir, true);
			return null;
		}

		@Callback(doc = "function():boolean -- checks whether the floodlight can turn again yet")
		public Object[] canTurnAroundY(Context context, Arguments args)
		{
			return new Object[] {getTileEntity().canComputerTurn()};
		}

		@Callback(doc = "function(on:boolean) -- turns the floodlight on and off")
		public Object[] setEnabled(Context context, Arguments args)
		{
			getTileEntity().computerOn = args.checkBoolean(0);
			return null;
		}

		@Callback(doc = "function():boolean -- checks whether the floodlight is on")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().active};
		}
	}
}
