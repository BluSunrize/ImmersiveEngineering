/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class WorkbenchCallbacks implements BlockCallback<Boolean>
{
	public static final WorkbenchCallbacks INSTANCE = new WorkbenchCallbacks();

	@Override
	public Boolean extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof ModWorkbenchBlockEntity workbench))
			return false;
		ModWorkbenchBlockEntity master = workbench.master();
		return master!=null&&master.getInventory().get(0).getItem() instanceof EngineersBlueprintItem;
	}

	private static final IEObjState normalDisplayList = new IEObjState(VisibilityList.show("cube0"));
	private static final IEObjState blueprintDisplayList = new IEObjState(VisibilityList.show("cube0", "blueprint"));

	@Override
	public IEObjState getIEOBJState(Boolean hasBlueprint)
	{
		if(hasBlueprint)
			return blueprintDisplayList;
		return normalDisplayList;
	}
}
