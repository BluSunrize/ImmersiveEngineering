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
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.level.block.Blocks;
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

public class DirectionalMiningExplosion extends Explosion
{
	private final Level world;
	private final float size;
	private final Set<BlockPos> remove = new HashSet<>();

	public DirectionalMiningExplosion(Level world, Entity igniter, double x, double y, double z, float size, boolean isFlaming)
	{
		super(world, igniter, x, y, z, size, isFlaming, BlockInteraction.KEEP);
		this.world = world;
		this.size = size;
	}
/*
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
						double d4 = center().x;
						double d6 = center().y;
						double d8 = center().z;

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
		Vec3 center = center();
		this.getToBlow().sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(center)));

		float f3 = this.size*2.0F;
		int k1 = Mth.floor(center.x-(double)f3-1.0D);
		int l1 = Mth.floor(center.x+(double)f3+1.0D);
		int i2 = Mth.floor(center.y-(double)f3-1.0D);
		int i1 = Mth.floor(center.y+(double)f3+1.0D);
		int j2 = Mth.floor(center.z-(double)f3-1.0D);
		int j1 = Mth.floor(center.z+(double)f3+1.0D);
		List<Entity> list = this.world.getEntities(this.getDirectSourceEntity(), new AABB(k1, i2, j2, l1, i1, j1));
		net.neoforged.neoforge.event.EventHooks.onExplosionDetonate(this.world, this, list, f3);
		Vec3 vec3 = new Vec3(center.x, center.y, center.z);

		for(int k2 = 0; k2 < list.size(); ++k2)
		{
			Entity entity = list.get(k2);
			if(!entity.ignoreExplosion(this))
			{
				double d12 = entity.position()
						.distanceToSqr(center.x, center.y, center.z)/(double)f3;
				if(d12 <= 1.0D)
				{
					double d5 = entity.getX()-center.x;
					double d7 = entity.getY()+(double)entity.getEyeHeight()-center.y;
					double d9 = entity.getZ()-center.z;
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
	}*/

