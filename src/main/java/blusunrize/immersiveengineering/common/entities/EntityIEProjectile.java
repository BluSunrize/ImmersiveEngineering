/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class EntityIEProjectile extends EntityArrow//Yes I have to extend arrow or else it's all weird and broken >_>
{
	protected int blockX = -1;
	protected int blockY = -1;
	protected int blockZ = -1;
	protected Block inBlock;
	protected int inMeta;
	public boolean inGround;
	public int ticksInGround;
	public int ticksInAir;

	private int tickLimit=40;
	private static final DataParameter<String> dataMarker_shooter = EntityDataManager.createKey(EntityIEProjectile.class, DataSerializers.STRING);

	public EntityIEProjectile(World world)
	{
		super(world);
		this.setSize(.125f,.125f);
		this.pickupStatus = PickupStatus.DISALLOWED;
	}
	public EntityIEProjectile(World world, double x, double y, double z, double ax, double ay, double az)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		this.pickupStatus = PickupStatus.DISALLOWED;
	}
	public EntityIEProjectile(World world, EntityLivingBase living, double ax, double ay, double az)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(living.posX, living.posY+living.getEyeHeight(), living.posZ, living.rotationYaw, living.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
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
		//		this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		//		this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		//		this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI));

		this.shoot(this.motionX, this.motionY, this.motionZ, 2*1.5F, 1.0F);
		this.pickupStatus = PickupStatus.DISALLOWED;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(dataMarker_shooter, "");
	}


	public void setTickLimit(int limit)
	{
		this.tickLimit=limit;
	}

	public void setShooterSynced()
	{
		this.dataManager.set(dataMarker_shooter, this.shootingEntity.getName());
	}
	public EntityLivingBase getShooterSynced()
	{
		String s = this.dataManager.get(dataMarker_shooter);
		if(s != null)
			return this.world.getPlayerEntityByName(s);
		return null;
	}
	public Entity getShooter()
	{
		return shootingEntity;
	}

	@Override
	protected ItemStack getArrowStack()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void onUpdate()
	{
		if(this.getShooter() == null && this.world.isRemote)
			this.shootingEntity = getShooterSynced();

		this.onEntityUpdate();

		BlockPos blockpos = new BlockPos(this.blockX, this.blockY, this.blockZ);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		Block block = iblockstate.getBlock();

		if(iblockstate.getMaterial() != Material.AIR)
		{
			AxisAlignedBB axisalignedbb = block.getCollisionBoundingBox(iblockstate, this.world, blockpos);
			if(axisalignedbb != null && axisalignedbb.contains(new Vec3d(this.posX, this.posY, this.posZ)))
				this.inGround = true;
		}

		if(this.inGround)
		{
			int j = block.getMetaFromState(iblockstate);
			if(block==this.inBlock && j==this.inMeta)
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

			Vec3d currentPos = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult mop = this.world.rayTraceBlocks(currentPos, nextPos, false,true,false);

			currentPos = new Vec3d(this.posX, this.posY, this.posZ);

			if(mop != null)
				nextPos = new Vec3d(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
			else
				nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if(mop==null || mop.entityHit==null)
			{
				Entity entity = null;
				List list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1), (e)->e.canBeCollidedWith());
				double d0 = 0.0D;
				for (int i = 0; i < list.size(); ++i)
				{
					Entity entity1 = (Entity)list.get(i);
					if(entity1.canBeCollidedWith() && (!entity1.isEntityEqual(this.shootingEntity) || this.ticksInAir>5))
					{
						float f = 0.3F;
						AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double)f, (double)f, (double)f);
						RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(currentPos, nextPos);

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
					mop = new RayTraceResult(entity);
			}

			if(mop!=null)
			{
				if(!this.isBurning() && this.canIgnite() && mop.entityHit!=null && mop.entityHit.isBurning())
					this.setFire(3);
				if(mop.entityHit!=null)
				{
					boolean allowHit = true;
					if(this.shootingEntity instanceof EntityPlayer && mop.entityHit instanceof EntityPlayer)
						allowHit = ((EntityPlayer)this.shootingEntity).canAttackPlayer((EntityPlayer)mop.entityHit);
					if(allowHit)
						this.onImpact(mop);
					this.setDead();
				}
				else if(mop.typeOfHit== RayTraceResult.Type.BLOCK)
				{
					this.onImpact(mop);
					this.blockX = mop.getBlockPos().getX();
					this.blockY = mop.getBlockPos().getY();
					this.blockZ = mop.getBlockPos().getZ();
					IBlockState state = this.world.getBlockState(mop.getBlockPos());
					this.inBlock = state.getBlock();
					this.inMeta = inBlock.getMetaFromState(state);
					this.motionX = mop.hitVec.x - this.posX;
					this.motionY = mop.hitVec.y - this.posY;
					this.motionZ = mop.hitVec.z - this.posZ;
					float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					this.posX -= this.motionX / (double)f2 * 0.05000000074505806D;
					this.posY -= this.motionY / (double)f2 * 0.05000000074505806D;
					this.posZ -= this.motionZ / (double)f2 * 0.05000000074505806D;
					//						this.posX = movingobjectposition.hitVec.xCoord;
					//						this.posY = movingobjectposition.hitVec.yCoord;
					//						this.posZ = movingobjectposition.hitVec.zCoord;
					//						this.setPosition(this.posX, this.posY, this.posZ);

					this.inGround = true;
					if(this.inBlock.getMaterial(state) != Material.AIR)
						this.inBlock.onEntityCollidedWithBlock(this.world, mop.getBlockPos(), state, this);
					//						return;
				}
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;

			float motion = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
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
					this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)f3, this.posY - this.motionY * (double)f3, this.posZ - this.motionZ * (double)f3, this.motionX, this.motionY, this.motionZ);
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

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
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

	public abstract void onImpact(RayTraceResult mop);

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
			nbt.setString("shootingEntity", this.shootingEntity.getName());

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
		if(this.world!=null)
			this.shootingEntity = this.world.getPlayerEntityByName(nbt.getString("shootingEntity"));
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		return false;
	}
}