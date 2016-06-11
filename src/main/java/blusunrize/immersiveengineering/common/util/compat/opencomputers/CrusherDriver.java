package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import java.util.Map;

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
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class CrusherDriver extends DriverSidedTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, BlockPos bp, EnumFacing facing)
	{
		TileEntity te = w.getTileEntity(bp);
		if (te instanceof TileEntityCrusher)
		{
			TileEntityCrusher crush = (TileEntityCrusher)te;
			TileEntityCrusher master = crush.master();
			if (master!=null&&crush.isRedstonePos())
				return new CrusherEnvironment(w, master.getPos(), TileEntityCrusher.class);
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityCrusher.class;
	}


	public class CrusherEnvironment extends ManagedEnvironmentIE<TileEntityCrusher>
	{
		public CrusherEnvironment(World w, BlockPos bp, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, bp, teClass);
		}

		@Callback(doc = "function(enable:boolean) -- enable or disable the crusher")
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

		@Callback(doc = "function():boolean -- get whether the crusher is currently crushing items")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().shouldRenderAsActive()};
		}
		//TODO document changes in the manual
		@Callback(doc = "function(n:int):table -- get the n'th stack in the input queue with additional information as described in the manual")
		public Object[] getInputStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			TileEntityCrusher master = getTileEntity();
			if (slot<1||slot>master.processQueue.size())
				throw new IllegalArgumentException("The requested place in the queue does not exist");
			MultiblockProcessInWorld<CrusherRecipe> process = ((MultiblockProcessInWorld<CrusherRecipe>)master.processQueue.get(slot-1));
			Map<String, Object> ret = Utils.saveStack(process.inputItem);
			ret.put("progress", process.processTick);
			ret.put("maxProgress", process.maxTicks);
			return new Object[]{ret};
		}
		@Override
		public String preferredName() {
			return "ie_crusher";
		}

		@Override
		public int priority() {
			return 1000;
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityCrusher te = getTileEntity();
			if (te!=null)
			{
				te.controllingComputers++;
				te.computerOn = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityCrusher te = getTileEntity();
			if (te!=null)
				te.controllingComputers--;
		}
	}
}
