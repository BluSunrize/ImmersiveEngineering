/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.entities.IEExplosiveEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.TNTBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GunpowderBarrelBlock extends TNTBlock
{

	public GunpowderBarrelBlock(String name)
	{
		super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2, 5));
		setRegistryName(ImmersiveEngineering.MODID, name);
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new BlockItemIE(this));
	}

	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face)
	{
		return 100;
	}

	@Override
	public void catchFire(BlockState state, World world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter)
	{
		IEExplosiveEntity explosive = new IEExplosiveEntity(world, pos, igniter, state, 4).setDropChance(1);
		world.addEntity(explosive);
		world.playSound(null, explosive.posX, explosive.posY, explosive.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.removeBlock(pos, false);
	}

	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion)
	{
		super.onBlockExploded(state, world, pos, explosion);
		if(!world.isRemote)
		{
			IEExplosiveEntity explosive = new IEExplosiveEntity(world, pos, explosion.getExplosivePlacedBy(), state, 4);
			explosive.setFuse((short)(world.rand.nextInt(explosive.getFuse()/4)+explosive.getFuse()/8));
			world.addEntity(explosive);
		}
	}

	@Override
	public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn)
	{

	}
}
