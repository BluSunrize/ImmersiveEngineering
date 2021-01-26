/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author Robustprogram - 26.1.2020
 */
public class ProcessProvider implements IProbeInfoProvider
{

	@Override
	public String getID()
	{
		return ImmersiveEngineering.MODID+":"+"ProcessInfo";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world,
		BlockState blockState, IProbeHitData data)
	{
		TileEntity tileEntity = world.getTileEntity(data.getPos());
		if(tileEntity instanceof IEBlockInterfaces.IProcessTile)
		{
			int[] curTicks = ((IEBlockInterfaces.IProcessTile) tileEntity).getCurrentProcessesStep();
			int[] maxTicks = ((IEBlockInterfaces.IProcessTile) tileEntity).getCurrentProcessesMax();
			int h = Math.max(4, (int)Math.ceil(12/(float)curTicks.length));

			for(int i = 0; i < curTicks.length; i++)
				if(maxTicks[i] > 0)
				{
					float current = curTicks[i]/(float)maxTicks[i]*100;
					probeInfo.progress(
						(int)current,
						100,
						probeInfo.defaultProgressStyle().showText(h >= 10).suffix("%").height(h)
					);
				}
		}
	}
}
