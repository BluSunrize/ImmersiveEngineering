/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.network.MessageBirthdayParty;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class RevolvershotEntity extends IEProjectileEntity
{
	private IBullet bulletType;
	public boolean bulletElectro = false;
	public ItemStack bulletPotion = ItemStack.EMPTY;
	private float gravity;
	private float movementDecay;

	public RevolvershotEntity(EntityType<? extends RevolvershotEntity> type, Level world)
	{
		super(type, world);
	}

	public RevolvershotEntity(EntityType<? extends RevolvershotEntity> eType, Level world, LivingEntity shooter, double x, double y, double z,
							  double ax, double ay, double az, IBullet type)
	{
		super(eType, world, shooter, x, y, z, ax, ay, az);
		this.setPos(x, y, z);
		this.bulletType = type;
	}

	public RevolvershotEntity(Level world, double x, double y, double z,
							  double ax, double ay, double az, IBullet type)
	{
		this(IEEntityTypes.REVOLVERSHOT.get(), world, null, x, y, z, ax, ay, az, type);
	}

	public RevolvershotEntity(Level world, LivingEntity living, double ax, double ay, double az, IBullet type)
	{
		this(IEEntityTypes.REVOLVERSHOT.get(), world, living, ax, ay, az, type);
	}

	public RevolvershotEntity(EntityType<? extends RevolvershotEntity> eType, Level world, LivingEntity living, double ax, double ay, double az, IBullet type)
	{
		this(eType, world, living, living.getX()+ax, living.getY()+living.getEyeHeight()+ay, living.getZ()+az, ax, ay, az, type);
		setShooterSynced();
		setDeltaMovement(Vec3.ZERO);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance)
	{
		double d1 = this.getBoundingBox().getSize()*4.0D;
		d1 *= 64.0D;
		return distance < d1*d1;
	}

	@Override
	public void onHit(HitResult mop)
	{
		boolean headshot = false;
		if(mop instanceof EntityHitResult)
		{
			Entity hitEntity = ((EntityHitResult)mop).getEntity();
			if(hitEntity instanceof LivingEntity)
				headshot = Utils.isVecInEntityHead((LivingEntity)hitEntity, position());
		}

		if(this.bulletType!=null)
		{
			bulletType.onHitTarget(level(), mop, this.shooterUUID, this, headshot);
			if(mop instanceof EntityHitResult)
			{
				Entity hitEntity = ((EntityHitResult)mop).getEntity();
				if(shooterUUID!=null&&headshot&&hitEntity instanceof LivingEntity&&((LivingEntity)hitEntity).isBaby()&&((LivingEntity)hitEntity).getHealth() <= 0)
				{
					Player shooter = level().getPlayerByUUID(shooterUUID);
					if(shooter!=null)
						Utils.unlockIEAdvancement(shooter, "tools/secret_birthdayparty");
					level().playSound(null, getX(), getY(), getZ(), IESounds.birthdayParty.get(), SoundSource.PLAYERS, 1.0F, 1.2F/(this.random.nextFloat()*0.2F+0.9F));
					ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_ENTITY.with(() -> hitEntity), new MessageBirthdayParty((LivingEntity)hitEntity));
				}
			}
		}
		if(!this.level().isClientSide)
			this.secondaryImpact(mop);
		if(mop instanceof BlockHitResult)
			this.onHitBlock((BlockHitResult)mop);
		this.discard();
	}


	public void secondaryImpact(HitResult mop)
	{
		if(!(mop instanceof EntityHitResult))
			return;
		Entity hitEntity = ((EntityHitResult)mop).getEntity();
		if(bulletElectro&&hitEntity instanceof LivingEntity&&shooterUUID!=null)
		{
			Player shooter = level().getPlayerByUUID(shooterUUID);
			float percentualDrain = .15f/(bulletType==null?1: bulletType.getProjectileCount(shooter));
			((LivingEntity)hitEntity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 4));
			for(EquipmentSlot slot : EquipmentSlot.values())
			{
				ItemStack stack = ((LivingEntity)hitEntity).getItemBySlot(slot);
				if(EnergyHelper.isFluxReceiver(stack)&&EnergyHelper.getEnergyStored(stack) > 0)
				{
					int drain = (int)Math.max(EnergyHelper.getEnergyStored(stack), EnergyHelper.getMaxEnergyStored(stack)*percentualDrain);
					int hasDrained = 0;
					while(hasDrained < drain)
					{
						int actualDrain = EnergyHelper.forceExtractFlux(stack, drain, false);
						if(actualDrain <= 0)
							break;
						hasDrained += actualDrain;
					}
				}
			}
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt)
	{
		super.addAdditionalSaveData(nbt);
		nbt.putByte("inGround", (byte)(this.inGround?1: 0));
		nbt.putString("bulletType", BulletHandler.findRegistryName(this.bulletType).toString());
		if(!bulletPotion.isEmpty())
			nbt.put("bulletPotion", bulletPotion.save(new CompoundTag()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt)
	{
		super.readAdditionalSaveData(nbt);
		this.bulletType = BulletHandler.getBullet(new ResourceLocation(nbt.getString("bulletType")));
		if(nbt.contains("bulletPotion", Tag.TAG_COMPOUND))
			this.bulletPotion = ItemStack.of(nbt.getCompound("bulletPotion"));
	}

	@Override
	public float getPickRadius()
	{
		return 1.0F;
	}

	@Override
	public boolean isPickable()
	{
		return false;
	}

	@Override
	public boolean hurt(DamageSource source, float amount)
	{
		return false;
	}

	public void setGravity(float gravity)
	{
		this.gravity = gravity;
	}

	@Override
	public double getGravity()
	{
		return gravity;
	}

	public void setMovementDecay(float movementDecay)
	{
		this.movementDecay = movementDecay;
	}

	@Override
	protected float getMotionDecayFactor()
	{
		return movementDecay;
	}
}