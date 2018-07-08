package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.items.ItemGraphiteElectrode;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
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

import java.util.Map;

public class ArcFurnaceDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityArcFurnace)
		{
			TileEntityArcFurnace arc = (TileEntityArcFurnace)te;
			TileEntityArcFurnace master = arc.master();
			if(master!=null&&arc.isRedstonePos())
				return new ArcFurnaceEnvironment(w, master.getPos(), TileEntityArcFurnace.class);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityArcFurnace.class;
	}

	public class ArcFurnaceEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityArcFurnace>
	{

		@Override
		public String preferredName()
		{
			return "ie_arc_furnace";
		}

		@Override
		public int priority()
		{
			return 1000;
		}

		public ArcFurnaceEnvironment(World w, BlockPos p, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, p, teClass);
		}

		@Callback(doc = "function():int -- gets the maximum amount of energy stored")
		public Object[] getMaxEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getMaxEnergyStored()};
		}

		@Callback(doc = "function():int -- gets the amount of energy stored")
		public Object[] getEnergyStored(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().energyStorage.getEnergyStored()};
		}

		@Callback(doc = "function():boolean -- checks whether the arc furnace is currently active")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().shouldRenderAsActive()};
		}

		@Callback(doc = "function(stack:int):table -- returns the specified input stack as described in the manual")
		public Object[] getInputStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if(slot < 1||slot > 12)
				throw new IllegalArgumentException("Input slots are 1-12");
			TileEntityArcFurnace master = getTileEntity();
			Map<String, Object> stack = Utils.saveStack(master.inventory.get(slot-1));
			mainLoop:
			for(MultiblockProcess<ArcFurnaceRecipe> p : master.processQueue)
				for(int i : ((MultiblockProcessInMachine<ArcFurnaceRecipe>)p).getInputSlots())
					if(i==slot-1)
					{
						stack.put("progress", p.processTick);
						stack.put("maxProgress", p.maxTicks);
						break mainLoop;
					}
			if(!stack.containsKey("progress"))
			{
				stack.put("progress", 0);
				stack.put("maxProgress", 0);
			}
			return new Object[]{stack};
		}

		@Callback(doc = "function(stack:int):table -- returns the specified output stack")
		public Object[] getOutputStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if(slot < 1||slot > 6)
				throw new IllegalArgumentException("Output slots are 1-6");
			return new Object[]{getTileEntity().inventory.get(slot+15)};
		}

		@Callback(doc = "function(stack:int):table -- returns the specified additive stack")
		public Object[] getAdditiveStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if(slot < 1||slot > 4)
				throw new IllegalArgumentException("Additive slots are 1-4");
			return new Object[]{getTileEntity().inventory.get(slot+11)};
		}

		@Callback(doc = "function():table -- returns the slag stack")
		public Object[] getSlagStack(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().inventory.get(22)};
		}

		@Callback(doc = "function():boolean -- checks whether the arc furnace has all 3 electrodes")
		public Object[] hasElectrodes(Context context, Arguments args)
		{
			TileEntityArcFurnace master = getTileEntity();
			return new Object[]{master.hasElectrodes()};
		}

		@Callback(doc = "function(electrode:int):table -- returns the specified electrode as an item stack")
		public Object[] getElectrode(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if(slot < 1||slot > 3)
				throw new IllegalArgumentException("Electrode slots are 1-3");
			ItemStack stack = getTileEntity().inventory.get(slot+22);
			Map<String, Object> map = Utils.saveStack(stack);
			if(!stack.isEmpty()&&stack.getItem() instanceof ItemGraphiteElectrode)
				map.put("damage", ItemNBTHelper.getInt(stack, "graphDmg"));
			return new Object[]{map};
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
	}

}
