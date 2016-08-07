package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExcavatorDriver extends DriverSidedTileEntity
{
	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos pos, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(pos);
		if(te instanceof TileEntityExcavator)
		{
			TileEntityExcavator exc = (TileEntityExcavator) te;
			TileEntityExcavator master = exc.master();
			if(master != null && exc.isRedstonePos())
				return new ExcavatorEnvironment(w, master.getPos());
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

		public ExcavatorEnvironment(World w, BlockPos pos)
		{
			super(w, pos, TileEntityExcavator.class);
		}


		@Override
		public String preferredName()
		{
			return "ie_excavator";
		}

		@Override
		public int priority()
		{
			return 1000;
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityExcavator te = getTileEntity();
			if(te != null)
			{
				te.controllingComputers++;
				te.computerOn = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityExcavator te = getTileEntity();
			if(te != null)
				te.controllingComputers--;
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
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().active};
		}

	}
}
