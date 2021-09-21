/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class IEProjectileEntity extends AbstractArrow//Yes I have to extend arrow or else it's all weird and broken >_>
{
	private static final EntityDataAccessor<Optional<UUID>> SHOOTER_PARAMETER =
			SynchedEntityData.defineId(IEProjectileEntity.class, EntityDataSerializers.OPTIONAL_UUID);

	protected BlockPos stuckIn = null;
	protected BlockState inBlockState;
	public boolean inGround;
	public int ticksInGround;
	public int ticksInAir;
	protected IntSet piercedEntities;
	@Nullable
	protected UUID shooterUUID;

	private int tickLimit = 40;

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, Level world)
	{
		super(type, world);
		this.pickup = Pickup.DISALLOWED;
	}

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, Level world, double x, double y, double z)
	{
		this(type, world);
		this.moveTo(x, y, z, this.getYRot(), this.getXRot());
		this.setPos(x, y, z);
	}

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, Level world, LivingEntity living, double ax, double ay, double az)
	{
		this(type, world, living, living.getX(), living.getY()+living.getEyeHeight(), living.getZ(), ax, ay, az);
	}

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, Level world, LivingEntity living, double x, double y, double z, double ax, double ay, double az)
	{
		this(type, world);
		float yaw = living!=null?living.getYRot(): 0;
		float pitch = living!=null?living.getXRot(): 0;
		this.moveTo(x, y, z, yaw, pitch);
		this.setPos(this.getX(), this.getY(), this.getZ());
		setDeltaMovement(ax, ay, az);
		setOwner(living);
		this.setShooterSynced();
		Vec3 motion = getDeltaMovement();
		this.shoot(motion.x, motion.y, motion.z, 2*1.5F, 1.0F);
	}

	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(SHOOTER_PARAMETER, Optional.empty());
	}

	@Nonnull
	@Override
	public EntityDimensions getDimensions(Pose poseIn)
	{
		return new EntityDimensions(.125f, .125f, true);
	}

	public void setTickLimit(int limit)
	{
		this.tickLimit = limit;
	}

	public void setShooterSynced()
	{
		this.entityData.set(SHOOTER_PARAMETER, Optional.ofNullable(this.shooterUUID));
	}

	public UUID getShooterSynced()
	{
		Optional<UUID> s = this.entityData.get(SHOOTER_PARAMETER);
		return s.orElse(null);
	}

	@Nullable
	public UUID getShooterUUID()
	{
		return shooterUUID;
	}

	@Nonnull
	@Override
	protected ItemStack getPickupItem()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void tick()
	{
		if(this.getOwner()==null&&this.level.isClientSide)
			this.shooterUUID = getShooterSynced();

		this.baseTick();
		BlockState localState;
		if(stuckIn!=null)
			localState = this.level.getBlockState(stuckIn);
		else
			localState = Blocks.AIR.defaultBlockState();

		//TODO better air check
		if(localState.getMaterial()!=Material.AIR)
		{
			VoxelShape shape = localState.getCollisionShape(this.level, stuckIn);
			for(AABB subbox : shape.toAabbs())
				if(subbox.contains(this.getX(), this.getY(), this.getZ()))
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
					this.discard();
			}
			else
			{
				this.inGround = false;
				setDeltaMovement(getDeltaMovement().scale(this.random.nextFloat()/5));
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}
		}
		else
		{
			++this.ticksInAir;

			if(ticksInAir >= tickLimit)
			{
				this.discard();
				return;
			}

			Vec3 currentPos = new Vec3(this.getX(), this.getY(), this.getZ());
			Vec3 nextPos = new Vec3(this.getX(), this.getY(), this.getZ()).add(getDeltaMovement());
			HitResult mop = this.level.clip(new ClipContext(currentPos, nextPos, Block.COLLIDER,
					Fluid.NONE, this));

			if(mop.getType()==Type.BLOCK)
				nextPos = mop.getLocation();

			if(mop.getType()!=Type.ENTITY)
			{
				Entity entity = null;
				List<Entity> list = this.level.getEntities(this, this.getBoundingBox().expandTowards(getDeltaMovement()).inflate(1), Entity::isPickable);
				double d0 = 0.0D;
				for(Entity entity1 : list)
				{
					if(entity1.isPickable()&&(!entity1.getUUID().equals(this.shooterUUID)||this.ticksInAir > 5))
					{
						float f = 0.3F;
						AABB axisalignedbb = entity1.getBoundingBox().inflate((double)f, (double)f, (double)f);
						Optional<Vec3> movingobjectposition1 = axisalignedbb.clip(currentPos, nextPos);

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
					mop = new EntityHitResult(entity);
			}

			if(mop.getType()!=Type.MISS)
			{
				if(mop.getType()==Type.ENTITY)
				{
					EntityHitResult entityHit = (EntityHitResult)mop;
					if(!this.isOnFire()&&this.canIgnite()&&entityHit.getEntity().isOnFire())
						this.setSecondsOnFire(3);
					boolean allowHit = true;
					if(shooterUUID!=null)
					{
						Player shooter = level.getPlayerByUUID(shooterUUID);
						if(shooter!=null&&entityHit.getEntity() instanceof Player)
							allowHit = shooter.canHarmPlayer((Player)entityHit.getEntity());
					}
					if(allowHit)
						this.onHit(mop);
					this.handlePiecing(entityHit.getEntity());
				}
				else if(mop.getType()==Type.BLOCK)
				{
					BlockHitResult blockHit = (BlockHitResult)mop;
					this.onHit(blockHit);
					this.stuckIn = blockHit.getBlockPos();
					this.inBlockState = this.level.getBlockState(blockHit.getBlockPos());
					setDeltaMovement(blockHit.getLocation().subtract(position()));
					float f2 = (float)getDeltaMovement().length();
					Vec3 motion = getDeltaMovement();
					this.setPos(
							this.getX()-motion.x/(double)f2*0.05,
							this.getY()-motion.y/(double)f2*0.05,
							this.getZ()-motion.z/(double)f2*0.05
					);

					this.inGround = true;
					if(this.inBlockState.getMaterial()!=Material.AIR)
						this.inBlockState.entityInside(this.level, blockHit.getBlockPos(), this);
				}
			}

			this.setPos(
					this.getX()+getDeltaMovement().x,
					this.getY()+getDeltaMovement().y,
					this.getZ()+getDeltaMovement().z
			);

			float absMotion = (float)getDeltaMovement().length();
			this.setYRot((float)(Math.atan2(getDeltaMovement().x, getDeltaMovement().z)*180.0D/Math.PI));
			this.setXRot((float)(Math.atan2(getDeltaMovement().y, absMotion)*180.0D/Math.PI));
			while(this.getXRot()-this.xRotO < -180.0F)
				this.xRotO -= 360.0F;
			while(this.getXRot()-this.xRotO >= 180.0F)
				this.xRotO += 360.0F;
			while(this.getYRot()-this.yRotO < -180.0F)
				this.yRotO -= 360.0F;
			while(this.getYRot()-this.yRotO >= 180.0F)
				this.yRotO += 360.0F;
			this.setXRot(this.xRotO+(this.getXRot()-this.xRotO)*0.2F);
			this.setYRot(this.yRotO+(this.getYRot()-this.yRotO)*0.2F);


			float movementDecay = getMotionDecayFactor();

			if(this.isInWater())
			{
				for(int j = 0; j < 4; ++j)
				{
					float f3 = 0.25F;
					this.level.addParticle(ParticleTypes.BUBBLE,
							this.getX()-getDeltaMovement().x*(double)f3,
							this.getY()-getDeltaMovement().y*(double)f3,
							this.getZ()-getDeltaMovement().z*(double)f3,
							getDeltaMovement().x,
							getDeltaMovement().y,
							getDeltaMovement().z);
				}
				movementDecay *= 0.8F;
			}
			if(movementDecay > 0)
				setDeltaMovement(getDeltaMovement().scale(movementDecay).add(0, -getGravity(), 0));
			this.setPos(this.getX(), this.getY(), this.getZ());
			this.checkInsideBlocks();
		}
	}

	@Override
	public void playerTouch(Player player)
	{
		if(!this.level.isClientSide&&(this.inGround||this.isNoPhysics())&&this.shakeTime <= 0)
		{
			boolean flag = this.pickup==AbstractArrow.Pickup.ALLOWED
					||this.pickup==AbstractArrow.Pickup.CREATIVE_ONLY&&player.getAbilities().instabuild
					||this.isNoPhysics()&&this.getOwner().getUUID()==player.getUUID();
			if(this.pickup==AbstractArrow.Pickup.ALLOWED
					&&!player.getInventory().add(this.getPickupItem()))
				flag = false;

			if(flag)
			{
				player.take(this, 1);
				this.discard();
			}

		}
	}

	protected void handlePiecing(Entity target)
	{
		if(this.getPierceLevel() > 0)
		{
			if(this.piercedEntities==null)
				this.piercedEntities = new IntOpenHashSet(this.getPierceLevel());
			if(this.piercedEntities.size() >= this.getPierceLevel()+1)
			{
				this.discard();
				return;
			}
			this.piercedEntities.add(target.getId());
		}
		else
			this.discard();
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distSq)
	{
		double d1 = this.getBoundingBox().getSize()*4.0D;
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

	public abstract void onHit(HitResult mop);

	protected void onHitBlock(BlockHitResult mop)
	{
		BlockState blockstate = this.level.getBlockState(mop.getBlockPos());
		blockstate.onProjectileHit(this.level, blockstate, mop, this);
	}

	protected float getMotionDecayFactor()
	{
		return 0.99F;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt)
	{
		super.addAdditionalSaveData(nbt);
		if(inBlockState!=null)
		{
			nbt.put("inPos", NbtUtils.writeBlockPos(stuckIn));
			nbt.put("inTile", NbtUtils.writeBlockState(inBlockState));
		}
		nbt.putByte("inGround", (byte)(this.inGround?1: 0));
		if(this.shooterUUID!=null)
			nbt.putUUID("Owner", this.shooterUUID);

	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt)
	{
		super.readAdditionalSaveData(nbt);
		if(nbt.contains("inTile", NBT.TAG_COMPOUND))
		{
			inBlockState = NbtUtils.readBlockState(nbt.getCompound("inTile"));
			stuckIn = NbtUtils.readBlockPos(nbt.getCompound("inPos"));
		}
		else
		{
			inBlockState = null;
			stuckIn = null;
		}
		this.inGround = nbt.getByte("inGround")==1;
		if(nbt.contains("Owner"))
			this.shooterUUID = nbt.getUUID("Owner");
		else
			this.shooterUUID = null;
	}

	@Override
	public boolean hurt(DamageSource source, float amount)
	{
		return false;
	}

	@Override
	public Packet<?> getAddEntityPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public void setOwner(@Nullable Entity entityIn)
	{
		super.setOwner(entityIn);
		if(entityIn!=null)
			this.shooterUUID = entityIn.getUUID();
	}
}
