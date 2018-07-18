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
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class EntityRevolvershotFlare extends EntityRevolvershot implements ILightProvider
{
	boolean shootUp = false;
	public int colour = -1;
	private static final DataParameter<Integer> dataMarker_colour = EntityDataManager.createKey(EntityRevolvershotFlare.class, DataSerializers.VARINT);
	private BlockPos lightPos;

	public EntityRevolvershotFlare(World world)
	{
		super(world);
		this.setTickLimit(400);
	}

	public EntityRevolvershotFlare(World world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(world, x, y, z, ax, ay, az, type);
		this.setTickLimit(400);
	}

	public EntityRevolvershotFlare(World world, EntityLivingBase living, double ax, double ay, double az, IBullet type, ItemStack stack)
	{
		super(world, living, ax, ay, az, type, stack);
		this.setTickLimit(400);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(dataMarker_colour, Integer.valueOf(-1));
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
	public void onUpdate()
	{
		super.onUpdate();
		if(colour < 0)
			colour = getColourSynced();
		if(world.isRemote&&ticksExisted%1==0)
		{
			float r = (getColour() >> 16&255)/255f;
			float g = (getColour() >> 8&255)/255f;
			float b = (getColour()&255)/255f;
			ImmersiveEngineering.proxy.spawnRedstoneFX(world, posX, posY, posZ, 0, 0, 0, 1, r, g, b);
			if(ticksExisted > 40)
				for(int i = 0; i < 20; i++)
				{
					Vec3d v = new Vec3d(Utils.RAND.nextDouble()-.5, Utils.RAND.nextDouble()-.5, Utils.RAND.nextDouble()-.5);
					ImmersiveEngineering.proxy.spawnRedstoneFX(world, posX+v.x, posY+v.y, posZ+v.z, v.x/10, v.y/10, v.z/10, 1, r, g, b);
				}
		}
		if(ticksExisted==40)
		{
			motionX = 0;
			motionY = -.1;
			motionZ = 0;
			float r = (getColour() >> 16&255)/255f;
			float g = (getColour() >> 8&255)/255f;
			float b = (getColour()&255)/255f;
			for(int i = 0; i < 80; i++)
			{
				Vec3d v = new Vec3d((Utils.RAND.nextDouble()-.5)*i > 40?2: 1, (Utils.RAND.nextDouble()-.5)*i > 40?2: 1, (Utils.RAND.nextDouble()-.5)*i > 40?2: 1);
				ImmersiveEngineering.proxy.spawnRedstoneFX(world, posX+v.x, posY+v.y, posZ+v.z, v.x/10, v.y/10, v.z/10, 1, r, g, b);
			}

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
	protected void onImpact(RayTraceResult mop)
	{
		if(ticksExisted <= 40)
		{
			if(!this.world.isRemote)
				if(mop.entityHit!=null)
				{
					if(!mop.entityHit.isImmuneToFire())
						mop.entityHit.setFire(8);
				}
				else if(mop.getBlockPos()!=null)
				{
					BlockPos pos = mop.getBlockPos().offset(mop.sideHit);
					if(this.world.isAirBlock(pos))
						this.world.setBlockState(pos, Blocks.FIRE.getDefaultState());
				}
			float r = (getColour() >> 16&255)/255f;
			float g = (getColour() >> 8&255)/255f;
			float b = (getColour()&255)/255f;
			for(int i = 0; i < 80; i++)
			{
				Vec3d v = new Vec3d((Utils.RAND.nextDouble()-.5)*i > 40?2: 1, (Utils.RAND.nextDouble()-.5)*i > 40?2: 1, (Utils.RAND.nextDouble()-.5)*i > 40?2: 1);
				ImmersiveEngineering.proxy.spawnRedstoneFX(world, posX+v.x, posY+v.y, posZ+v.z, v.x/10, v.y/10, v.z/10, 1, r, g, b);
			}
		}
		this.setDead();
	}

	@Nullable
	@Optional.Method(modid = "albedo")
	@SideOnly(Side.CLIENT)
	@Override
	public Light provideLight()
	{
		float r = (getColour() >> 16&255)/255f;
		float g = (getColour() >> 8&255)/255f;
		float b = (getColour()&255)/255f;
		if(lightPos!=null)
			return Light.builder().pos(lightPos).radius(16).color(r, g, b).build();
		return Light.builder().pos(this).radius(1).color(r, g, b).build();
	}
}