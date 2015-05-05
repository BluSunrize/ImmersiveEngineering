package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
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
import blusunrize.immersiveengineering.api.DieselHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockDieselGenerator;
import blusunrize.immersiveengineering.common.util.IESound;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityDieselGenerator extends TileEntityMultiblockPart implements IFluidHandler
{
	public int facing = 2;
	public FluidTank tank = new FluidTank(12000);
	public boolean active = false;

	public float fanRotation=0;
	public int fanFadeIn=0;
	public int fanFadeOut=0;

	public TileEntityDieselGenerator master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityDieselGenerator?(TileEntityDieselGenerator)te : null;
	}
	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		ItemStack s = MultiblockDieselGenerator.instance.getStructureManual()[pos%9/3][4-pos/9][pos%3];
		return s!=null?s.copy():null;
	}


	static IESound sound;
	@Override
	public void updateEntity()
	{
//		if(pos==0)
//		{
//			System.out.println("My pos: "+pos+", master: "+master());
//			worldObj.spawnParticle("smoke", xCoord, yCoord, zCoord, 0, 0, 0);
//			System.out.println("Master search: "+offset[0]+","+offset[1]+","+offset[2]);
			
//		}
		if(!formed || pos!=31)
			return;

		if(active || fanFadeIn>0 || fanFadeOut>0)
		{
			float base = 18f;
			float step = active?base:0;
			if(fanFadeIn>0)
			{
				step -= (fanFadeIn/80f)*base;
				fanFadeIn--;
			}
			if(fanFadeOut>0)
			{
				step += (fanFadeOut/80f)*base;
				fanFadeOut--;
			}
			fanRotation += step;
			fanRotation %= 360;
		}

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
				int burnTime = DieselHandler.getBurnTime(tank.getFluid().getFluid());
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
					{
						active=true;
						fanFadeIn = 80;
					}
					tank.drain(fluidConsumed, true);
					int splitOutput = output/connected;
					for(int i=0; i<3; i++)
					{
						IEnergyReceiver receiver = getOutput(i==1?-1:i==2?1:0);
						if(receiver!=null && receiver.canConnectEnergy(ForgeDirection.DOWN))
							receiver.receiveEnergy(ForgeDirection.DOWN,splitOutput,false);
					}

				}
				else if(active)
				{
					active=false;
					fanFadeOut=80;
				}
			}
			else if(active)
			{
				active=false;
				fanFadeOut=80;
			}

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
		super.readCustomNBT(nbt);
		facing = nbt.getInteger("facing");
		active = nbt.getBoolean("active");

		fanRotation = nbt.getFloat("fanRotation");
		fanFadeIn = nbt.getInteger("fanFadeIn");
		fanFadeOut = nbt.getInteger("fanFadeOut");

		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt)
	{
		super.writeCustomNBT(nbt);
		nbt.setInteger("facing", facing);
		nbt.setBoolean("active", active);

		nbt.setFloat("fanRotation", fanRotation);
		nbt.setInteger("fanFadeIn", fanFadeIn);
		nbt.setInteger("fanFadeOut", fanFadeOut);

		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
	}



	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(!formed)
			return 0;
		if(master()!=null)
		{
			if(pos!=36&&pos!=38)
				return 0;
			return master().fill(from,resource,doFill);
		}
		else if(resource!=null && DieselHandler.isValidFuel(resource.getFluid()))
		{
			int f = tank.fill(resource, doFill);
			return f;
		}
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
		return formed && pos==36||pos==38 && DieselHandler.isValidFuel(fluid);
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return formed && pos==36||pos==38;
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
		//
		//		if(formed && !worldObj.isRemote)
		//		{
		//			int f = facing;
		//			int il = pos/9;
		//			int ih = (pos%9/3)-1;
		//			int iw = (pos%3)-1;
		//			int startX = xCoord-(f==4?il: f==5?-il: f==2?-iw: iw);
		//			int startY = yCoord-ih;
		//			int startZ = zCoord-(f==2?il: f==3?-il: f==5?-iw: iw);
		//			for(int l=0;l<3;l++)
		//				for(int w=-1;w<=1;w++)
		//					for(int h=-1;h<=1;h++)
		//					{
		//						int xx = (f==4?l: f==5?-l: f==2?-w: w);
		//						int yy = h;
		//						int zz = (f==2?l: f==3?-l: f==5?-w: w);
		//
		//						ItemStack s = null;
		//						if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntityFermenter)
		//						{
		//							s = ((TileEntityFermenter)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).getOriginalBlock();
		//							((TileEntityFermenter)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
		//						}
		//						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
		//							s = this.getOriginalBlock();
		//						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
		//						{
		//							if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
		//								worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
		//							else
		//							{
		//								if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
		//									worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
		//								worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
		//							}
		//						}
		//					}
		//		}

//		if(sound!=null && sound.getXPosF()==xCoord && sound.getYPosF()==yCoord && sound.getZPosF()==zCoord)
//			sound.donePlaying=true;

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int il = pos/9;
			int ih = (pos%9/3)-1;
			int iw = (pos%3)-1;
			int startX = xCoord-(f==5?il: f==4?-il: f==2?iw: -iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==3?il: f==2?-il: f==5?iw: -iw);
			System.out.println("break start: "+startX+","+startY+","+startZ);
			for(int l=0;l<5;l++)
				for(int w=-1;w<=1;w++)
					for(int h=-1;h<=(l==4?0:1);h++)
					{
						int xx = (f==5?l: f==4?-l: f==2?-w: w);
						int yy = h;
						int zz = (f==3?l: f==2?-l: f==5?-w: w);

						ItemStack s = null;
						if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntityDieselGenerator)
						{
							s = ((TileEntityDieselGenerator)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).getOriginalBlock();
							((TileEntityDieselGenerator)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
						}
						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
							else
							{
								if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
									worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
								worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
							}
						}
					}
		}
	}
}