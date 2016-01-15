package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import java.util.HashMap;

import blusunrize.immersiveengineering.api.energy.DieselHandler.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public class RefineryDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityRefinery)
		{
			int pos = ((TileEntityRefinery)te).pos;
			if (pos==9)
			{
				TileEntityRefinery master = ((TileEntityRefinery)te).master();
				return new RefineryEnvironment(w, master.xCoord, master.yCoord, master.zCoord, TileEntityRefinery.class);
			}
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityRefinery.class;
	}
	public class RefineryEnvironment extends ManagedEnvironmentIE<TileEntityRefinery>
	{

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
		public RefineryEnvironment(World w, int x, int y, int z, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, x, y, z, teClass);
		}

		// "override" what gets injected by OC's class transformer
		public void onConnect(Node node)
		{
			TileEntityRefinery master = getTileEntity();
			if (master!=null)
			{
				master.computerControlled = true;
				master.computerOn = true;
			}
		}

		// "override" what gets injected by OC's class transformer
		public void onDisconnect(Node node)
		{
			TileEntityRefinery te = getTileEntity();
			if (te!=null)
				te.computerControlled = false;
		}

		@Callback(doc = "function(enable:boolean) -- enable or disable the refinery")
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

		@Callback(doc = "function():table -- get tankinfo for input tanks")
		public Object[] getInputFluidTanks(Context context, Arguments args)
		{
			TileEntityRefinery master = getTileEntity();
			HashMap<String, FluidTankInfo> ret = new HashMap<>(2);
			ret.put("input0",master.tank0.getInfo());
			ret.put("input1",master.tank1.getInfo());
			return new Object[]{ret};
		}

		@Callback(doc = "function():table -- get tankinfo for output tank")
		public Object[] getOutputTank(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tank2.getInfo()};
		}

		@Callback(doc = "function():table -- get current recipe")
		public Object[] getRecipe(Context context, Arguments args)
		{
			RefineryRecipe recipe = getTileEntity().getRecipe(false);
			if(recipe==null)
				throw new IllegalArgumentException("The recipe of the refinery is invalid");
			HashMap<String, FluidStack> ret = new HashMap<>(3);
			ret.put("input0", recipe.input0);
			ret.put("input1", recipe.input1);
			ret.put("output", recipe.output);
			return new Object[]{ret};
		}

		@Callback(doc = "function():boolean -- check whether a valid recipe exists for the current inputs")
		public Object[] isValidRecipe(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getRecipe(false)!=null};
		}

		@Callback(doc = "function():table -- return item input slot contents for both input and output tanks")
		public Object[] getEmptyCannister(Context context, Arguments args)
		{
			TileEntityRefinery te = getTileEntity();
			HashMap<String, ItemStack> ret = new HashMap<>(3);
			ret.put("input0", te.inventory[1]);
			ret.put("input1", te.inventory[3]);
			ret.put("output", te.inventory[4]);
			return new Object[]{ret};
		}

		@Callback(doc = "function():table -- return item output slot contents for both input and output tanks")
		public Object[] getFullCannisters(Context context, Arguments args)
		{
			TileEntityRefinery te = getTileEntity();
			HashMap<String, ItemStack> ret = new HashMap<>(3);
			ret.put("input0", te.inventory[0]);
			ret.put("input1", te.inventory[2]);
			ret.put("output", te.inventory[5]);
			return new Object[]{ret};
		}

	}
}
