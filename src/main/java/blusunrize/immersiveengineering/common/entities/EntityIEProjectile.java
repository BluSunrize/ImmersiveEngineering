package blusunrize.immersiveengineering.common.entities;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class EntityIEProjectile extends Entity
{
	private int blockX = -1;
	private int blockY = -1;
	private int blockZ = -1;
	private Block inBlock;
	private int inMeta;
	private boolean inGround;
	public EntityLivingBase shootingEntity;
	private int ticksAlive;
	private int ticksInAir;

	private int tickLimit=40;
	final static int dataMarker_shooter = 12;

	public EntityIEProjectile(World world)
	{
		super(world);
		this.renderDistanceWeight=10;
		this.setSize(.125f,.125f);
	}
	public EntityIEProjectile(World world, double x, double y, double z, double ax, double ay, double az)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
	}
	public EntityIEProjectile(World world, EntityLivingBase living, double ax, double ay, double az)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(living.posX+ax, living.posY+living.getEyeHeight()+ay, living.posZ+az, living.rotationYaw, living.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.shootingEntity = living;
		this.setShooterSynced();
	}
	@Override
	protected void entityInit()
	{
		this.dataWatcher.addObject(dataMarker_shooter, "");
	}

	public void setTickLimit(int limit)
	{
		this.tickLimit=limit;
	}

	public void setShooterSynced()
	{
		this.dataWatcher.updateObject(dataMarker_shooter, this.shootingEntity.getCommandSenderName());
	}
	public EntityLivingBase getShooterSynced()
	{
		return this.worldObj.getPlayerEntityByName(this.dataWatcher.getWatchableObjectString(dataMarker_shooter));
	}
	public EntityLivingBase getShooter()
	{
		return shootingEntity;
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
	public void onEntityUpdate()
	{
		//		if(!this.worldObj.isRemote && (this.shootingEntity != null && this.shootingEntity.isDead || !this.worldObj.blockExists((int)this.posX, (int)this.posY, (int)this.posZ)))
		//			this.setDead();
		//		else
		{
			if(this.getShooter() == null && this.worldObj.isRemote)
				this.shootingEntity = getShooterSynced();

			super.onEntityUpdate();

			Block block = this.worldObj.getBlock(this.blockX, this.blockY, this.blockZ);
			if(block.getMaterial() != Material.air)
			{
				block.setBlockBoundsBasedOnState(this.worldObj, this.blockX, this.blockY, this.blockZ);
				AxisAlignedBB aabb = block.getCollisionBoundingBoxFromPool(this.worldObj, this.blockX, this.blockY, this.blockZ);
				if(aabb != null && aabb.isVecInside(Vec3.createVectorHelper(this.posX, this.posY, this.posZ)))
					this.inGround = true;
			}

			if(this.inGround)
			{
				if(this.worldObj.getBlock(this.blockX, this.blockY, this.blockZ)==this.inBlock && this.worldObj.getBlockMetadata(this.blockX, this.blockY, this.blockZ)==this.inMeta)
				{
					++this.ticksAlive;
					if (this.ticksAlive>=100)
						this.setDead();
				}
				else
				{
					this.inGround = false;
					this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
					this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
					this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
					this.ticksAlive = 0;
					this.ticksInAir = 0;
				}
			}
			else
			{
				++this.ticksInAir;

				if(ticksInAir>=tickLimit)
				{
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
					if(entity1.canBeCollidedWith() && (!entity1.isEntityEqual(this.shootingEntity) || this.ticksInAir>5))
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

				if(entity!=null)
					movingobjectposition = new MovingObjectPosition(entity);

				if(movingobjectposition!=null)
				{
					if(movingobjectposition.entityHit!=null && movingobjectposition.entityHit instanceof EntityLivingBase)
					{
						this.onImpact(movingobjectposition);
						this.setDead();
					}
					else if(movingobjectposition.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK)
					{
						this.blockX = movingobjectposition.blockX;
						this.blockY = movingobjectposition.blockY;
						this.blockZ = movingobjectposition.blockZ;
						this.inBlock = this.worldObj.getBlock(this.blockX, this.blockY, this.blockZ);
						this.inMeta = this.worldObj.getBlockMetadata(this.blockX, this.blockY, this.blockZ);
						this.motionX = (double)((float)(movingobjectposition.hitVec.xCoord - this.posX));
						this.motionY = (double)((float)(movingobjectposition.hitVec.yCoord - this.posY));
						this.motionZ = (double)((float)(movingobjectposition.hitVec.zCoord - this.posZ));
						float f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
						this.posX -= this.motionX / (double)f2 * 0.05000000074505806D;
						this.posY -= this.motionY / (double)f2 * 0.05000000074505806D;
						this.posZ -= this.motionZ / (double)f2 * 0.05000000074505806D;
						this.posX = movingobjectposition.hitVec.xCoord;
						this.posY = movingobjectposition.hitVec.yCoord;
						this.posZ = movingobjectposition.hitVec.zCoord;
						this.setPosition(this.posX, this.posY, this.posZ);

						this.inGround = true;
						if(this.inBlock.getMaterial() != Material.air)
							this.inBlock.onEntityCollidedWithBlock(this.worldObj, this.blockX, this.blockY, this.blockZ, this);
						return;
					}
				}

				this.posX += this.motionX;
				this.posY += this.motionY;
				this.posZ += this.motionZ;
				float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
				this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

				for(this.rotationPitch = (float)(Math.atan2((double)f1, this.motionY) * 180.0D / Math.PI) - 90.0F; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);

				while(this.rotationPitch - this.prevRotationPitch >= 180.0F)
					this.prevRotationPitch += 360.0F;
				while(this.rotationYaw - this.prevRotationYaw < -180.0F)
					this.prevRotationYaw -= 360.0F;
				while(this.rotationYaw - this.prevRotationYaw >= 180.0F)
					this.prevRotationYaw += 360.0F;

				this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
				this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

				float movementDecay = 0.99F;

				if(this.isInWater())
				{
					for(int j = 0; j < 4; ++j)
					{
						float f3 = 0.25F;
						this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double)f3, this.posY - this.motionY * (double)f3, this.posZ - this.motionZ * (double)f3, this.motionX, this.motionY, this.motionZ);
					}
					movementDecay = 0.8F;
				}

				this.motionX *= (double)movementDecay;
				this.motionY *= (double)movementDecay;
				this.motionZ *= (double)movementDecay;
				this.motionY -= getGravity();
				this.setPosition(this.posX, this.posY, this.posZ);
			}
		}
	}

	public double getGravity()
	{
		return 0.05F;
//		gravity*(getFluid().getDensity()<0?-1:1)
	}
	
	public abstract void onImpact(MovingObjectPosition mop);

	protected float getMotionFactor()
	{
		return 0.95F;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt)
	{
		nbt.setShort("xTile", (short)this.blockX);
		nbt.setShort("yTile", (short)this.blockY);
		nbt.setShort("zTile", (short)this.blockZ);
		nbt.setByte("inTile", (byte)Block.getIdFromBlock(this.inBlock));
		nbt.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		nbt.setTag("direction", this.newDoubleNBTList(new double[] {this.motionX, this.motionY, this.motionZ}));
		if(this.shootingEntity!=null)
			nbt.setString("shootingEntity", this.shootingEntity.getCommandSenderName());

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
		this.blockX = nbt.getShort("xTile");
		this.blockY = nbt.getShort("yTile");
		this.blockZ = nbt.getShort("zTile");
		this.inBlock = Block.getBlockById(nbt.getByte("inTile") & 255);
		this.inGround = nbt.getByte("inGround") == 1;
		if(this.worldObj!=null)
			this.shootingEntity = this.worldObj.getPlayerEntityByName(nbt.getString("shootingEntity"));

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
}