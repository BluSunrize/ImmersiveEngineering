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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Robustprogram - 26.1.2020
 */
public class ProcessProvider implements IProbeInfoProvider
{

	@Override
	public ResourceLocation getID()
	{
		return ImmersiveEngineering.rl("process_info");
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
		BlockState blockState, IProbeHitData data)
	{
		BlockEntity blockEntity = world.getBlockEntity(data.getPos());
		if(blockEntity instanceof IEBlockInterfaces.IProcessBE processBE)
		{
			int[] curTicks = processBE.getCurrentProcessesStep();
			int[] maxTicks = processBE.getCurrentProcessesMax();
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
