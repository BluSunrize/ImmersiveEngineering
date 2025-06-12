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
import blusunrize.immersiveengineering.common.register.IEEntityDataSerializers;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class ChemthrowerShotEntity extends IEProjectileEntity
{
	@Nonnull
	private FluidStack fluid = FluidStack.EMPTY;
	private static final EntityDataAccessor<FluidStack> dataMarker_fluid = SynchedEntityData.defineId(
			ChemthrowerShotEntity.class, IEEntityDataSerializers.FLUID_STACK.get()
	);

	public ChemthrowerShotEntity(EntityType<ChemthrowerShotEntity> type, Level world)
	{
		super(type, world);
	}

	public ChemthrowerShotEntity(Level world, double x, double y, double z, @Nonnull FluidStack fluid)
	{
		super(IEEntityTypes.CHEMTHROWER_SHOT.get(), world, x, y, z);
		this.fluid = fluid;
		this.setFluidSynced();
		this.pickup = Pickup.DISALLOWED;
	}

	public ChemthrowerShotEntity(Level world, LivingEntity living, double ax, double ay, double az, @Nonnull FluidStack fluid)
	{
		super(IEEntityTypes.CHEMTHROWER_SHOT.get(), world, living, ax, ay, az);
		this.fluid = fluid;
		this.setFluidSynced();
		this.pickup = Pickup.DISALLOWED;
	}

	@Override
	protected void defineSynchedData(Builder builder)
	{
		super.defineSynchedData(builder);
		builder.define(dataMarker_fluid, FluidStack.EMPTY);
	}

	public void setFluidSynced()
	{
		this.entityData.set(dataMarker_fluid, this.getFluid());
	}

	@Nonnull
	public FluidStack getFluidSynced()
	{
		return this.entityData.get(dataMarker_fluid);
	}

	@Nonnull
	public FluidStack getFluid()
	{
		return fluid;
	}

	@Override
	public double getDefaultGravity()
	{
		if(getFluid().isEmpty())
			return super.getDefaultGravity();
		FluidStack fluidStack = getFluid();
		boolean isGas = fluidStack.getFluid().is(Tags.Fluids.GASEOUS);
		return (isGas?.025f: .05F)*(fluidStack.getFluid().getFluidType().getDensity(fluidStack) < 0?-1: 1);
	}

	@Override
	public boolean canIgnite()
	{
		return ChemthrowerHandler.isFlammable(getFluid().getFluid());
	}

	@Override
	public void baseTick()
	{
		if(this.level().isClientSide)
			this.fluid = getFluidSynced();
		BlockState state = level().getBlockState(blockPosition());
		// TODO this is a very rough port of the previous material-based check
		if(this.canIgnite()&&(state.is(BlockTags.FIRE)||state.getFluidState().is(FluidTags.LAVA)))
			this.igniteForSeconds(6);
		super.baseTick();
	}

	@Override
	public void igniteForTicks(int p_320711_)
	{
		if(!canIgnite())
			return;
		super.igniteForTicks(p_320711_);
	}

	@Override
	public void onHit(HitResult mop)
	{
		if(this.level().isClientSide||getFluid().isEmpty())
			return;
		FluidStack fluidStack = getFluid();
		Fluid fluid = fluidStack.getFluid();
		ChemthrowerEffect effect = ChemthrowerHandler.getEffect(fluid);
		boolean fire = fluid.getFluidType().getTemperature(fluidStack) > 1000;
		if(effect!=null)
		{
			ItemStack thrower = ItemStack.EMPTY;
			Player shooter = (Player)this.getOwner();
			if(shooter!=null)
				thrower = shooter.getItemInHand(InteractionHand.MAIN_HAND);

			if(mop.getType()==Type.ENTITY&&((EntityHitResult)mop).getEntity() instanceof LivingEntity)
				effect.applyToEntity((LivingEntity)((EntityHitResult)mop).getEntity(), shooter, this, thrower, fluidStack);
			else if(mop.getType()==Type.BLOCK)
				effect.applyToBlock(level(), mop, shooter, this, thrower, fluidStack);
		}
		else if(mop.getType()==Type.ENTITY&&fluid.getFluidType().getTemperature(fluidStack) > 500)
		{
			int tempDiff = fluid.getFluidType().getTemperature(fluidStack)-300;
			int damage = Math.abs(tempDiff)/500;
			Entity hit = ((EntityHitResult)mop).getEntity();
			if(hit.hurt(hit.damageSources().lava(), damage))
				hit.invulnerableTime = (int)(hit.invulnerableTime*.75);
		}
		if(mop.getType()==Type.ENTITY)
		{
			int f = this.isOnFire()?getRemainingFireTicks(): fire?3: 0;
			if(f > 0)
			{
				Entity hit = ((EntityHitResult)mop).getEntity();
				hit.igniteForSeconds(f);
				if(hit.hurt(hit.damageSources().inFire(), 2))
					hit.invulnerableTime = (int)(hit.invulnerableTime*.75);
			}
		}
	}

	public int getBrightnessForRender()
	{
		FluidStack fluidStack = getFluid();
		if(!fluidStack.isEmpty())
		{
			int light = this.isOnFire()?15: fluidStack.getFluid().getFluidType().getLightLevel(fluidStack);
			int superBrightness = 0;
			light = (superBrightness&(0xff<<20))|(light<<4);
			if(light > 0)
				return light;
		}
		return 0;
	}

	@Nonnull
	@Override
	protected ItemStack getDefaultPickupItem()
	{
		return Items.WATER_BUCKET.getDefaultInstance();
	}
}