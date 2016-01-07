package blusunrize.immersiveengineering.common.entities;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityIEProjectile extends EntityArrow//Yes I have to extend arrow or else it's all weird and broken >_>
{
	protected int blockX = -1;
	protected int blockY = -1;
	protected int blockZ = -1;
	protected Block inBlock;
	protected int inMeta;
	protected boolean inGround;
	protected int ticksInGround;
	protected int ticksInAir;

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
		this.setLocationAndAngles(living.posX, living.posY+living.getEyeHeight(), living.posZ, living.rotationYaw, living.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.motionX = ax;
		this.motionY = ay;
		this.motionZ = az;
		this.shootingEntity = living;
		this.setShooterSynced();
		//		this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		//		this.posY -= 0.10000000149011612D;
		//		this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		//		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		//		this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		//		this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		//		this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI));

		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 2*1.5F, 1.0F);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
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
	public Entity getShooter()
	{
		return shootingEntity;
	}

	@Override
	public void onUpdate()
	{
		if(this.getShooter() == null && this.worldObj.isRemote)
			this.shootingEntity = getShooterSynced();

		this.onEntityUpdate();

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
				++this.ticksInGround;
				if (this.ticksInGround>=getMaxTicksInGround())
					this.setDead();
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
		{
			++this.ticksInAir;

			if(ticksInAir>=tickLimit)
			{
				this.setDead();
				return;
			}

			Vec3 currentPos = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			Vec3 nextPos = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition movingobjectposition = this.worldObj.func_147447_a(currentPos, nextPos, false,true,false);

			currentPos = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);

			if(movingobjectposition != null)
				nextPos = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
			else
				nextPos = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if(movingobjectposition==null || movingobjectposition.entityHit==null)
			{
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
						MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(currentPos, nextPos);

						if (movingobjectposition1 != null)
						{
							double d1 = currentPos.distanceTo(movingobjectposition1.hitVec);
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
			}

			if(movingobjectposition!=null)
			{
				if(!this.isBurning() && this.canIgnite() && movingobjectposition.entityHit!=null && movingobjectposition.entityHit.isBurning())
					this.setFire(3);
				if(movingobjectposition.entityHit instanceof EntityLivingBase)
				{
					this.onImpact(movingobjectposition);
					this.setDead();
				}
				else if(movingobjectposition.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK)
				{
					this.onImpact(movingobjectposition);
					this.blockX = movingobjectposition.blockX;
					this.blockY = movingobjectposition.blockY;
					this.blockZ = movingobjectposition.blockZ;
					this.inBlock = this.worldObj.getBlock(this.blockX, this.blockY, this.blockZ);
					this.inMeta = this.worldObj.getBlockMetadata(this.blockX, this.blockY, this.blockZ);
					this.motionX = movingobjectposition.hitVec.xCoord - this.posX;
					this.motionY = movingobjectposition.hitVec.yCoord - this.posY;
					this.motionZ = movingobjectposition.hitVec.zCoord - this.posZ;
					float f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					this.posX -= this.motionX / (double)f2 * 0.05000000074505806D;
					this.posY -= this.motionY / (double)f2 * 0.05000000074505806D;
					this.posZ -= this.motionZ / (double)f2 * 0.05000000074505806D;
					//						this.posX = movingobjectposition.hitVec.xCoord;
					//						this.posY = movingobjectposition.hitVec.yCoord;
					//						this.posZ = movingobjectposition.hitVec.zCoord;
					//						this.setPosition(this.posX, this.posY, this.posZ);

					this.inGround = true;
					if(this.inBlock.getMaterial() != Material.air)
						this.inBlock.onEntityCollidedWithBlock(this.worldObj, this.blockX, this.blockY, this.blockZ, this);
					//						return;
				}
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;

			float motion = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

			for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)motion) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);
			while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
				this.prevRotationPitch += 360.0F;
			while (this.rotationYaw - this.prevRotationYaw < -180.0F)
				this.prevRotationYaw -= 360.0F;
			while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
				this.prevRotationYaw += 360.0F;
			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;


			float movementDecay = getMotionDecayFactor();

			if(this.isInWater())
			{
				for(int j = 0; j < 4; ++j)
				{
					float f3 = 0.25F;
					this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double)f3, this.posY - this.motionY * (double)f3, this.posZ - this.motionZ * (double)f3, this.motionX, this.motionY, this.motionZ);
				}
				movementDecay *= 0.8F;
			}

			this.motionX *= movementDecay;
			this.motionY *= movementDecay;
			this.motionZ *= movementDecay;
			this.motionY -= getGravity();
			this.setPosition(this.posX, this.posY, this.posZ);
			this.func_145775_I();
		}
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer p_70100_1_)
	{
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.boundingBox.getAverageEdgeLength() * 4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1 * d1;
	}

	public double getGravity()
	{
		return 0.05F;
	}

	public boolean canIgnite()
	{
		return false;
	}

	public int getMaxTicksInGround()
	{
		return 100;
	}

	public abstract void onImpact(MovingObjectPosition mop);

	protected float getMotionDecayFactor()
	{
		return 0.99F;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setShort("xTile", (short)this.blockX);
		nbt.setShort("yTile", (short)this.blockY);
		nbt.setShort("zTile", (short)this.blockZ);
		nbt.setByte("inTile", (byte)Block.getIdFromBlock(this.inBlock));
		nbt.setByte("inMeta", (byte)this.inMeta);
		nbt.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		if(this.shootingEntity!=null)
			nbt.setString("shootingEntity", this.shootingEntity.getCommandSenderName());

	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.blockX = nbt.getShort("xTile");
		this.blockY = nbt.getShort("yTile");
		this.blockZ = nbt.getShort("zTile");
		this.inBlock = Block.getBlockById(nbt.getByte("inTile") & 255);
		this.inGround = nbt.getByte("inGround") == 1;
		if(this.worldObj!=null)
			this.shootingEntity = this.worldObj.getPlayerEntityByName(nbt.getString("shootingEntity"));
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		return false;
	}
}