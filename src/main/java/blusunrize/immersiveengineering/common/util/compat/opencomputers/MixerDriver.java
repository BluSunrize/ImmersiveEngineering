package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMixer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.util.Utils;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class MixerDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if(te instanceof TileEntityMixer)
		{
			TileEntityMixer arc = (TileEntityMixer)te;
			TileEntityMixer master = arc.master();
			if(master!=null&&arc.isRedstonePos())
				return new MixerEnvironment(w, master.getPos(), TileEntityMixer.class);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityMixer.class;
	}

	public class MixerEnvironment extends ManagedEnvironmentIE.ManagedEnvMultiblock<TileEntityMixer>
	{


		public MixerEnvironment(World w, BlockPos p, Class<? extends TileEntityIEBase> teClass)
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

		@Callback(doc = "function():boolean -- checks whether the mixer is currently active")
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
			TileEntityMixer master = getTileEntity();
			Map<String, Object> stack = Utils.saveStack(master.inventory.get(slot-1));
			mainLoop:
			for(MultiblockProcess<MixerRecipe> p : master.processQueue)
				for(int i : ((MultiblockProcessInMachine<MixerRecipe>)p).getInputSlots())
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

		// Only wants to return info on the bottom fluid. Might be able to force displaying all fluids, not sure yet.
		@Callback(doc = "function():table -- get bottom fluid in tank")
		public Object[] getTank(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().tank.getInfo()};
		}

		// Only returns true if machine is set to active. Don't think that's fixable from here.
		@Callback(doc = "function():boolean -- check whether a valid recipe exists for the current inputs")
		public Object[] isValidRecipe(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().processQueue.get(0).recipe!=null};
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
			return "ie_mixer";
		}

		@Override
		public int priority()
		{
			return 1000;
		}
	}
}