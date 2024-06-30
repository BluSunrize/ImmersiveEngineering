/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.util.DirectionalMiningExplosion;
import blusunrize.immersiveengineering.mixin.accessors.TNTEntityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nonnull;

public class GunpowderBarrelEntity extends PrimedTnt
{
	private float size;
	private Explosion.BlockInteraction mode = BlockInteraction.DESTROY;
	private boolean isFlaming = false;
	public BlockState block;
	private Component name;

	private static final EntityDataAccessor<BlockState> dataMarker_block = SynchedEntityData.defineId(GunpowderBarrelEntity.class, EntityDataSerializers.BLOCK_STATE);
	private static final EntityDataAccessor<Integer> dataMarker_fuse = SynchedEntityData.defineId(GunpowderBarrelEntity.class, EntityDataSerializers.INT);

	public GunpowderBarrelEntity(EntityType<GunpowderBarrelEntity> type, Level world)
	{
		super(type, world);
	}

	public GunpowderBarrelEntity(Level world, BlockPos pos, LivingEntity igniter, BlockState blockstate, float size)
	{
		super(IEEntityTypes.EXPLOSIVE.get(), world);
		this.setPos(pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5);
		double jumpingDirection = world.random.nextDouble()*2*Math.PI;
		this.setDeltaMovement(-Math.sin(jumpingDirection)*0.02D, 0.2, -Math.cos(jumpingDirection)*0.02D);
		this.setFuse(80);
		this.xo = getX();
		this.yo = getY();
		this.zo = getZ();
		((TNTEntityAccess)this).setOwner(igniter);
		this.size = size;
		this.block = blockstate;
		this.setBlockSynced();
	}

	public GunpowderBarrelEntity setMode(BlockInteraction smoke)
	{
		this.mode = smoke;
		return this;
	}

	public GunpowderBarrelEntity setFlaming(boolean fire)
	{
		this.isFlaming = fire;
		return this;
	}

	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(dataMarker_block, Blocks.AIR.defaultBlockState());
		this.entityData.define(dataMarker_fuse, 0);
	}

	private void setBlockSynced()
	{
		if(this.block!=null)
		{
			this.entityData.set(dataMarker_block, this.block);
			this.entityData.set(dataMarker_fuse, this.getFuse());
		}
	}

	private void getBlockSynced()
	{
		this.block = this.entityData.get(dataMarker_block);
		if(this.block.isAir())
			this.block = null;
		this.setFuse(this.entityData.get(dataMarker_fuse));
	}

	@Nonnull
	@Override
	public Component getName()
	{
		if(this.block!=null&&name==null)
		{
			ItemStack s = new ItemStack(this.block.getBlock(), 1);
			if(!s.isEmpty()&&s.getItem()!=Items.AIR)
				name = s.getHoverName();
		}
		if(name!=null)
			return name;
		return super.getName();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tagCompound)
	{
		super.addAdditionalSaveData(tagCompound);
		tagCompound.putFloat("explosionPower", size);
		tagCompound.putInt("explosionSmoke", mode.ordinal());
		tagCompound.putBoolean("explosionFire", isFlaming);
		if(this.block!=null)
			tagCompound.putInt("block", Block.getId(this.block));
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tagCompound)
	{
		super.readAdditionalSaveData(tagCompound);
		size = tagCompound.getFloat("explosionPower");
		mode = BlockInteraction.values()[tagCompound.getInt("explosionSmoke")];
		isFlaming = tagCompound.getBoolean("explosionFire");
		if(tagCompound.contains("block", Tag.TAG_INT))
			this.block = Block.stateById(tagCompound.getInt("block"));
	}


	@Override
	public void tick()
	{
		if(level().isClientSide&&this.block==null)
			this.getBlockSynced();

		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		if(!this.isNoGravity())
		{
			this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
		}

		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
		if(this.onGround())
		{
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
		}
		int newFuse = this.getFuse()-1;
		this.setFuse(newFuse);
		if(newFuse <= 0)
		{
			this.discard();
			Explosion explosion = new DirectionalMiningExplosion(level(), this, getX(), getY()+(getBbHeight()/16f), getZ(), size, isFlaming);
			if(!EventHooks.onExplosionStart(level(), explosion))
			{
				explosion.explode();
				explosion.finalizeExplosion(true);
			}
		}
		else
		{
			this.updateInWaterStateAndDoFluidPushing();
			this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY()+0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
		}
	}

}