/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.mixin.accessors.ExplosionAccess;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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

import java.util.*;

public class DirectionalMiningExplosion extends Explosion
{
	private static final int SIZE = 8;
	private static final float BLASTING_LENGTH = 525;
	private static final float SUBSURFACE_LENGTH = 700;
	private static final float SUBSURFACE_RESISTANCE = 1500;
	private static final int MIN_AIR = 3;
	private static final float MAX_SHOCKWAVE_RESISTANCE = 0.4f;
	private static final float MAX_SURFACE_RESISTANCE = 1.75f;
	private static final float MAX_SUBSURFACE_RESISTANCE = 6f;
	private static final float MAX_BLASTING_RESISTANCE = 25f;

	private final Level world;
	private final DamageSource damageSource;

	/**
	 * This explosion type is a bit special because it has a constant, tuned size to behave like a mining explosive.
	 * It is NOT INTENDED for any other use than with the gunpowder barrel.
	 * It WILL behave unpredictably with larger sizes, so user beware if they decide to customize it!
	 */
	public DirectionalMiningExplosion(Level world, Entity igniter, double x, double y, double z, boolean isFlaming)
	{
		super(world, igniter, x, y, z, SIZE, isFlaming, BlockInteraction.KEEP);
		this.world = world;
		this.damageSource = world.damageSources().explosion(this);
	}

	@Override
	public void explode()
	{
		// variables used for the rest of the explosion
		Vec3 center = center();
		BlockPos centerBlock = new BlockPos((int)(center.x)+(center.x<0?-1:0), (int)center.y, (int)(center.z)+(center.z<0?-1:0));
		// iteration to identify the basic characteristics of the explosion
		// variables collated during the iteration
		double totalResistance = 0;
		Vec3 weaknesses = new Vec3(0,0, 0);
		BlockState cBlock;
		FluidState cFluid;
		// iterate over an area of size (2*power+1)^3 and collect the resistance of blocks in a sphere around our center
		for (int x=-SIZE;x<=SIZE;x++)
			for (int y=-SIZE;y<=SIZE;y++)
				for (int z=-SIZE;z<=SIZE;z++)
				{
					BlockPos pos = centerBlock.offset(x, y, z);
					if (new Vec3(x, y, z).length()<=SIZE)
					{
						cBlock = world.getBlockState(pos);
						cFluid = world.getFluidState(pos);
						if(!cBlock.isAir()||!cFluid.isEmpty())
						{
							float resistance = cBlock.getExplosionResistance(world, pos, this)+cFluid.getExplosionResistance(world, pos, this);
							totalResistance += resistance;
							weaknesses = weaknesses.add(x==0?0:resistance/x, y==0?0:resistance/y, z==0?0:resistance/z);
						}
					}
				}
		// establish the weakest direction and the length of the explosive step we should be taking
		weaknesses = weaknesses.reverse();
		// handle explosion based on criteria for explosions: either surface, subsurface, or blasting
		int air = checkAir(centerBlock);
		if(air<MIN_AIR&&weaknesses.length()<BLASTING_LENGTH)
			stagedExplosionDetonation(centerBlock, weaknesses.scale(30*SIZE/totalResistance), 0.4f*SIZE, 0.4f*SIZE, MAX_BLASTING_RESISTANCE, true);
		else if(air<=MIN_AIR&&weaknesses.length()<SUBSURFACE_LENGTH&&totalResistance>=SUBSURFACE_RESISTANCE)
			stagedExplosionDetonation(centerBlock, null, 3, SIZE*1.25f, MAX_SUBSURFACE_RESISTANCE, false);
		else
			stagedExplosionDetonation(centerBlock, null, 2, SIZE*2, MAX_SURFACE_RESISTANCE, false);





		//Vec3 step = weaknesses.scale(30*size/totalResistance);
/*
		// find entities in range and set them into the list to damage them
		// TODO: this should scale with 1/explosion radius
		double shock = 0.75f*size;
		List<Entity> damage = new ArrayList<>(world.getEntities(this.getDirectSourceEntity(),
				new AABB(centerBlock.getX()-shock, centerBlock.getY()-shock, centerBlock.getZ()-shock,
						centerBlock.getX()+shock, centerBlock.getY()+shock, centerBlock.getZ()+shock)));
		// filter for radius, then filter out items
		damage = damage.stream().filter(e -> center.distanceTo(e.position())<=shock).filter(e -> !(e instanceof ItemEntity)).toList();
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
=			// if we have a step length greater than ~0.6 power we need to scale it down a little bit because we still have penetration
			if (step.length()>size*0.6) step = step.scale(1.75/Math.pow(step.length(), 0.7f));
			// if we have a step length smaller than ~0.225 power but aren't on a surface explosion, we should enhance it
			else if (step.length()<size*0.225) step = step.scale(1.3f);
=			// delineate blocks to break from the first "stage", ie the explosion directly around the barrel
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

		// remove blocks & damage entities
		damageEntities(damage, totalResistance);
		for (BlockPos pos : remove)
			removeExplodedBlock(pos);*/
	}

