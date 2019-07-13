package blusunrize.immersiveengineering.common.util.compat112.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.EnergyMeterTileEntity;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyMeterDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, Direction facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof EnergyMeterTileEntity&&((EnergyMeterTileEntity)te).lower)
		{
			return new EnergyMeterEnvironment(w, bp);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return EnergyMeterTileEntity.class;
	}


	public class EnergyMeterEnvironment extends ManagedEnvironmentIE<EnergyMeterTileEntity>
	{
		public EnergyMeterEnvironment(World w, BlockPos bp)
		{
			super(w, bp, EnergyMeterTileEntity.class);
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
