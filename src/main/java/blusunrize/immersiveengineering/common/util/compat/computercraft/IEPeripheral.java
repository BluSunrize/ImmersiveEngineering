package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.EventHandler;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public abstract class IEPeripheral implements IPeripheral
{
	World w;
	BlockPos pos;
	public IEPeripheral(World w, BlockPos p)
	{
		this.w = w;
		pos = p;
	}
	protected TileEntity getTileEntity(Class<? extends TileEntity> type)
	{
		boolean usePipeline = FMLCommonHandler.instance().getEffectiveSide()!=Side.SERVER;
		TileEntity te = usePipeline?EventHandler.requestTE(w, pos):w.getTileEntity(pos);
		if (te!=null&&type.isAssignableFrom(te.getClass()))
			return te;
		return null;
	}
	@Override
	public boolean equals(IPeripheral other)
	{
		if (!(other instanceof IEPeripheral))
			return false;
		IEPeripheral otherPer = (IEPeripheral) other;
		return w==otherPer.w&&otherPer.pos.equals(pos);
	}
}
