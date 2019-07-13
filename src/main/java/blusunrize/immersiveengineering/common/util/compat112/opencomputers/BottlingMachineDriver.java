package blusunrize.immersiveengineering.common.util.compat112.opencomputers;

import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.BottlingMachineTileEntity;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BottlingMachineDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, Direction facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof BottlingMachineTileEntity)
		{
			BottlingMachineTileEntity ref = (BottlingMachineTileEntity)te;
			BottlingMachineTileEntity master = ref.master();
			if(master!=null&&ref.isRedstonePos())
				return new BottlingMachineEnvironment(w, master.getPos(), BottlingMachineTileEntity.class);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return BottlingMachineTileEntity.class;
	}

	public class BottlingMachineEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<BottlingMachineTileEntity>
	{

		public BottlingMachineEnvironment(World w, BlockPos bp, Class<? extends IEBaseTileEntity> teClass)
		{
			super(w, bp, teClass);
		}

		@Callback(doc = "function():boolean -- checks whether the Bottling Machine is currently active")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().shouldRenderAsActive()};
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

		@Callback(doc = "function():table -- get tankinfo for fluid tank")
		public Object[] getTank(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tanks[0].getInfo()};
		}


		@Override
		@Callback(doc = "function(enabled:bool):nil -- Enables or disables computer control for the attached machine")
		public Object[] enableComputerControl(Context context, Arguments args)
		{
			return super.enableComputerControl(context, args);
		}

		@Override
		@Callback(doc = "function(enabled:bool):nil -- Enables or disables the machine. Call \"enableComputerControl(true)\" before using this and disable computer control before removing the computer")
		public Object[] setEnabled(Context context, Arguments args)
		{
			return super.setEnabled(context, args);
		}

		@Override
		public String preferredName()
		{
			return "ie_bottling_machine";
		}

		@Override
		public int priority()
		{
			return 1000;
		}
	}
}
