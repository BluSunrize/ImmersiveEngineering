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
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class IEExplosion extends Explosion
{
	public float dropChance = 1;
	private int blockDestroyInt = 0;
	public int blocksPerTick = 8;
	public boolean isExplosionFinished = false;

	public IEExplosion(World world, Entity igniter, double x, double y, double z, float size, boolean isFlaming, boolean isSmoking)
	{
		super(world, igniter, x, y, z, size, isFlaming, isSmoking);
		this.dropChance = 1/this.size;
	}

	public IEExplosion setDropChance(float chance)
	{
		this.dropChance = chance;
		return this;
	}

	public void doExplosionTick()
	{
		int max = Math.min(blockDestroyInt+blocksPerTick, this.affectedBlockPositions.size());
		for(; blockDestroyInt < max; blockDestroyInt++)
		{
			BlockPos pos = this.affectedBlockPositions.get(blockDestroyInt);
			IBlockState state = this.world.getBlockState(pos);
			Block block = state.getBlock();

//			if(spawnParticles)
			{
				double d0 = (double)((float)pos.getX()+Utils.RAND.nextFloat());
				double d1 = (double)((float)pos.getY()+Utils.RAND.nextFloat());
				double d2 = (double)((float)pos.getZ()+Utils.RAND.nextFloat());
				double d3 = d0-this.x;
				double d4 = d1-this.y;
				double d5 = d2-this.z;
				double d6 = (double)MathHelper.sqrt(d3*d3+d4*d4+d5*d5);
				d3 = d3/d6;
				d4 = d4/d6;
				d5 = d5/d6;
				double d7 = 0.5D/(d6/(double)this.size+0.1D);
				d7 = d7*(double)(Utils.RAND.nextFloat()*Utils.RAND.nextFloat()+0.3F);
				d3 = d3*d7;
				d4 = d4*d7;
				d5 = d5*d7;
				this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0+this.x*1.0D)/2.0D, (d1+this.y*1.0D)/2.0D, (d2+this.z*1.0D)/2.0D, d3, d4, d5);
				this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
			}

			if(state.getMaterial()!=Material.AIR)
			{
				if(block.canDropFromExplosion(this))
					block.dropBlockAsItemWithChance(this.world, pos, this.world.getBlockState(pos), dropChance, 0);
				block.onBlockExploded(this.world, pos, this);
			}
		}
		if(blockDestroyInt >= this.affectedBlockPositions.size())
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
						double d0 = (double)((float)j/15.0F*2.0F-1.0F);
						double d1 = (double)((float)k/15.0F*2.0F-1.0F);
						double d2 = (double)((float)l/15.0F*2.0F-1.0F);
						double d3 = Math.sqrt(d0*d0+d1*d1+d2*d2);
						d0 = d0/d3;
						d1 = d1/d3;
						d2 = d2/d3;
						float f = this.size*(0.7F+Utils.RAND.nextFloat()*0.6F);
						double d4 = this.x;
						double d6 = this.y;
						double d8 = this.z;

						for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F)
						{
							BlockPos blockpos = new BlockPos(d4, d6, d8);
							IBlockState iblockstate = this.world.getBlockState(blockpos);

							if(iblockstate.getMaterial()!=Material.AIR)
							{
								float f2 = this.exploder!=null?this.exploder.getExplosionResistance(this, this.world, blockpos, iblockstate): iblockstate.getBlock().getExplosionResistance(world, blockpos, null, this);
								f -= (f2+0.3F)*0.3F;
							}

							if(f > 0.0F&&(this.exploder==null||this.exploder.canExplosionDestroyBlock(this, this.world, blockpos, iblockstate, f)))
								set.add(blockpos);

							d4 += d0*0.30000001192092896D;
							d6 += d1*0.30000001192092896D;
							d8 += d2*0.30000001192092896D;
						}
					}

		this.affectedBlockPositions.addAll(set);
		Collections.sort(this.affectedBlockPositions, new Comparator<BlockPos>()
		{
			@Override
			public int compare(BlockPos arg0, BlockPos arg1)
			{
				return Double.compare(arg0.distanceSq(x, y, z), arg1.distanceSq(x, y, z));
			}
		});

		float f3 = this.size*2.0F;
		int k1 = MathHelper.floor(this.x-(double)f3-1.0D);
		int l1 = MathHelper.floor(this.x+(double)f3+1.0D);
		int i2 = MathHelper.floor(this.y-(double)f3-1.0D);
		int i1 = MathHelper.floor(this.y+(double)f3+1.0D);
		int j2 = MathHelper.floor(this.z-(double)f3-1.0D);
		int j1 = MathHelper.floor(this.z+(double)f3+1.0D);
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, list, f3);
		Vec3d vec3 = new Vec3d(this.x, this.y, this.z);

		for(int k2 = 0; k2 < list.size(); ++k2)
		{
			Entity entity = list.get(k2);
			if(!entity.isImmuneToExplosions())
			{
				double d12 = entity.getDistance(this.x, this.y, this.z)/(double)f3;
				if(d12 <= 1.0D)
				{
					double d5 = entity.posX-this.x;
					double d7 = entity.posY+(double)entity.getEyeHeight()-this.y;
					double d9 = entity.posZ-this.z;
					double d13 = (double)MathHelper.sqrt(d5*d5+d7*d7+d9*d9);
					if(d13!=0.0D)
					{
						d5 = d5/d13;
						d7 = d7/d13;
						d9 = d9/d13;
						double d14 = (double)this.world.getBlockDensity(vec3, entity.getEntityBoundingBox());
						double d10 = (1.0D-d12)*d14;
						entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((d10*d10+d10)/2.0D*8.0D*(double)f3+1.0D)));
						double d11 = entity instanceof EntityLivingBase?EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)entity, d10): d10;
						entity.motionX += d5*d11;
						entity.motionY += d7*d11;
						entity.motionZ += d9*d11;
						if(entity instanceof EntityPlayer&&!((EntityPlayer)entity).capabilities.disableDamage)
							this.playerKnockbackMap.put((EntityPlayer)entity, new Vec3d(d5*d10, d7*d10, d9*d10));
					}
				}
			}
		}
	}

	@Override
	public void doExplosionB(boolean spawnParticles)
	{
		this.world.playSound(this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.NEUTRAL, 4.0F, (1.0F+(Utils.RAND.nextFloat()-Utils.RAND.nextFloat())*0.2F)*0.7F, true);

		if(this.size >= 2.0F&&this.damagesTerrain)
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
		else
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);

		EventHandler.currentExplosions.add(this);
