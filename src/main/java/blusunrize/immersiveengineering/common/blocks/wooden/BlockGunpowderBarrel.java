/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.entities.EntityIEExplosive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGunpowderBarrel extends BlockTNT
{

	public BlockGunpowderBarrel(String name)
	{
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(2, 5));
	}

	@Override
	public void explode(World world, BlockPos pos, @Nullable EntityLivingBase igniter)
	{
		EntityIEExplosive explosive = new EntityIEExplosive(world, pos, igniter, world.getBlockState(pos), 4).setDropChance(1);
		world.spawnEntity(explosive);
		world.playSound(null, explosive.posX, explosive.posY, explosive.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.removeBlock(pos);
	}
}
