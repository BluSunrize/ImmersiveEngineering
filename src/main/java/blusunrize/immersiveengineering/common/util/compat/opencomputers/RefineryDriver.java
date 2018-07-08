package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import java.util.HashMap;

public class RefineryDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityRefinery)
		{
			TileEntityRefinery ref = (TileEntityRefinery)te;
			TileEntityRefinery master = ref.master();
			if(master!=null&&ref.isRedstonePos())
				return new RefineryEnvironment(w, master.getPos(), TileEntityRefinery.class);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityRefinery.class;
	}

	public class RefineryEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityRefinery>
	{

		public RefineryEnvironment(World w, BlockPos bp, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, bp, teClass);
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

		@Callback(doc = "function():table -- get tankinfo for input tanks")
		public Object[] getInputFluidTanks(Context context, Arguments args)
		{
			TileEntityRefinery master = getTileEntity();
			HashMap<String, FluidTankInfo> ret = new HashMap<>(2);
			ret.put("input1", master.tanks[0].getInfo());
			ret.put("input2", master.tanks[1].getInfo());
			return new Object[]{ret};
		}

		@Callback(doc = "function():table -- get tankinfo for output tank")
		public Object[] getOutputTank(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tanks[2].getInfo()};
		}

		@Callback(doc = "function():table -- get current recipe")
		public Object[] getRecipe(Context context, Arguments args)
		{
			RefineryRecipe recipe = getTileEntity().processQueue.get(0).recipe;
			if(recipe==null)
				throw new IllegalArgumentException("The recipe of the refinery is invalid");
			HashMap<String, FluidStack> ret = new HashMap<>(3);
			ret.put("input1", recipe.input0);
			ret.put("input2", recipe.input1);
			ret.put("output", recipe.output);
			return new Object[]{ret};
		}

		@Callback(doc = "function():boolean -- check whether a valid recipe exists for the current inputs")
		public Object[] isValidRecipe(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().processQueue.get(0).recipe!=null};
		}

		@Callback(doc = "function():table -- return item input slot contents for both input and output tanks")
		public Object[] getEmptyCannisters(Context context, Arguments args)
		{
			TileEntityRefinery te = getTileEntity();
			HashMap<String, ItemStack> ret = new HashMap<>(3);
			ret.put("input1", te.inventory.get(1));
			ret.put("input2", te.inventory.get(3));
			ret.put("output", te.inventory.get(4));
			return new Object[]{ret};
		}

		@Callback(doc = "function():table -- return item output slot contents for both input and output tanks")
		public Object[] getFullCannisters(Context context, Arguments args)
		{
			TileEntityRefinery te = getTileEntity();
			HashMap<String, ItemStack> ret = new HashMap<>(3);
			ret.put("input1", te.inventory.get(0));
			ret.put("input2", te.inventory.get(2));
			ret.put("output", te.inventory.get(5));
			return new Object[]{ret};
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
			return "ie_refinery";
		}

		@Override
		public int priority()
		{
			return 1000;
		}
	}
}
