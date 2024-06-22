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
import blusunrize.immersiveengineering.client.models.obj.callback.block.BalloonCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.cloth.BalloonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class BalloonCallbacks implements BlockCallback<Key>
{
	public static final BalloonCallbacks INSTANCE = new BalloonCallbacks();
	private static final Key INVALID = new Key(0, DyeColor.BLACK, DyeColor.PURPLE);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof BalloonBlockEntity balloon))
			return getDefaultKey();
		return new Key(balloon.style, balloon.colour0, balloon.colour1);
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public Color4 getRenderColor(Key key, String group, String material, ShaderCase shaderCase, Color4 original)
	{
		if(shaderCase!=null)
			return original;
		if(key.style()==0)
		{
			if(group.startsWith("balloon1_"))
				return Color4.from(key.color1());
			if(group.startsWith("balloon0_"))
				return Color4.from(key.color0());
		}
		else
		{
			if(group.endsWith("_1"))
				return Color4.from(key.color1());
			if(group.endsWith("_0"))
				return Color4.from(key.color0());
		}
		return original;
	}

	public record Key(int style, DyeColor color0, DyeColor color1)
	{
	}
}
