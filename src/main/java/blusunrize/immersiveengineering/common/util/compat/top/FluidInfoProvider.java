/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.metal.SheetmetalTankTileEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Robustprogram - 26.1.2020
 */
public class FluidInfoProvider implements IProbeInfoProvider
{

	@Override
	public String getID()
	{
		return ImmersiveEngineering.MODID+":"+"FluidInfo";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
			BlockState blockState, IProbeHitData data)
	{
		BlockEntity tileEntity = world.getBlockEntity(data.getPos());
		if(tileEntity instanceof SheetmetalTankTileEntity)
		{
			SheetmetalTankTileEntity master = ((SheetmetalTankTileEntity) tileEntity).master();
			int current = master.tank.getFluidAmount();
			int max = master.tank.getCapacity();

			if(current > 0)
			{
				probeInfo.progress(current, max,
						probeInfo.defaultProgressStyle()
								.suffix("mB")
								.numberFormat(NumberFormat.COMPACT));
			}
		}
	}
}
