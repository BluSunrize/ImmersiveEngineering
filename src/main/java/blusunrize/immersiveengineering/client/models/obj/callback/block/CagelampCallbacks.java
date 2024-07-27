/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class CagelampCallbacks implements BlockCallback<Boolean>
{
	public static final CagelampCallbacks INSTANCE = new CagelampCallbacks();

	@Override
	public Boolean extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return state.getValue(IEProperties.ACTIVE);
	}

	@Override
	public Boolean getDefaultKey()
	{
		return false;
	}

	@Override
	public boolean shadeQuads(Boolean object, String material)
	{
		return !"emissive".equals(material);
	}
}
