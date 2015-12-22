package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class IEPeripheralProvider implements IPeripheralProvider
{

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
			if (te instanceof TileEntityRefinery)
			{
				TileEntityRefinery ref = (TileEntityRefinery) te;
				if (ref.pos==9&&ref.facing==side)
					return new PeripheralRefinery(world, x, y, z);
				else
					return null;
			}
			if (te instanceof TileEntityDieselGenerator)
			{
				TileEntityDieselGenerator gen = (TileEntityDieselGenerator) te;
				TileEntityDieselGenerator master = gen.master();
				if (master==null)
					return null;
				if (((gen.pos==21 && !master.mirrored) || (gen.pos==23 && master.mirrored)))
					return new PeripheralDieselGenerator(world, x, y, z);
				else
					return null;
			}
			if (te instanceof TileEntitySampleDrill)
			{
				if (((TileEntitySampleDrill) te).pos==0)
					return new PeripheralCoreDrill(world, x, y, z);
				else
					return null;
			}
			
		}
		return null;
	}

}
