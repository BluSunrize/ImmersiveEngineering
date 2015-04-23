package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.api.DieselGeneratorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.IESound;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityDieselGenerator extends TileEntityIEBase implements IFluidHandler
{
	public int facing = 2;
	public boolean formed = false;
	public int pos;
	public FluidTank tank = new FluidTank(12000);
	public boolean active = false;

	public int[] offset = {0,0,0};
	public TileEntityDieselGenerator master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		System.out.println("searching for master at "+(xCoord-offset[0])+", "+(yCoord-offset[1])+", "+(zCoord-offset[2]));
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityDieselGenerator?(TileEntityDieselGenerator)te : null;
	}

	public static boolean _Immovable()
	{
		return true;
	}


	//409.600
	static IESound sound;
	@Override
	public void updateEntity()
	{
		if(!formed || pos!=31)
			return;

		if(worldObj.isRemote)
		{
			if(sound!=null)
			{
				if(sound.getXPosF()==xCoord && sound.getYPosF()==yCoord && sound.getZPosF()==zCoord)
				{
					if(!active)
						sound.donePlaying=true;
				}
				else
				{
					double dx = (sound.getXPosF()-ClientUtils.mc().renderViewEntity.posX)*(sound.getXPosF()-ClientUtils.mc().renderViewEntity.posX);
					double dy = (sound.getYPosF()-ClientUtils.mc().renderViewEntity.posY)*(sound.getYPosF()-ClientUtils.mc().renderViewEntity.posY);
					double dz = (sound.getZPosF()-ClientUtils.mc().renderViewEntity.posZ)*(sound.getZPosF()-ClientUtils.mc().renderViewEntity.posZ);
					double dx1 = (xCoord-ClientUtils.mc().renderViewEntity.posX)*(xCoord-ClientUtils.mc().renderViewEntity.posX);
					double dy1 = (yCoord-ClientUtils.mc().renderViewEntity.posY)*(yCoord-ClientUtils.mc().renderViewEntity.posY);
					double dz1 = (zCoord-ClientUtils.mc().renderViewEntity.posZ)*(zCoord-ClientUtils.mc().renderViewEntity.posZ);
					if((dx1+dy1+dz1)<(dx+dy+dz))
						//System.out.println(xCoord+","+yCoord+","+zCoord+" is closer");
						//System.out.println("orig pos "+sound.origPos[0]+sound.origPos[1]+sound.origPos[2] );
						sound.setPos(xCoord, yCoord, zCoord);
				}
			}
			if(active)
			{
				if(worldObj.getTotalWorldTime()%4==0)
				{
					ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
					worldObj.spawnParticle("largesmoke", xCoord+.5+fd.offsetX*1.25, yCoord+2.25, zCoord+.5+fd.offsetZ*1.25, 0,0,0);
				}
				if(sound==null || sound.isDonePlaying())
				{
					sound = new IESound(new ResourceLocation("immersiveengineering:dieselGenerator"), 1f,1f, true,0, xCoord,yCoord,zCoord, AttenuationType.LINEAR);
					ClientUtils.mc().getSoundHandler().playSound(sound);
				}
			}
		}
		else
		{
			boolean prevActive = active;
			if(tank.getFluid()!=null && tank.getFluid().getFluid()!=null)
			{
				int burnTime = DieselGeneratorHandler.getBurnTime(tank.getFluid().getFluid());
				int fluidConsumed = 1000/burnTime;
				int output = Config.getInt("dieselGen_output");
				int connected = 0;
				for(int i=0; i<3; i++)
				{
					IEnergyReceiver receiver = getOutput(i==1?-1:i==2?1:0);
					if(receiver!=null && receiver.canConnectEnergy(ForgeDirection.DOWN))
						connected++;
				}
				if(connected>0 && tank.getFluidAmount()>=fluidConsumed)
				{
					if(!active)
						active=true;
					tank.drain(fluidConsumed, true);
					int splitOutput = output/connected;
					for(int i=0; i<3; i++)
					{
						IEnergyReceiver receiver = getOutput(i==1?-1:i==2?1:0);
						if(receiver!=null && receiver.canConnectEnergy(ForgeDirection.DOWN))
							receiver.receiveEnergy(ForgeDirection.DOWN,splitOutput,false);
						//System.out.println("put out power to "+Utils.toCC(receiver)+", fuel left: "+tank.getFluidAmount());
					}

				}
				else if(active)
					active=false;
			}else if(active)
				active=false;

			if(prevActive != active)
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	IEnergyReceiver getOutput(int w)
	{
		TileEntity eTile = worldObj.getTileEntity(xCoord+(facing==4?-1:facing==5?1: w), yCoord+1, zCoord+(facing==2?-1:facing==3?1: w));
		if(eTile!=null && eTile instanceof IEnergyReceiver)
			return (IEnergyReceiver)eTile;
		return null;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt)
	{
		facing = nbt.getInteger("facing");
		formed = nbt.getBoolean("formed");
		pos = nbt.getInteger("pos");
		active = nbt.getBoolean("active");
		offset = nbt.getIntArray("offset");

		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("facing", facing);
		nbt.setBoolean("formed", formed);
		nbt.setInteger("pos", pos);
		nbt.setBoolean("active", active);
		nbt.setIntArray("offset", offset);

		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
	}



	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		System.out.println("attempt fill");
		if(!formed)
			return 0;
		if(master()!=null)
		{
			if(pos!=36&&pos!=38)
				return 0;
			System.out.println("redirecting!");
			return master().fill(from,resource,doFill);
		}
		else if(resource!=null && DieselGeneratorHandler.isValidFuel(resource.getFluid()))
		{
			System.out.println("filling at master "+pos+">"+offset[0]+","+offset[1]+","+offset[2]+":"+resource+" - "+doFill);
			int f = tank.fill(resource, doFill);
			System.out.println("contained: "+tank.getFluidAmount());
			return f;
		}
		System.out.println("IT BROKE D:");
		return 0;
	}
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
		{
			if(pos!=36&&pos!=38)
				return null;
			return master().drain(from,resource,doDrain);
		}
		else if(resource!=null)
			return drain(from, resource.amount, doDrain);
		return null;
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
		{
			if(pos!=36&&pos!=38)
				return null;
			return master().drain(from,maxDrain,doDrain);
		}
		else
			return tank.drain(maxDrain, doDrain);
	}
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if(!formed||(pos!=36&&pos!=38)||!DieselGeneratorHandler.isValidFuel(fluid))
			return false;
		return true;
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if(!formed||(pos!=36&&pos!=38))
			return false;
		return true;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(!formed)
			return new FluidTankInfo[]{};
		if(master()!=null)
			return master().getTankInfo(from);
		return new FluidTankInfo[]{tank.getInfo()};
	}


	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==31)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing==5?3: 1),yCoord-1,zCoord-(facing==3?3: 1), xCoord+(facing==4?4:2),yCoord+2,zCoord+(facing==2?4:2));

		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int il = pos/9;
			int ih = (pos%9/3)-1;
			int iw = (pos%3)-1;
			int startX = xCoord-(f==5?il: f==4?-il: iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==3?il: f==2?-il: iw);
			for(int l=0;l<5;l++)
				for(int w=-1;w<=1;w++)
					for(int h=-1;h<=(l==4?0:1);h++)
					{
						int xx = (f==5?l: f==4?-l: w);
						int yy = h;
						int zz = (f==3?l: f==2?-l: w);
						if((startX+xx!=xCoord) || (startY+yy!=yCoord) || (startZ+zz!=zCoord))
						{
							if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntityDieselGenerator)
								((TileEntityDieselGenerator)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
							worldObj.setBlock(startX+xx,startY+yy,startZ+zz, IEContent.blockMetalDecoration, l==4?BlockMetalDecoration.META_generator: l==0?BlockMetalDecoration.META_radiator: BlockMetalDecoration.META_engine, 0x3);
						}
						else
							worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, new ItemStack(IEContent.blockMetalDecoration, 1, l==4?BlockMetalDecoration.META_generator: l==0?BlockMetalDecoration.META_radiator: BlockMetalDecoration.META_engine)));
					}
		}
	}
}