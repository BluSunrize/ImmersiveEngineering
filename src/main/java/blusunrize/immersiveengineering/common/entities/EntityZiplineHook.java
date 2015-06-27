package blusunrize.immersiveengineering.common.entities;

import java.util.List;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.compat.IC2Helper;
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityZiplineHook extends Entity
{
	private int ticksAlive;
	private int ticksInAir;

	//	private int tickLimit=40;

	public EntityZiplineHook(World world)
	{
		super(world);
		this.setSize(.125f,.125f);
	}
	public EntityZiplineHook(World world, double x, double y, double z, double ax, double ay, double az, int type)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
	}
	public EntityZiplineHook(World world, EntityLivingBase living, double ax, double ay, double az, int type, ItemStack stack)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(living.posX+ax, living.posY+living.getEyeHeight()+ay, living.posZ+az, living.rotationYaw, living.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = this.motionY = this.motionZ = 0.0D;
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
		if(this.riddenByEntity==null)
			this.setDead();
		super.onUpdate();

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
        return -1;
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