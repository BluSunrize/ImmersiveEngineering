package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class EntityChemthrowerShot extends EntityIEProjectile
{
	private Fluid fluid;
	final static int dataMarker_fluid = 13;

	public EntityChemthrowerShot(World world)
	{
		super(world);
	}
	public EntityChemthrowerShot(World world, double x, double y, double z, double ax, double ay, double az, Fluid fluid)
	{
		super(world, x,y,z, ax,ay,az);
		this.fluid = fluid;
		this.setFluidSynced();
	}
	public EntityChemthrowerShot(World world, EntityLivingBase living, double ax, double ay, double az, Fluid fluid)
	{
		super(world, living, ax, ay, az);
		this.fluid = fluid;
		this.setFluidSynced();
	}
	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(dataMarker_fluid, "");
	}

	public void setFluidSynced()
	{
		if(this.getFluid()!=null)
			this.dataWatcher.updateObject(dataMarker_fluid, this.getFluid().getName());
	}
	public Fluid getFluidSynced()
	{
		return FluidRegistry.getFluid(this.dataWatcher.getWatchableObjectString(dataMarker_fluid));
	}
	public Fluid getFluid()
	{
		return fluid;
	}

	@Override
	public double getGravity()
	{
		if(getFluid()!=null)
		{
			boolean isGas = getFluid().isGaseous()||ChemthrowerHandler.isGas(getFluid());
			return (isGas?.025f:.05F) * (getFluid().getDensity()<0?-1:1);
		}
		return super.getGravity();
	}

	@Override
	public boolean canIgnite()
	{
		return ChemthrowerHandler.isFlammable(getFluid());
	}

	@Override
	public void onEntityUpdate()
	{
		if(this.getFluid() == null && this.worldObj.isRemote)
			this.fluid = getFluidSynced();
		IBlockState state = worldObj.getBlockState(new BlockPos(posX,posY,posZ));
		Block b = state.getBlock();
		if(b!=null && this.canIgnite() && (b.getMaterial()==Material.fire||b.getMaterial()==Material.lava))
			this.setFire(6);
		super.onEntityUpdate();
	}

	@Override
	public void onImpact(MovingObjectPosition mop)
	{
		if(!this.worldObj.isRemote && getFluid()!=null)
		{
			ChemthrowerEffect effect = ChemthrowerHandler.getEffect(getFluid());
			boolean fire = getFluid().getTemperature()>1000;
			if(effect!=null)
			{
				ItemStack thrower = null;
				EntityPlayer shooter = (EntityPlayer)this.getShooter();
				if(shooter!=null)
					thrower = shooter.getCurrentEquippedItem();

				if(mop.typeOfHit==MovingObjectType.ENTITY)
					effect.applyToEntity((EntityLivingBase)mop.entityHit, shooter, thrower, fluid);
				else if(mop.typeOfHit==MovingObjectType.BLOCK)
					effect.applyToBlock(worldObj, mop, shooter, thrower, fluid);
			}
			else if(mop.entityHit!=null && getFluid().getTemperature()>500)
			{
				int tempDiff = getFluid().getTemperature()-300;
				int damage = Math.abs(tempDiff)/500;
				if(mop.entityHit.attackEntityFrom(DamageSource.lava, damage))
					mop.entityHit.hurtResistantTime = (int)(mop.entityHit.hurtResistantTime*.75);
			}
			if(mop.entityHit!=null)
			{
				int f = this.isBurning()?this.fire: fire?3: 0;
				if(f>0)
				{
					mop.entityHit.setFire(f);
					if(mop.entityHit.attackEntityFrom(DamageSource.inFire, 2))
						mop.entityHit.hurtResistantTime = (int)(mop.entityHit.hurtResistantTime*.75);
				}
			}
		}
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