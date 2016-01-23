package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ExcavatorDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityExcavator)
		{
			TileEntityExcavator exc = (TileEntityExcavator)te;
			int pos = ((TileEntityExcavator)te).pos;
			if (pos==3)
				return new ExcavatorEnvironment(w, exc.xCoord-exc.offset[0], exc.yCoord-exc.offset[1], exc.zCoord-exc.offset[2]);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityExcavator.class;
	}


	public class ExcavatorEnvironment extends ManagedEnvironmentIE<TileEntityExcavator>
	{

		public ExcavatorEnvironment(World w, int x, int y, int z)
		{
			super(w, x, y, z, TileEntityExcavator.class);
		}


		@Override
		public String preferredName() {
			return "ie_excavator";
		}

		@Override
		public int priority() {
			return 1000;
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityExcavator te = getTileEntity();
			if (te!=null)
			{
				te.computerControlled = true;
				te.computerOn = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityExcavator te = getTileEntity();
			if (te!=null)
				te.computerControlled = false;
		}

		@Callback(doc = "function():boolean -- get whether the excavator is enabled")
		public Object[] getEnabled(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().computerOn};
		}

		@Callback(doc = "function(enable:boolean) -- enable or disable the excavator")
		public Object[] setEnabled(Context context, Arguments args)
		{
			getTileEntity().computerOn = args.checkBoolean(0);
			return null;
		}

		@Callback(doc = "function():number -- get energy storage capacity")
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getEnergyStored()};
		}

		@Callback(doc = "function():number -- get currently stored energy")
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getMaxEnergyStored()};
		}

		@Callback(doc = "function():boolean -- get whether the excavator is currently running")
		public Object[] isRunning(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().active};
		}

	}
}
