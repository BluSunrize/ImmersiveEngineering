/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.Direction;
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
public class SideConfigProvider implements IProbeInfoProvider
{

	@Override
	public ResourceLocation getID()
	{
		return ImmersiveEngineering.rl("side_config_info");
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
		BlockState blockState, IProbeHitData data)
	{
		if(data.getSideHit()==null)
			return;
		BlockEntity te = world.getBlockEntity(data.getPos());
		if(te instanceof IEBlockInterfaces.IConfigurableSides configSides)
		{
			boolean flip = player.isShiftKeyDown();
			Direction side = flip?data.getSideHit().getOpposite(): data.getSideHit();
			IOSideConfig config = configSides.getSideConfig(side);

			TextComponent combined = new TextComponent("");
			TranslatableComponent direction =
					new TranslatableComponent(Lib.DESC_INFO+"blockSide."+(flip?"opposite": "facing"));
			TranslatableComponent connection =
					new TranslatableComponent(Lib.DESC_INFO+"blockSide.io."+config.getSerializedName());
			
			combined.append(direction);
			combined.append(": ");
			combined.append(connection);
			
			probeInfo.text(combined);
		}
	}
}
