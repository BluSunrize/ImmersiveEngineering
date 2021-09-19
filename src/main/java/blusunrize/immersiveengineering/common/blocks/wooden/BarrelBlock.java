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
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelTileEntity;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class BarrelBlock extends IETileProviderBlock
{
	private final boolean metal;

	public BarrelBlock(String name, boolean metal)
	{
		super(name, getProperties(metal), BlockItemIE::new);
		this.metal = metal;
	}

	@Nullable
	@Override
	public BlockEntity createTileEntity(@Nonnull BlockState state, @Nonnull BlockGetter world)
	{
		if(metal)
			return new MetalBarrelTileEntity();
		else
			return new WoodenBarrelTileEntity();
	}

	private static Block.Properties getProperties(boolean metal)
	{
		Block.Properties base;
		if(metal)
			base = Block.Properties.of(Material.METAL).sound(SoundType.METAL);
		else
			base = Block.Properties.of(Material.WOOD).sound(SoundType.WOOD);
		return base.strength(2, 5);
	}
}
