package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EnergyMeterDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityEnergyMeter&&((TileEntityEnergyMeter)te).dummy)
		{
			return new EnergyMeterEnvironment(w, te.xCoord, te.yCoord+1, te.zCoord);
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
		public EnergyMeterEnvironment(World w, int x, int y, int z)
		{
			super(w, x, y, z, TileEntityEnergyMeter.class);
		}


		@Override
		public String preferredName() {
			return "ie_current_transformer";
		}

		@Override
		public int priority() {
			return 1000;
		}

		@Override
		public void onConnect(Node node)
		{}

		@Override
		public void onDisconnect(Node node)
		{}

		@Callback(doc = "function():int -- returns the average amount of energy transferred during the last 20 ticks")
		public Object[] getLastMeasurements(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getAveragePower()};
		}
	}
}
