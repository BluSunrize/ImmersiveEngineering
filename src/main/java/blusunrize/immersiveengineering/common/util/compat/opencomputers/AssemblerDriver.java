package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import java.util.ArrayList;
import java.util.HashMap;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.util.Utils;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class AssemblerDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityAssembler)
		{
			int offsetY = ((TileEntityAssembler)te).offset[1];
			if (offsetY==-1)
			{
				TileEntityAssembler master = ((TileEntityAssembler)te).master();
				return new AssemblerEnvironment(w, master.xCoord, master.yCoord, master.zCoord, TileEntityAssembler.class);
			}
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityAssembler.class;
	}
	public class AssemblerEnvironment extends ManagedEnvironmentIE<TileEntityAssembler>
	{

		@Override
		public String preferredName()
		{
			return "ie_assembler";
		}

		@Override
		public int priority()
		{
			return 1000;
		}
		public AssemblerEnvironment(World w, int x, int y, int z, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, x, y, z, teClass);
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityAssembler master = getTileEntity();
			if (master!=null)
			{
				master.computerControlled = true;
				master.computerOn[0] = true;
				master.computerOn[1] = true;
				master.computerOn[2] = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityAssembler te = getTileEntity();
			if (te!=null)
				te.computerControlled = false;
		}
		@Callback(doc = "function(recipe:int):boolean -- get whether the ingredients for the specified recipe are available")
		public Object[] hasIngredients(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if (recipe>2||recipe<0)
				throw new IllegalArgumentException("Only recipes 0-2 are available");
			TileEntityAssembler master = getTileEntity();
			if (master.patterns[recipe].inv[9]==null)
				throw new IllegalArgumentException("The requested recipe is invalid");
			ArrayList<ItemStack> queryList = new ArrayList<>();
			for(ItemStack stack : master.inventory)
				if(stack!=null)
					queryList.add(stack.copy());
			return new Object[]{master.hasIngredients(master.patterns[recipe], queryList)};
		}

		@Callback(doc = "function(recipe:int) -- enables or disables the specified recipe")
		public Object[] setEnabled(Context context, Arguments args)
		{
			boolean on = args.checkBoolean(1);
			int recipe = args.checkInteger(0);
			if (recipe>2||recipe<0)
				throw new IllegalArgumentException("Only recipes 0-2 are available");
			getTileEntity().computerOn[recipe] = on;
			return null;
		}

		@Callback(doc = "function(recipe:int):table -- get the recipe in the specified position")
		public Object[] getRecipe(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if (recipe>2||recipe<0)
				throw new IllegalArgumentException("Only recipes 0-2 are available");
			TileEntityAssembler te = getTileEntity();
			HashMap<String, Object> ret = new HashMap<>();
			for (int i = 0;i<9;i++)
				ret.put("in"+(i+1), te.patterns[recipe].inv[i]);
			ret.put("out", te.patterns[recipe].inv[9]);
			return new Object[]{ret};
		}

		@Callback(doc = "function(recipe:int):boolean -- check whether the recipe in the specified position has an output")
		public Object[] isValidRecipe(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if (recipe>2||recipe<0)
				throw new IllegalArgumentException("Only recipes 0-2 are available");
			return new Object[]{getTileEntity().patterns[recipe].inv[9]!=null};
		}

		@Callback(doc = "function(tank:int):table -- gets the specified tank")
		public Object[] getTank(Context context, Arguments args)
		{
			int tank = args.checkInteger(0);
			if (tank>2||tank<0)
				throw new IllegalArgumentException("Only tanks 0-2 are available");
			return new Object[]{Utils.saveFluidTank(getTileEntity().tanks[tank])};
		}

		@Callback(doc = "function():int -- returns the maximum amount of energy that can be stored")
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getMaxEnergyStored()};
		}

		@Callback(doc = "function():int -- returns the amount of energy stored")
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getEnergyStored()};
		}
		@Callback(doc = "function(slot:int):table -- returns the stack in the specified slot")
		public Object[] getStackInSlot(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if (slot<0||slot>17)
				throw new IllegalArgumentException("Only slots 0-17 are available");
			return new Object[]{getTileEntity().inventory[slot]};
		}
		@Callback(doc = "function(slot:int):table -- returns the stack in the output slot of the specified recipe")
		public Object[] getBufferStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if (slot<0||slot>2)
				throw new IllegalArgumentException("Only recipes 0-2 are available");
			return new Object[]{getTileEntity().inventory[18+slot]};
		}
		
	}
}
