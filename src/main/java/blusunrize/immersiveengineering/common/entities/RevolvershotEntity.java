/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.network.MessageBirthdayParty;
import blusunrize.immersiveengineering.common.register.IEEntityDataSerializers;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
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
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RevolvershotEntity extends IEProjectileEntity
{
	private static final EntityDataAccessor<BulletData<?>> DATAMARKER_BULLET = SynchedEntityData.defineId(
			RevolvershotEntity.class, IEEntityDataSerializers.BULLET.get()
	);

	private BulletData<?> bullet;
	public boolean bulletElectro = false;
	private float gravity;
	private float movementDecay;

	public RevolvershotEntity(EntityType<? extends RevolvershotEntity> type, Level world)
	{
		super(type, world);
	}

	public <T> RevolvershotEntity(
			EntityType<? extends RevolvershotEntity> eType, Level world, LivingEntity shooter,
			double x, double y, double z, double ax, double ay, double az,
			IBullet<T> bullet, T bulletData
	)
	{
		super(eType, world, shooter, x, y, z, ax, ay, az);
		this.setPos(x, y, z);
		this.bullet = new BulletData<>(bullet, bulletData);
		this.entityData.set(DATAMARKER_BULLET, this.bullet);
	}

	public <T> RevolvershotEntity(
			Level world,
			double x, double y, double z, double ax, double ay, double az,
			IBullet<T> bullet, T bulletData
	)
	{
		this(IEEntityTypes.REVOLVERSHOT.get(), world, null, x, y, z, ax, ay, az, bullet, bulletData);
	}

	public <T> RevolvershotEntity(
			Level world, LivingEntity living,
			double ax, double ay, double az,
			IBullet<T> bullet, T bulletData
	)
	{
		this(IEEntityTypes.REVOLVERSHOT.get(), world, living, ax, ay, az, bullet, bulletData);
	}

	public <T> RevolvershotEntity(
			EntityType<? extends RevolvershotEntity> eType, Level world, LivingEntity living,
			double ax, double ay, double az,
			IBullet<T> bullet, T bulletData
	)
	{
		this(eType, world, living, living.getX()+ax, living.getY()+living.getEyeHeight()+ay, living.getZ()+az, ax, ay, az, bullet, bulletData);
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
	protected void defineSynchedData(Builder builder)
	{
		super.defineSynchedData(builder);
		builder.define(DATAMARKER_BULLET, new BulletData<>(BulletHandler.getBullet(IEBullets.CASULL)));
	}

	public BulletData<?> getBullet()
	{
		if(level().isClientSide)
			return entityData.get(DATAMARKER_BULLET);
		else
			return bullet;
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

		if(this.bullet!=null)
		{
			Entity owner = getOwner();
			UUID shooterUUID = owner!=null?owner.getUUID():null;
			bullet.onHitTarget(level(), mop, shooterUUID, this, headshot);
			if(mop instanceof EntityHitResult)
			{
				Entity hitEntity = ((EntityHitResult)mop).getEntity();
				if(shooterUUID!=null&&headshot&&hitEntity instanceof LivingEntity&&((LivingEntity)hitEntity).isBaby()&&((LivingEntity)hitEntity).getHealth() <= 0)
				{
					Player shooter = level().getPlayerByUUID(shooterUUID);
					if(shooter!=null)
						Utils.unlockIEAdvancement(shooter, "tools/secret_birthdayparty");
					level().playSound(null, getX(), getY(), getZ(), IESounds.birthdayParty.value(), SoundSource.PLAYERS, 1.0F, 1.2F/(this.random.nextFloat()*0.2F+0.9F));
					PacketDistributor.sendToPlayersTrackingEntity(hitEntity, new MessageBirthdayParty(hitEntity.getId()));
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
		Entity owner = getOwner();
		UUID shooterUUID = owner!=null?owner.getUUID():null;
		if(bulletElectro&&hitEntity instanceof LivingEntity&&shooterUUID!=null)
		{
			Player shooter = level().getPlayerByUUID(shooterUUID);
			float percentualDrain = .15f/(bullet==null?1: bullet.bullet.getProjectileCount(shooter));
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
		nbt.put("bullet", BulletData.CODECS.toNBT(bullet));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt)
	{
		super.readAdditionalSaveData(nbt);
		this.bullet = BulletData.CODECS.fromNBT(nbt.get("bullet"));
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

	@Nonnull
	@Override
	protected ItemStack getDefaultPickupItem()
	{
		return BulletHandler.getBulletStack(IEBullets.CASULL);
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
	public double getDefaultGravity()
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

	public record BulletData<T>(IBullet<T> bullet, T data)
	{
		public static final DualCodec<RegistryFriendlyByteBuf, BulletData<?>> CODECS = DualCodecs.RESOURCE_LOCATION
				.<RegistryFriendlyByteBuf>castStream()
				.dispatch(
						bd -> BulletHandler.findRegistryName(bd.bullet),
						rl -> specificCodec(BulletHandler.getBullet(rl))
				);

		public BulletData(IBullet<T> bullet)
		{
			this(bullet, bullet.getCodec().defaultValue());
		}

		public void onHitTarget(
				Level level, HitResult mop, UUID shooterUUID, RevolvershotEntity revolvershotEntity, boolean headshot
		)
		{
			bullet.onHitTarget(level, mop, shooterUUID, revolvershotEntity, headshot, data);
		}

		public <T1> T1 getForOptional(IBullet<T1> type)
		{
			return type==bullet?(T1)data: null;
		}

		public <T1> T1 getFor(IBullet<T1> type)
		{
			return Preconditions.checkNotNull(getForOptional(type));
		}

		private static <T> DualMapCodec<RegistryFriendlyByteBuf, BulletData<?>> specificCodec(IBullet<T> bullet)
		{
			DualCodec<? super RegistryFriendlyByteBuf, BulletData<?>> codec = bullet.getCodec().codecs().map(
					t -> new BulletData<>(bullet, t), bd -> (T)bd.data
			);
			return codec.<RegistryFriendlyByteBuf>castStream().fieldOf("data");
		}
	}
}