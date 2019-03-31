/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCushion extends BlockIEBase
{
	public BlockCushion()
	{
		super("cushion", Block.Properties.create(Material.CLOTH).hardnessAndResistance(0.8F), ItemBlockIEBase.class);
	}

	@Override
	public void onFallenUpon(World w, BlockPos pos, Entity entity, float fallStrength)
	{
		entity.fallDistance = 0;
	}
}
