/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.ITeslaEntity;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilBlockEntity;
import blusunrize.immersiveengineering.common.items.FluorescentTubeItem;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class FluorescentTubeEntity extends Entity implements ITeslaEntity
{
	public static final float TUBE_LENGTH = 1.5F;

	private static final EntityDataAccessor<Boolean> dataMarker_active = SynchedEntityData.defineId(FluorescentTubeEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> dataMarker_r = SynchedEntityData.defineId(FluorescentTubeEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> dataMarker_g = SynchedEntityData.defineId(FluorescentTubeEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> dataMarker_b = SynchedEntityData.defineId(FluorescentTubeEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> dataMarker_angleHorizontal = SynchedEntityData.defineId(FluorescentTubeEntity.class, EntityDataSerializers.FLOAT);

	private int timer = 0;
	public boolean active = false;
	public Color4 rgb = Color4.WHITE;
	boolean firstTick = true;
	public float angleHorizontal = 0;

	public FluorescentTubeEntity(Level world, ItemStack tube, float angleVert)
	{
		this(IEEntityTypes.FLUORESCENT_TUBE.get(), world);
		setYRot(angleVert);
		rgb = FluorescentTubeItem.getRGB(tube);
	}

	public FluorescentTubeEntity(EntityType<FluorescentTubeEntity> type, Level world)
	{
		super(type, world);
	}


	@Override
	public void tick()
	{
		super.tick();
		//movement logic
		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		Vec3 motion = getDeltaMovement();
		motion = motion.add(0, -.4, 0);
		this.move(MoverType.SELF, motion);
		motion = motion.scale(0.98);

		if(this.onGround())
			motion = new Vec3(motion.x*0.7, motion.y*-0.5, motion.z*0.7);
		if(firstTick&&!level().isClientSide&&rgb!=null)
		{
			entityData.set(dataMarker_r, rgb.r());
			entityData.set(dataMarker_g, rgb.g());
			entityData.set(dataMarker_b, rgb.b());
			entityData.set(dataMarker_angleHorizontal, angleHorizontal);
			firstTick = false;
		}
		// tube logic
		if(timer > 0&&!level().isClientSide)
		{
			timer--;
			if(timer <= 0)
				entityData.set(dataMarker_active, false);
		}
		if(level().isClientSide)
		{
			active = entityData.get(dataMarker_active);
			rgb = new Color4(entityData.get(dataMarker_r), entityData.get(dataMarker_g), entityData.get(dataMarker_b), 1);
			angleHorizontal = entityData.get(dataMarker_angleHorizontal);
		}
		setDeltaMovement(motion);
	}

	@Override
	protected void defineSynchedData(Builder builder)
	{
		builder.define(dataMarker_r, 1F);
		builder.define(dataMarker_g, 1F);
		builder.define(dataMarker_b, 1F);
		builder.define(dataMarker_active, false);
		builder.define(dataMarker_angleHorizontal, 0F);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt)
	{
		CompoundTag comp = nbt.getCompound("nbt");
		rgb = Color4.load(comp.get("color"));
		angleHorizontal = nbt.getFloat("angleHor");

	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt)
	{
		CompoundTag comp = new CompoundTag();
		comp.put("color", rgb.save());
		angleHorizontal = nbt.getFloat("angleHor");
		nbt.put("nbt", comp);
		nbt.putFloat("angleHor", angleHorizontal);
	}

	@Override
	public boolean hurt(DamageSource source, float amount)
	{
		if(isAlive()&&!level().isClientSide)
		{
			ItemStack tube = new ItemStack(Misc.FLUORESCENT_TUBE);
			FluorescentTubeItem.setRGB(tube, rgb);
			ItemEntity ent = new ItemEntity(level(), getX(), getY(), getZ(), tube);
			level().addFreshEntity(ent);
			discard();
		}
		return super.hurt(source, amount);
	}

	@Override
	public boolean isPickable()
	{
		return isAlive();
	}

	@Override
	public void onHit(BlockEntity te, boolean lowPower)
	{
		if(te instanceof TeslaCoilBlockEntity&&((TeslaCoilBlockEntity)te).energyStorage.extractEnergy(1, false) > 0)
		{
			timer = 35;
			entityData.set(dataMarker_active, true);
		}
	}

	@Nonnull
	@Override
	public InteractionResult interactAt(Player player, Vec3 targetVec3, InteractionHand hand)
	{
		if(Utils.isHammer(player.getItemInHand(hand)))
		{
			angleHorizontal += (player.isShiftKeyDown()?10: 1);
			angleHorizontal %= 360;
			entityData.set(dataMarker_angleHorizontal, angleHorizontal);
			return InteractionResult.SUCCESS;
		}
		else if (player.isShiftKeyDown())
		{
			if(isAlive()&&!level().isClientSide&&player.getItemInHand(hand).isEmpty())
			{
				ItemStack tube = new ItemStack(Misc.FLUORESCENT_TUBE);
				FluorescentTubeItem.setRGB(tube, rgb);
				ItemEntity ent = new ItemEntity(level(), player.getX(), player.getY(), player.getZ(), tube, 0, 0, 0);
				level().addFreshEntity(ent);
				discard();
			}
			return InteractionResult.SUCCESS;
		}
		return super.interactAt(player, targetVec3, hand);
	}
}
