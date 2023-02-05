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
import blusunrize.immersiveengineering.common.blocks.metal.BreakerSwitchBlockEntity;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class BreakerSwitchCallbacks implements BlockCallback<Integer>
{
	public static final BreakerSwitchCallbacks INSTANCE = new BreakerSwitchCallbacks();

	@Override
	public Integer extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return blockEntity instanceof BreakerSwitchBlockEntity breaker?breaker.rotation: getDefaultKey();
	}

	@Override
	public Integer getDefaultKey()
	{
		return 0;
	}

	@Override
	public Transformation applyTransformations(Integer rotation, String group, Transformation transform)
	{
		return transform.compose(new Transformation(
				null,
				new Quaternionf().rotateXYZ(0, Mth.HALF_PI*rotation, 0),
				null, null
		));
	}
}
