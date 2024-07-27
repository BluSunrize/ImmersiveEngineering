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
import blusunrize.immersiveengineering.client.models.obj.callback.block.TimerCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.metal.RedstoneTimerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector4f;

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
	public Vector4f getRenderColor(Key key, String group, String material, ShaderCase shader, Vector4f original)
	{
		if("coloured".equals(group))
		{
			float[] rgb = key.output().getTextureDiffuseColors();
			return new Vector4f(rgb[0], rgb[1], rgb[2], 1);
		}
		return original;
	}

	public record Key(DyeColor output)
	{
	}
}
