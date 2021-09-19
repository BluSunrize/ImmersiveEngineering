/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RevolvershotFlareEntity extends RevolvershotEntity
{
	public static final EntityType<RevolvershotFlareEntity> TYPE = Builder
			.<RevolvershotFlareEntity>of(RevolvershotFlareEntity::new, MobCategory.MISC)
			.sized(0.125f, 0.125f)
			.build(ImmersiveEngineering.MODID+":revolver_shot_flare");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "revolver_shot_flare");
	}

	public int colour = -1;
	private static final EntityDataAccessor<Integer> dataMarker_colour = SynchedEntityData.defineId(RevolvershotFlareEntity.class, EntityDataSerializers.INT);
	private BlockPos lightPos;

	public RevolvershotFlareEntity(EntityType<RevolvershotFlareEntity> type, Level world)
	{
		super(type, world);
		this.setTickLimit(400);
	}

	public RevolvershotFlareEntity(Level world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(TYPE, world, null, x, y, z, ax, ay, az, type);
		this.setTickLimit(400);
	}

	public RevolvershotFlareEntity(Level world, LivingEntity living, double ax, double ay, double az, IBullet type, ItemStack stack)
	{
		super(TYPE, world, living, ax, ay, az, type);
		this.setTickLimit(400);
	}

	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(dataMarker_colour, -1);
	}

	public void setColourSynced()
	{
		this.entityData.set(dataMarker_colour, colour);
	}

	public int getColourSynced()
	{
		return this.entityData.get(dataMarker_colour);
	}

	public int getColour()
	{
		return colour;
	}

	@Override
	public void tick()
	{
		super.tick();
		if(colour < 0)
			colour = getColourSynced();
		if(level.isClientSide)
		{
			float r = (getColour() >> 16&255)/255f;
			float g = (getColour() >> 8&255)/255f;
			float b = (getColour()&255)/255f;
			ImmersiveEngineering.proxy.spawnRedstoneFX(level, getX(), getY(), getZ(), 0, 0, 0, 1, r, g, b);
			if(tickCount > 40)
				for(int i = 0; i < 20; i++)
				{
					Vec3 v = new Vec3(Utils.RAND.nextDouble()-.5, Utils.RAND.nextDouble()-.5, Utils.RAND.nextDouble()-.5);
					ImmersiveEngineering.proxy.spawnRedstoneFX(level, getX()+v.x, getY()+v.y, getZ()+v.z, v.x/10, v.y/10, v.z/10, 1, r, g, b);
				}
		}
		if(tickCount==40)
		{
			setDeltaMovement(0, -.1, 0);
			spawnParticles();
			lightPos = this.blockPosition();
			for(int i = 0; i < 128; i++)
				if(level.isEmptyBlock(lightPos))
					lightPos = lightPos.below();
				else
				{
					lightPos = lightPos.above(6);
					break;
				}
		}
	}

	@Override
	public void onHit(HitResult mop)
	{
		if(tickCount <= 40)
		{
			if(!this.level.isClientSide)
				if(mop instanceof EntityHitResult)
				{
					Entity hit = ((EntityHitResult)mop).getEntity();
					if(!hit.fireImmune())
						hit.setSecondsOnFire(8);
				}
				else if(mop instanceof BlockHitResult)
				{
					BlockHitResult blockRTR = (BlockHitResult)mop;
					BlockPos pos = blockRTR.getBlockPos().relative(blockRTR.getDirection());
					if(this.level.isEmptyBlock(pos))
						this.level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
				}
			spawnParticles();
		}
		this.remove();
	}

	private void spawnParticles()
	{
		float r = (getColour() >> 16&255)/255f;
		float g = (getColour() >> 8&255)/255f;
		float b = (getColour()&255)/255f;
		for(int i = 0; i < 80; i++)
		{
			Vec3 v = new Vec3((Utils.RAND.nextDouble()-.5)*i > 40?2: 1, (Utils.RAND.nextDouble()-.5)*i > 40?2: 1, (Utils.RAND.nextDouble()-.5)*i > 40?2: 1);
			ImmersiveEngineering.proxy.spawnRedstoneFX(level, getX()+v.x, getY()+v.y, getZ()+v.z, v.x/10, v.y/10, v.z/10, 1, r, g, b);
		}
	}
}