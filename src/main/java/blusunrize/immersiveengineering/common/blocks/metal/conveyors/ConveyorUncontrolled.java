package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import net.minecraft.tileentity.TileEntity;

/**
 * @author BluSunrize - 06.05.2017
 */
public class ConveyorUncontrolled extends ConveyorBasic
{
	@Override
	public boolean isActive(TileEntity tile)
	{
		return true;
	}
}
