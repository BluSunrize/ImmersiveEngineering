package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PeripheralCrusher extends IEPeripheral {
	public PeripheralCrusher(World w, int _x, int _y, int _z) {
		super(w, _x, _y, _z);
	}

	World w;
	int x, y, z;
	@Override
	public String getType() {
		return "IE:crusher";
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"getQueueLength", "setEnabled", "getActive", "getInputStack"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException {
		TileEntityCrusher te = (TileEntityCrusher) getTileEntity(TileEntityCrusher.class);
		if (te==null)
			throw new LuaException("The crusher was removed");
		switch (method) {
		case 0: //queue length
			synchronized (te.inputs) {
				return new Object[]{te.inputs.size()};
			}
		case 1: //set RS simulation
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean param = (boolean)arguments[0];
			te.computerOn = param;
			return null;
		case 2: //get active
			return new Object[]{te.active};
		case 3: //get queue element
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int id = (int) (double)arguments[0];
			ItemStack stack;
			synchronized (te.inputs) {
				if (id<0||id>=te.inputs.size())
					throw new LuaException("The requested place in the queue does not exist");
				stack = te.inputs.get(id);
			}
			if (stack==null||stack.getItem()==null)
				return null;
			return new Object[]{stack.stackSize, stack.getItem().getUnlocalizedName(stack), stack.getItemDamage()};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {
		TileEntityCrusher te = (TileEntityCrusher) getTileEntity(TileEntityCrusher.class);
		if (te==null)
			return;
		te.computerControlled = true;
		te.computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer) {
		TileEntityCrusher te = (TileEntityCrusher) getTileEntity(TileEntityCrusher.class);
		if (te==null)
			return;
		te.computerControlled = false;
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (!(other instanceof PeripheralCrusher))
			return false;
		PeripheralCrusher otherPer = (PeripheralCrusher) other;
		return w==otherPer.w&&x==otherPer.x&&y==otherPer.y&&z==otherPer.z;
	}
	@Override
	protected TileEntity getTileEntity(Class<? extends TileEntity> type) {
		TileEntityCrusher te = (TileEntityCrusher) super.getTileEntity(type);
		return te==null?null:te.master();
	}
}