	private void stagedExplosionDetonation(BlockPos center, Vec3 step, float crater, float shockwave, float resistance, boolean blasting)
	{
		// handle shockwave and crater block damage that come with any explosion
		int shock = (int)shockwave;
		for(int x = -shock; x <= shock; x++)
			for(int y = -shock; y <= shock; y++)
				for(int z = -shock; z <= shock; z++)
				{
					double length = Math.sqrt(x*x+y*y+z*z);
					if (length<crater-0.9f)
						removeExplodedBlock(center.offset(x, y, z), resistance, 0f);
					else if (length<crater)
						removeExplodedBlock(center.offset(x, y, z), resistance, 0.1f);
					else if(length<shock)
						removeExplodedBlock(center.offset(x, y, z), MAX_SHOCKWAVE_RESISTANCE, 0f);
				}
		// handle entity damage from shockwave
		List<Entity> damage = new ArrayList<>(world.getEntities(this.getDirectSourceEntity(),
				new AABB(center.getX()-shock, center.getY()-shock, center.getZ()-shock,
						 center.getX()+shock, center.getY()+shock, center.getZ()+shock)));
		damage = damage.stream().filter(e -> center().distanceTo(e.position())<=shock).filter(e -> !(e instanceof ItemEntity)).toList();
		damageEntities(damage, shockwave/SIZE);
		// handle directional explosions that come with a buried explosive barrel
		if(blasting)
		{
			//scale weakness shatter vector into something to 'step' explosions by
			if (step.length()>SIZE*0.6)
				step = step.scale(1.75/Math.pow(step.length(), 0.7f));
			else if (step.length()<SIZE*0.225)
				step = step.scale(1.3f);
			// first explosion propagation sphere
			BlockPos centerOffset = center.offset((int)step.x(), (int)step.y(), (int)step.z());
			int blast1 = (int)(crater*1.25f);
			for(int x = -blast1; x <= blast1; x++)
				for(int y = -blast1; y <= blast1; y++)
					for(int z = -blast1; z <= blast1; z++)
					{
						int length = (int)Math.sqrt(x*x+y*y+z*z);
						if (length<blast1-0.9f)
							removeExplodedBlock(centerOffset.offset(x, y, z), resistance, 0f);
						else if (length<blast1)
							removeExplodedBlock(centerOffset.offset(x, y, z), resistance, 0.1f);
					}
			// second explosion propagation sphere
			centerOffset = center.offset((int)step.x()*2, (int)step.y()*2, (int)step.z()*2);
			int blast2 = (int)(crater*1.5f);
			for(int x = -blast2; x <= blast2; x++)
				for(int y = -blast2; y <= blast2; y++)
					for(int z = -blast2; z <= blast2; z++)
					{
						int length = (int)Math.sqrt(x*x+y*y+z*z);
						if (length<blast2-0.9f)
							removeExplodedBlock(centerOffset.offset(x, y, z), resistance, 0f);
						else if (length<blast2)
							removeExplodedBlock(centerOffset.offset(x, y, z), resistance, 0.1f);
					}
		}
	}

	private void removeExplodedBlock(BlockPos pos, float resistance, float chance)
	{
		ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
		BlockState state = this.world.getBlockState(pos);

		if(!state.isAir()&&state.getExplosionResistance(world, pos, this)<=resistance&&ApiUtils.RANDOM.nextFloat()>chance)
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

	/*
	 * This code is copied and modified from the base explosion class because I don't care about fine-tuning it for a mining explosive
	 */
	private void damageEntities(List<Entity> list, float intensity)
	{
		net.neoforged.neoforge.event.EventHooks.onExplosionDetonate(this.world, this, list, SIZE*2);
		for(Entity entity : list)
			if(!entity.ignoreExplosion(this))
			{
				// relative distance
				double x = entity.getX()-center().x();
				double y = entity.getY()+entity.getBbHeight()/2-center().y();
				double z = entity.getZ()-center().z();
				float length = (float)Math.sqrt(x*x+y*y+z*z);
				x = (x/length)*(x/length);
				y = (y/length)*(y/length);
				z = (z/length)*(z/length);
				// other useful variables
				float exposed = getSeenPercent(center(), entity);
				float damage = exposed*(float)((SIZE*SIZE*SIZE)/(4*Math.PI*length*length))*intensity;
				double knockback = (entity instanceof LivingEntity living)?ProtectionEnchantment.getExplosionKnockbackAfterDampener(living, damage): damage;
				// actually do damage & knockback
				entity.hurt(damageSource, damage);
				entity.setDeltaMovement(entity.getDeltaMovement().add(knockback/(x*x), knockback/(y*y), knockback/(z*z)));
			}
	}

	private int checkAir(BlockPos pos)
	{
		int air = 0;
		for (Direction direction : Direction.values())
			air += world.getBlockState(pos.relative(direction)).getExplosionResistance(world, pos.relative(direction), this)<MAX_SHOCKWAVE_RESISTANCE?1:0;
		return air;
	}

	@Override
	public void finalizeExplosion(boolean spawnParticles)
	{
		Vec3 pos = center();
		if(this.world.isClientSide)
			this.world.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 4.0F, (1.0F+(ApiUtils.RANDOM.nextFloat()-ApiUtils.RANDOM.nextFloat())*0.2F)*0.7F, true);
		this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
	}
}