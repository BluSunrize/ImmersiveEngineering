package blusunrize.immersiveengineering.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntitySkylineHook extends Entity
{
	Connection connection;
	ChunkCoordinates target;
	Vec3[] subPoints;
	int targetPoint=0;
	public EntitySkylineHook(World world)
	{
		super(world);
		this.setSize(.125f,.125f);
//		this.noClip=true;
	}
	public EntitySkylineHook(World world, double x, double y, double z, Connection connection, ChunkCoordinates target, Vec3[] subPoints)
	{
		super(world);
//		this.noClip=true;
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		this.connection = connection;
		this.target = target;
		this.subPoints = subPoints;

		float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

		for (this.rotationPitch = (float)(Math.atan2((double)f1, this.motionY) * 180.0D / Math.PI) - 90.0F; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			this.prevRotationPitch += 360.0F;
		while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			this.prevRotationYaw -= 360.0F;
		while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			this.prevRotationYaw += 360.0F;
	}
	protected void entityInit() {}


	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.boundingBox.getAverageEdgeLength() * 4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1 * d1;
	}


	@Override
	public void onUpdate()
	{
		if(this.ticksExisted==1&&!worldObj.isRemote)
			IELogger.debug("init tick at "+System.currentTimeMillis());
		super.onUpdate();
		//		if(this.ticksExisted>40)
		//			this.setDead();
		//		if(worldObj.isRemote)
		//			return;

		EntityPlayer player = null;
		if(this.riddenByEntity instanceof EntityPlayer)
			player = ((EntityPlayer)this.riddenByEntity);

		if(subPoints!=null && targetPoint<subPoints.length-1)
		{
			double dist = subPoints[targetPoint].distanceTo(Vec3.createVectorHelper(posX,posY,posZ));
			IELogger.debug("dist: "+dist);
			if(dist<=.05)
			{
				this.posX = subPoints[targetPoint].xCoord;
				this.posY = subPoints[targetPoint].yCoord;
				this.posZ = subPoints[targetPoint].zCoord;
				targetPoint++;
				IELogger.debug("next vertex: "+targetPoint);
				//				double dx = (subPoints[targetPoint].xCoord-posX);//connection.length;
				//				double dy = (subPoints[targetPoint].yCoord-posY);//connection.length;
				//				double dz = (subPoints[targetPoint].zCoord-posZ);//connection.length;
				//				Vec3 moveVec = Vec3.createVectorHelper(dx,dy,dz);
				float speed = 2f;
				if(player!=null && player.getCurrentEquippedItem()!=null&&player.getCurrentEquippedItem().getItem() instanceof ItemSkyhook)
					speed = ((ItemSkyhook)player.getCurrentEquippedItem().getItem()).getSkylineSpeed(player.getCurrentEquippedItem());
				Vec3 moveVec = SkylineHelper.getSubMovementVector(Vec3.createVectorHelper(posX, posY, posZ), subPoints[targetPoint], speed);
				motionX = moveVec.xCoord;//*speed;
				motionY = moveVec.yCoord;//*speed;
				motionZ = moveVec.zCoord;//*speed;
				return;
			}
		}

		if(target!=null&&targetPoint==subPoints.length-1)
		{
			TileEntity end = this.worldObj.getTileEntity(target.posX, target.posY, target.posZ);
			IImmersiveConnectable iicEnd = Utils.toIIC(end, worldObj);
			if(iicEnd==null)
			{
				this.setDead();
				return;
			}
			Vec3 vEnd = Vec3.createVectorHelper(target.posX, target.posY, target.posZ);
			vEnd = Utils.addVectors(vEnd, iicEnd.getConnectionOffset(connection));


			double gDist = vEnd.distanceTo(Vec3.createVectorHelper(posX, posY, posZ));
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
		float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
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
				this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double)f3, this.posY - this.motionY * (double)f3, this.posZ - this.motionZ * (double)f3, this.motionX, this.motionY, this.motionZ);
			}
		}

		if(player!=null)
		{
			double dx = this.posX-this.prevPosX;
			double dy = this.posY-this.prevPosY;
			double dz = this.posZ-this.prevPosZ;
			int distTrvl = Math.round(MathHelper.sqrt_double(dx*dx + dy*dy + dz*dz) * 100.0F);
			if(distTrvl>0)
				player.addStat(IEAchievements.statDistanceSkyhook, distTrvl);
			if(player instanceof EntityPlayerMP)
				if(((EntityPlayerMP)player).func_147099_x().writeStat(IEAchievements.statDistanceSkyhook)>100000)
					player.triggerAchievement(IEAchievements.skyhookPro);
		}

		this.setPosition(this.posX, this.posY, this.posZ);
	}

	public void reachedTarget(TileEntity end)
	{
		this.setDead();
		IELogger.debug("last tick at "+System.currentTimeMillis());
		if(!(this.riddenByEntity instanceof EntityPlayer))
			return;
		ItemStack hook = ((EntityPlayer)this.riddenByEntity).getCurrentEquippedItem();
		if(hook==null || !(hook.getItem() instanceof ItemSkyhook))
			return;
		Connection line = SkylineHelper.getTargetConnection(worldObj, target.posX,target.posY,target.posZ, (EntityLivingBase)this.riddenByEntity, connection);

		if(line!=null)
		{
			((EntityPlayer)this.riddenByEntity).setItemInUse(hook, hook.getItem().getMaxItemUseDuration(hook));
			SkylineHelper.spawnHook((EntityPlayer)this.riddenByEntity, end, line);
			//					ChunkCoordinates cc0 = line.end==target?line.start:line.end;
			//					ChunkCoordinates cc1 = line.end==target?line.end:line.start;
			//					double dx = cc0.posX-cc1.posX;
			//					double dy = cc0.posY-cc1.posY;
			//					double dz = cc0.posZ-cc1.posZ;
			//
			//					EntityZiplineHook zip = new EntityZiplineHook(worldObj, target.posX+.5,target.posY+.5,target.posZ+.5, line, cc0);
			//					zip.motionX = dx*.05f;
			//					zip.motionY = dy*.05f;
			//					zip.motionZ = dz*.05f;
			//					if(!worldObj.isRemote)
			//						worldObj.spawnEntityInWorld(zip);
			//					ItemSkyHook.existingHooks.put(this.riddenByEntity.getCommandSenderName(), zip);
			//					this.riddenByEntity.mountEntity(zip);
		}
		else
		{
			((EntityPlayer)this.riddenByEntity).motionX = motionX;
			((EntityPlayer)this.riddenByEntity).motionY = motionY;
			((EntityPlayer)this.riddenByEntity).motionZ = motionZ;
			IELogger.debug("player motion: "+((EntityPlayer)this.riddenByEntity).motionX+","+((EntityPlayer)this.riddenByEntity).motionY+","+((EntityPlayer)this.riddenByEntity).motionZ);
		}
	}

	@Override
	public Vec3 getLookVec()
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
		return Vec3.createVectorHelper((double)(f2 * f3), (double)f4, (double)(f1 * f3));
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
	@SideOnly(Side.CLIENT)
	@Override
	public float getShadowSize()
	{
		return 0.0F;
	}
	@Override
	public float getBrightness(float p_70013_1_)
	{
		return 1.0F;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public int getBrightnessForRender(float p_70070_1_)
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
	//	@Override
	//	protected void readEntityFromNBT(NBTTagCompound p_70037_1_) {
	//		// TODO Auto-generated method stub
	//		
	//	}
	//	@Override
	//	protected void writeEntityToNBT(NBTTagCompound p_70014_1_) {
	//		// TODO Auto-generated method stub
	//		
	//	}
}