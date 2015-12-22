package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class IEPeripheralProvider implements IPeripheralProvider {

	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityIEBase)
		{
			if (te instanceof TileEntityEnergyMeter&&((TileEntityEnergyMeter)te).dummy)
			{
				return new PeripheralEnergyMeter(world, x, y, z);
			}
			if (te instanceof TileEntityCrusher)
			{
				TileEntityCrusher crush = (TileEntityCrusher) te;
				if (crush.pos==9&&crush.facing==side)
					return new PeripheralCrusher(world, x, y, z);
				else
					return null;
			}
			if (te instanceof TileEntityArcFurnace)
			{
				TileEntityArcFurnace arc = (TileEntityArcFurnace) te;
				if (arc.pos==25&&arc.facing==side)
					return new PeripheralArcFurnace(world, x, y, z);
				else
					return null;
			}
			if (te instanceof TileEntityExcavator)
			{
				TileEntityExcavator exc = (TileEntityExcavator) te;
				if (exc.pos==3&&exc.facing==side)
					return new PeripheralExcavator(world, x, y, z);
				else
					return null;
			}
		}
		return null;
	}

}
