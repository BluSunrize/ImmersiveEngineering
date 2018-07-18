package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInWorld;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrusherDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityCrusher)
		{
			TileEntityCrusher crush = (TileEntityCrusher)te;
			TileEntityCrusher master = crush.master();
			if(master!=null&&crush.isRedstonePos())
				return new CrusherEnvironment(w, master.getPos(), TileEntityCrusher.class);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityCrusher.class;
	}


	public class CrusherEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityCrusher>
	{
		public CrusherEnvironment(World w, BlockPos bp, Class<? extends TileEntityIEBase> teClass)
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

		@Callback(doc = "function():boolean -- get whether the crusher is currently crushing items")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().shouldRenderAsActive()};
		}

		@Callback(doc = "function():table -- returns the entire input queue of the crusher")
		public Object[] getInputQueue(Context context, Arguments args)
		{
			TileEntityCrusher master = getTileEntity();
			Map<Integer, Object> ret = new HashMap<>();
			List<MultiblockProcess<CrusherRecipe>> queue = master.processQueue;
			for(int i = 0; i < queue.size(); i++)
			{
				MultiblockProcess<CrusherRecipe> currTmp = queue.get(i);
				if(currTmp instanceof MultiblockProcessInWorld)
				{
					MultiblockProcessInWorld<CrusherRecipe> curr = (MultiblockProcessInWorld<CrusherRecipe>)currTmp;
					Map<String, Object> recipe = new HashMap<>();
					recipe.put("progress", curr.processTick);
					recipe.put("maxProgress", curr.maxTicks);
					List<Map<String, Object>> input = new ArrayList(curr.inputItems.size());
					for(ItemStack in : curr.inputItems)
						input.add(Utils.saveStack(in));
					recipe.put("input", input);
					recipe.put("output", Utils.saveStack(curr.recipe.output));
					ret.put(i+1, recipe);
				}
			}
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
			return "ie_crusher";
		}

		@Override
		public int priority()
		{
			return 1000;
		}
	}
}
