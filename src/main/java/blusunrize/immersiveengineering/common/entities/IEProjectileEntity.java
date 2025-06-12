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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nonnull;

public abstract class IEProjectileEntity extends AbstractArrow//Yes I have to extend arrow or else it's all weird and broken >_>
{
	public int ticksInAir;
	protected IntSet piercedEntities;
	// Hack to disable vanilla gravity code in tick(). Using the vanilla setter would make MC sync the data value every
	// tick
	private boolean forceNoGravity;

	private int tickLimit = 40;

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, Level world)
	{
		super(type, world);
		this.pickup = Pickup.DISALLOWED;
		this.setSoundEvent(SoundEvents.EMPTY);
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

	public IEProjectileEntity(EntityType<? extends IEProjectileEntity> type, Level world, @Nonnull LivingEntity living, float velocity, float inaccuracy)
	{
		this(type, world);
		setOwner(living);
		this.setPos(living.getX(), living.getEyeY()-0.1, living.getZ());
		this.shootFromRotation(living, living.getXRot(), living.getYRot(), 0.0F, velocity, inaccuracy);
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
		Vec3 motion = getDeltaMovement();
		this.shoot(motion.x, motion.y, motion.z, 2*1.5F, 1.0F);
	}

	@Nonnull
	@Override
	public EntityDimensions getDimensions(Pose poseIn)
	{
		return new EntityDimensions(.125f, .125f, .125f, EntityAttachments.createDefault(0, 0), true);
	}

	public void setTickLimit(int limit)
	{
		this.tickLimit = limit;
	}

	@Nonnull
	@Override
	protected ItemStack getPickupItem()
	{
		return ItemStack.EMPTY;
	}

	public boolean isInGround()
	{
		return this.inGround;
	}

	@Override
	public void tick()
	{
		if(!isInGround())
			++ticksInAir;
		if(this.ticksInAir >= this.tickLimit||this.inGroundTime >= this.getMaxTicksInGround())
			this.discard();

		// store previous movement
		Vec3 delta = this.getDeltaMovement().add(0, 0, 0);
		float xRotPrev = this.getXRot();
		float yRotPrev = this.getYRot();
		float xRot0Prev = this.xRotO;
		float yRot0Prev = this.yRotO;
		// disable vanilla gravity
		this.forceNoGravity = true;

		// perform vanilla tick
		super.tick();

		// enable gravity
		this.forceNoGravity = false;

		// Vanilla has a fun issue where it ignores a block hit result if it found any entities instead
		// so we check for block hits here again...
		if(!this.isRemoved() && !this.inGround)
		{
			Vec3 vec32 = this.position();
			Vec3 vec33 = vec32.add(delta);
			BlockHitResult blockHitResult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
			if(blockHitResult.getType()!=HitResult.Type.MISS&&!EventHooks.onProjectileImpact(this, blockHitResult))
			{
				this.onHit(blockHitResult);
				this.hasImpulse = true;
			}
		}

		if(!this.inGround)
		{
			// restore rotations
			this.setXRot(xRotPrev);
			this.setYRot(yRotPrev);
			this.xRotO = xRot0Prev;
			this.yRotO = yRot0Prev;

			// perform custom movement changes
			float absMotion = (float)delta.length();
			this.setYRot((float)(Math.atan2(delta.x, delta.z)*180.0D/Math.PI));
			this.setXRot((float)(Math.atan2(delta.y, absMotion)*180.0D/Math.PI));
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
				movementDecay *= 0.8F;
			if(movementDecay > 0)
				setDeltaMovement(delta.scale(movementDecay).add(0, -getGravity(), 0));
		}
	}

	@Override
	public void playerTouch(Player player)
	{
		if(!this.level().isClientSide&&(this.inGround||this.isNoPhysics())&&this.shakeTime <= 0)
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

	public double getDefaultGravity()
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

	protected float getMotionDecayFactor()
	{
		return 0.99F;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt)
	{
		super.addAdditionalSaveData(nbt);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt)
	{
		super.readAdditionalSaveData(nbt);
	}

	@Override
	public boolean hurt(DamageSource source, float amount)
	{
		return false;
	}

	@Override
	public boolean isNoGravity()
	{
		return this.forceNoGravity||super.isNoGravity();
	}
}
