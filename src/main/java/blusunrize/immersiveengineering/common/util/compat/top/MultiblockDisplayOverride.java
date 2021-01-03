/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

/**
 * @author Robustprogram - 26.1.2020
 */
public class MultiblockDisplayOverride implements IBlockDisplayOverride
{
	@Override
	public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world,
		BlockState blockState, IProbeHitData data)
	{
		TileEntity te = world.getTileEntity(data.getPos());
		if(te instanceof MultiblockPartTileEntity)
		{
			ItemStack stack = new ItemStack(blockState.getBlock(), 1);
			if(Tools.show(mode, Config.getRealConfig().getShowModName()))
			{
				probeInfo.horizontal()
						.item(stack)
						.vertical()
						.itemLabel(stack)
						.text(new StringTextComponent(TextStyleClass.MODNAME+ImmersiveEngineering.MODNAME));
			}
			else
			{
				probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
						.item(stack)
						.itemLabel(stack);
			}
			return true;
		}
		return false;
	}
}
