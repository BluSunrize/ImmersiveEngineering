package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FermenterDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityFermenter)
		{
			TileEntityFermenter ferment = (TileEntityFermenter)te;
			TileEntityFermenter master = ferment.master();
			if(master!=null&&ferment.isRedstonePos())
				return new FermenterEnvironment(w, master.getPos());
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityFermenter.class;
	}


	public class FermenterEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityFermenter>
	{

		public FermenterEnvironment(World w, BlockPos bp)
		{
			super(w, bp, TileEntityFermenter.class);
		}


		@Callback(doc = "function(slot:int):table, table, table, int -- returns the recipe for the specified input slot")
		public Object[] getRecipe(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if(slot < 1||slot > 8)
				throw new IllegalArgumentException("Input slots are 1-8");
			TileEntityFermenter master = getTileEntity();
			FermenterRecipe recipe = FermenterRecipe.findRecipe(master.inventory.get(slot-1));
			if(recipe!=null)
				return new Object[]{master.inventory.get(slot-1), recipe.itemOutput, recipe.fluidOutput, recipe.getTotalProcessTime()};
			else
				return null;
		}

		@Callback(doc = "function(slot:int) -- returns the stack in the specified input slot")
		public Object[] getInputStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if(slot < 1||slot > 8)
				throw new IllegalArgumentException("Input slots are 1-8");
			return new Object[]{getTileEntity().inventory.get(slot-1)};
		}

		@Callback(doc = "function():table -- returns the stack in the output slot")
		public Object[] getOutputStack(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().inventory.get(8)};
		}

		@Callback(doc = "function():table -- returns the output fluid tank")
		public Object[] getFluid(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tanks[0].getInfo()};
		}

		@Callback(doc = "function():table -- returns the stack in the empty cannisters slot")
		public Object[] getEmptyCannisters(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().inventory.get(9)};
		}

		@Callback(doc = "function():table -- returns the stack in the filled cannisters slot")
		public Object[] getFilledCannisters(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().inventory.get(10)};
		}

		@Callback(doc = "function():int -- returns the maximum amount of energy stored")
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getMaxEnergyStored()};
		}

		@Callback(doc = "function():int -- returns the amount of energy stored")
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getEnergyStored()};
		}

		@Callback(doc = "function():boolean -- returns whether the fermenter is running")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().shouldRenderAsActive()};
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
			return "ie_fermenter";
		}

		@Override
		public int priority()
		{
			return 1000;
		}

	}
}
