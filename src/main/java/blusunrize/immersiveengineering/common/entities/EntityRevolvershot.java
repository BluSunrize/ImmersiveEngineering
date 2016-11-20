package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxContainerItem;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityRevolvershot extends Entity
{
	private int xTile = -1;
	private int yTile = -1;
	private int zTile = -1;
	private Block inTile;
	private int inData;
	private boolean inGround;
	public EntityLivingBase shootingEntity;
	private int ticksInGround;
	private int ticksInAir;

	private int tickLimit=40;
	String bulletType;
	public boolean bulletElectro = false;
	public ItemStack bulletPotion = null;

	private static final DataParameter<String> dataMarker_shooter = EntityDataManager.createKey(EntityIEProjectile.class, DataSerializers.STRING);

	public EntityRevolvershot(World world)
	{
		super(world);
		this.setSize(.125f,.125f);
	}

	public EntityRevolvershot(World world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		this.bulletType = BulletHandler.findRegistryName(type);
	}

	public EntityRevolvershot(World world, EntityLivingBase living, double ax, double ay, double az, IBullet type, ItemStack stack)
	{
		this(world, living, ax, ay, az, BulletHandler.findRegistryName(type),stack);
	}
	public EntityRevolvershot(World world, EntityLivingBase living, double ax, double ay, double az, String type, ItemStack stack)
	{
		super(world);
		this.shootingEntity = living;
		setShooterSynced();
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(living.posX+ax, living.posY+living.getEyeHeight()+ay, living.posZ+az, living.rotationYaw, living.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		//		this.yOffset = 0.0F;
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.bulletType = type;
	}

	public void setTickLimit(int limit)
	{
		this.tickLimit=limit;
	}


	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1 * d1;
	}

	@Override
	protected void entityInit()
	{
		this.dataManager.register(dataMarker_shooter, "");
	}

	public void setShooterSynced()
	{
		this.dataManager.set(dataMarker_shooter, this.shootingEntity.getName());
	}
	public EntityLivingBase getShooterSynced()
	{
		return this.worldObj.getPlayerEntityByName(this.dataManager.get(dataMarker_shooter));
	}
	public Entity getShooter()
	{
		return shootingEntity;
	}

	@Override
	public void onUpdate()
	{
		if(this.getShooter() == null && this.worldObj.isRemote)
			this.shootingEntity = getShooterSynced();

		if(!this.worldObj.isRemote && (this.shootingEntity != null && this.shootingEntity.isDead))
			this.setDead();
		else
		{
			BlockPos blockpos = new BlockPos(this.xTile, this.yTile, this.zTile);
			IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
			Block block = iblockstate.getBlock();

			if(iblockstate.getMaterial() != Material.AIR)
			{
				AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(this.worldObj, blockpos);

				if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3d(this.posX, this.posY, this.posZ)))
				{
					this.inGround = true;
				}
			}

			super.onUpdate();

			if(this.inGround)
			{
				int j = block.getMetaFromState(iblockstate);

				if(block==this.inTile && j==this.inData)
				{
					++this.ticksInGround;

					if (this.ticksInGround >= 1200)
					{
						this.setDead();
					}
				}
				else
				{
					this.inGround = false;
					this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
					this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
					this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
					this.ticksInGround = 0;
					this.ticksInAir = 0;
				}
			}
			else
				++this.ticksInAir;

			if(ticksInAir>=tickLimit)
			{
				this.onExpire();
				this.setDead();
				return;
			}

			Vec3d vec3 = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d vec31 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult movingobjectposition = this.worldObj.rayTraceBlocks(vec3, vec31);
			vec3 = new Vec3d(this.posX, this.posY, this.posZ);
			vec31 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (movingobjectposition != null)
				vec31 = new Vec3d(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);

			Entity entity = null;
			List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = list.get(i);
				if (entity1.canBeCollidedWith() && (!entity1.isEntityEqual(this.shootingEntity)))
				{
					float f = 0.3F;
					AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
					RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

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
				movingobjectposition = new RayTraceResult(entity);

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
					this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)f3, this.posY - this.motionY * (double)f3, this.posZ - this.motionZ * (double)f3, this.motionX, this.motionY, this.motionZ);
				}
			}

			if(ticksExisted%4==0)
				this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	protected void onImpact(RayTraceResult mop)
	{
		boolean headshot = false;
		if(mop.entityHit instanceof EntityLivingBase)
			headshot = Utils.isVecInEntityHead((EntityLivingBase) mop.entityHit, new Vec3d(posX, posY, posZ));

		if(this.bulletType != null)
		{
			IBullet bullet = BulletHandler.getBullet(bulletType);
			if(bullet != null)
				bullet.onHitTarget(worldObj, mop, this.shootingEntity, this, headshot);
			if(headshot && mop.entityHit instanceof EntityAgeable && ((EntityAgeable)mop.entityHit).isChild())
			{
				if(this.shootingEntity instanceof EntityPlayer)
					((EntityPlayer)this.shootingEntity).addStat(IEAchievements.secret_birthdayParty);
				this.playSound(IESounds.birthdayParty, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
			}
		}
		if(!this.worldObj.isRemote)
		{
//			if(bulletType==3)
//				worldObj.createExplosion(shootingEntity, posX, posY, posZ, 2, false);
			this.secondaryImpact(mop);
		}
		this.setDead();
	}
	public void secondaryImpact(RayTraceResult mop)
	{
		if(bulletElectro && mop.entityHit instanceof EntityLivingBase)
		{
			((EntityLivingBase)mop.entityHit).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"),15,4));
			for(EntityEquipmentSlot slot : EntityEquipmentSlot.values())
			{
				ItemStack stack = ((EntityLivingBase)mop.entityHit).getItemStackFromSlot(slot);
				if(stack!=null && stack.getItem() instanceof IFluxContainerItem)
				{
					int maxStore = ((IFluxContainerItem)stack.getItem()).getMaxEnergyStored(stack);
					int drain = Math.min((int)(maxStore*.15f), ((IFluxContainerItem)stack.getItem()).getEnergyStored(stack));
					int hasDrained = 0;
					while(hasDrained<drain)
					{
						int actualDrain = ((IFluxContainerItem)stack.getItem()).extractEnergy(stack, drain, false);
						if(actualDrain<=0)
							break;
						hasDrained += actualDrain;
					}
				}
//				if(stack!=null && Lib.IC2)
//				{
//					double charge = IC2Helper.getMaxItemCharge(stack);
//					IC2Helper.dischargeItem(stack, charge*.15f);
//				}
			}
		}

//		if(bulletType==6)
//		{
//			Vec3d v = new Vec3d(-motionX, -motionY, -motionZ);
//			int split = 6;
//			for(int i=0; i<split; i++)
//			{
//				float angle = i * (360f/split);
//				Matrix4 matrix = new Matrix4();
//				matrix.rotate(angle, v.xCoord,v.yCoord,v.zCoord);
//				Vec3d vecDir = new Vec3d(0, 1, 0);
//				vecDir = matrix.apply(vecDir);
//
//				EntityWolfpackShot bullet = new EntityWolfpackShot(worldObj, this.shootingEntity, vecDir.xCoord*1.5,vecDir.yCoord*1.5,vecDir.zCoord*1.5, this.bulletType, null);
//				if(mop.entityHit instanceof EntityLivingBase)
//					bullet.targetOverride = (EntityLivingBase)mop.entityHit;
//				bullet.setPosition(posX+vecDir.xCoord, posY+vecDir.yCoord, posZ+vecDir.zCoord);
//				bullet.motionX = vecDir.xCoord*.375;
//				bullet.motionY = vecDir.yCoord*.375;
//				bullet.motionZ = vecDir.zCoord*.375;
//				worldObj.spawnEntityInWorld(bullet);
//			}
//		}
//		if(bulletType==8 && bulletPotion!=null && bulletPotion.getItem() instanceof ItemPotion)
//		{
//			PotionType potionType = PotionUtils.getPotionFromItem(bulletPotion);
//			List<PotionEffect> effects = PotionUtils.getEffectsFromStack(bulletPotion);
//			if(effects!=null)
//				if(bulletPotion.getItem() instanceof ItemLingeringPotion)
//				{
//					EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(this.worldObj, this.posX, this.posY, this.posZ);
//					entityareaeffectcloud.setOwner(shootingEntity);
//					entityareaeffectcloud.setRadius(3.0F);
//					entityareaeffectcloud.setRadiusOnUse(-0.5F);
//					entityareaeffectcloud.setWaitTime(10);
//					entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius() / (float)entityareaeffectcloud.getDuration());
//					entityareaeffectcloud.setPotion(potionType);
//					for(PotionEffect potioneffect : effects)
//						entityareaeffectcloud.addEffect(new PotionEffect(potioneffect.getPotion(), potioneffect.getDuration(), potioneffect.getAmplifier()));
//					this.worldObj.spawnEntityInWorld(entityareaeffectcloud);
//				}
//				else if(bulletPotion.getItem() instanceof ItemSplashPotion)
//				{
//					List<EntityLivingBase> livingEntities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D));
//					if(livingEntities!=null && !livingEntities.isEmpty())
//						for(EntityLivingBase living : livingEntities)
//							if(living.canBeHitWithPotion())
//							{
//								double dist = this.getDistanceSqToEntity(living);
//								if(dist<16D)
//								{
//									double dist2 = 1-Math.sqrt(dist)/4D;
//									if(living == mop.entityHit)
//										dist2 = 1D;
//									for(PotionEffect p : effects)
//										if(p.getPotion().isInstant())
//											p.getPotion().affectEntity(this, this.shootingEntity, living,  p.getAmplifier(), dist2);
//										else
//										{
//											int j = (int)(dist2*p.getDuration()+.5D);
//											if(j>20)
//												living.addPotionEffect(new PotionEffect(p.getPotion(),j, p.getAmplifier()));
//										}
//								}
//							}
//
//				}
//				else if(mop.entityHit instanceof EntityLivingBase)
//					for(PotionEffect p : effects)
//					{
//						if(p.getDuration()<1)
//							p = new PotionEffect(p.getPotion(),1);
//						((EntityLivingBase)mop.entityHit).addPotionEffect(p);
//					}
//			worldObj.playEvent(2002, new BlockPos(this), PotionType.getID(potionType));
//		}
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
		nbt.setShort("xTile", (short)this.xTile);
		nbt.setShort("yTile", (short)this.yTile);
		nbt.setShort("zTile", (short)this.zTile);
		nbt.setByte("inTile", (byte)Block.getIdFromBlock(this.inTile));
		nbt.setInteger("inData", this.inData);
		nbt.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		nbt.setTag("direction", this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
		nbt.setString("bulletType", this.bulletType);
		if(bulletPotion!=null)
			nbt.setTag("bulletPotion", bulletPotion.writeToNBT(new NBTTagCompound()));
		if(this.shootingEntity!=null)
			nbt.setString("shootingEntity", this.shootingEntity.getName());
	}

	@Override
	//	public void readFromNBT(NBTTagCompound nbt)
	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
		//		super.readFromNBT(nbt);
		this.xTile = nbt.getShort("xTile");
		this.yTile = nbt.getShort("yTile");
		this.zTile = nbt.getShort("zTile");
		this.inTile = Block.getBlockById(nbt.getByte("inTile") & 255);
		this.inData = nbt.getInteger("inData");
		this.inGround = nbt.getByte("inGround") == 1;
		this.bulletType = nbt.getString("bulletType");
		if(nbt.hasKey("bulletPotion"))
			this.bulletPotion= ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("bulletPotion"));

		if (nbt.hasKey("direction", 9))
		{
			NBTTagList nbttaglist = nbt.getTagList("direction", 6);
			this.motionX = nbttaglist.getFloatAt(0);
			this.motionY = nbttaglist.getFloatAt(1);
			this.motionZ = nbttaglist.getFloatAt(2);
		}
		else
		{
			this.setDead();
		}

		if(this.worldObj!=null)
			this.shootingEntity = this.worldObj.getPlayerEntityByName(nbt.getString("shootingEntity"));
	}

	@Override
	public float getCollisionBorderSize()
	{
		return 1.0F;
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
}