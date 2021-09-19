/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class SorterBlock extends IETileProviderBlock
{
	boolean fluid;

	public SorterBlock(String name, boolean fluid)
	{
		super(name, Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2F, 5F),
				BlockItemIE::new);
		this.fluid = fluid;
	}

	@Nullable
	@Override
	public BlockEntity createTileEntity(@Nonnull BlockState state, @Nonnull BlockGetter world)
	{
		if(fluid)
			return new FluidSorterTileEntity();
		else
			return new SorterTileEntity();
	}
}
