package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorCreative;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodlight;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class IEPeripheralProvider implements IPeripheralProvider
{

	@Override
	public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityIEBase)
		{
			if (te instanceof TileEntityEnergyMeter&&((TileEntityEnergyMeter)te).dummy)
			{
				return new PeripheralEnergyMeter(world, pos);
			}
			if (te instanceof TileEntityCrusher)
			{
				TileEntityCrusher crush = (TileEntityCrusher) te;
				if (crush.isRedstonePos())
					return new PeripheralCrusher(world, pos.add(-crush.offset[0], -crush.offset[1], -crush.offset[2]));
				else
					return null;
			}
			if (te instanceof TileEntityArcFurnace)
			{
				TileEntityArcFurnace arc = (TileEntityArcFurnace) te;
				if (arc.isRedstonePos())
					return new PeripheralArcFurnace(world, pos.add(-arc.offset[0], -arc.offset[1], -arc.offset[2]));
				else
					return null;
			}
			if (te instanceof TileEntityExcavator)
			{
				TileEntityExcavator exc = (TileEntityExcavator) te;
				if (exc.isRedstonePos())
					return new PeripheralExcavator(world, pos.add(-exc.offset[0], -exc.offset[1], -exc.offset[2]));
				else
					return null;
			}
			if (te instanceof TileEntityRefinery)
			{
				TileEntityRefinery ref = (TileEntityRefinery) te;
				if (ref.isRedstonePos())
					return new PeripheralRefinery(world, pos.add(-ref.offset[0], -ref.offset[1], -ref.offset[2]));
				else
					return null;
			}
			if (te instanceof TileEntityDieselGenerator)
			{
				TileEntityDieselGenerator gen = (TileEntityDieselGenerator) te;
				TileEntityDieselGenerator master = gen.master();
				if (master==null)
					return null;
				if (gen.isRedstonePos())
					return new PeripheralDieselGenerator(world, pos.add(-gen.offset[0], -gen.offset[1], -gen.offset[2]));
				else
					return null;
			}
			if (te instanceof TileEntitySampleDrill)
			{
				if (!((TileEntitySampleDrill) te).isDummy())
					return new PeripheralCoreDrill(world, pos);
				else
					return null;
			}
			if (te instanceof TileEntityFloodlight)
				return new PeripheralFloodlight(world, pos);
			if (te instanceof TileEntityFermenter)
			{
				TileEntityFermenter fer = (TileEntityFermenter) te;
				if (fer.isRedstonePos())
					return new PeripheralFermenter(world, pos.add(-fer.offset[0], -fer.offset[1], -fer.offset[2]));
				else
					return null;
			}
			if (te instanceof TileEntitySqueezer)
			{
				TileEntitySqueezer sq = (TileEntitySqueezer) te;
				if (sq.isRedstonePos())
					return new PeripheralSqueezer(world, pos.add(-sq.offset[0], -sq.offset[1], -sq.offset[2]));
				else
					return null;
			}
			if (te instanceof TileEntityAssembler)
			{
				TileEntityAssembler assembler = (TileEntityAssembler) te;
				if (assembler.isRedstonePos())
					return new PeripheralAssembler(world, pos.add(-assembler.offset[0], -assembler.offset[1], -assembler.offset[2]));
				else
					return null;
			}
			if (te instanceof TileEntityTeslaCoil)
			{
				BlockPos pos2 = (((TileEntityTeslaCoil) te).isDummy()?pos.down():pos);
				return new PeripheralTeslaCoil(world, pos2);
			}
			if (te instanceof TileEntityCapacitorLV)
			{

				String type = "";
				if (te instanceof TileEntityCapacitorCreative)
					type = "creative";
				else if (te instanceof TileEntityCapacitorHV)
					type = "hv";
				else if (te instanceof TileEntityCapacitorMV)
					type = "mv";
				else if (te instanceof TileEntityCapacitorLV)
					type = "lv";
				return new PeripheralCapacitor(world, pos, type);
			}
		}
		return null;
	}

}
