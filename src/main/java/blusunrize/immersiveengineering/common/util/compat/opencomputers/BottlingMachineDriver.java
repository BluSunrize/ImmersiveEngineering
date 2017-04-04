package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import java.util.HashMap;

public class BottlingMachineDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityBottlingMachine)
		{
			TileEntityBottlingMachine ref = (TileEntityBottlingMachine) te;
			TileEntityBottlingMachine master = ref.master();
			if(master != null && ref.isRedstonePos())
				return new BottlingMachineEnvironment(w, master.getPos(), TileEntityBottlingMachine.class);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityBottlingMachine.class;
	}

	public class BottlingMachineEnvironment extends ManagedEnvironmentIE<TileEntityBottlingMachine>
	{

		public BottlingMachineEnvironment(World w, BlockPos bp, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, bp, teClass);
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityBottlingMachine master = getTileEntity();
			if(master != null)
			{
				master.controllingComputers++;
				master.computerOn = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityBottlingMachine te = getTileEntity();
			if(te != null)
				te.controllingComputers--;
		}

		@Callback(doc = "function():boolean -- checks whether the Bottling Machine is currently active")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().shouldRenderAsActive()};
		}
		
		@Callback(doc = "function(enable:boolean) -- enable or disable the Bottling Machine")
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

		@Callback(doc = "function():table -- get tankinfo for fluid tank")
		public Object[] getTank(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tanks[0].getInfo()};
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
