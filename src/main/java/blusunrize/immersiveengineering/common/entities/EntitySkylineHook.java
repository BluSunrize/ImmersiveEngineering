/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageSkyhookSync;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class EntitySkylineHook extends Entity
{
	public Connection connection;
	public BlockPos target;
	public Vec3d[] subPoints;
	public int targetPoint;
	public EntitySkylineHook(World world)
	{
		super(world);
		this.setSize(.125f,.125f);
		//		this.noClip=true;
	}
	public EntitySkylineHook(World world, double x, double y, double z, Connection connection, BlockPos target,
							 Vec3d[] subPoints, int next)
	{
		super(world);
		targetPoint = next;
		//		this.noClip=true;
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		this.connection = connection;
		this.target = target;
		this.subPoints = subPoints;

		float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

		for (this.rotationPitch = (float)(Math.atan2((double)f1, this.motionY) * 180.0D / Math.PI) - 90.0F; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			this.prevRotationPitch += 360.0F;
		while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			this.prevRotationYaw -= 360.0F;
		while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			this.prevRotationYaw += 360.0F;
	}
	@Override
	protected void entityInit() {}


	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1 * d1;
	}


	@Override
	public void onUpdate()
	{
		EntityPlayer player = null;
//		if(this.getControllingPassenger() instanceof EntityPlayer)
//			player = ((EntityPlayer)this.getControllingPassenger());
		List<Entity> list = this.getPassengers();
		if(!list.isEmpty() && list.get(0) instanceof EntityPlayer)
			player = (EntityPlayer)list.get(0);

		if(this.ticksExisted==1&&!world.isRemote)
		{
			IELogger.debug("init tick at "+System.currentTimeMillis());
			if(player instanceof EntityPlayerMP)
				ImmersiveEngineering.packetHandler.sendTo(new MessageSkyhookSync(this), (EntityPlayerMP)player);
		}
		super.onUpdate();
		//		if(this.ticksExisted>40)
		//			this.setDead();
		//		if(world.isRemote)
		//			return;
		if(subPoints!=null && targetPoint<subPoints.length-1)
		{
			double dist = subPoints[targetPoint].distanceTo(new Vec3d(posX,posY,posZ));
			IELogger.debug("dist: "+dist);
			if(dist<=0)
			{
				this.posX = subPoints[targetPoint].x;
				this.posY = subPoints[targetPoint].y;
				this.posZ = subPoints[targetPoint].z;
				targetPoint++;
				if (player instanceof EntityPlayerMP)
					ImmersiveEngineering.packetHandler.sendTo(new MessageSkyhookSync(this), (EntityPlayerMP)player);
				IELogger.debug("next vertex: "+targetPoint);
				return;
			}
			float speed = 2f;
			if(player!=null && !player.getActiveItemStack().isEmpty()&&player.getActiveItemStack().getItem() instanceof ItemSkyhook)
				speed = ((ItemSkyhook)player.getActiveItemStack().getItem()).getSkylineSpeed(player.getActiveItemStack());
			Vec3d moveVec = SkylineHelper.getSubMovementVector(new Vec3d(posX, posY, posZ), subPoints[targetPoint], speed);
			motionX = moveVec.x;//*speed;
			motionY = moveVec.y;//*speed;
			motionZ = moveVec.z;//*speed;
		}

		if(target!=null&&targetPoint==subPoints.length-1)
		{
			TileEntity end = this.world.getTileEntity(target);
			IImmersiveConnectable iicEnd = ApiUtils.toIIC(end, world);
			if(iicEnd==null)
			{
				this.setDead();
				return;
			}
			Vec3d vEnd = new Vec3d(target.getX(), target.getY(), target.getZ());
			vEnd = Utils.addVectors(vEnd, iicEnd.getConnectionOffset(connection));


			double gDist = vEnd.distanceTo(new Vec3d(posX, posY, posZ));
			IELogger.debug("distance to goal: "+gDist);
			if(gDist<=.3)
			{
				reachedTarget(end);
				return;
			}
			else if(gDist>5)
			{
				setDead();
				return;
			}
		}
		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

		for (this.rotationPitch = (float)(Math.atan2((double)f1, this.motionY) * 180.0D / Math.PI) - 90.0F; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			this.prevRotationPitch += 360.0F;
		while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			this.prevRotationYaw -= 360.0F;
		while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			this.prevRotationYaw += 360.0F;

		this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
		this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

		if (this.isInWater())
		{
			for (int j = 0; j < 4; ++j)
			{
				float f3 = 0.25F;
				this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)f3, this.posY - this.motionY * (double)f3, this.posZ - this.motionZ * (double)f3, this.motionX, this.motionY, this.motionZ);
			}
		}

		if(player!=null)
		{
			double dx = this.posX-this.prevPosX;
			double dy = this.posY-this.prevPosY;
			double dz = this.posZ-this.prevPosZ;
			int distTrvl = Math.round(MathHelper.sqrt(dx*dx + dy*dy + dz*dz) * 100.0F);
//			if(distTrvl>0)
//				player.addStat(IEAchievements.statDistanceSkyhook, distTrvl);
			if (!world.isRemote&&SkylineHelper.isInBlock(player, world))
			{
//				setDead();
//				player.setPosition(posX-3*dx, posY-3*dy+getMountedYOffset(),posZ-3*dz);
			}

			//TODO
//			if(player instanceof EntityPlayerMP)
//				if(((EntityPlayerMP)player).getStatFile().func_150870_b(IEAchievements.statDistanceSkyhook)>100000)
//					player.triggerAchievement(IEAchievements.skyhookPro);
		}

		this.setPosition(this.posX, this.posY, this.posZ);
	}

	public void reachedTarget(TileEntity end)
	{
		this.setDead();
		IELogger.debug("last tick at "+System.currentTimeMillis());
		List<Entity> list = this.getPassengers();
		if(list.isEmpty() || !(list.get(0) instanceof EntityPlayer))
			return;
//		if(!(this.getControllingPassenger() instanceof EntityPlayer))
//			return;
//		EntityPlayer player = (EntityPlayer)this.getControllingPassenger();
		EntityPlayer player = (EntityPlayer)list.get(0);
		ItemStack hook = player.getActiveItemStack();
		if(hook.isEmpty() || !(hook.getItem() instanceof ItemSkyhook))
			return;
		Connection line = SkylineHelper.getTargetConnection(world, player, connection);
		if(line!=null)
		{
			player.setActiveHand(player.getActiveHand());
//					setItemInUse(hook, hook.getItem().getMaxItemUseDuration(hook));
			SkylineHelper.spawnHook(player, end, line);
			//					ChunkCoordinates cc0 = line.end==target?line.start:line.end;
			//					ChunkCoordinates cc1 = line.end==target?line.end:line.start;
			//					double dx = cc0.posX-cc1.posX;
			//					double dy = cc0.posY-cc1.posY;
			//					double dz = cc0.posZ-cc1.posZ;
			//
			//					EntityZiplineHook zip = new EntityZiplineHook(world, target.posX+.5,target.posY+.5,target.posZ+.5, line, cc0);
			//					zip.motionX = dx*.05f;
			//					zip.motionY = dy*.05f;
			//					zip.motionZ = dz*.05f;
			//					if(!world.isRemote)
			//						world.spawnEntity(zip);
			//					ItemSkyHook.existingHooks.put(this.riddenByEntity.getCommandSenderName(), zip);
			//					this.riddenByEntity.mountEntity(zip);
		}
		else
		{
			player.motionX = motionX;
			player.motionY = motionY;
			player.motionZ = motionZ;
			IELogger.debug("player motion: "+player.motionX+","+player.motionY+","+player.motionZ);
		}
	}

	@Override
	public Vec3d getLookVec()
	{
		float f1;
		float f2;
		float f3;
		float f4;

		//		if (1 == 1.0F)
		//		{
		f1 = MathHelper.cos(-this.rotationYaw * 0.017453292F - (float)Math.PI);
		f2 = MathHelper.sin(-this.rotationYaw * 0.017453292F - (float)Math.PI);
		f3 = -MathHelper.cos(-this.rotationPitch * 0.017453292F);
		f4 = MathHelper.sin(-this.rotationPitch * 0.017453292F);
		return new Vec3d((double)(f2 * f3), (double)f4, (double)(f1 * f3));
		//		}
		//		else
		//		{
		//			f1 = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 1;
		//			f2 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 1;
		//			f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
		//			f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
		//			float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		//			float f6 = MathHelper.sin(-f1 * 0.017453292F);
		//			return Vec3.createVectorHelper((double)(f4 * f5), (double)f6, (double)(f3 * f5));
		//		}
	}

	@Override
	@Nullable
	public Entity getControllingPassenger()
	{
		return null;
//		List<Entity> list = this.getPassengers();
//		return list.isEmpty() ? null : (Entity)list.get(0);
	}
	@Override
	public boolean shouldRiderSit()
	{
		return false;
	}

	@Override
	public boolean isInvisible()
	{
		return true;	
	}
	@Override
	public boolean canRenderOnFire()
	{
		return false;	
	}
	@Override
	public boolean isPushedByWater()
	{
		return false;
	}

	@Override
	public double getMountedYOffset()
	{
		return -2;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt)
	{
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
	}

	@Override
	public float getCollisionBorderSize()
	{
		return 0.0F;
	}
	@Override
	public float getBrightness()
	{
		return 1.0F;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public int getBrightnessForRender()
	{
		return 15728880;
	}
	@Override
	public boolean canBeCollidedWith()
	{
		return false;
	}
	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		this.setDead();
		return true;
		//		return false;
	}
}