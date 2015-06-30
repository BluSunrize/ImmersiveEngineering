package blusunrize.immersiveengineering.common.util.compat;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class IC2Helper
{
	public static void loadIC2Tile(TileEntity tile)
	{
		MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile)tile));
	}
	public static void unloadIC2Tile(TileEntity tile)
	{
		MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile)tile));
	}

	public static boolean isEnergySink(TileEntity sink)
	{
		return sink instanceof IEnergySink;
	}
	public static boolean isAcceptingEnergySink(TileEntity sink, TileEntity tile, ForgeDirection fd)
	{
		return sink instanceof IEnergySink && ((IEnergySink)sink).acceptsEnergyFrom(tile, fd);
	}

	public static double injectEnergy(TileEntity sink, ForgeDirection fd, double amount, double voltage, boolean simulate)
	{
		double demanded = Math.max(0, ((IEnergySink)sink).getDemandedEnergy());
		double accepted = Math.min(demanded, amount);
		if(!simulate)
			((IEnergySink)sink).injectEnergy(fd, amount, voltage);
		return amount-accepted;
	}

	public static double getMaxItemCharge(ItemStack stack)
	{
		if(stack!=null && stack.getItem() instanceof IElectricItem)
			return ((IElectricItem)stack.getItem()).getMaxCharge(stack);
		return 0;	
	}
	public static double dischargeItem(ItemStack stack, double amount)
	{
		if(stack!=null && stack.getItem() instanceof IElectricItem)
			return ElectricItem.manager.discharge(stack, amount, 5, true, false, false);
		return 0;
	}
}
