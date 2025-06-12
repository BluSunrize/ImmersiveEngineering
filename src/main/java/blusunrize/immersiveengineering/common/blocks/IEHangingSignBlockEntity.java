/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IEHangingSignBlockEntity extends SignBlockEntity
{
	public IEHangingSignBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(IEBlockEntities.HANGING_SIGN.get(), pos, blockState);
	}

	@Override
	public int getTextLineHeight()
	{
		return 9;
	}

	@Override
	public int getMaxTextLineWidth()
	{
		return 60;
	}

	@Override
	public SoundEvent getSignInteractionFailedSoundEvent()
	{
		return SoundEvents.WAXED_HANGING_SIGN_INTERACT_FAIL;
	}
}
