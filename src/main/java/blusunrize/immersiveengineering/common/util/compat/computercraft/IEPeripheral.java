package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.EventHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public abstract class IEPeripheral implements IPeripheral
{
	World w;
	int x, y, z;
	public IEPeripheral(World w, int _x, int _y, int _z)
	{
		this.w = w;
		x = _x;
		y = _y;
		z = _z;
	}
	protected TileEntity getTileEntity(Class<? extends TileEntity> type)
	{
		boolean usePipeline = FMLCommonHandler.instance().getEffectiveSide()!=Side.SERVER;
		TileEntityRequest req = null;
		if (usePipeline) {
			req = new TileEntityRequest(w, x, y, z);
			synchronized (req) {
				EventHandler.ccRequestedTEs.add(req);
				int timeout = 100;
				while (!req.checked&&timeout>0)
				{
					try
					{
						req.wait(50);
					}
					catch (InterruptedException e)
					{}
					timeout--;
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
			ret[0] = "Empty";
		return ret;
	}
	protected Object[] saveFluidTank(FluidTank tank, Object[] ret, int offset)
	{
		if (tank==null||tank.getFluid()==null)
			ret[0+offset] = "Empty";
		else
		{
			ret[0+offset] = tank.getFluid().getFluid().getUnlocalizedName();
			ret[1+offset] = tank.getFluidAmount();
			ret[2+offset] = tank.getCapacity();
		}
		return ret;
	}
	protected Object[] saveFluidStack(FluidStack tank, Object[] ret, int offset)
	{
		if (tank==null||tank.getFluid()==null)
			ret[0+offset] = "Empty";
		else
		{
			ret[0+offset] = tank.getFluid().getUnlocalizedName();
			ret[1+offset] = tank.amount;
		}
		return ret;
	}
	@Override
	public boolean equals(IPeripheral other)
	{
		if (!(other instanceof IEPeripheral))
			return false;
		IEPeripheral otherPer = (IEPeripheral) other;
		return w==otherPer.w&&x==otherPer.x&&y==otherPer.y&&z==otherPer.z;
	}
}
