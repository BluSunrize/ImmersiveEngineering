package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SampleDrillDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntitySampleDrill)
		{
			TileEntitySampleDrill drill = (TileEntitySampleDrill)te;
			if (drill.pos==0)
				return new SampleDrillEnvironment(w, drill.xCoord, drill.yCoord, drill.zCoord);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntitySampleDrill.class;
	}


	public class SampleDrillEnvironment extends ManagedEnvironmentIE<TileEntitySampleDrill>
	{

		public SampleDrillEnvironment(World w, int x, int y, int z)
		{
			super(w, x, y, z, TileEntitySampleDrill.class);
		}

		@Override
		public String preferredName()
		{
			return "ie_sample_drill";
		}

		@Override
		public int priority()
		{
			return 1000;
		}
		@Callback
		public Object[] getSampleProgress(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getSampleProgress()};
		}
		@Callback
		public Object[] isSamplingFinished(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().isSamplingFinished()};
		}
		@Callback
		public Object[] getVeinUnlocalizedName(Context context, Arguments args)
		{
			TileEntitySampleDrill te = getTileEntity();
			if(te.isSamplingFinished())
				return new Object[]{te.getVeinUnlocalizedName()};
			return new Object[0];
		}
		@Callback
		public Object[] getVeinLocalizedName(Context context, Arguments args)
		{
			TileEntitySampleDrill te = getTileEntity();
			if(te.isSamplingFinished())
				return new Object[]{te.getVeinLocalizedName()};
			return new Object[0];
		}
		@Callback
		public Object[] getVeinIntegrity(Context context, Arguments args)
		{
			TileEntitySampleDrill te = getTileEntity();
			if(te.isSamplingFinished())
				return new Object[]{te.getVeinIntegrity()};
			return new Object[0];
		}
		@Callback
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getEnergyStored()};
		}
		@Callback
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getMaxEnergyStored()};
		}

	}
}
