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
import blusunrize.immersiveengineering.common.util.IEFluid;
import com.google.common.base.Optional;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;


@net.minecraftforge.fml.common.Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class EntityChemthrowerShot extends EntityIEProjectile implements ILightProvider
{
	private FluidStack fluid;
	private static final DataParameter<Optional<FluidStack>> dataMarker_fluid = EntityDataManager.createKey(EntityChemthrowerShot.class, IEFluid.OPTIONAL_FLUID_STACK);

	public EntityChemthrowerShot(World world)
	{
		super(world);
	}

	public EntityChemthrowerShot(World world, double x, double y, double z, double ax, double ay, double az, FluidStack fluid)
	{
		super(world, x, y, z, ax, ay, az);
		this.fluid = fluid;
		this.setFluidSynced();
	}

	public EntityChemthrowerShot(World world, EntityLivingBase living, double ax, double ay, double az, FluidStack fluid)
	{
		super(world, living, ax, ay, az);
		this.fluid = fluid;
		this.setFluidSynced();
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(dataMarker_fluid, Optional.absent());
	}

	public void setFluidSynced()
	{
		if(this.getFluid()!=null)
			this.dataManager.set(dataMarker_fluid, Optional.of(this.getFluid()));
	}

	public FluidStack getFluidSynced()
	{
		return this.dataManager.get(dataMarker_fluid).orNull();
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
			boolean isGas = fluidStack.getFluid().isGaseous(fluidStack)||ChemthrowerHandler.isGas(fluidStack.getFluid());
			return (isGas?.025f: .05F)*(fluidStack.getFluid().getDensity(fluidStack) < 0?-1: 1);
		}
		return super.getGravity();
	}

	@Override
	public boolean canIgnite()
	{
		return ChemthrowerHandler.isFlammable(getFluid()==null?null: getFluid().getFluid());
	}

	@Override
	public void onEntityUpdate()
	{
		if(this.getFluid()==null&&this.world.isRemote)
			this.fluid = getFluidSynced();
		IBlockState state = world.getBlockState(new BlockPos(posX, posY, posZ));
		Block b = state.getBlock();
		if(b!=null&&this.canIgnite()&&(state.getMaterial()==Material.FIRE||state.getMaterial()==Material.LAVA))
			this.setFire(6);
		super.onEntityUpdate();
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
			boolean fire = fluid.getTemperature(fluidStack) > 1000;
			if(effect!=null)
			{
				ItemStack thrower = ItemStack.EMPTY;
				EntityPlayer shooter = (EntityPlayer)this.getShooter();
				if(shooter!=null)
					thrower = shooter.getHeldItem(EnumHand.MAIN_HAND);

				if(mop.typeOfHit==Type.ENTITY&&mop.entityHit instanceof EntityLivingBase)
					effect.applyToEntity((EntityLivingBase)mop.entityHit, shooter, thrower, fluidStack);
				else if(mop.typeOfHit==Type.BLOCK)
					effect.applyToBlock(world, mop, shooter, thrower, fluidStack);
			}
			else if(mop.entityHit!=null&&fluid.getTemperature(fluidStack) > 500)
			{
				int tempDiff = fluid.getTemperature(fluidStack)-300;
				int damage = Math.abs(tempDiff)/500;
				if(mop.entityHit.attackEntityFrom(DamageSource.LAVA, damage))
					mop.entityHit.hurtResistantTime = (int)(mop.entityHit.hurtResistantTime*.75);
			}
			if(mop.entityHit!=null)
			{
				int f = this.isBurning()?this.fire: fire?3: 0;
				if(f > 0)
				{
					mop.entityHit.setFire(f);
					if(mop.entityHit.attackEntityFrom(DamageSource.IN_FIRE, 2))
						mop.entityHit.hurtResistantTime = (int)(mop.entityHit.hurtResistantTime*.75);
				}
			}
		}
	}

	@Nullable
	@Override
	public Light provideLight()
	{
		FluidStack fluidStack = getFluid();
		if(fluidStack!=null)
		{
			int light = this.isBurning()?15: fluidStack.getFluid().getLuminosity(fluidStack);
			if(light > 0)
				return Light.builder().pos(this).radius(.05f*light).color(1, 1, 1).build();
		}
		return null;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender()
	{
		FluidStack fluidStack = getFluid();
		if(fluidStack!=null)
		{
			int light = this.isBurning()?15: fluidStack.getFluid().getLuminosity(fluidStack);
			int superBrightness = super.getBrightnessForRender();
			light = (superBrightness&(0xff<<20))|(light<<4);
			if(light > 0)
				return Math.max(light, superBrightness);
		}
		return super.getBrightnessForRender();
	}

	@Override
	public float getBrightness()
	{
		FluidStack fluidStack = getFluid();
		if(fluidStack!=null)
		{
			int light = this.isBurning()?15: fluidStack.getFluid().getLuminosity(fluidStack);
			if(light > 0)
				return Math.max(light, super.getBrightness());
		}
		return super.getBrightness();
	}

//	@Override
//	protected void writeEntityToNBT(NBTTagCompound nbt)
//	{
//		super.writeEntityToNBT(nbt);
//		if(this.fluid!=null)
//			nbt.setString("fluid", this.fluid.getName());
//	}
//
//	@Override
//	protected void readEntityFromNBT(NBTTagCompound nbt)
//	{
//		super.readEntityFromNBT(nbt);
//		this.fluid = FluidRegistry.getFluid(nbt.getString("fluid"));
//	}
}