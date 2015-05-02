package blusunrize.immersiveengineering.common.util;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
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

	public static double injectEnergy(TileEntity sink, ForgeDirection fd, double amount, double voltage)
	{
		return ((IEnergySink)sink).injectEnergy(fd, amount, voltage);
	}
}
