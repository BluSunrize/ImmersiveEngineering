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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class EntityRevolvershot extends EntityIEProjectile
{
	public static final EntityType<EntityRevolvershot> TYPE = new Builder<>(EntityRevolvershot.class, EntityRevolvershot::new)
			.build(ImmersiveEngineering.MODID+":revolver_shot");

	private IBullet bulletType;
	public boolean bulletElectro = false;
	public ItemStack bulletPotion = ItemStack.EMPTY;

	public EntityRevolvershot(EntityType<? extends EntityRevolvershot> type, World world)
	{
		super(type, world);
		this.setSize(.125f, .125f);
	}

	public EntityRevolvershot(World world)
	{
		this(TYPE, world);
	}

	public EntityRevolvershot(EntityType<? extends EntityRevolvershot> eType, World world, double x, double y, double z,
							  double ax, double ay, double az, IBullet type)
	{
		super(eType, world, x, y, z, ax, ay, az);
		this.setSize(.125f, .125f);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		this.bulletType = type;
	}

	public EntityRevolvershot(World world, double x, double y, double z,
							  double ax, double ay, double az, IBullet type)
	{
		this(TYPE, world, x, y, z, ax, ay, az, type);
	}

	public EntityRevolvershot(World world, EntityLivingBase living, double ax, double ay, double az, IBullet type)
	{
		this(world, living, ax, ay, az, BulletHandler.findRegistryName(type));
	}

	public EntityRevolvershot(World world, EntityLivingBase living, double ax, double ay, double az, String type)
	{
		this(TYPE, world, living.posX+ax, living.posY+living.getEyeHeight()+ay, living.posZ+az, ax, ay, az, BulletHandler.getBullet(type));
		setShooterSynced();
		this.motionX = this.motionY = this.motionZ = 0.0D;
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
		if(mop.entity instanceof EntityLivingBase)
			headshot = Utils.isVecInEntityHead((EntityLivingBase)mop.entity, new Vec3d(posX, posY, posZ));

		if(this.bulletType!=null)
		{
			bulletType.onHitTarget(world, mop, this.shootingEntity, this, headshot);
			if(headshot&&mop.entity instanceof EntityAgeable&&((EntityAgeable)mop.entity).isChild()&&((EntityLivingBase)mop.entity).getHealth() <= 0)
			{
				EntityPlayer shooter = world.getPlayerEntityByUUID(shootingEntity);
				if(shooter!=null)
					Utils.unlockIEAdvancement(shooter, "main/secret_birthdayparty");
				world.playSound(null, posX, posY, posZ, IESounds.birthdayParty, SoundCategory.PLAYERS, 1.0F, 1.2F/(this.rand.nextFloat()*0.2F+0.9F));
				ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_ENTITY.with(() -> mop.entity), new MessageBirthdayParty((EntityLivingBase)mop.entity));
			}
		}
		if(!this.world.isRemote)
			this.secondaryImpact(mop);
		if(mop.type==Type.BLOCK)
		{
			IBlockState state = this.world.getBlockState(mop.getBlockPos());
			if(state.getBlock().getMaterial(state)!=Material.AIR)
				state.getBlock().onEntityCollision(state, this.world, mop.getBlockPos(), this);
		}
		this.remove();
	}


	public void secondaryImpact(RayTraceResult mop)
	{
		if(bulletElectro&&mop.entity instanceof EntityLivingBase)
		{
			EntityPlayer shooter = world.getPlayerEntityByUUID(shootingEntity);
			float percentualDrain = .15f/(bulletType==null?1: bulletType.getProjectileCount(shooter));
			((EntityLivingBase)mop.entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 15, 4));
			for(EntityEquipmentSlot slot : EntityEquipmentSlot.values())
			{
				ItemStack stack = ((EntityLivingBase)mop.entity).getItemStackFromSlot(slot);
				if(EnergyHelper.isFluxItem(stack)&&EnergyHelper.getEnergyStored(stack) > 0)
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
	public void writeAdditional(NBTTagCompound nbt)
	{
		super.writeAdditional(nbt);
		nbt.setByte("inGround", (byte)(this.inGround?1: 0));
		nbt.setString("bulletType", BulletHandler.findRegistryName(this.bulletType));
		if(!bulletPotion.isEmpty())
			nbt.setTag("bulletPotion", bulletPotion.write(new NBTTagCompound()));
	}

	@Override
	public void readAdditional(NBTTagCompound nbt)
	{
		super.readAdditional(nbt);
		this.bulletType = BulletHandler.getBullet(nbt.getString("bulletType"));
		if(nbt.hasKey("bulletPotion"))
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
}