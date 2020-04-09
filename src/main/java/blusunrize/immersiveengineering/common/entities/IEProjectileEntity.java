/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class IEProjectileEntity extends AbstractArrowEntity//Yes I have to extend arrow or else it's all weird and broken >_>
{
	protected BlockPos stuckIn = null;
	protected BlockState inBlockState;
	public boolean inGround;
	public int ticksInGround;
	public int ticksInAir;

	private int tickLimit = 40;

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, World world)
	{
		super(type, world);
		this.pickupStatus = PickupStatus.DISALLOWED;
	}

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, World world, double x, double y, double z)
	{
		this(type, world);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
	}

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, World world, LivingEntity living, double ax, double ay, double az)
	{
		this(type, world, living, living.posX, living.posY+living.getEyeHeight(), living.posZ, ax, ay, az);
	}

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, World world, LivingEntity living, double x, double y, double z, double ax, double ay, double az)
	{
		this(type, world);
		float yaw = living!=null?living.rotationYaw: 0;
		float pitch = living!=null?living.rotationPitch: 0;
		this.setLocationAndAngles(x, y, z, yaw, pitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		setMotion(ax, ay, az);
		this.shootingEntity = living!=null?living.getUniqueID(): UUID.randomUUID();
		this.setShooterSynced();
		Vec3d motion = getMotion();
		this.shoot(motion.x, motion.y, motion.z, 2*1.5F, 1.0F);
	}

	@Nonnull
	@Override
	public EntitySize getSize(Pose p_213305_1_)
	{
		return new EntitySize(.125f, .125f, true);
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

	public UUID getShooterUUID()
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
		BlockState localState;
		if(stuckIn!=null)
			localState = this.world.getBlockState(stuckIn);
		else
			localState = Blocks.AIR.getDefaultState();

		//TODO better air check
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
				setMotion(getMotion().scale(this.rand.nextFloat()/5));
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
			Vec3d nextPos = new Vec3d(this.posX, this.posY, this.posZ).add(getMotion());
			RayTraceResult mop = this.world.rayTraceBlocks(new RayTraceContext(currentPos, nextPos, BlockMode.COLLIDER,
					FluidMode.NONE, this));

			if(mop.getType()==Type.BLOCK)
				nextPos = mop.getHitVec();

			if(mop.getType()!=Type.ENTITY)
			{
				Entity entity = null;
				List list = this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().expand(getMotion()).grow(1), Entity::canBeCollidedWith);
				double d0 = 0.0D;
				for(int i = 0; i < list.size(); ++i)
				{
					Entity entity1 = (Entity)list.get(i);
					if(entity1.canBeCollidedWith()&&(!entity1.getUniqueID().equals(this.shootingEntity)||this.ticksInAir > 5))
					{
						float f = 0.3F;
						AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow((double)f, (double)f, (double)f);
						Optional<Vec3d> movingobjectposition1 = axisalignedbb.rayTrace(currentPos, nextPos);

						if(movingobjectposition1.isPresent())
						{
							double d1 = currentPos.distanceTo(movingobjectposition1.get());
							if(d1 < d0||d0==0.0D)
							{
								entity = entity1;
								d0 = d1;
							}
						}
					}
				}
				if(entity!=null)
					mop = new EntityRayTraceResult(entity);
			}

			if(mop.getType()!=Type.MISS)
			{
				if(mop.getType()==Type.ENTITY)
				{
					EntityRayTraceResult entityHit = (EntityRayTraceResult)mop;
					if(!this.isBurning()&&this.canIgnite()&&entityHit.getEntity().isBurning())
						this.setFire(3);
					boolean allowHit = true;
					if(shootingEntity!=null)
					{
						PlayerEntity shooter = world.getPlayerByUuid(shootingEntity);
						if(shooter!=null&&entityHit.getEntity() instanceof PlayerEntity)
							allowHit = shooter.canAttackPlayer((PlayerEntity)entityHit.getEntity());
					}
					if(allowHit)
						this.onImpact(mop);
					if(this.getPierceLevel() <= 0)
						this.remove();
				}
				else if(mop.getType()==Type.BLOCK)
				{
					BlockRayTraceResult blockHit = (BlockRayTraceResult)mop;
					this.onImpact(blockHit);
					this.stuckIn = blockHit.getPos();
					this.inBlockState = this.world.getBlockState(blockHit.getPos());
					setMotion(blockHit.getHitVec().subtract(getPositionVector()));
					float f2 = (float)getMotion().length();
					Vec3d motion = getMotion();
					this.posX -= motion.x/(double)f2*0.05;
					this.posY -= motion.y/(double)f2*0.05;
					this.posZ -= motion.z/(double)f2*0.05;

					this.inGround = true;
					if(this.inBlockState.getMaterial()!=Material.AIR)
						this.inBlockState.onEntityCollision(this.world, blockHit.getPos(), this);
				}
			}

			this.posX += getMotion().x;
			this.posY += getMotion().y;
			this.posZ += getMotion().z;

			float absMotion = (float)getMotion().length();
			this.rotationYaw = (float)(Math.atan2(getMotion().x, getMotion().z)*180.0D/Math.PI);
			this.rotationPitch = (float)(Math.atan2(getMotion().y, (double)absMotion)*180.0D/Math.PI);
			while(this.rotationPitch-this.prevRotationPitch < -180.0F)
				this.prevRotationPitch -= 360.0F;
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
					this.world.addParticle(ParticleTypes.BUBBLE,
							this.posX-getMotion().x*(double)f3,
							this.posY-getMotion().y*(double)f3,
							this.posZ-getMotion().z*(double)f3,
							getMotion().x,
							getMotion().y,
							getMotion().z);
				}
				movementDecay *= 0.8F;
			}
			if(movementDecay > 0)
				setMotion(getMotion().scale(movementDecay).add(0, -getGravity(), 0));
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}
	}

	@Override
	public void onCollideWithPlayer(PlayerEntity player)
	{
		if(!this.world.isRemote&&(this.inGround||this.getNoClip())&&this.arrowShake <= 0)
		{
			boolean flag = this.pickupStatus==AbstractArrowEntity.PickupStatus.ALLOWED
					||this.pickupStatus==AbstractArrowEntity.PickupStatus.CREATIVE_ONLY&&player.abilities.isCreativeMode
					||this.getNoClip()&&this.getShooter().getUniqueID()==player.getUniqueID();
			if(this.pickupStatus==AbstractArrowEntity.PickupStatus.ALLOWED
					&&!player.inventory.addItemStackToInventory(this.getArrowStack()))
				flag = false;

			if(flag)
			{
				player.onItemPickup(this, 1);
				this.remove();
			}

		}
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
	public void writeAdditional(CompoundNBT nbt)
	{
		super.writeAdditional(nbt);
		if(inBlockState!=null)
		{
			nbt.put("inPos", NBTUtil.writeBlockPos(stuckIn));
			nbt.put("inTile", NBTUtil.writeBlockState(inBlockState));
		}
		nbt.putByte("inGround", (byte)(this.inGround?1: 0));
		if(this.shootingEntity!=null)
			nbt.putUniqueId("shootingEntity", this.shootingEntity);

	}

	@Override
	public void readAdditional(CompoundNBT nbt)
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
		this.shootingEntity = nbt.getUniqueId("shootingEntity");
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		return false;
	}

	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}