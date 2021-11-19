/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Robustprogram - 26.1.2020
 */
public class EnergyInfoProvider implements IProbeInfoProvider, IProbeConfigProvider
{
	@Override
	public ResourceLocation getID()
	{
		return ImmersiveEngineering.rl("energy_info");
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
		BlockState blockState, IProbeHitData data)
	{
	}

	@Override
	public void getProbeConfig(IProbeConfig config, Player player, Level world, Entity entity,
		IProbeHitEntityData data)
	{
	}

	@Override
	public void getProbeConfig(IProbeConfig config, Player player, Level world,
		BlockState blockState, IProbeHitData data)
	{
		if(config.getRFMode()==0&&world.getBlockEntity(data.getPos()) instanceof IEBaseBlockEntity)
			config.setRFMode(1);
	}
}
