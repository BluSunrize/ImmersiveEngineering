package blusunrize.immersiveengineering.common.util.compat.computercraft;

import java.util.HashMap;
import java.util.Map;

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
		TileEntity te = usePipeline?EventHandler.requestTE(w, x, y, z):w.getTileEntity(x, y, z);
		if (te!=null&&te.getClass().equals(type))
			return te;
		return null;
	}
	protected Map<String, Object> saveStack(ItemStack stack)
	{
		HashMap<String, Object> ret = new HashMap<>();
		if (stack!=null&&stack.getItem()!=null)
		{
			ret.put("stackSize", stack.stackSize);
			ret.put("item", stack.getItem().getUnlocalizedName(stack));
			ret.put("damage", stack.getItemDamage());
		}
		return ret;
	}
	protected Map<String, Object> saveFluidTank(FluidTank tank)
	{
		HashMap<String, Object> ret = new HashMap<>();
		if (tank!=null&&tank.getFluid()!=null)
		{
			ret.put("fluid", tank.getFluid().getFluid().getUnlocalizedName());
			ret.put("amount", tank.getFluidAmount());
			ret.put("capacity", tank.getCapacity());
		}
		return ret;
	}
	protected Map<String, Object> saveFluidStack(FluidStack tank)
	{
		HashMap<String, Object> ret = new HashMap<>();
		if (tank!=null&&tank.getFluid()!=null)
		{
			ret.put("fluid", tank.getFluid().getUnlocalizedName());
			ret.put("amount", tank.amount);
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
