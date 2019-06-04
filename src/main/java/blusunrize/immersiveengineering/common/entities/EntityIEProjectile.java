/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Particles;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class EntityIEProjectile extends EntityArrow//Yes I have to extend arrow or else it's all weird and broken >_>
{
	protected BlockPos stuckIn = null;
	protected IBlockState inBlockState;
	public boolean inGround;
	public int ticksInGround;
	public int ticksInAir;

	private int tickLimit = 40;

	public EntityIEProjectile(EntityType<? extends EntityIEProjectile> type, World world)
	{
		super(type, world);
		this.setSize(.125f, .125f);
		this.pickupStatus = PickupStatus.DISALLOWED;
	}

	public EntityIEProjectile(EntityType<? extends EntityIEProjectile> type, World world, double x, double y, double z, double ax, double ay, double az)
	{
		this(type, world);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
	}

	public EntityIEProjectile(EntityType<? extends EntityIEProjectile> type, World world, EntityLivingBase living, double ax, double ay, double az)
	{
		this(type, world);
		this.setLocationAndAngles(living.posX, living.posY+living.getEyeHeight(), living.posZ, living.rotationYaw, living.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.motionX = ax;
		this.motionY = ay;
		this.motionZ = az;
		this.shootingEntity = living.getUniqueID();
		this.setShooterSynced();
		this.shoot(this.motionX, this.motionY, this.motionZ, 2*1.5F, 1.0F);
	}

	@Override
	protected void registerData()
	{
		super.registerData();
		this.dataManager.register(field_212362_a, Optional.empty());
	}


	public void setTickLimit(int limit)
	{
		this.tickLimit = limit;
	}

	public void setShooterSynced()
	{
		this.dataManager.set(field_212362_a, Optional.ofNullable(this.shootingEntity));
	}

	public UUID getShooterSynced()
	{
		Optional<UUID> s = this.dataManager.get(field_212362_a);
		return s.orElse(null);
	}

	public UUID getShooter()
	{
		return shootingEntity;
	}

	@Nonnull
	@Override
	protected ItemStack getArrowStack()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void tick()
	{
		if(this.getShooter()==null&&this.world.isRemote)
			this.shootingEntity = getShooterSynced();

		this.baseTick();

		IBlockState localState = this.world.getBlockState(stuckIn);

		if(localState.getMaterial()!=Material.AIR)
		{
			VoxelShape shape = localState.getCollisionShape(this.world, stuckIn);
			for(AxisAlignedBB subbox : shape.toBoundingBoxList())
				if(subbox.contains(this.posX, this.posY, this.posZ))
				{
					inGround = true;
					break;
				}
		}

		if(this.inGround)
		{
			if(localState==inBlockState)
			{
				++this.ticksInGround;
				if(this.ticksInGround >= getMaxTicksInGround())
					this.remove();
			}
			else
			{
				this.inGround = false;
				this.motionX *= (double)(this.rand.nextFloat()*0.2F);
				this.motionY *= (double)(this.rand.nextFloat()*0.2F);
				this.motionZ *= (double)(this.rand.nextFloat()*0.2F);
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}
		}
		else
		{
			++this.ticksInAir;

			if(ticksInAir >= tickLimit)
			{
				this.remove();
				return;
			}

			Vec3d currentPos = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d nextPos = new Vec3d(this.posX+this.motionX, this.posY+this.motionY, this.posZ+this.motionZ);
			RayTraceResult mop = this.world.rayTraceBlocks(currentPos, nextPos, RayTraceFluidMode.NEVER,
					true, false);

			currentPos = new Vec3d(this.posX, this.posY, this.posZ);

			if(mop!=null)
				nextPos = new Vec3d(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
			else
				nextPos = new Vec3d(this.posX+this.motionX, this.posY+this.motionY, this.posZ+this.motionZ);

			if(mop==null||mop.entity==null)
			{
				Entity entity = null;
				List list = this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1), Entity::canBeCollidedWith);
				double d0 = 0.0D;
				for(int i = 0; i < list.size(); ++i)
				{
					Entity entity1 = (Entity)list.get(i);
					if(entity1.canBeCollidedWith()&&(!entity1.getUniqueID().equals(this.shootingEntity)||this.ticksInAir > 5))
					{
						float f = 0.3F;
						AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow((double)f, (double)f, (double)f);
						RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(currentPos, nextPos);

						if(movingobjectposition1!=null)
						{
							double d1 = currentPos.distanceTo(movingobjectposition1.hitVec);
							if(d1 < d0||d0==0.0D)
							{
								entity = entity1;
								d0 = d1;
							}
						}
					}
				}
				if(entity!=null)
					mop = new RayTraceResult(entity);
			}

			if(mop!=null)
			{
				if(!this.isBurning()&&this.canIgnite()&&mop.entity!=null&&mop.entity.isBurning())
					this.setFire(3);
				if(mop.entity!=null)
				{
					boolean allowHit = true;
					EntityPlayer shooter = world.getPlayerEntityByUUID(shootingEntity);
					if(shooter!=null&&mop.entity instanceof EntityPlayer)
						allowHit = shooter.canAttackPlayer((EntityPlayer)mop.entity);
					if(allowHit)
						this.onImpact(mop);
					this.remove();
				}
				else if(mop.type==RayTraceResult.Type.BLOCK)
				{
					this.onImpact(mop);
					this.stuckIn = mop.getBlockPos();
					this.inBlockState = this.world.getBlockState(mop.getBlockPos());
					this.motionX = mop.hitVec.x-this.posX;
					this.motionY = mop.hitVec.y-this.posY;
					this.motionZ = mop.hitVec.z-this.posZ;
					float f2 = MathHelper.sqrt(this.motionX*this.motionX+this.motionY*this.motionY+this.motionZ*this.motionZ);
					this.posX -= this.motionX/(double)f2*0.05;
					this.posY -= this.motionY/(double)f2*0.05;
					this.posZ -= this.motionZ/(double)f2*0.05;

					this.inGround = true;
					if(this.inBlockState.getMaterial()!=Material.AIR)
						this.inBlockState.onEntityCollision(this.world, mop.getBlockPos(), this);
				}
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;

			float motion = MathHelper.sqrt(this.motionX*this.motionX+this.motionZ*this.motionZ);
			this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ)*180.0D/Math.PI);

			for(this.rotationPitch = (float)(Math.atan2(this.motionY, (double)motion)*180.0D/Math.PI); this.rotationPitch-this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
				;
			while(this.rotationPitch-this.prevRotationPitch >= 180.0F)
				this.prevRotationPitch += 360.0F;
			while(this.rotationYaw-this.prevRotationYaw < -180.0F)
				this.prevRotationYaw -= 360.0F;
			while(this.rotationYaw-this.prevRotationYaw >= 180.0F)
				this.prevRotationYaw += 360.0F;
			this.rotationPitch = this.prevRotationPitch+(this.rotationPitch-this.prevRotationPitch)*0.2F;
			this.rotationYaw = this.prevRotationYaw+(this.rotationYaw-this.prevRotationYaw)*0.2F;


			float movementDecay = getMotionDecayFactor();

			if(this.isInWater())
			{
				for(int j = 0; j < 4; ++j)
				{
					float f3 = 0.25F;
					this.world.spawnParticle(Particles.BUBBLE, this.posX-this.motionX*(double)f3, this.posY-this.motionY*(double)f3, this.posZ-this.motionZ*(double)f3, this.motionX, this.motionY, this.motionZ);
				}
				movementDecay *= 0.8F;
			}

			this.motionX *= movementDecay;
			this.motionY *= movementDecay;
			this.motionZ *= movementDecay;
			this.motionY -= getGravity();
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer p_70100_1_)
	{
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double distSq)
	{
		double d1 = this.getBoundingBox().getAverageEdgeLength()*4.0D;
		d1 *= 64.0D;
		return distSq < d1*d1;
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

	public abstract void onImpact(RayTraceResult mop);

	protected float getMotionDecayFactor()
	{
		return 0.99F;
	}

	@Override
	public void writeAdditional(NBTTagCompound nbt)
	{
		super.writeAdditional(nbt);
		if(inBlockState!=null)
		{
			nbt.setTag("inPos", NBTUtil.writeBlockPos(stuckIn));
			nbt.setTag("inTile", NBTUtil.writeBlockState(inBlockState));
		}
		nbt.setByte("inGround", (byte)(this.inGround?1: 0));
		if(this.shootingEntity!=null)
			nbt.setUniqueId("shootingEntity", this.shootingEntity);

	}

	@Override
	public void readAdditional(NBTTagCompound nbt)
	{
		super.readAdditional(nbt);
		if(nbt.contains("inTile", NBT.TAG_COMPOUND))
		{
			inBlockState = NBTUtil.readBlockState(nbt.getCompound("inTile"));
			stuckIn = NBTUtil.readBlockPos(nbt.getCompound("inPos"));
		}
		else
		{
			inBlockState = null;
			stuckIn = null;
		}
		this.inGround = nbt.getByte("inGround")==1;
		if(this.world!=null)
			this.shootingEntity = nbt.getUniqueId("shootingEntity");
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		return false;
	}
}