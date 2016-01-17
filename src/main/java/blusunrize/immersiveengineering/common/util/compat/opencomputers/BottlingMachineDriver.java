package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import static blusunrize.immersiveengineering.common.util.Utils.saveStack;

import java.util.Map;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine;
import blusunrize.immersiveengineering.common.util.Utils;
import dan200.computercraft.api.lua.LuaException;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BottlingMachineDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityBottlingMachine)
		{
			int offsetY = ((TileEntityBottlingMachine)te).offset[1];
			int pos = ((TileEntityBottlingMachine)te).pos;
			if (offsetY==0&&pos!=0&&pos!=2)
			{
				TileEntityBottlingMachine master = ((TileEntityBottlingMachine)te).master();
				return new BottlingMachineEnvironment(w, master.xCoord, master.yCoord, master.zCoord, TileEntityBottlingMachine.class);
			}
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityBottlingMachine.class;
	}
	public class BottlingMachineEnvironment extends ManagedEnvironmentIE<TileEntityBottlingMachine>
	{

		@Override
		public String preferredName()
		{
			return "ie_bottling_machine";
		}

		@Override
		public int priority()
		{
			return 1000;
		}
		public BottlingMachineEnvironment(World w, int x, int y, int z, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, x, y, z, teClass);
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityBottlingMachine master = getTileEntity();
			if (master!=null)
			{
				master.computerControlled = true;
				master.computerOn = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityBottlingMachine te = getTileEntity();
			if (te!=null)
				te.computerControlled = false;
		}
		@Callback(doc = "function():table -- returns the internal fluid tank")
		public Object[] getFluid(Context context, Arguments args)
		{
			return new Object[]{Utils.saveFluidTank(getTileEntity().tank)};
		}

		@Callback(doc = "function(pos:int):table -- returns the empty cannister at the specified position")
		public Object[] getEmptyCannister(Context context, Arguments args)
		{
			int param = args.checkInteger(0);
			if (param<0||param>4)
				throw new IllegalArgumentException("Only 0-4 are valid cannister positions");
			TileEntityBottlingMachine master = getTileEntity();
			int id = master.getEmptyCannister(param);
			Map<String, Object> map = saveStack(master.inventory[id]);
			map.put("process", master.process[id]);
			return new Object[]{map};
		}

		@Callback(doc = "function():int -- returns amount of empty cannisters")
		public Object[] getEmptyCannisterCount(Context context, Arguments args)
		{
			TileEntityBottlingMachine master = getTileEntity();
			return new Object[]{master.getEmptyCount()};
		}

		@Callback(doc = "function(pos:int):table -- returns the filled cannister at the specified position")
		public Object[] getFilledCannister(Context context, Arguments args)
		{
			int param = args.checkInteger(0);
			if (param<0||param>4)
				throw new IllegalArgumentException("Only 0-4 are valid cannister positions");
			TileEntityBottlingMachine master = getTileEntity();
			int id = master.getFilledCannister(param);
			Map<String, Object> map = saveStack(master.inventory[id]);
			map.put("process", master.process[id]);
			return new Object[]{map};
		}

		@Callback(doc = "function():int -- returns the amount of filled cannisters")
		public Object[] getFilledCannisterCount(Context context, Arguments args)
		{
			TileEntityBottlingMachine master = getTileEntity();
			return new Object[]{master.getFilledCount()};
		}

		@Callback(doc = "function(on:boolean) -- turns the bottling machine on or off")
		public Object[] setEnabled(Context context, Arguments args)
		{
			boolean on = args.checkBoolean(0);
			getTileEntity().computerOn = on;
			return null;
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

	}
}
