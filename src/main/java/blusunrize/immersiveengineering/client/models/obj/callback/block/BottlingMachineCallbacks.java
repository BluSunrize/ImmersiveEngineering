/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import com.mojang.datafixers.util.Unit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class BottlingMachineCallbacks implements BlockCallback<Unit>
{
	public static final BottlingMachineCallbacks INSTANCE = new BottlingMachineCallbacks();

	@Override
	public Unit extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return getDefaultKey();
	}

	@Override
	public Unit getDefaultKey()
	{
		return Unit.INSTANCE;
	}

	@Override
	public boolean dependsOnLayer()
	{
		return true;
	}

	@Override
	public boolean shouldRenderGroup(Unit object, String group, RenderType layer)
	{
		return "glass".equals(group)==(RenderType.translucent()==layer);
	}
}