//		if(this.isSmoking)
//			for(BlockPos blockpos : this.affectedBlockPositions)
//			{
//				Block block = this.world.getBlockState(blockpos).getBlock();
//
//				if(spawnParticles)
//				{
//					double d0 = (double)((float)blockpos.getX() + this.Utils.RAND.nextFloat());
//					double d1 = (double)((float)blockpos.getY() + this.Utils.RAND.nextFloat());
//					double d2 = (double)((float)blockpos.getZ() + this.Utils.RAND.nextFloat());
//					double d3 = d0 - this.x;
//					double d4 = d1 - this.y;
//					double d5 = d2 - this.z;
//					double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
//					d3 = d3 / d6;
//					d4 = d4 / d6;
//					d5 = d5 / d6;
//					double d7 = 0.5D / (d6 / (double)this.size + 0.1D);
//					d7 = d7 * (double)(this.Utils.RAND.nextFloat() * this.Utils.RAND.nextFloat() + 0.3F);
//					d3 = d3 * d7;
//					d4 = d4 * d7;
//					d5 = d5 * d7;
//					this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.x * 1.0D) / 2.0D, (d1 + this.y * 1.0D) / 2.0D, (d2 + this.z * 1.0D) / 2.0D, d3, d4, d5, new int[0]);
//					this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
//				}
//
//				if(block.getMaterial() != Material.air)
//				{
//					if(block.canDropFromExplosion(this))
//						block.dropBlockAsItemWithChance(this.world, blockpos, this.world.getBlockState(blockpos), dropChance, 0);
//					block.onBlockExploded(this.world, blockpos, this);
//				}
//			}
//
//		if(this.isFlaming)
//			for(BlockPos blockpos1 : this.affectedBlockPositions)
//				if(this.world.getBlockState(blockpos1).getBlock().getMaterial() == Material.air && this.world.getBlockState(blockpos1.down()).getBlock().isFullBlock() && this.explosionRNG.nextInt(3) == 0)
//					this.world.setBlockState(blockpos1, Blocks.fire.getDefaultState());
	}
}