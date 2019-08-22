package blusunrize.immersiveengineering.common.util.compat112.opencomputers;

import blusunrize.immersiveengineering.common.blocks.metal.CapacitorCreativeTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorHVTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorLVTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorMVTileEntity;
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

public class CapacitorDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, Direction facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof CapacitorLVTileEntity)
		{
			String pre = "";
			if(te instanceof CapacitorCreativeTileEntity)
				pre = "creative";
			else if(te instanceof CapacitorHVTileEntity)
				pre = "hv";
			else if(te instanceof CapacitorMVTileEntity)
				pre = "mv";
			else if(te instanceof CapacitorLVTileEntity)
				pre = "lv";
			return new CapacitorEnvironment(w, bp, pre);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return CapacitorLVTileEntity.class;
	}


	public class CapacitorEnvironment extends ManagedEnvironmentIE<CapacitorLVTileEntity>
	{
		String prefix;

		public CapacitorEnvironment(World w, BlockPos bp, String name)
		{
			super(w, bp, CapacitorLVTileEntity.class);
			prefix = name;
		}


		@Override
		public String preferredName()
		{
			return "ie_"+prefix+"_capacitor";
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

		@Callback(doc = "function():int -- returns the amount of energy that can be stored")
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getMaxEnergyStored(Direction.UP)};
		}

		@Callback(doc = "function():int -- returns the amount of energy that can be stored")
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getEnergyStored(Direction.DOWN)};
		}

	}
}
