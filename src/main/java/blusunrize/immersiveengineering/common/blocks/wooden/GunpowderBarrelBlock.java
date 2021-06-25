/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.entities.IEExplosiveEntity;
import net.minecraft.block.*;
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
import java.util.function.Supplier;

public class GunpowderBarrelBlock extends TNTBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.create(Material.WOOD)
			.sound(SoundType.WOOD)
			.hardnessAndResistance(2, 5);

	public GunpowderBarrelBlock(AbstractBlock.Properties props)
	{
		super(props);
	}

	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face)
	{
		return 100;
	}

	@Override
	public void catchFire(BlockState state, World world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter)
	{
		IEExplosiveEntity explosive = spawnExplosive(world, pos, state, igniter);
		world.playSound(null, explosive.getPosX(), explosive.getPosY(), explosive.getPosZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.removeBlock(pos, false);
	}

	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion)
	{
		super.onBlockExploded(state, world, pos, explosion);
		if(!world.isRemote)
		{
			IEExplosiveEntity explosive = spawnExplosive(world, pos, state, explosion.getExplosivePlacedBy());
			explosive.setFuse((short)(world.rand.nextInt(explosive.getFuse()/4)+explosive.getFuse()/8));
		}
	}

	private IEExplosiveEntity spawnExplosive(World world, BlockPos pos, BlockState state, @Nullable LivingEntity igniter)
	{
		IEExplosiveEntity explosive = new IEExplosiveEntity(world, pos, igniter, state, 5);
		explosive.setDropChance(1);
		world.addEntity(explosive);
		return explosive;
	}

	@Override
	public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn)
	{

	}
}
