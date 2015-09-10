package blusunrize.immersiveengineering.common.entities;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemPotion;
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
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.compat.EtFuturumHelper;
import blusunrize.immersiveengineering.common.util.compat.IC2Helper;
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityRevolvershot extends Entity
{
	private int field_145795_e = -1;
	private int field_145793_f = -1;
	private int field_145794_g = -1;
	private Block field_145796_h;
	private boolean inGround;
	public EntityLivingBase shootingEntity;
	private int ticksAlive;
	private int ticksInAir;

	private int tickLimit=40;
	int bulletType = 0;
	public boolean bulletElectro = false;
	public ItemStack bulletPotion = null;

	public EntityRevolvershot(World world)
	{
		super(world);
		this.renderDistanceWeight=10;
		this.setSize(.125f,.125f);
	}
	public EntityRevolvershot(World world, double x, double y, double z, double ax, double ay, double az, int type)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		this.bulletType = type;
	}
	public EntityRevolvershot(World world, EntityLivingBase living, double ax, double ay, double az, int type, ItemStack stack)
	{
		super(world);
		this.shootingEntity = living;
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(living.posX+ax, living.posY+living.getEyeHeight()+ay, living.posZ+az, living.rotationYaw, living.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.bulletType = type;
	}
	protected void entityInit() {}

	public void setTickLimit(int limit)
	{
		this.tickLimit=limit;
	}


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
		if(!this.worldObj.isRemote && (this.shootingEntity != null && this.shootingEntity.isDead || !this.worldObj.blockExists((int)this.posX, (int)this.posY, (int)this.posZ)))
			this.setDead();
		else
		{
			super.onUpdate();

			if (this.inGround)
			{
				if (this.worldObj.getBlock(this.field_145795_e, this.field_145793_f, this.field_145794_g) == this.field_145796_h)
				{
					++this.ticksAlive;
					if (this.ticksAlive == 600)
						this.setDead();

					return;
				}

				this.inGround = false;
				this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
				this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
				this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
				this.ticksAlive = 0;
				this.ticksInAir = 0;
			}
			else
				++this.ticksInAir;

			if(ticksInAir>=tickLimit)
			{
				this.onExpire();
				this.setDead();
				return;
			}

			Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			Vec3 vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec3, vec31);
			vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (movingobjectposition != null)
				vec31 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);

			Entity entity = null;
			List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = (Entity)list.get(i);
				if (entity1.canBeCollidedWith() && (!entity1.isEntityEqual(this.shootingEntity)))
				{
					float f = 0.3F;
					AxisAlignedBB axisalignedbb = entity1.boundingBox.expand((double)f, (double)f, (double)f);
					MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

					if (movingobjectposition1 != null)
					{
						double d1 = vec3.distanceTo(movingobjectposition1.hitVec);
						if (d1 < d0 || d0 == 0.0D)
						{
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null)
				movingobjectposition = new MovingObjectPosition(entity);

			if (movingobjectposition != null)
				this.onImpact(movingobjectposition);

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

			if(ticksExisted%4==0)
				this.worldObj.spawnParticle("smoke", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	protected void onImpact(MovingObjectPosition mop)
	{
		if(!this.worldObj.isRemote)
		{
			if(mop.entityHit != null)
			{
				switch(bulletType)
				{
				case 0:
					mop.entityHit.attackEntityFrom(IEDamageSources.causeCasullDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-Casull"));
					break;
				case 1:
					mop.entityHit.attackEntityFrom(IEDamageSources.causePiercingDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-AP"));
					break;
				case 2:
					mop.entityHit.attackEntityFrom(IEDamageSources.causeBuckshotDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-Buck"));
					mop.entityHit.hurtResistantTime=0;
					break;
				case 4:
					if(mop.entityHit.attackEntityFrom(IEDamageSources.causeDragonsbreathDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-Dragon")))
						mop.entityHit.setFire(3);
				case 5:
					mop.entityHit.attackEntityFrom(IEDamageSources.causeHomingDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-Homing"));
					break;
				case 6:
					mop.entityHit.attackEntityFrom(IEDamageSources.causeWolfpackDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-Wolfpack"));
					break;
				case 7:
					mop.entityHit.attackEntityFrom(IEDamageSources.causeSilverDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-Silver"));
					break;
				case 8:
					mop.entityHit.attackEntityFrom(IEDamageSources.causePotionDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-Potion"));
					break;
				}
			}
			if(bulletType==3)
				worldObj.createExplosion(shootingEntity, posX, posY, posZ, 2, false);

			this.secondaryImpact(mop);
		}
		this.setDead();
	}
	public void secondaryImpact(MovingObjectPosition mop)
	{
		if(bulletElectro && mop.entityHit instanceof EntityLivingBase)
		{
			((EntityLivingBase)mop.entityHit).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id,15,4));
			for(int i=0; i<=4; i++)
			{
				ItemStack stack = ((EntityLivingBase)mop.entityHit).getEquipmentInSlot(i);
				if(stack!=null && stack.getItem() instanceof IEnergyContainerItem)
				{
					int drain = (int)(((IEnergyContainerItem)stack.getItem()).getMaxEnergyStored(stack)*.15f);
					((IEnergyContainerItem)stack.getItem()).extractEnergy(stack, drain, false);
				}
				if(stack!=null && Lib.IC2)
				{
					double charge = IC2Helper.getMaxItemCharge(stack);
					IC2Helper.dischargeItem(stack, charge*.15f);
				}
			}
		}

		if(bulletType==6)
		{
			Vec3 v = Vec3.createVectorHelper(-motionX, -motionY, -motionZ);
			for(int i=0; i<6; i++)
			{
				//				float angleV = this.rand.nextFloat()*360f;
				//			double my = Math.sin(angleV);
				//			double md = Math.cos(angleV);
				double d = Math.sqrt(motionX*motionX + motionZ*motionZ + motionY*motionY);
				double modX = -motionZ/d;
				double modZ = -motionX/d;

				Vec3 vecDir = v.addVector((rand.nextDouble()-.5)*modX, (rand.nextDouble()-.5), (rand.nextDouble()-.5)*modZ).normalize();
				EntityWolfpackShot bullet = new EntityWolfpackShot(worldObj, this.shootingEntity, vecDir.xCoord*1.5,vecDir.yCoord*1.5,vecDir.zCoord*1.5, this.bulletType, null);
				bullet.setPosition(posX+vecDir.xCoord, posY+vecDir.yCoord, posZ+vecDir.zCoord);
				bullet.motionX = vecDir.xCoord*.375;
				bullet.motionY = vecDir.yCoord*.375;
				bullet.motionZ = vecDir.zCoord*.375;
				worldObj.spawnEntityInWorld(bullet);
			}
		}
		if(bulletType==8 && bulletPotion!=null && bulletPotion.getItem() instanceof ItemPotion)
		{
			List<PotionEffect> effects = ((ItemPotion)bulletPotion.getItem()).getEffects(bulletPotion);

			if(bulletPotion.getItem().getClass().getName().equalsIgnoreCase("ganymedes01.etfuturum.items.LingeringPotion"))
				EtFuturumHelper.createLingeringPotionEffect(worldObj, posX, posY, posZ, bulletPotion, shootingEntity);
			else if(ItemPotion.isSplash(bulletPotion.getItemDamage()))
			{
				List<EntityLivingBase> livingEntities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(4.0D, 2.0D, 4.0D));
				if(livingEntities!=null && !livingEntities.isEmpty())
					for(EntityLivingBase living : livingEntities)
					{
						double dist = this.getDistanceSqToEntity(living);
						if(dist<16D)
						{
							double dist2 = 1-Math.sqrt(dist)/4D;
							if(living == mop.entityHit)
								dist2 = 1D;
							for(PotionEffect p : effects)
							{
								int id = p.getPotionID();
								if(Potion.potionTypes[id].isInstant())
									Potion.potionTypes[id].affectEntity(this.shootingEntity, living, p.getAmplifier(), dist2);
								else
								{
									int j = (int)(dist2*p.getDuration()+.5D);
									if(j>20)
										living.addPotionEffect(new PotionEffect(id, j, p.getAmplifier()));
								}
							}
						}
					}

			}
			else if(mop.entityHit!=null && mop.entityHit instanceof EntityLivingBase)
				for(PotionEffect p : effects)
					((EntityLivingBase)mop.entityHit).addPotionEffect(p);
			worldObj.playAuxSFX(2002, (int) Math.round(posX), (int) Math.round(posY), (int) Math.round(posZ), bulletPotion.getItemDamage());
		}
	}
	public void onExpire()
	{

	}

	protected float getMotionFactor()
	{
		return 0.95F;
	}

	@Override
	//	public void writeToNBT(NBTTagCompound nbt)
	protected void writeEntityToNBT(NBTTagCompound nbt)
	{
		//		super.writeToNBT(nbt);
		nbt.setShort("xTile", (short)this.field_145795_e);
		nbt.setShort("yTile", (short)this.field_145793_f);
		nbt.setShort("zTile", (short)this.field_145794_g);
		nbt.setByte("inTile", (byte)Block.getIdFromBlock(this.field_145796_h));
		nbt.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		nbt.setTag("direction", this.newDoubleNBTList(new double[] {this.motionX, this.motionY, this.motionZ}));
		nbt.setShort("bulletType", (short)this.bulletType);
		if(bulletPotion!=null)
			nbt.setTag("bulletPotion", bulletPotion.writeToNBT(new NBTTagCompound()));
	}

	@Override
	//	public void readFromNBT(NBTTagCompound nbt)
	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
		//		super.readFromNBT(nbt);
		this.field_145795_e = nbt.getShort("xTile");
		this.field_145793_f = nbt.getShort("yTile");
		this.field_145794_g = nbt.getShort("zTile");
		this.field_145796_h = Block.getBlockById(nbt.getByte("inTile") & 255);
		this.inGround = nbt.getByte("inGround") == 1;
		this.bulletType= nbt.getShort("bulletType");
		if(nbt.hasKey("bulletPotion"))
			this.bulletPotion= ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("bulletPotion"));

		if (nbt.hasKey("direction", 9))
		{
			NBTTagList nbttaglist = nbt.getTagList("direction", 6);
			this.motionX = nbttaglist.func_150309_d(0);
			this.motionY = nbttaglist.func_150309_d(1);
			this.motionZ = nbttaglist.func_150309_d(2);
		}
		else
		{
			this.setDead();
		}
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