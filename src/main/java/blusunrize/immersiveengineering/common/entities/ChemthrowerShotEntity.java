/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;

public class ChemthrowerShotEntity extends IEProjectileEntity
{
	private FluidStack fluid;
	private static final DataParameter<Optional<FluidStack>> dataMarker_fluid = EntityDataManager.createKey(ChemthrowerShotEntity.class, IEFluid.OPTIONAL_FLUID_STACK);
	public static final EntityType<ChemthrowerShotEntity> TYPE = Builder
			.<ChemthrowerShotEntity>create(ChemthrowerShotEntity::new, EntityClassification.MISC)
			.size(0.1F, 0.1F)
			.build(ImmersiveEngineering.MODID+":chemthrower_shot");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "chemthrower_shot");
	}

	public ChemthrowerShotEntity(EntityType<ChemthrowerShotEntity> type, World world)
	{
		super(type, world);
	}

	public ChemthrowerShotEntity(World world, double x, double y, double z, double ax, double ay, double az, FluidStack fluid)
	{
		super(TYPE, world, x, y, z);
		this.fluid = fluid;
		this.setFluidSynced();
	}

	public ChemthrowerShotEntity(World world, LivingEntity living, double ax, double ay, double az, FluidStack fluid)
	{
		super(TYPE, world, living, ax, ay, az);
		this.fluid = fluid;
		this.setFluidSynced();
	}

	@Override
	protected void registerData()
	{
		super.registerData();
		this.dataManager.register(dataMarker_fluid, Optional.empty());
	}

	public void setFluidSynced()
	{
		if(this.getFluid()!=null)
			this.dataManager.set(dataMarker_fluid, Optional.of(this.getFluid()));
	}

	public FluidStack getFluidSynced()
	{
		return this.dataManager.get(dataMarker_fluid).orElse(null);
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
		if(this.getFluid()==null&&this.world.isRemote)
			this.fluid = getFluidSynced();
		BlockState state = world.getBlockState(getPosition());
		Block b = state.getBlock();
		if(b!=null&&this.canIgnite()&&(state.getMaterial()==Material.FIRE||state.getMaterial()==Material.LAVA))
			this.setFire(6);
		super.baseTick();
	}

	@Override
	public void setFire(int seconds)
	{
		if(!canIgnite())
			return;
		super.setFire(seconds);
	}

	@Override
	public void onImpact(RayTraceResult mop)
	{
		if(!this.world.isRemote&&getFluid()!=null)
		{
			FluidStack fluidStack = getFluid();
			Fluid fluid = fluidStack.getFluid();
			ChemthrowerEffect effect = ChemthrowerHandler.getEffect(fluid);
			boolean fire = fluid.getAttributes().getTemperature(fluidStack) > 1000;
			if(effect!=null)
			{
				ItemStack thrower = ItemStack.EMPTY;
				PlayerEntity shooter = (PlayerEntity)this.func_234616_v_();
				if(shooter!=null)
					thrower = shooter.getHeldItem(Hand.MAIN_HAND);

				if(mop.getType()==Type.ENTITY&&((EntityRayTraceResult)mop).getEntity() instanceof LivingEntity)
					effect.applyToEntity((LivingEntity)((EntityRayTraceResult)mop).getEntity(), shooter, thrower, fluidStack);
				else if(mop.getType()==Type.BLOCK)
					effect.applyToBlock(world, mop, shooter, thrower, fluidStack);
			}
			else if(mop.getType()==Type.ENTITY&&fluid.getAttributes().getTemperature(fluidStack) > 500)
			{
				int tempDiff = fluid.getAttributes().getTemperature(fluidStack)-300;
				int damage = Math.abs(tempDiff)/500;
				Entity hit = ((EntityRayTraceResult)mop).getEntity();
				if(hit.attackEntityFrom(DamageSource.LAVA, damage))
					hit.hurtResistantTime = (int)(hit.hurtResistantTime*.75);
			}
			if(mop.getType()==Type.ENTITY)
			{
				int f = this.isBurning()?this.fire: fire?3: 0;
				if(f > 0)
				{
					Entity hit = ((EntityRayTraceResult)mop).getEntity();
					hit.setFire(f);
					if(hit.attackEntityFrom(DamageSource.IN_FIRE, 2))
						hit.hurtResistantTime = (int)(hit.hurtResistantTime*.75);
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
			int light = this.isBurning()?15: fluidStack.getFluid().getAttributes().getLuminosity(fluidStack);
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
			int light = this.isBurning()?15: fluidStack.getFluid().getAttributes().getLuminosity(fluidStack);
			if(light > 0)
				return Math.max(light, super.getBrightness());
		}
		return super.getBrightness();
	}
}