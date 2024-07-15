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
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class SirenCallbacks implements BlockCallback<Boolean>
{
	public static final SirenCallbacks INSTANCE = new SirenCallbacks();

	private static final IEObjState VERTICAL = new IEObjState(VisibilityList.show("main", "base", "extra_horn"));
	private static final IEObjState HORIZONTAL = new IEObjState(VisibilityList.show("main", "wallmount"));

	@Override
	public Boolean extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return state.getValue(IEProperties.FACING_ALL).getAxis()==Axis.Y;
	}

	@Override
	public Boolean getDefaultKey()
	{
		return true;
	}

	@Override
	public IEObjState getIEOBJState(Boolean vertical)
	{
		return vertical?VERTICAL: HORIZONTAL;

	}

}
