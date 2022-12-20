/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.register.IEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public abstract class StoneMultiblock extends IETemplateMultiblock
{
	public StoneMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, IEBlocks.BlockEntry<?> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, size, baseState);
	}

	public StoneMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, RegistryObject<? extends Block> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, size, baseState);
	}

	@Override
	public boolean canBeMirrored()
	{
		return false;
	}
}
