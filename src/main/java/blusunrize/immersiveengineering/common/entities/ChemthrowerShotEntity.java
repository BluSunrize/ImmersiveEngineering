/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.mixin.accessors.EntityAccess;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;

public class ChemthrowerShotEntity extends IEProjectileEntity
{
	private FluidStack fluid;
	private static final EntityDataAccessor<Optional<FluidStack>> dataMarker_fluid = SynchedEntityData.defineId(ChemthrowerShotEntity.class, IEFluid.OPTIONAL_FLUID_STACK);

	public ChemthrowerShotEntity(EntityType<ChemthrowerShotEntity> type, Level world)
	{
		super(type, world);
	}

	public ChemthrowerShotEntity(Level world, double x, double y, double z, double ax, double ay, double az, FluidStack fluid)
	{
		super(IEEntityTypes.CHEMTHROWER_SHOT.get(), world, x, y, z);
		this.fluid = fluid;
		this.setFluidSynced();
		this.pickup = Pickup.DISALLOWED;
	}

	public ChemthrowerShotEntity(Level world, LivingEntity living, double ax, double ay, double az, FluidStack fluid)
	{
		super(IEEntityTypes.CHEMTHROWER_SHOT.get(), world, living, ax, ay, az);
		this.fluid = fluid;
		this.setFluidSynced();
		this.pickup = Pickup.DISALLOWED;
	}

	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(dataMarker_fluid, Optional.empty());
	}

	public void setFluidSynced()
	{
		if(this.getFluid()!=null)
			this.entityData.set(dataMarker_fluid, Optional.of(this.getFluid()));
	}

	public FluidStack getFluidSynced()
	{
		return this.entityData.get(dataMarker_fluid).orElse(null);
	}

	public FluidStack getFluid()
	{
		return fluid;
	}

	@Override
	public double getGravity()
	{
		if(getFluid()!=null)
		{
			FluidStack fluidStack = getFluid();
			boolean isGas = fluidStack.getFluid().getAttributes().isGaseous(fluidStack);
			return (isGas?.025f: .05F)*(fluidStack.getFluid().getAttributes().getDensity(fluidStack) < 0?-1: 1);
		}
		return super.getGravity();
	}

	@Override
	public boolean canIgnite()
	{
		return ChemthrowerHandler.isFlammable(getFluid()==null?null: getFluid().getFluid());
	}

	@Override
	public void baseTick()
	{
		if(this.getFluid()==null&&this.level.isClientSide)
			this.fluid = getFluidSynced();
		BlockState state = level.getBlockState(blockPosition());
		Block b = state.getBlock();
		if(b!=null&&this.canIgnite()&&(state.getMaterial()==Material.FIRE||state.getMaterial()==Material.LAVA))
			this.setSecondsOnFire(6);
		super.baseTick();
	}

	@Override
	public void setSecondsOnFire(int seconds)
	{
		if(!canIgnite())
			return;
		super.setSecondsOnFire(seconds);
	}

	@Override
	public void onHit(HitResult mop)
	{
		if(!this.level.isClientSide&&getFluid()!=null)
		{
			FluidStack fluidStack = getFluid();
			Fluid fluid = fluidStack.getFluid();
			ChemthrowerEffect effect = ChemthrowerHandler.getEffect(fluid);
			boolean fire = fluid.getAttributes().getTemperature(fluidStack) > 1000;
			if(effect!=null)
			{
				ItemStack thrower = ItemStack.EMPTY;
				Player shooter = (Player)this.getOwner();
				if(shooter!=null)
					thrower = shooter.getItemInHand(InteractionHand.MAIN_HAND);

				if(mop.getType()==Type.ENTITY&&((EntityHitResult)mop).getEntity() instanceof LivingEntity)
					effect.applyToEntity((LivingEntity)((EntityHitResult)mop).getEntity(), shooter, thrower, fluidStack);
				else if(mop.getType()==Type.BLOCK)
					effect.applyToBlock(level, mop, shooter, thrower, fluidStack);
			}
			else if(mop.getType()==Type.ENTITY&&fluid.getAttributes().getTemperature(fluidStack) > 500)
			{
				int tempDiff = fluid.getAttributes().getTemperature(fluidStack)-300;
				int damage = Math.abs(tempDiff)/500;
				Entity hit = ((EntityHitResult)mop).getEntity();
				if(hit.hurt(DamageSource.LAVA, damage))
					hit.invulnerableTime = (int)(hit.invulnerableTime*.75);
			}
			if(mop.getType()==Type.ENTITY)
			{
				int f = this.isOnFire()?((EntityAccess)this).getRemainingFireTicks(): fire?3: 0;
				if(f > 0)
				{
					Entity hit = ((EntityHitResult)mop).getEntity();
					hit.setSecondsOnFire(f);
					if(hit.hurt(DamageSource.IN_FIRE, 2))
						hit.invulnerableTime = (int)(hit.invulnerableTime*.75);
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender()
	{
		FluidStack fluidStack = getFluid();
		if(fluidStack!=null)
		{
			int light = this.isOnFire()?15: fluidStack.getFluid().getAttributes().getLuminosity(fluidStack);
			int superBrightness = 0;
			light = (superBrightness&(0xff<<20))|(light<<4);
			if(light > 0)
				return Math.max(light, superBrightness);
		}
		return 0;
	}

	@Override
	public float getBrightness()
	{
		FluidStack fluidStack = getFluid();
		if(fluidStack!=null)
		{
			int light = this.isOnFire()?15: fluidStack.getFluid().getAttributes().getLuminosity(fluidStack);
			if(light > 0)
				return Math.max(light, super.getBrightness());
		}
		return super.getBrightness();
	}
}