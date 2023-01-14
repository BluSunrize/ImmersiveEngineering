/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ExcavatorLogic;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ExcavatorMultiblock extends IETemplateMultiblock
{
	public ExcavatorMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/excavator"),
				new BlockPos(1, 1, 5), new BlockPos(1, 1, 5), new BlockPos(3, 3, 6),
				IEMultiblockLogic.EXCAVATOR);
	}

	@Override
	public boolean createStructure(Level world, BlockPos pos, Direction side, Player player)
	{
		final boolean excavatorFormed = super.createStructure(world, pos, side, player);
		if(excavatorFormed)
		{
			// Try to also form the bucket wheel
			BlockEntity clickedTE = world.getBlockEntity(pos);
			if(clickedTE instanceof IMultiblockBE<?> excavator)
			{
				final IMultiblockLevel mbLevel = excavator.getHelper().getContext().getLevel();
				BlockPos wheelCenter = mbLevel.toAbsolute(ExcavatorLogic.WHEEL_CENTER);
				IEMultiblocks.BUCKET_WHEEL.createStructure(world, wheelCenter, side.getCounterClockWise(), player);
			}
		}
		return excavatorFormed;
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}