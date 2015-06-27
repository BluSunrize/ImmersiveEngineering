package blusunrize.immersiveengineering.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityZiplineHook extends Entity
{
	ChunkCoordinates target;
	public EntityZiplineHook(World world)
	{
		super(world);
		this.setSize(.125f,.125f);
				this.noClip=true;
	}
	public EntityZiplineHook(World world, double x, double y, double z, ChunkCoordinates target)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		this.target = target;
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
		super.onUpdate();
		
		if(this.ticksExisted>40)
			this.setDead();
//
		if(this.riddenByEntity==null)
			this.setDead();
		if(target!=null)
		{
			TileEntity goal = this.worldObj.getTileEntity(target.posX, target.posY, target.posZ);
			if(goal!=null && goal.getDistanceFrom(posX, posY, posZ)<.5)
			{
				System.out.println("arrivign at Target");
				this.setDead();
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

		this.setPosition(this.posX, this.posY, this.posZ);
		//
		//		if(ticksInAir>=tickLimit)
		//		{
		//			this.onExpire();
		//			this.setDead();
		//			return;
		//		}

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
		return 1.0F;
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
		return false;
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