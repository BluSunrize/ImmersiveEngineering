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
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class GunpowderBarrelBlock extends TntBlock
{

	public GunpowderBarrelBlock(String name)
	{
		super(Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2, 5));
		setRegistryName(ImmersiveEngineering.MODID, name);
		IEContent.registeredIEBlocks.add(this);
		IEContent.registeredIEItems.add(new BlockItemIE(this));
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return 100;
	}

	@Override
	public void catchFire(BlockState state, Level world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter)
	{
		IEExplosiveEntity explosive = spawnExplosive(world, pos, state, igniter);
		world.playSound(null, explosive.getX(), explosive.getY(), explosive.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
		world.removeBlock(pos, false);
	}

	@Override
	public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion)
	{
		super.onBlockExploded(state, world, pos, explosion);
		if(!world.isClientSide)
		{
			IEExplosiveEntity explosive = spawnExplosive(world, pos, state, explosion.getSourceMob());
			explosive.setFuse((short)(world.random.nextInt(explosive.getLife()/4)+explosive.getLife()/8));
		}
	}

	private IEExplosiveEntity spawnExplosive(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity igniter)
	{
		IEExplosiveEntity explosive = new IEExplosiveEntity(world, pos, igniter, state, 5);
		explosive.setDropChance(1);
		world.addFreshEntity(explosive);
		return explosive;
	}

	@Override
	public void wasExploded(Level worldIn, BlockPos pos, Explosion explosionIn)
	{

	}
}
