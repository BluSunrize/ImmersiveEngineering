/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FertilizerItem extends IEBaseItem
{
	public FertilizerItem()
	{
		super(new Properties());
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext)
	{
		Level level = pContext.getLevel();
		BlockPos clickedPos = pContext.getClickedPos();
		BlockPos offsetPos = clickedPos.relative(pContext.getClickedFace());
		if(BoneMealItem.applyBonemeal(pContext.getItemInHand(), level, clickedPos, pContext.getPlayer()))
		{
			if(!level.isClientSide)
				level.levelEvent(1505, clickedPos, 0);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		else
		{
			BlockState blockstate = level.getBlockState(clickedPos);
			boolean isSturdy = blockstate.isFaceSturdy(level, clickedPos, pContext.getClickedFace());
			if(isSturdy&&BoneMealItem.growWaterPlant(pContext.getItemInHand(), level, offsetPos, pContext.getClickedFace()))
			{
				if(!level.isClientSide)
					level.levelEvent(1505, offsetPos, 0);
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
			else
				return InteractionResult.PASS;
		}
	}

}