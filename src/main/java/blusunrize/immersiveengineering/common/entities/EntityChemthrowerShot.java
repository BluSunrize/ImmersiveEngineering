package blusunrize.immersiveengineering.common.entities;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;

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
		this.dataWatcher.addObject(dataMarker_fluid, Integer.valueOf(0));
	}

	public void setFluidSynced()
	{
		if(this.getFluid()!=null)
			this.dataWatcher.updateObject(dataMarker_fluid, Integer.valueOf(FluidRegistry.getFluidID(this.getFluid())));
	}
	public Fluid getFluidSynced()
	{
		return FluidRegistry.getFluid(this.dataWatcher.getWatchableObjectInt(dataMarker_fluid));
	}
	public Fluid getFluid()
	{
		return fluid;
	}

	@Override
	public void onEntityUpdate()
	{
		if(this.getFluid() == null && this.worldObj.isRemote)
			this.fluid = getFluidSynced();
		Block b = worldObj.getBlock((int)posX, (int)posY, (int)posZ);
		if(b!=null && (b.getMaterial()==Material.fire||b.getMaterial()==Material.lava))
			this.setFire(6);
		super.onEntityUpdate();
	}

	@Override
	public void onImpact(MovingObjectPosition mop)
	{
		if(mop.entityHit!=null && !this.worldObj.isRemote && getFluid()!=null)
		{
			ChemthrowerEffect effect = ChemthrowerHandler.getEffect(getFluid());
			if(effect!=null)
			{
				ItemStack thrower = null;
				EntityPlayer shooter = (EntityPlayer)this.getShooter();
				if(shooter!=null)
					thrower = shooter.getCurrentEquippedItem();
				effect.apply((EntityLivingBase)mop.entityHit, shooter, thrower, fluid);
			}
			else if(getFluid().getTemperature()>500)
			{
				int tempDiff = getFluid().getTemperature()-300;
				int damage = Math.abs(tempDiff)/500;
				mop.entityHit.attackEntityFrom(DamageSource.lava, damage);
			}
			if(this.isBurning())
				mop.entityHit.setFire(this.fire);
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		if(this.fluid!=null)
			nbt.setString("fluid", this.fluid.getName());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.fluid = FluidRegistry.getFluid(nbt.getString("fluid"));
	}
}