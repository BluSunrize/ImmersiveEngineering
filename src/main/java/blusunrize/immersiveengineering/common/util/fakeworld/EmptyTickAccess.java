/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.NotNull;

public class EmptyTickAccess<T> implements LevelTickAccess<T>
{
	@Override
	public boolean willTickThisTick(@NotNull BlockPos p_193197_, @NotNull T p_193198_)
	{
		return false;
	}

	@Override
	public void schedule(@NotNull ScheduledTick<T> p_193428_)
	{
	}

	@Override
	public boolean hasScheduledTick(@NotNull BlockPos p_193429_, @NotNull T p_193430_)
	{
		return false;
	}

	@Override
	public int count()
	{
		return 0;
	}
}
