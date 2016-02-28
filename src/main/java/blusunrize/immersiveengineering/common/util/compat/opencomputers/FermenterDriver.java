package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.DieselHandler.FermenterRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.util.Utils;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class FermenterDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityFermenter)
		{
			TileEntityFermenter ferment = (TileEntityFermenter)te;
			return new FermenterEnvironment(w, ferment.xCoord-ferment.offset[0], ferment.yCoord-ferment.offset[1], ferment.zCoord-ferment.offset[2]);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityFermenter.class;
	}


	public class FermenterEnvironment extends ManagedEnvironmentIE<TileEntityFermenter>
	{

		public FermenterEnvironment(World w, int x, int y, int z)
		{
			super(w, x, y, z, TileEntityFermenter.class);
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
		@Callback(doc = "function(slot:int):table, table, table, int -- returns the recipe for the specified input slot")
		public Object[] getRecipe(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if (slot<1||slot>9)
				throw new IllegalArgumentException("Input slots are 1-9");
			TileEntityFermenter master = getTileEntity();
			FermenterRecipe recipe = DieselHandler.findFermenterRecipe(master.getStackInSlot(slot-1));
			if (recipe!=null)
				return new Object[]{master.getStackInSlot(slot-1), recipe.output, recipe.fluid, recipe.time};
			else
				return null;
		}

		@Callback(doc = "function(slot:int) -- returns the stack in the specified input slot")
		public Object[] getInputStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if (slot<1||slot>9)
				throw new IllegalArgumentException("Input slots are 1-9");
			return new Object[]{getTileEntity().getStackInSlot(slot-1)};
		}

		@Callback(doc = "function():table -- returns the stack in the output slot")
		public Object[] getOutputStack(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getStackInSlot(11)};
		}

		@Callback(doc = "function():table -- returns the output fluid tank")
		public Object[] getFluid(Context context, Arguments args)
		{
			return new Object[]{Utils.saveFluidTank(getTileEntity().tank)};
		}

		@Callback(doc = "function():table -- returns the stack in the empty cannisters slot")
		public Object[] getEmptyCannisters(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getStackInSlot(9)};
		}

		@Callback(doc = "function():table -- returns the stack in the filled cannisters slot")
		public Object[] getFilledCannisters(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getStackInSlot(10)};
		}

		@Callback(doc = "function():int -- returns the current fermenting progress")
		public Object[] getProgress(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tick};
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
	}
}
