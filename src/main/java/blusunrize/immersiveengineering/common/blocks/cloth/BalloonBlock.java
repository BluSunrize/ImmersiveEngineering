/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderAndCase;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BalloonBlock extends IEEntityBlock<BalloonBlockEntity>
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of()
			.mapColor(MapColor.WOOL)
			.ignitedByLava()
			.sound(SoundType.WOOL)
			.strength(0.8F)
			.lightLevel(s -> 13)
			.noOcclusion();

	public BalloonBlock(Properties props)
	{
		super(IEBlockEntities.BALLOON, props);
		setHasColours();
		setLightOpacity(0);
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public void fallOn(@Nonnull Level w, @Nonnull BlockState state, @Nonnull BlockPos pos, Entity entity, float fallStrength)
	{
		entity.fallDistance = 0;
	}

	@Override
	@Deprecated
	public void onProjectileHit(@Nonnull Level level, @Nonnull BlockState p_60454_, @Nonnull BlockHitResult hitResult, @Nonnull Projectile p_60456_)
	{
		super.onProjectileHit(level, p_60454_, hitResult, p_60456_);
		BlockPos bPos = hitResult.getBlockPos();
		Vec3 pos = Vec3.atCenterOf(bPos);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_BLAST,
				SoundSource.BLOCKS, 1.5f, 0.7f);
		level.removeBlock(bPos, false);
		level.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 0, .05, 0);
		if(level.getBlockEntity(bPos) instanceof BalloonBlockEntity balloon)
		{
			ShaderAndCase shader = ShaderRegistry.getStoredShaderAndCase(balloon.getShader());
			if(shader!=null)
				shader.registryEntry().getEffectFunction().execute(level, shader.shader(), null, shader.sCase().getShaderType().toString(), pos, null, .375f);
		}
	}
}
