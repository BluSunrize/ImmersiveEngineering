package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
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
			TileEntityAssembler assembler = (TileEntityAssembler)te;
			TileEntityAssembler master = assembler.master();
			if(master!=null&&assembler.isRedstonePos())
				return new AssemblerEnvironment(w, master.getPos(), TileEntityAssembler.class);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityAssembler.class;
	}

	public class AssemblerEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityAssembler>
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

		@Callback(doc = "function(recipe:int):boolean -- get whether the ingredients for the specified recipe are available")
		public Object[] hasIngredients(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if(recipe > 3||recipe < 1)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			TileEntityAssembler master = getTileEntity();
			if(master.patterns[recipe-1].inv.get(9).isEmpty())
				throw new IllegalArgumentException("The requested recipe is invalid");
			TileEntityAssembler.CrafterPatternInventory pattern = master.patterns[recipe-1];
			AssemblerHandler.IRecipeAdapter adapter = AssemblerHandler.findAdapter(pattern.recipe);
			ArrayList<ItemStack> queryList = new ArrayList<>();
			for(ItemStack stack : master.inventory)
				if(!stack.isEmpty())
					queryList.add(stack.copy());
			RecipeQuery[] queries = adapter.getQueriedInputs(pattern.recipe, pattern.inv);
			if(queries==null)
				throw new IllegalArgumentException("The Assembler cannot craft this recipe");
			return new Object[]{master.consumeIngredients(queries, queryList, false, null)};
		}

		@Callback(doc = "function(recipe:int):table -- get the recipe in the specified position")
		public Object[] getRecipe(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if(recipe > 3||recipe < 1)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			TileEntityAssembler te = getTileEntity();
			HashMap<String, Object> ret = new HashMap<>();
			for(int i = 0; i < 9; i++)
				ret.put("in"+(i+1), te.patterns[recipe-1].inv.get(i));
			ret.put("out", te.patterns[recipe-1].inv.get(9));
			return new Object[]{ret};
		}

		@Callback(doc = "function(recipe:int):boolean -- check whether the recipe in the specified position has an output")
		public Object[] isValidRecipe(Context context, Arguments args)
		{
			int recipe = args.checkInteger(0);
			if(recipe > 3||recipe < 1)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			return new Object[]{!getTileEntity().patterns[recipe-1].inv.get(9).isEmpty()};
		}

		@Callback(doc = "function(tank:int):table -- gets the specified tank")
		public Object[] getTank(Context context, Arguments args)
		{
			int tank = args.checkInteger(0);
			if(tank > 3||tank < 1)
				throw new IllegalArgumentException("Only tanks 1-3 are available");
			return new Object[]{getTileEntity().tanks[tank-1].getInfo()};
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
			if(slot < 1||slot > 18)
				throw new IllegalArgumentException("Only slots 1-18 are available");
			return new Object[]{getTileEntity().inventory.get(slot-1)};
		}

		@Callback(doc = "function(slot:int):table -- returns the stack in the output slot of the specified recipe")
		public Object[] getBufferStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if(slot < 1||slot > 3)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			return new Object[]{getTileEntity().inventory.get(17+slot)};
		}

		@Override
		@Callback(doc = "function(enabled:bool):nil -- Enables or disables computer control for the attached machine")
		public Object[] enableComputerControl(Context context, Arguments args)
		{
			TileEntityAssembler te = getTileEntity();
			te.isComputerControlled = args.checkBoolean(0);
			for(int i = 0; i < 3; i++)
				te.computerOn[i] = true;
			return null;
		}

		@Override
		@Callback(doc = "function(recipe:int) -- enables or disables the specified recipe")
		public Object[] setEnabled(Context context, Arguments args)
		{
			boolean on = args.checkBoolean(1);
			int recipe = args.checkInteger(0);
			if(recipe > 3||recipe < 1)
				throw new IllegalArgumentException("Only recipes 1-3 are available");
			getTileEntity().computerOn[recipe-1] = on;
			return null;
		}
	}
}
