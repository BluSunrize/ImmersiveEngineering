package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CrusherDriver extends DriverTileEntity
{

	@Override
	public ManagedEnvironment createEnvironment(World w, int x, int y, int z)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityCrusher)
		{
			int pos = ((TileEntityCrusher)te).pos;
			if (pos==9)
			{
				TileEntityCrusher master = ((TileEntityCrusher)te).master();
				return new CrusherEnvironment(w, master.xCoord, master.yCoord, master.zCoord, TileEntityCrusher.class);
			}
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
		public CrusherEnvironment(World w, int x, int y, int z, Class<? extends TileEntityIEBase> teClass)
		{
			super(w, x, y, z, teClass);
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
			return new Object[]{getTileEntity().active};
		}

		@Callback(doc = "function(n:int):table -- get the n'th stack in the input queue")
		public Object[] getInputStack(Context context, Arguments args)
		{
			int slot = args.checkInteger(0);
			TileEntityCrusher master = getTileEntity();
			if (slot<0||slot>=master.inputs.size())
				throw new IllegalArgumentException("The requested place in the queue does not exist");
			return new Object[]{master.inputs.get(slot)};
		}

		@Callback(doc = "function():int -- get the current grinding progress in RF")
		public Object[] getCurrentProgress(Context context, Arguments args)
		{
			TileEntityCrusher master = getTileEntity();
			if (master.inputs.isEmpty())
				throw new IllegalArgumentException("The crusher doesn't have any inputs");
			int time = master.getRecipeTime(master.inputs.get(0))-master.process;
			if (time<=0)
				throw new IllegalArgumentException("The current crusher recipe is invalid");
			return new Object[]{time};
		}

		@Callback(doc = "function():int -- get the grinding progress in RF at which the current grinding will be done")
		public Object[] getCurrentMaxProgress(Context context, Arguments args)
		{
			TileEntityCrusher master = getTileEntity();
			if (master.inputs.isEmpty())
				throw new IllegalArgumentException("The crusher doesn't have any inputs");
			int time = master.getRecipeTime(master.inputs.get(0));
			if (time<=0)
				throw new IllegalArgumentException("The current crusher recipe is invalid");
			return new Object[]{time};
		}

		@Callback(doc = "function():int -- get the length of the input queue")
		public Object[] getQueueLength(Context context, Arguments args)
		{
			return new Object[]{getTileEntity().inputs.size()};
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
				te.computerControlled = true;
				te.computerOn = true;
			}
		}

		@Override
		public void onDisconnect(Node node)
		{
			TileEntityCrusher te = getTileEntity();
			if (te!=null)
				te.computerControlled = false;
		}
	}
}
