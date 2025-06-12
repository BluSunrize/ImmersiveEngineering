/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.entities.GunpowderBarrelEntity;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class GunpowderBarrelBlock extends TntBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.WOOD)
			.ignitedByLava()
			.instrument(NoteBlockInstrument.BASS)
			.sound(SoundType.WOOD)
			.strength(2, 0);

	public GunpowderBarrelBlock(BlockBehaviour.Properties props)
	{
		super(props);
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return 100;
	}

	@Override
	public void onCaughtFire(BlockState state, Level world, BlockPos pos, @org.jetbrains.annotations.Nullable Direction face, @org.jetbrains.annotations.Nullable LivingEntity igniter)
	{
		if(!world.isClientSide)
		{
			GunpowderBarrelEntity explosive = spawnExplosive(world, pos, state, igniter);
			world.playSound(null, explosive.getX(), explosive.getY(), explosive.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
			world.removeBlock(pos, false);
		}
	}

	@Override
	public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion)
	{
		super.onBlockExploded(state, world, pos, explosion);
		if(!world.isClientSide)
		{
			GunpowderBarrelEntity explosive = spawnExplosive(world, pos, state, explosion.getIndirectSourceEntity());
			explosive.setFuse((short)(world.random.nextInt(explosive.getFuse()/4)+explosive.getFuse()/8));
		}
	}

	private GunpowderBarrelEntity spawnExplosive(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity igniter)
	{
		GunpowderBarrelEntity explosive = new GunpowderBarrelEntity(world, pos, igniter, state, 8);
		world.addFreshEntity(explosive);
		return explosive;
	}

	@Override
	public void wasExploded(Level worldIn, BlockPos pos, Explosion explosionIn)
	{

	}
}
