/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class BarrelBlock
{
	public static GenericTileBlock<? extends WoodenBarrelTileEntity> make(BlockBehaviour.Properties props, boolean metal) {
		if (metal)
			return new GenericTileBlock<>(IETileTypes.METAL_BARREL, props);
		else
			return new GenericTileBlock<>(IETileTypes.WOODEN_BARREL, props);
	}

	public static Block.Properties getProperties(boolean metal)
	{
		Block.Properties base;
		if(metal)
			base = Block.Properties.of(Material.METAL).sound(SoundType.METAL);
		else
			base = Block.Properties.of(Material.WOOD).sound(SoundType.WOOD);
		return base.strength(2, 5);
	}
}
