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
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SheetmetalTankLogic;
import mcjty.theoneprobe.api.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Robustprogram - 26.1.2020
 */
public class FluidInfoProvider implements IProbeInfoProvider
{

	@Override
	public ResourceLocation getID()
	{
		return ImmersiveEngineering.rl("fluid_info");
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world,
			BlockState blockState, IProbeHitData data)
	{
		if(!(world.getBlockEntity(data.getPos()) instanceof IMultiblockBE<?> multiblockBE))
			return;
		if(!(multiblockBE.getHelper().getState() instanceof SheetmetalTankLogic.State tankState))
			return;
		int current = tankState.tank.getFluidAmount();
		int max = tankState.tank.getCapacity();

		if(current > 0)
			probeInfo.progress(current, max,
					probeInfo.defaultProgressStyle()
							.suffix("mB")
							.numberFormat(NumberFormat.COMPACT));
	}
}
