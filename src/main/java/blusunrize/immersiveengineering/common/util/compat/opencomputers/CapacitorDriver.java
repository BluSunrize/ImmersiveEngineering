package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorCreative;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorMV;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class CapacitorDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityCapacitorLV)
		{
			String pre = "";
			if (te instanceof TileEntityCapacitorCreative)
				pre = "creative";
			else if (te instanceof TileEntityCapacitorHV)
				pre = "hv";
			else if (te instanceof TileEntityCapacitorMV)
				pre = "mv";
			else if (te instanceof TileEntityCapacitorLV)
				pre = "lv";
			return new CapacitorEnvironment(w, te.xCoord, te.yCoord, te.zCoord, pre);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityCapacitorLV.class;
	}


	public class CapacitorEnvironment extends ManagedEnvironmentIE<TileEntityCapacitorLV>
	{
		String prefix;
		public CapacitorEnvironment(World w, int x, int y, int z, String name)
		{
			super(w, x, y, z, TileEntityCapacitorLV.class);
			prefix = name;
		}


		@Override
		public String preferredName() {
			return "ie_"+prefix+"_capacitor";
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

		@Callback(doc = "function():int -- returns the amount of energy that can be stored")
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getMaxEnergyStored(ForgeDirection.UP)};
		}

		@Callback(doc = "function():int -- returns the amount of energy that can be stored")
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getEnergyStored(ForgeDirection.DOWN)};
		}

	}
}
