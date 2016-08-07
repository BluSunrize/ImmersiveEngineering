package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.util.Utils;
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

import java.util.ArrayList;
import java.util.HashMap;

public class AssemblerDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing s)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityAssembler)
		{
			TileEntityAssembler assembler = (TileEntityAssembler) te;
			TileEntityAssembler master = assembler.master();
			if(master != null && assembler.isRedstonePos())
				return new AssemblerEnvironment(w, master.getPos(), TileEntityAssembler.class);
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

		public AssemblerEnvironment(World w, BlockPos bp, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, bp, teClass);
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityAssembler master = getTileEntity();
			if(master != null)
			{
				master.controllingComputers++;
				master.computerOn[0] = true;
				master.computerOn[1] = true;
				master.computerOn[2] = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityAssembler te = getTileEntity();
			if(te != null)
				te.controllingComputers--;
		}

		@Callback(doc = "function(recipe:int):boolean -- get whether the ingredients for the specified recipe are available")
		public Object[] hasIngredients(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if(recipe > 3 || recipe < 1)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			TileEntityAssembler master = getTileEntity();
			if(master.patterns[recipe - 1].inv[9] == null)
				throw new IllegalArgumentException("The requested recipe is invalid");
			ArrayList<ItemStack> queryList = new ArrayList<>();
			for(ItemStack stack : master.inventory)
				if(stack != null)
					queryList.add(stack.copy());
			return new Object[]{master.hasIngredients(master.patterns[recipe - 1], queryList)};
		}

		@Callback(doc = "function(recipe:int) -- enables or disables the specified recipe")
		public Object[] setEnabled(Context context, Arguments args)
		{
			boolean on = args.checkBoolean(1);
			int recipe = args.checkInteger(0);
			if(recipe > 3 || recipe < 1)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			getTileEntity().computerOn[recipe - 1] = on;
			return null;
		}

		@Callback(doc = "function(recipe:int):table -- get the recipe in the specified position")
		public Object[] getRecipe(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if(recipe > 3 || recipe < 1)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			TileEntityAssembler te = getTileEntity();
			HashMap<String, Object> ret = new HashMap<>();
			for(int i = 0; i < 9; i++)
				ret.put("in" + (i + 1), te.patterns[recipe - 1].inv[i]);
			ret.put("out", te.patterns[recipe - 1].inv[9]);
			return new Object[]{ret};
		}

		@Callback(doc = "function(recipe:int):boolean -- check whether the recipe in the specified position has an output")
		public Object[] isValidRecipe(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if(recipe > 3 || recipe < 1)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			return new Object[]{getTileEntity().patterns[recipe - 1].inv[9] != null};
		}

		@Callback(doc = "function(tank:int):table -- gets the specified tank")
		public Object[] getTank(Context context, Arguments args)
		{
			int tank = args.checkInteger(0);
			if(tank > 3 || tank < 1)
				throw new IllegalArgumentException("Only tanks 1-3 are available");
			return new Object[]{Utils.saveFluidTank(getTileEntity().tanks[tank - 1])};
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
			if(slot < 1 || slot > 18)
				throw new IllegalArgumentException("Only slots 1-18 are available");
			return new Object[]{getTileEntity().inventory[slot - 1]};
		}

		@Callback(doc = "function(slot:int):table -- returns the stack in the output slot of the specified recipe")
		public Object[] getBufferStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if(slot < 1 || slot > 3)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			return new Object[]{getTileEntity().inventory[17 + slot]};
		}

	}
}
