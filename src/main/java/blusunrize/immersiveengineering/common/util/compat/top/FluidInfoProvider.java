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
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author BluSunrize - 3.1.2020
 */
public class FluidInfoProvider implements IProbeInfoProvider
{

	@Override
	public String getID()
	{
		return ImmersiveEngineering.MODID+":"+"FluidInfo";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world,
			BlockState blockState, IProbeHitData data)
	{
		TileEntity tileEntity = world.getTileEntity(data.getPos());
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
