/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class DirectionalMiningExplosion extends Explosion
{
	private static final int SIZE = 8;
	private static final int SCAN = SIZE-1;
	private static final float BLASTING_LENGTH = 80;
	private static final float SUBSURFACE_LENGTH = 175;
	private static final int THIRD_VOLUME = (int)((1.0/3)*(4.0/3)*Math.PI*SIZE*SIZE*SIZE);
	private static final float MAX_SHOCKWAVE_RESISTANCE = 0.4f;
	private static final float MAX_SURFACE_RESISTANCE = 1.75f;
	private static final float MAX_SUBSURFACE_RESISTANCE = 6f;
	private static final float MAX_BLASTING_RESISTANCE = 25f;
	private static final float BLASTING_SHAPING_RESISTANCE = 10000;
	private static final int BASE_DAMAGE = (int)(0.375f*SIZE*SIZE*SIZE);

	private final Level world;
	private final DamageSource damageSource;

	/**
	 * This explosion type is a bit special because it has a constant, tuned size to behave like a mining explosive.
	 * It is NOT INTENDED for any other use than with the gunpowder barrel.
	 * It WILL behave unpredictably with larger sizes, so user beware if they decide to customize it!
	 */
	public DirectionalMiningExplosion(Level world, Entity igniter, double x, double y, double z, boolean isFlaming)
	{
		super(world, igniter, x, y, z, SIZE, isFlaming, BlockInteraction.DESTROY);
		this.world = world;
		this.damageSource = world.damageSources().explosion(this);
	}

	/**
	 * This method is the entry method for starting the explosion, and does most of the pre-explosion calculation to assess explosion dynamics.
	 * First, a spherical area is scanned around the entity, and four properties are collected:
	 *     1. The total number of blocks in the area
	 *     2. The total blast resistances of blocks in the area
	 *     3. The sum of the vectors of all blocks in area of the form blast resistance divided by the distance from the center
	 *     4. The sum of the vectors of all blocks in area of the form one divided by the distance from the center
	 * These properties are then composed into a vector in which the explosion should propagate
	 * Finally, a subtype (surface, subsurface, blasting) of explosion is selected based on these parameters, and DirectionalMiningExplosion#stagedExplosionDetonation() is called
	 */
	@Override
	public void explode()
	{
		// variables used for the rest of the explosion
		Vec3 center = center();
		BlockPos centerBlock = new BlockPos((int)(center.x-0.5f), (int)(center.y>0?(center.y+0.5f):(center.y-0.5f)), (int)(center.z-0.5f));
		// iteration to identify the basic characteristics of the explosion
		// variables collated during the iteration
		int totalBlocks = 0;
		float totalResistance = 0;
		Vec3 weaknesses = new Vec3(0,0, 0);
		Vec3 blastWeaknesses = new Vec3(0,0, 0);
		BlockState cBlock;
		FluidState cFluid;
		double length;
		// iterate over an area of size (2*(power-1)+1)^3 and collect the resistance of blocks in a sphere around our center
		for (int x=-SCAN;x<=SCAN;x++)
			for (int y=-SCAN;y<=SCAN;y++)
				for (int z=-SCAN;z<=SCAN;z++)
				{
					BlockPos pos = centerBlock.offset(x, y, z);
					length = new Vec3(x, y, z).length();
					if (length<=SCAN)
					{
						cBlock = world.getBlockState(pos);
						cFluid = world.getFluidState(pos);
						if (!cBlock.isAir()||!cFluid.isEmpty())
						{
							totalResistance += cBlock.getExplosionResistance(world, pos, this)+cFluid.getExplosionResistance(world, pos, this);
							totalBlocks += 1;
						}
						if (cBlock.canBeReplaced()&&cFluid.isEmpty()) {
							weaknesses = weaknesses.add(x==0?0: 1.0/x, y==0?0: 1.0/y, z==0?0: 1.0/z);
							if (length<SCAN-2) blastWeaknesses = blastWeaknesses.add(x==0?0: 1.0/x, y==0?0: 1.0/y, z==0?0: 1.0/z);
						}
					}
				}
		// establish the weakest direction and the length of the explosive step we should be taking
		Vec3 step = blastWeaknesses.scale((0.5*SIZE+1-Math.sqrt(blastWeaknesses.length()/SIZE))/blastWeaknesses.length());
		// handle explosion based on criteria for explosions: either surface, subsurface, or blasting
		if(weaknesses.length()<BLASTING_LENGTH&&totalBlocks>=THIRD_VOLUME&&totalResistance<=BLASTING_SHAPING_RESISTANCE)
			stagedExplosionDetonation(centerBlock, step, 0.4f * SIZE, 0.4f * SIZE, MAX_BLASTING_RESISTANCE, true);
		else if(weaknesses.length()<SUBSURFACE_LENGTH&&totalBlocks>=THIRD_VOLUME)
			stagedExplosionDetonation(centerBlock, null, 3, SIZE*1.375f, MAX_SUBSURFACE_RESISTANCE, false);
		else
			stagedExplosionDetonation(centerBlock, null, 2, SIZE*2, MAX_SURFACE_RESISTANCE, false);
	}

	/**
	 * Actuates a directional explosion detonation using parameters calculated in DirectionalMiningExplosion#explode().
	 * Multiple spheres of blocks are broken to correspond to different parts of the explosion, including a shockwave.
	 * Blasting explosions create a 'cone' of removed material via two separate removal spheres
	 * @param center BlockPos position that the center of the explosion is at
	 * @param step Vec3 vector that the staged block break steps should move each iteration
	 * @param crater float radius that the crater block break step should break out to
	 * @param shockwave float radius of the shockwave step
	 * @param resistance float maximum blast resistance the explosion can remove blocks of
	 * @param blasting boolean whether the explosion should treat itself as a blasting explosion or a shockwave
	 */
	private void stagedExplosionDetonation(BlockPos center, Vec3 step, float crater, float shockwave, float resistance, boolean blasting)
	{
		// clear toBlow in case it has blocks still in it
		this.clearToBlow();
		// handle shockwave and crater block damage that come with any explosion
		int shock = (int)shockwave;
		for(int x = -shock; x <= shock; x++)
			for(int y = -shock; y <= shock; y++)
				for(int z = -shock; z <= shock; z++)
				{
					double length = Math.sqrt(x*x+y*y+z*z);
					if (length<crater-0.9f)
						scheduleBlockExplosion(center.offset(x, y, z), resistance, 0f);
					else if (length<crater)
						scheduleBlockExplosion(center.offset(x, y, z), resistance, 0.1f);
					else if(length<shock)
						scheduleBlockExplosion(center.offset(x, y, z), MAX_SHOCKWAVE_RESISTANCE, 0f);
				}
		// handle entity damage from shockwave
		List<Entity> damage = new ArrayList<>(world.getEntities(this.getDirectSourceEntity(),
				new AABB(center.getX()-shock, center.getY()-shock, center.getZ()-shock,
						 center.getX()+shock, center.getY()+shock, center.getZ()+shock)));
		damageEntities(damage, shockwave/SIZE);
		// handle directional explosions that come with a buried explosive barrel
		if(blasting)
		{
			// first explosion propagation sphere
			BlockPos centerOffset = center.offset((int)step.x(), (int)step.y(), (int)step.z());
			int blast1 = (int)(crater*1.25f);
			for(int x = -blast1; x <= blast1; x++)
				for(int y = -blast1; y <= blast1; y++)
					for(int z = -blast1; z <= blast1; z++)
					{
						int length = (int)Math.sqrt(x*x+y*y+z*z);
						if (length<blast1-0.9f)
							scheduleBlockExplosion(centerOffset.offset(x, y, z), resistance, 0f);
						else if (length<blast1)
							scheduleBlockExplosion(centerOffset.offset(x, y, z), resistance, 0.1f);
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
							scheduleBlockExplosion(centerOffset.offset(x, y, z), resistance, 0f);
						else if (length<blast2)
							scheduleBlockExplosion(centerOffset.offset(x, y, z), resistance, 0.1f);
					}
		}
	}

	/**
	 * This method queues up blocks to be destroyed in the super method Explosion#finalizeExplosion at a given chance & under a given resistance
	 * @param pos BlockPos position at which to remove the block
	 * @param resistance float maximum blast resistance that can be removed at this position
	 * @param chance float chance not to remove the block
	 */
	private void scheduleBlockExplosion(BlockPos pos, float resistance, float chance)
	{
		BlockState state = this.world.getBlockState(pos);
		if(!state.isAir()&&state.getExplosionResistance(world, pos, this)<=resistance&&ApiUtils.RANDOM.nextFloat()>chance)
			this.getToBlow().add(pos);
	}

	/**
	 * This code is copied and modified from the base explosion class because I don't care about fine-tuning the exact for a mining explosive
	 * It ahs been partially modified to mostly fit the necessary parameters but is not rigorously defined like the other functions in this class
	 * @param list List of entities to damage
	 * @param intensity float intensity for the intensity of the shockwave
	 */
	private void damageEntities(List<Entity> list, float intensity)
	{
		net.neoforged.neoforge.event.EventHooks.onExplosionDetonate(this.world, this, list, SIZE*2);
		for(Entity entity : list)
			if(!entity.ignoreExplosion(this)&&!(entity instanceof ItemEntity))
			{
				// relative distance
				double x = entity.getX()-center().x();
				double y = entity.getY()+entity.getBbHeight()/2-center().y();
				double z = entity.getZ()-center().z();
				float length = (float)Math.sqrt(x*x+y*y+z*z);
				if(length<intensity*SIZE) {
					double x2 = (x / length);
					double y2 = (y / length);
					double z2 = (z / length);
					// other useful variables
					float damage = ((intensity*intensity*intensity)/(length * length)) * BASE_DAMAGE;
					double knockback = Math.sqrt((entity instanceof LivingEntity living) ? ProtectionEnchantment.getExplosionKnockbackAfterDampener(living, damage) : damage);
					// actually do damage & knockback
					entity.hurt(damageSource, damage);
					entity.setDeltaMovement(entity.getDeltaMovement().add(org.joml.Math.clamp(-5f, 5f, x2*knockback), org.joml.Math.clamp(-5f, 5f, y2*knockback), org.joml.Math.clamp(-5f, 5f, z2*knockback)));
					System.out.println(entity.getDeltaMovement().length());
				}
			}
	}
}