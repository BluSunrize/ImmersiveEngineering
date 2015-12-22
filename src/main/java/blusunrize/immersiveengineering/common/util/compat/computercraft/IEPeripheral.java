package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.EventHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class IEPeripheral implements IPeripheral {
	World w;
	int x, y, z;
	public IEPeripheral(World w, int _x, int _y, int _z)
	{
		this.w = w;
		x = _x;
		y = _y;
		z = _z;
	}
	protected TileEntity getTileEntity(Class<? extends TileEntity> type) {
		boolean usePipeline = FMLCommonHandler.instance().getEffectiveSide()!=Side.SERVER;
		TileEntityRequest req = null;
		if (usePipeline) {
			req = new TileEntityRequest(w, x, y, z);
			synchronized (req) {
				EventHandler.ccRequestedTEs.add(req);
				try {
					while (!req.checked)
						req.wait();
				} catch (InterruptedException e) {
					return null;
				}
			}
		}
		TileEntity te = usePipeline?req.te:w.getTileEntity(x, y, z);
		if (te!=null&&te.getClass().equals(type))
			return te;
		return null;
	}
	protected Object[] saveStack(ItemStack stack, Object[] ret)
	{
		if (stack!=null&&stack.getItem()!=null) {
			ret[0] = stack.stackSize;
			ret[1] = stack.getItem().getUnlocalizedName(stack);
			ret[2] = stack.getItemDamage();
		} else
			ret[1] = "Empty";
		return ret;
	}
}
