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
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class RevolvershotFlareEntity extends RevolvershotEntity
{
	public int colour = -1;
	private static final DataParameter<Integer> dataMarker_colour = EntityDataManager.createKey(RevolvershotFlareEntity.class, DataSerializers.VARINT);
	private BlockPos lightPos;

	public RevolvershotFlareEntity(EntityType<RevolvershotFlareEntity> type, World world)
	{
		super(type, world);
		this.setTickLimit(400);
	}

	public RevolvershotFlareEntity(World world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(IEEntityTypes.FLARE_REVOLVERSHOT.get(), world, null, x, y, z, ax, ay, az, type);
		this.setTickLimit(400);
	}

	public RevolvershotFlareEntity(World world, LivingEntity living, double ax, double ay, double az, IBullet type, ItemStack stack)
	{
		super(IEEntityTypes.FLARE_REVOLVERSHOT.get(), world, living, ax, ay, az, type);
		this.setTickLimit(400);
	}

	@Override
	protected void registerData()
	{
		super.registerData();
		this.dataManager.register(dataMarker_colour, -1);
	}

	public void setColourSynced()
	{
		this.dataManager.set(dataMarker_colour, colour);
	}

	public int getColourSynced()
	{
		return this.dataManager.get(dataMarker_colour);
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
		if(world.isRemote)
		{
			float r = (getColour() >> 16&255)/255f;
			float g = (getColour() >> 8&255)/255f;
			float b = (getColour()&255)/255f;
			ImmersiveEngineering.proxy.spawnRedstoneFX(world, getPosX(), getPosY(), getPosZ(), 0, 0, 0, 1, r, g, b);
			if(ticksExisted > 40)
				for(int i = 0; i < 20; i++)
				{
					Vector3d v = new Vector3d(Utils.RAND.nextDouble()-.5, Utils.RAND.nextDouble()-.5, Utils.RAND.nextDouble()-.5);
					ImmersiveEngineering.proxy.spawnRedstoneFX(world, getPosX()+v.x, getPosY()+v.y, getPosZ()+v.z, v.x/10, v.y/10, v.z/10, 1, r, g, b);
				}
		}
		if(ticksExisted==40)
		{
			setMotion(0, -.1, 0);
			spawnParticles();
			lightPos = this.getPosition();
			for(int i = 0; i < 128; i++)
				if(world.isAirBlock(lightPos))
					lightPos = lightPos.down();
				else
				{
					lightPos = lightPos.up(6);
					break;
				}
		}
	}

	@Override
	public void onImpact(RayTraceResult mop)
	{
		if(ticksExisted <= 40)
		{
			if(!this.world.isRemote)
				if(mop instanceof EntityRayTraceResult)
				{
					Entity hit = ((EntityRayTraceResult)mop).getEntity();
					if(!hit.isImmuneToFire())
						hit.setFire(8);
				}
				else if(mop instanceof BlockRayTraceResult)
				{
					BlockRayTraceResult blockRTR = (BlockRayTraceResult)mop;
					BlockPos pos = blockRTR.getPos().offset(blockRTR.getFace());
					if(this.world.isAirBlock(pos))
						this.world.setBlockState(pos, Blocks.FIRE.getDefaultState());
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
			Vector3d v = new Vector3d((Utils.RAND.nextDouble()-.5)*i > 40?2: 1, (Utils.RAND.nextDouble()-.5)*i > 40?2: 1, (Utils.RAND.nextDouble()-.5)*i > 40?2: 1);
			ImmersiveEngineering.proxy.spawnRedstoneFX(world, getPosX()+v.x, getPosY()+v.y, getPosZ()+v.z, v.x/10, v.y/10, v.z/10, 1, r, g, b);
		}
	}
}