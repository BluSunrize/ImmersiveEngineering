package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SampleDrillDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntitySampleDrill)
		{
			TileEntitySampleDrill drill = (TileEntitySampleDrill) te;
			if(drill.dummy == 0)
				return new SampleDrillEnvironment(w, bp);
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

		public SampleDrillEnvironment(World w, BlockPos bp)
		{
			super(w, bp, TileEntitySampleDrill.class);
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
			return new Object[]{getTileEntity().getVein()};
		}

		@Callback
		public Object[] getVeinExpectedYield(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getExpectedVeinYield()};
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

		@Callback
		public Object[] reset(Context context, Arguments args)
		{
			TileEntitySampleDrill d = getTileEntity();
			d.process = 0;
			d.active = true;
			d.sample = null;
			return new Object[0];
		}

	}
}
