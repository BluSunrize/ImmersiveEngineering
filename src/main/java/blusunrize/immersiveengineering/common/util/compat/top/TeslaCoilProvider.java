/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilBlockEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Robustprogram - 26.1.2020
 */
public class TeslaCoilProvider implements IProbeInfoProvider
{

	@Override
	public ResourceLocation getID()
	{
		return ImmersiveEngineering.rl("teslacoil_info");
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
		BlockState blockState, IProbeHitData data)
	{
		BlockEntity blockEntity = world.getBlockEntity(data.getPos());

		if(blockEntity instanceof TeslaCoilBlockEntity teslaCoil)
		{
			teslaCoil = teslaCoil.master();
			if(teslaCoil==null)
			{
				probeInfo.text(new TextComponent("<ERROR>"));
				return;
			}

			probeInfo.text(new TranslatableComponent(
					Lib.CHAT_INFO+"rsControl."+(teslaCoil.redstoneControlInverted?"invertedOn": "invertedOff")
			));

			probeInfo.text(new TranslatableComponent(
					Lib.CHAT_INFO+"tesla."+(teslaCoil.lowPower?"lowPower": "highPower")
			));
		}
	}
}
