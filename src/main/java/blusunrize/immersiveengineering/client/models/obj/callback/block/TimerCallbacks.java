/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.client.models.obj.callback.block.TimerCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.metal.RedstoneTimerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class TimerCallbacks implements BlockCallback<Key>
{
	public static final TimerCallbacks INSTANCE = new TimerCallbacks();
	private static final Key INVALID = new Key(DyeColor.WHITE);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof RedstoneTimerBlockEntity timer))
			return getDefaultKey();
		return new Key(timer.redstoneChannel);
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public Color4 getRenderColor(Key key, String group, String material, ShaderCase shader, Color4 original)
	{
		if("coloured".equals(group))
			return Color4.from(key.output());
		return original;
	}

	public record Key(DyeColor output)
	{
	}
}
