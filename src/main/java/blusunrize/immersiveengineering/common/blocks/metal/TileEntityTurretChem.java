package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot;
import blusunrize.immersiveengineering.common.util.IESounds;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

public class TileEntityTurretChem extends TileEntityTurret
{
	public FluidTank tank = new FluidTank(4000);
	public boolean ignite = false;

	@Override
	protected double getRange()
	{
		return 8;
	}
	@Override
	protected boolean canActivate()
	{
		return tank.getFluidAmount()>0 && this.energyStorage.getEnergyStored()>= IEConfig.Machines.turret_chem_consumption;
	}
	@Override
	protected int getChargeupTicks()
	{
		return 10;
	}
	@Override
	protected int getActiveTicks()
	{
		return 1;
	}
	@Override
	protected boolean loopActivation()
	{
		return true;
	}
	@Override
	protected void activate()
	{
		FluidStack fs = this.tank.getFluid();
		if(fs!=null && fs.getFluid()!=null)
		{
			int consumed = IEConfig.Tools.chemthrower_consumption;
			int energy = IEConfig.Machines.turret_chem_consumption;
			if(consumed<=fs.amount && this.energyStorage.extractEnergy(energy, true)>=energy)
			{
				tank.drain(consumed, true);
				this.energyStorage.extractEnergy(energy, false);
				double dX = target.posX-(getPos().getX()+.5);
				double dY = target.posY-(getPos().getY()+.5);
				double dZ = target.posZ-(getPos().getZ()+.5);
				Vec3d v = new Vec3d(dX,dY,dZ).normalize();

				int split = 8;
				boolean isGas = fs.getFluid().isGaseous()||ChemthrowerHandler.isGas(fs.getFluid());

				float scatter = isGas?.15f: .05f;
				float range = isGas?.5f: 1f;
//				if(getUpgrades(stack).getBoolean("focus"))
//				{
//					range += .25f;
//					scatter -= .025f;
//				}
				boolean ignite = ChemthrowerHandler.isFlammable(fs.getFluid())&&this.ignite;
				for(int i = 0; i < split; i++)
				{
					Vec3d vecDir = v.addVector(world.rand.nextGaussian()*scatter, world.rand.nextGaussian()*scatter, world.rand.nextGaussian()*scatter);
					EntityChemthrowerShot chem = new EntityChemthrowerShot(world, getPos().getX()+.5+v.xCoord*0.875,getPos().getY()+1.5+v.yCoord*0.875,getPos().getZ()+.5+v.zCoord*0.875, 0,0,0, fs);
					chem.motionX = vecDir.xCoord*range;
					chem.motionY = vecDir.yCoord*range;
					chem.motionZ = vecDir.zCoord*range;
					if(ignite)
						chem.setFire(10);
					if(!world.isRemote)
						world.spawnEntity(chem);
				}
				if(tick%4==0)
					if(ignite)
						world.playSound(null, getPos(), IESounds.sprayFire, SoundCategory.BLOCKS, .5F,1.5F);
					else
						world.playSound(null, getPos(), IESounds.spray, SoundCategory.BLOCKS, .5F,.75F);
			}
		}
	}


	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		super.receiveMessageFromClient(message);
		if(message.hasKey("ignite"))
			ignite = message.getBoolean("ignite");
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		ignite = nbt.getBoolean("ignite");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		nbt.setBoolean("ignite", ignite);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if(!dummy && capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing==null||facing==EnumFacing.DOWN||facing==this.facing.getOpposite()))
			return true;
		return super.hasCapability(capability, facing);
	}
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(!dummy && capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing==null||facing==EnumFacing.DOWN||facing==this.facing.getOpposite()))
			return (T)tank;
		return super.getCapability(capability, facing);
	}
}