/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ChunkLoaderLogic.State;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class ChunkLoaderCallbacks implements BlockCallback<Boolean>
{
	public static final ChunkLoaderCallbacks INSTANCE = new ChunkLoaderCallbacks();

	@Override
	public Boolean extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState blockState, BlockEntity blockEntity)
	{
		if(blockEntity instanceof IMultiblockBE<?> multiblockBE&&
				multiblockBE.getHelper().getState() instanceof State state)
			return state.renderAsActive;
		return getDefaultKey();
	}

	@Override
	public Boolean getDefaultKey()
	{
		return false;
	}

	@Override
	public boolean dependsOnLayer()
	{
		return true;
	}

	@Override
	public boolean shouldRenderGroup(Boolean paper, String group, RenderType layer)
	{
		if(layer==null)
			return !"glass".equals(group);
		if("glass".equals(group))
			return layer==RenderType.translucent();
		if("amethyst".equals(group))
			return layer==RenderType.cutout();
		if("paper".equals(group))
			return paper&&layer==RenderType.cutout();
		return layer==RenderType.solid();
	}
}
