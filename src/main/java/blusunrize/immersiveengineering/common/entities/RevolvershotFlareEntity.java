/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RevolvershotFlareEntity extends RevolvershotEntity
{
	private BlockPos lightPos;

	public RevolvershotFlareEntity(EntityType<RevolvershotFlareEntity> type, Level world)
	{
		super(type, world);
		this.setTickLimit(400);
	}

	public RevolvershotFlareEntity(Level world, double x, double y, double z, double ax, double ay, double az, Color4 color)
	{
		super(IEEntityTypes.FLARE_REVOLVERSHOT.get(), world, null, x, y, z, ax, ay, az, IEBullets.FLARE_TYPE, color);
		this.setTickLimit(400);
	}

	public RevolvershotFlareEntity(Level world, LivingEntity living, double ax, double ay, double az, Color4 color)
	{
		super(IEEntityTypes.FLARE_REVOLVERSHOT.get(), world, living, ax, ay, az, IEBullets.FLARE_TYPE, color);
		this.setTickLimit(400);
	}

	public Color4 getColour()
	{
		return getBullet().getFor(IEBullets.FLARE_TYPE);
	}

	@Override
	public void tick()
	{
		super.tick();
		if(level().isClientSide)
		{
			var color = getColour();
			level().addParticle(new DustParticleOptions(color.toVector3f(), 1), getX(), getY(), getZ(), 0, 0, 0);
			if(tickCount > 40)
				for(int i = 0; i < 20; i++)
				{
					Vec3 v = new Vec3(ApiUtils.RANDOM.nextDouble()-.5, ApiUtils.RANDOM.nextDouble()-.5, ApiUtils.RANDOM.nextDouble()-.5);
					level().addParticle(new DustParticleOptions(color.toVector3f(), 1), getX()+v.x, getY()+v.y, getZ()+v.z, v.x/10, v.y/10, v.z/10);
				}
		}
		if(tickCount==40)
		{
			setDeltaMovement(0, -.1, 0);
			spawnParticles();
			lightPos = this.blockPosition();
			for(int i = 0; i < 128; i++)
				if(level().isEmptyBlock(lightPos))
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
			if(!this.level().isClientSide)
				if(mop instanceof EntityHitResult)
				{
					Entity hit = ((EntityHitResult)mop).getEntity();
					if(!hit.fireImmune())
						hit.igniteForSeconds(8);
				}
				else if(mop instanceof BlockHitResult)
				{
					BlockHitResult blockRTR = (BlockHitResult)mop;
					BlockPos pos = blockRTR.getBlockPos().relative(blockRTR.getDirection());
					if(this.level().isEmptyBlock(pos))
						this.level().setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
				}
			spawnParticles();
		}
		this.discard();
	}

	private void spawnParticles()
	{
		for(int i = 0; i < 80; i++)
		{
			Vec3 v = new Vec3((ApiUtils.RANDOM.nextDouble()-.5)*i > 40?2: 1, (ApiUtils.RANDOM.nextDouble()-.5)*i > 40?2: 1, (ApiUtils.RANDOM.nextDouble()-.5)*i > 40?2: 1);
			level().addParticle(new DustParticleOptions(getColour().toVector3f(), 1), getX()+v.x, getY()+v.y, getZ()+v.z, v.x/10, v.y/10, v.z/10);
		}
	}
}