	@Override
	public void explode()
	{
		// variables used for the rest of the explosion
		int power = (int)size;
		Vec3 center = center();
		BlockPos centerBlock = new BlockPos((int)(center.x)+(center.x<0?-1:0), (int)center.y, (int)(center.z)+(center.z<0?-1:0));

		// variables collated during the iteration
		Set<BlockPos> area = new HashSet<>();
		double totalResistance = 0;
		Vec3 weakestDirection = new Vec3(0,0, 0);
		// to be used and reused in the iteration
		BlockState cBlock;
		FluidState cFluid;
		// iterate over an area of size (2*power+1)^3 and collect the resistance of blocks in a sphere around our center
		for (int x=-power;x<=power;x++)
			for (int y=-power;y<=power;y++)
				for (int z=-power;z<=power;z++)
				{
					BlockPos pos = centerBlock.offset(x, y, z);
					area.add(pos);
					if (new Vec3(x, y, z).length()<=power)
					{
						cBlock = world.getBlockState(pos);
						cFluid = world.getFluidState(pos);
						if(!cBlock.isAir()||!cFluid.isEmpty())
						{
							double resistance = cBlock.getExplosionResistance(world, pos, this)+cFluid.getExplosionResistance(world, pos, this);
							totalResistance += resistance;
							weakestDirection = weakestDirection.add(x==0?0:resistance/x, y==0?0:resistance/y, z==0?0:resistance/z);
						}
					}
				}

		// establish the weakest direction and the length of the explosive step we should be taking
		weakestDirection = weakestDirection.reverse();
		Vec3 step = weakestDirection.scale(30*size/totalResistance);

		// offset center to be used in other calculations
		BlockPos centerOffset;
		// if step length is high we have a "surface burst" and the explosion dynamics should be different
		if (step.length()>size*5)
		{
			// offset explosion by fixed distance proportional to size
			step = step.normalize().scale(size/4);
			centerOffset = centerBlock.offset((int)step.x, (int)step.y, (int)step.z);
			int r = (int)(0.6f*power);
			for(int x = -r; x <= r; x++)
				for(int y = -r; y <= r; y++)
					for(int z = -r; z <= r; z++)
						addToRemoveRandom(centerOffset.offset(x, y, z), r-1, r, (new Vec3(x, y, z)).length(), 0.1f);
		}
		// otherwise proceed with embedded explosion dynamics
		else
		{
			// if we have a step length greater than ~0.6 power we need to scale it down a little bit because we still have penetration
			if (step.length()>size*0.6) step = step.scale(1.75/Math.pow(step.length(), 0.7f));
			// if we have a step length smaller than ~0.225 power but aren't on a surface explosion, we should enhance it
			else if (step.length()<size*0.225) step = step.scale(1.3f);
			// delineate blocks to break from the first "stage", ie the explosion directly around the barrel
			int r1 = (int)(0.4f*power);
			for(int x = -r1; x <= r1; x++)
				for(int y = -r1; y <= r1; y++)
					for(int z = -r1; z <= r1; z++)
						addToRemoveRandom(centerBlock.offset(x, y, z), r1-1, r1, (new Vec3(x, y, z)).length(), 0.05f);
			// run the second and third stages, 'biased' explosions that are larger and make this a directional charge
			centerOffset = centerBlock.offset((int)step.x, (int)step.y, (int)step.z);
			int r2 = (int)(0.5f*power);
			for(int x = -r2; x <= r2; x++)
				for(int y = -r2; y <= r2; y++)
					for(int z = -r2; z <= r2; z++)
						addToRemoveRandom(centerOffset.offset(x, y, z), r2-1, r2, (new Vec3(x, y, z)).length(), 0.075f);
			centerOffset = centerBlock.offset((int)step.x*2, (int)step.y*2, (int)step.z*2);
			int r3 = (int)(0.6f*power);
			for(int x = -r3; x <= r3; x++)
				for(int y = -r3; y <= r3; y++)
					for(int z = -r3; z <= r3; z++)
						addToRemoveRandom(centerOffset.offset(x, y, z), r3-1, r3, (new Vec3(x, y, z)).length(), 0.1f);
		}

		// remove blocks
		for (BlockPos pos : remove)
			removeExplodedBlock(pos);
	}

	private void addToRemoveRandom(BlockPos pos, int inner, int outer, double radius, float chance)
	{
		if(radius<=inner+0.1f) remove.add(pos);
		if(outer>radius&&radius>inner&&ApiUtils.RANDOM.nextFloat()>chance) remove.add(pos);
	}

	private void removeExplodedBlock(BlockPos pos)
	{
		ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
		BlockState state = this.world.getBlockState(pos);

		if(!state.isAir())
		{
			if(this.world instanceof ServerLevel&&state.canDropFromExplosion(this.world, pos, this))
			{
				BlockEntity tile = this.world.getBlockEntity(pos);
				LootParams.Builder lootCtx = new LootParams.Builder((ServerLevel)this.world)
						.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
						.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
						.withOptionalParameter(LootContextParams.BLOCK_ENTITY, tile);
				state.getDrops(lootCtx).forEach((stack) -> {
					ExplosionAccess.callAddOrAppendStack(objectarraylist, stack, pos);
				});
				state.onBlockExploded(world, pos, this);
			}
		}
		for(Pair<ItemStack, BlockPos> pair : objectarraylist)
			Block.popResource(this.world, pair.getSecond(), pair.getFirst());
	}

	@Override
	public void finalizeExplosion(boolean spawnParticles)
	{
		Vec3 pos = center();
		if(this.world.isClientSide)
			this.world.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 4.0F, (1.0F+(ApiUtils.RANDOM.nextFloat()-ApiUtils.RANDOM.nextFloat())*0.2F)*0.7F, true);

		if(this.size >= 2.0F)
			this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
		else
			this.world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
	}
}