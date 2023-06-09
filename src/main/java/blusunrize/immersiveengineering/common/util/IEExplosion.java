/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.mixin.accessors.ExplosionAccess;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IEExplosion extends Explosion
{
	public float dropChance = 1;
	private int blockDestroyInt = 0;
	public int blocksPerTick = 8;
	public boolean isExplosionFinished = false;
	private final Level world;
	private final float size;
	private final BlockInteraction damagesTerrain;

	public IEExplosion(Level world, Entity igniter, double x, double y, double z, float size, boolean isFlaming, BlockInteraction damageTerrain)
	{
		super(world, igniter, null, null, x, y, z, size, isFlaming, damageTerrain);
		this.dropChance = 1/size;
		this.world = world;
		damagesTerrain = damageTerrain;
		this.size = size;
	}

	public IEExplosion setDropChance(float chance)
	{
		this.dropChance = chance;
		return this;
	}

	public void doExplosionTick()
	{
		ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
		int max = Math.min(blockDestroyInt+blocksPerTick, this.getToBlow().size());
		for(; blockDestroyInt < max; blockDestroyInt++)
		{
			BlockPos pos = this.getToBlow().get(blockDestroyInt);
			BlockState state = this.world.getBlockState(pos);
			Block block = state.getBlock();

//			if(spawnParticles)
			{
				double d0 = (float)pos.getX()+ApiUtils.RANDOM.nextFloat();
				double d1 = (float)pos.getY()+ApiUtils.RANDOM.nextFloat();
				double d2 = (float)pos.getZ()+ApiUtils.RANDOM.nextFloat();
				double d3 = d0-getPosition().x;
				double d4 = d1-getPosition().y;
				double d5 = d2-getPosition().z;
				double d6 = Mth.sqrt((float)(d3*d3+d4*d4+d5*d5));
				d3 = d3/d6;
				d4 = d4/d6;
				d5 = d5/d6;
				double d7 = 0.5D/(d6/(double)this.size+0.1D);
				d7 = d7*(double)(ApiUtils.RANDOM.nextFloat()*ApiUtils.RANDOM.nextFloat()+0.3F);
				d3 = d3*d7;
				d4 = d4*d7;
				d5 = d5*d7;
				this.world.addParticle(ParticleTypes.EXPLOSION, (d0+getPosition().x*1.0D)/2.0D, (d1+getPosition().y*1.0D)/2.0D, (d2+getPosition().z*1.0D)/2.0D, d3, d4, d5);
				this.world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
			}

			if(!state.isAir())
			{
				if(this.world instanceof ServerLevel&&state.canDropFromExplosion(this.world, pos, this))
				{
					BlockEntity tile = this.world.getBlockEntity(pos);
					LootParams.Builder lootCtx = new LootParams.Builder((ServerLevel)this.world)
							.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
							.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
							.withOptionalParameter(LootContextParams.BLOCK_ENTITY, tile);
					if(damagesTerrain==Explosion.BlockInteraction.DESTROY)
						lootCtx.withParameter(LootContextParams.EXPLOSION_RADIUS, this.size);
					state.getDrops(lootCtx).forEach((stack) -> {
						ExplosionAccess.callAddBlockDrops(objectarraylist, stack, pos);
					});
					state.onBlockExploded(world, pos, this);
				}
			}
		}
		for(Pair<ItemStack, BlockPos> pair : objectarraylist)
		{
			Block.popResource(this.world, pair.getSecond(), pair.getFirst());
		}
		if(blockDestroyInt >= this.getToBlow().size())
			this.isExplosionFinished = true;
	}

	@Override
	public void explode()
	{
		Set<BlockPos> set = Sets.newHashSet();
		int i = 16;

		for(int j = 0; j < 16; ++j)
			for(int k = 0; k < 16; ++k)
				for(int l = 0; l < 16; ++l)
					if(j==0||j==15||k==0||k==15||l==0||l==15)
					{
						double d0 = (float)j/15.0F*2.0F-1.0F;
						double d1 = (float)k/15.0F*2.0F-1.0F;
						double d2 = (float)l/15.0F*2.0F-1.0F;
						double d3 = Math.sqrt(d0*d0+d1*d1+d2*d2);
						d0 = d0/d3;
						d1 = d1/d3;
						d2 = d2/d3;
						float f = this.size*(0.7F+ApiUtils.RANDOM.nextFloat()*0.6F);
						double d4 = getPosition().x;
						double d6 = getPosition().y;
						double d8 = getPosition().z;

						for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F)
						{
							BlockPos blockpos = BlockPos.containing(d4, d6, d8);
							BlockState iblockstate = this.world.getBlockState(blockpos);
							FluidState ifluidstate = this.world.getFluidState(blockpos);
							if(!iblockstate.isAir()||!ifluidstate.isEmpty())
							{
								float f2 = Math.max(iblockstate.getExplosionResistance(world, blockpos, this), ifluidstate.getExplosionResistance(world, blockpos, this));
								if(this.getDirectSourceEntity()!=null)
								{
									f2 = this.getDirectSourceEntity().getBlockExplosionResistance(this, this.world, blockpos, iblockstate, ifluidstate, f2);
								}

								f -= (f2+0.3F)*0.3F;
							}

							if(f > 0.0F&&(this.getDirectSourceEntity()==null||this.getDirectSourceEntity().shouldBlockExplode(this, this.world, blockpos, iblockstate, f)))
							{
								set.add(blockpos);
							}

							d4 += d0*(double)0.3F;
							d6 += d1*(double)0.3F;
							d8 += d2*(double)0.3F;
						}
					}

		this.getToBlow().addAll(set);
		this.getToBlow().sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(getPosition())));

		float f3 = this.size*2.0F;
		int k1 = Mth.floor(getPosition().x-(double)f3-1.0D);
		int l1 = Mth.floor(getPosition().x+(double)f3+1.0D);
		int i2 = Mth.floor(getPosition().y-(double)f3-1.0D);
		int i1 = Mth.floor(getPosition().y+(double)f3+1.0D);
		int j2 = Mth.floor(getPosition().z-(double)f3-1.0D);
		int j1 = Mth.floor(getPosition().z+(double)f3+1.0D);
		List<Entity> list = this.world.getEntities(this.getDirectSourceEntity(), new AABB(k1, i2, j2, l1, i1, j1));
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, list, f3);
		Vec3 vec3 = new Vec3(getPosition().x, getPosition().y, getPosition().z);

		for(int k2 = 0; k2 < list.size(); ++k2)
		{
			Entity entity = list.get(k2);
			if(!entity.ignoreExplosion())
			{
				double d12 = entity.position()
						.distanceToSqr(getPosition().x, getPosition().y, getPosition().z)/(double)f3;
				if(d12 <= 1.0D)
				{
					double d5 = entity.getX()-getPosition().x;
					double d7 = entity.getY()+(double)entity.getEyeHeight()-getPosition().y;
					double d9 = entity.getZ()-getPosition().z;
					double d13 = Mth.sqrt((float)(d5*d5+d7*d7+d9*d9));
					if(d13!=0.0D)
					{
						d5 = d5/d13;
						d7 = d7/d13;
						d9 = d9/d13;
						double d14 = getSeenPercent(vec3, entity);
						double d10 = (1.0D-d12)*d14;
						entity.hurt(entity.damageSources().explosion(this), (float)((int)((d10*d10+d10)/2.0D*8.0D*(double)f3+1.0D)));
						double d11 = entity instanceof LivingEntity?ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity)entity, d10): d10;
						entity.setDeltaMovement(entity.getDeltaMovement().add(d5*d11,
								d7*d11,
								d9*d11));
						if(entity instanceof Player&&!((Player)entity).getAbilities().invulnerable)
							this.getHitPlayers().put((Player)entity, new Vec3(d5*d10, d7*d10, d9*d10));
					}
				}
			}
		}
	}

	@Override
	public void finalizeExplosion(boolean spawnParticles)
	{
		Vec3 pos = getPosition();
		if(this.world.isClientSide)
			this.world.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 4.0F, (1.0F+(ApiUtils.RANDOM.nextFloat()-ApiUtils.RANDOM.nextFloat())*0.2F)*0.7F, true);

		if(this.size >= 2.0F&&this.damagesTerrain!=BlockInteraction.KEEP)
			this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
		else
			this.world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);

		if(!this.world.isClientSide)
			EventHandler.currentExplosions.computeIfAbsent(this.world, $ -> new HashSet<>()).add(this);
	}
}