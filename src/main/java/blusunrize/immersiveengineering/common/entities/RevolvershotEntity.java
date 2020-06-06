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
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.PacketDistributor;

public class RevolvershotEntity extends IEProjectileEntity
{
	public static final EntityType<RevolvershotEntity> TYPE = Builder
			.<RevolvershotEntity>create(RevolvershotEntity::new, EntityClassification.MISC)
			.size(0.125f, 0.125f)
			.build(ImmersiveEngineering.MODID+":revolver_shot");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "revolvershot");
	}

	private IBullet bulletType;
	public boolean bulletElectro = false;
	public ItemStack bulletPotion = ItemStack.EMPTY;
	private float gravity;
	private float movementDecay;

	public RevolvershotEntity(EntityType<? extends RevolvershotEntity> type, World world)
	{
		super(type, world);
	}

	public RevolvershotEntity(World world)
	{
		this(TYPE, world);
	}

	public RevolvershotEntity(EntityType<? extends RevolvershotEntity> eType, World world, LivingEntity shooter, double x, double y, double z,
							  double ax, double ay, double az, IBullet type)
	{
		super(eType, world, shooter, x, y, z, ax, ay, az);
		this.setPosition(x, y, z);
		this.bulletType = type;
	}

	public RevolvershotEntity(World world, double x, double y, double z,
							  double ax, double ay, double az, IBullet type)
	{
		this(TYPE, world, null, x, y, z, ax, ay, az, type);
	}

	public RevolvershotEntity(World world, LivingEntity living, double ax, double ay, double az, IBullet type)
	{
		this(TYPE, world, living, ax, ay, az, type);
	}

	public RevolvershotEntity(World world, LivingEntity living, double ax, double ay, double az, ResourceLocation type)
	{
		this(TYPE, world, living, ax, ay, az, BulletHandler.getBullet(type));
	}

	public RevolvershotEntity(EntityType<? extends RevolvershotEntity> eType, World world, LivingEntity living, double ax, double ay, double az, IBullet type)
	{
		this(eType, world, living, living.posX+ax, living.posY+living.getEyeHeight()+ay, living.posZ+az, ax, ay, az, type);
		setShooterSynced();
		setMotion(Vec3d.ZERO);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.getBoundingBox().getAverageEdgeLength()*4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1*d1;
	}

	@Override
	public void onImpact(RayTraceResult mop)
	{
		boolean headshot = false;
		if(mop instanceof EntityRayTraceResult)
		{
			Entity hitEntity = ((EntityRayTraceResult)mop).getEntity();
			if(hitEntity instanceof LivingEntity)
				headshot = Utils.isVecInEntityHead((LivingEntity)hitEntity, getPositionVec());
		}

		if(this.bulletType!=null)
		{
			bulletType.onHitTarget(world, mop, this.shootingEntity, this, headshot);
			if(mop instanceof EntityRayTraceResult)
			{
				Entity hitEntity = ((EntityRayTraceResult)mop).getEntity();
				if(headshot&&hitEntity instanceof LivingEntity&&((LivingEntity)hitEntity).isChild()&&((LivingEntity)hitEntity).getHealth() <= 0)
				{
					PlayerEntity shooter = world.getPlayerByUuid(shootingEntity);
					if(shooter!=null)
						Utils.unlockIEAdvancement(shooter, "main/secret_birthdayparty");
					world.playSound(null, posX, posY, posZ, IESounds.birthdayParty, SoundCategory.PLAYERS, 1.0F, 1.2F/(this.rand.nextFloat()*0.2F+0.9F));
					ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_ENTITY.with(() -> hitEntity), new MessageBirthdayParty((LivingEntity)hitEntity));
				}
			}
		}
		if(!this.world.isRemote)
			this.secondaryImpact(mop);
		if(mop instanceof BlockRayTraceResult)
		{
			BlockPos hitPos = ((BlockRayTraceResult)mop).getPos();
			BlockState state = this.world.getBlockState(hitPos);
			if(state.getBlock().getMaterial(state)!=Material.AIR)
				state.getBlock().onEntityCollision(state, this.world, hitPos, this);
		}
		this.remove();
	}


	public void secondaryImpact(RayTraceResult mop)
	{
		if(!(mop instanceof EntityRayTraceResult))
			return;
		Entity hitEntity = ((EntityRayTraceResult)mop).getEntity();
		if(bulletElectro&&hitEntity instanceof LivingEntity)
		{
			PlayerEntity shooter = world.getPlayerByUuid(shootingEntity);
			float percentualDrain = .15f/(bulletType==null?1: bulletType.getProjectileCount(shooter));
			((LivingEntity)hitEntity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 15, 4));
			for(EquipmentSlotType slot : EquipmentSlotType.values())
			{
				ItemStack stack = ((LivingEntity)hitEntity).getItemStackFromSlot(slot);
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

	public void onExpire()
	{

	}

	protected float getMotionFactor()
	{
		return 0.95F;
	}

	@Override
	public void writeAdditional(CompoundNBT nbt)
	{
		super.writeAdditional(nbt);
		nbt.putByte("inGround", (byte)(this.inGround?1: 0));
		nbt.putString("bulletType", BulletHandler.findRegistryName(this.bulletType).toString());
		if(!bulletPotion.isEmpty())
			nbt.put("bulletPotion", bulletPotion.write(new CompoundNBT()));
	}

	@Override
	public void readAdditional(CompoundNBT nbt)
	{
		super.readAdditional(nbt);
		this.bulletType = BulletHandler.getBullet(new ResourceLocation(nbt.getString("bulletType")));
		if(nbt.contains("bulletPotion", NBT.TAG_COMPOUND))
			this.bulletPotion = ItemStack.read(nbt.getCompound("bulletPotion"));
	}

	@Override
	public float getCollisionBorderSize()
	{
		return 1.0F;
	}

	@Override
	public float getBrightness()
	{
		return 1.0F;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getBrightnessForRender()
	{
		return 15728880;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
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