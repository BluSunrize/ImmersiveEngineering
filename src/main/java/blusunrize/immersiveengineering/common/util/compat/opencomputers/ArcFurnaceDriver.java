package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import java.util.Map;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.items.ItemGraphiteElectrode;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
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

public class ArcFurnaceDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityArcFurnace)
		{
			int pos = ((TileEntityArcFurnace)te).pos;
			if (pos==25)
			{
				TileEntityArcFurnace arc = (TileEntityArcFurnace)te;
				return new ArcFurnaceEnvironment(w, arc.xCoord-arc.offset[0], arc.yCoord-arc.offset[1], arc.zCoord-arc.offset[2], TileEntityArcFurnace.class);
			}
		}
		return null;
	}

	@Override
	public Class<?> getTileEntityClass()
	{
		return TileEntityArcFurnace.class;
	}
	public class ArcFurnaceEnvironment extends ManagedEnvironmentIE<TileEntityArcFurnace>
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
		public ArcFurnaceEnvironment(World w, int x, int y, int z, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, x, y, z, teClass);
		}

		@Override
		public void onConnect(Node node)
		{
			TileEntityArcFurnace master = getTileEntity();
			if (master!=null)
			{
				master.computerControlled = true;
				master.computerOn = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityArcFurnace te = getTileEntity();
			if (te!=null)
				te.computerControlled = false;
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

		@Callback(doc = "function(on:boolean) -- turns the excavator on or off")
		public Object[] setEnabled(Context context, Arguments args)
		{
			getTileEntity().computerOn = args.checkBoolean(0);
			return null;
		}

		@Callback(doc = "function():boolean -- checks whether the arc furnace is currently active")
		public Object[] isActive(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().active};
		}

		@Callback(doc = "function(stack:int):table -- returns the specified input stack as described in the manual")
		public Object[] getInputStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if (slot<1||slot>12)
				throw new IllegalArgumentException("Input slots are 1-12");
			TileEntityArcFurnace master = getTileEntity();
			Map<String, Object> stack = Utils.saveStack(master.getStackInSlot(slot-1));
			stack.put("progress", master.process[slot-1]);
			stack.put("maxProgress", master.processMax[slot-1]);
			return new Object[]{stack};
		}

		@Callback(doc = "function(stack:int):table -- returns the specified output stack")
		public Object[] getOutputStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if (slot<1||slot>6)
				throw new IllegalArgumentException("Output slots are 1-6");
			return new Object[]{getTileEntity().getStackInSlot(slot+15)};
		}

		@Callback(doc = "function(stack:int):table -- returns the specified additive stack")
		public Object[] getAdditiveStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if (slot<1||slot>4)
				throw new IllegalArgumentException("Additive slots are 1-4");
			return new Object[]{getTileEntity().getStackInSlot(slot+11)};
		}

		@Callback(doc = "function():table -- returns the slag stack")
		public Object[] getSlagStack(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().getStackInSlot(22)};
		}

		@Callback(doc = "function():boolean -- checks whether the arc furnace has all 3 electrodes")
		public Object[] hasElectrodes(Context context, Arguments args)
		{
			TileEntityArcFurnace master = getTileEntity();
			return new Object[]{master.electrodes[0]&&master.electrodes[1]&&master.electrodes[2]};
		}

		@Callback(doc = "function(electrode:int):table -- returns the specified electrode as an item stack")
		public Object[] getElectrode(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			if (slot<1||slot>3)
				throw new IllegalArgumentException("Electrode slots are 1-3");
			ItemStack stack = getTileEntity().getStackInSlot(slot+22);
			Map<String, Object> map = Utils.saveStack(stack);
			if (stack!=null&&stack.getItem() instanceof ItemGraphiteElectrode)
				map.put("damage", ItemNBTHelper.getInt(stack, "graphDmg"));
			return new Object[]{map};
		}
	}
}
