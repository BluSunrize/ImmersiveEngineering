/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.common.EventHandler;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class IEExplosion extends Explosion
{
	public float dropChance = 1;
	private int blockDestroyInt = 0;
	public int blocksPerTick = 8;
	public boolean isExplosionFinished = false;
	private final World world;
	private final float size;
	private final Mode damagesTerrain;

	public IEExplosion(World world, Entity igniter, double x, double y, double z, float size, boolean isFlaming, Mode damageTerrain)
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
		int max = Math.min(blockDestroyInt+blocksPerTick, this.getAffectedBlockPositions().size());
		for(; blockDestroyInt < max; blockDestroyInt++)
		{
			BlockPos pos = this.getAffectedBlockPositions().get(blockDestroyInt);
			BlockState state = this.world.getBlockState(pos);
			Block block = state.getBlock();

//			if(spawnParticles)
			{
				double d0 = (float)pos.getX()+Utils.RAND.nextFloat();
				double d1 = (float)pos.getY()+Utils.RAND.nextFloat();
				double d2 = (float)pos.getZ()+Utils.RAND.nextFloat();
				double d3 = d0-getPosition().x;
				double d4 = d1-getPosition().y;
				double d5 = d2-getPosition().z;
				double d6 = MathHelper.sqrt(d3*d3+d4*d4+d5*d5);
				d3 = d3/d6;
				d4 = d4/d6;
				d5 = d5/d6;
				double d7 = 0.5D/(d6/(double)this.size+0.1D);
				d7 = d7*(double)(Utils.RAND.nextFloat()*Utils.RAND.nextFloat()+0.3F);
				d3 = d3*d7;
				d4 = d4*d7;
				d5 = d5*d7;
				this.world.addParticle(ParticleTypes.EXPLOSION, (d0+getPosition().x*1.0D)/2.0D, (d1+getPosition().y*1.0D)/2.0D, (d2+getPosition().z*1.0D)/2.0D, d3, d4, d5);
				this.world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
			}

			if(!state.isAir(world, pos))
			{
				if(this.world instanceof ServerWorld&&state.canDropFromExplosion(this.world, pos, this))
				{
					TileEntity tile = state.hasTileEntity()?this.world.getTileEntity(pos): null;
					LootContext.Builder lootCtx = new LootContext.Builder((ServerWorld)this.world)
							.withRandom(this.world.rand)
							.withParameter(LootParameters.field_237457_g_, Vector3d.copyCentered(pos))
							.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
							.withNullableParameter(LootParameters.BLOCK_ENTITY, tile);
					if(damagesTerrain==Explosion.Mode.DESTROY)
						lootCtx.withParameter(LootParameters.EXPLOSION_RADIUS, this.size);
					state.getDrops(lootCtx).forEach((p_229977_2_) -> {
						handleExplosionDrops(objectarraylist, p_229977_2_, pos);
					});
					state.onBlockExploded(world, pos, this);
				}
			}
		}
		for(Pair<ItemStack, BlockPos> pair : objectarraylist)
		{
			Block.spawnAsEntity(this.world, pair.getSecond(), pair.getFirst());
		}
		if(blockDestroyInt >= this.getAffectedBlockPositions().size())
			this.isExplosionFinished = true;
	}

	@Override
	public void doExplosionA()
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
						float f = this.size*(0.7F+Utils.RAND.nextFloat()*0.6F);
						double d4 = getPosition().x;
						double d6 = getPosition().y;
						double d8 = getPosition().z;

						for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F)
						{
							BlockPos blockpos = new BlockPos(d4, d6, d8);
							BlockState iblockstate = this.world.getBlockState(blockpos);
							FluidState ifluidstate = this.world.getFluidState(blockpos);
							if(!iblockstate.isAir(world, blockpos)||!ifluidstate.isEmpty())
							{
								float f2 = Math.max(iblockstate.getExplosionResistance(world, blockpos, this), ifluidstate.getExplosionResistance(world, blockpos, this));
								if(this.getExplosivePlacedBy()!=null)
								{
									f2 = this.getExplosivePlacedBy().getExplosionResistance(this, this.world, blockpos, iblockstate, ifluidstate, f2);
								}

								f -= (f2+0.3F)*0.3F;
							}

							if(f > 0.0F&&(this.getExplosivePlacedBy()==null||this.getExplosivePlacedBy().canExplosionDestroyBlock(this, this.world, blockpos, iblockstate, f)))
							{
								set.add(blockpos);
							}

							d4 += d0*(double)0.3F;
							d6 += d1*(double)0.3F;
							d8 += d2*(double)0.3F;
						}
					}

		this.getAffectedBlockPositions().addAll(set);
		this.getAffectedBlockPositions().sort(
				Comparator.comparingDouble(pos -> pos.distanceSq(getPosition().x, getPosition().y, getPosition().z, true))
		);

		float f3 = this.size*2.0F;
		int k1 = MathHelper.floor(getPosition().x-(double)f3-1.0D);
		int l1 = MathHelper.floor(getPosition().x+(double)f3+1.0D);
		int i2 = MathHelper.floor(getPosition().y-(double)f3-1.0D);
		int i1 = MathHelper.floor(getPosition().y+(double)f3+1.0D);
		int j2 = MathHelper.floor(getPosition().z-(double)f3-1.0D);
		int j1 = MathHelper.floor(getPosition().z+(double)f3+1.0D);
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.getExplosivePlacedBy(), new AxisAlignedBB(k1, i2, j2, l1, i1, j1));
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, list, f3);
		Vector3d vec3 = new Vector3d(getPosition().x, getPosition().y, getPosition().z);

		for(int k2 = 0; k2 < list.size(); ++k2)
		{
			Entity entity = list.get(k2);
			if(!entity.isImmuneToExplosions())
			{
				double d12 = entity.getPositionVec()
						.squareDistanceTo(getPosition().x, getPosition().y, getPosition().z)/(double)f3;
				if(d12 <= 1.0D)
				{
					double d5 = entity.getPosX()-getPosition().x;
					double d7 = entity.getPosY()+(double)entity.getEyeHeight()-getPosition().y;
					double d9 = entity.getPosZ()-getPosition().z;
					double d13 = MathHelper.sqrt(d5*d5+d7*d7+d9*d9);
					if(d13!=0.0D)
					{
						d5 = d5/d13;
						d7 = d7/d13;
						d9 = d9/d13;
						double d14 = getBlockDensity(vec3, entity);
						double d10 = (1.0D-d12)*d14;
						entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((d10*d10+d10)/2.0D*8.0D*(double)f3+1.0D)));
						double d11 = entity instanceof LivingEntity?ProtectionEnchantment.getBlastDamageReduction((LivingEntity)entity, d10): d10;
						entity.setMotion(entity.getMotion().add(d5*d11,
								d7*d11,
								d9*d11));
						if(entity instanceof PlayerEntity&&!((PlayerEntity)entity).abilities.disableDamage)
							this.getPlayerKnockbackMap().put((PlayerEntity)entity, new Vector3d(d5*d10, d7*d10, d9*d10));
					}
				}
			}
		}
	}

	@Override
	public void doExplosionB(boolean spawnParticles)
	{
		Vector3d pos = getPosition();
		this.world.playSound(pos.x, pos.y, pos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.NEUTRAL, 4.0F, (1.0F+(Utils.RAND.nextFloat()-Utils.RAND.nextFloat())*0.2F)*0.7F, true);

		if(this.size >= 2.0F&&this.damagesTerrain!=Mode.NONE)
			this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
		else
			this.world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);

		EventHandler.currentExplosions.add(this);
	}
}