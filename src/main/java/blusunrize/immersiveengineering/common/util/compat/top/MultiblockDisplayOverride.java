/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Robustprogram - 26.1.2020
 */
public class MultiblockDisplayOverride implements IBlockDisplayOverride
{
	@Override
	public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
		BlockState blockState, IProbeHitData data)
	{
		BlockEntity te = world.getBlockEntity(data.getPos());
		if(te instanceof IMultiblockBE<?>)
		{
			ItemStack stack = new ItemStack(blockState.getBlock(), 1);
			if(Tools.show(mode, Config.getRealConfig().getShowModName()))
				probeInfo.horizontal()
						.item(stack)
						.vertical()
						.itemLabel(stack)
						.text(Component.literal(TextStyleClass.MODNAME+ImmersiveEngineering.MODNAME));
			else
				probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
						.item(stack)
						.itemLabel(stack);
			return true;
		}
		return false;
	}
}
