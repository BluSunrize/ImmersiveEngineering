package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
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

public class EnergyMeterDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityEnergyMeter&&((TileEntityEnergyMeter)te).lower)
		{
			return new EnergyMeterEnvironment(w, bp);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityEnergyMeter.class;
	}


	public class EnergyMeterEnvironment extends ManagedEnvironmentIE<TileEntityEnergyMeter>
	{
		public EnergyMeterEnvironment(World w, BlockPos bp)
		{
			super(w, bp, TileEntityEnergyMeter.class);
		}


		@Override
		public String preferredName()
		{
			return "ie_current_transformer";
		}

		@Override
		public int priority()
		{
			return 1000;
		}

		@Override
		public void onConnect(Node node)
		{
		}

		@Override
		public void onDisconnect(Node node)
		{
		}

		@Callback(doc = "function():int -- returns the average amount of energy transferred during the last 20 ticks")
		public Object[] getAvgEnergy(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getAveragePower()};
		}
	}
}
