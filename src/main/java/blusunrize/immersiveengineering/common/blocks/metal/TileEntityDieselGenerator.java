package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockDieselGenerator;
import blusunrize.immersiveengineering.common.util.IESound;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityDieselGenerator extends TileEntityMultiblockPart implements IFluidHandler, ISoundTile, IEnergyConnection
{
	public int facing = 2;
	public FluidTank tank = new FluidTank(8000);
	public boolean active = false;

	public float fanRotationStep=0;
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
		if(pos<0)
			return null;
		ItemStack s = MultiblockDieselGenerator.instance.getStructureManual()[pos%9/3][4-pos/9][pos%3];
		return s!=null?s.copy():null;
	}


	static IESound sound;
	@Override
	public void updateEntity()
	{
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
			fanRotationStep = step;
			fanRotation += step;
			fanRotation %= 360;
		}

		//		worldObj.spawnParticle("reddust", xCoord+(facing==4||facing==(mirrored?2:3)?1:-1)+.5, yCoord+.5, zCoord+(facing==2||facing==(mirrored?5:4)?1:-1)+.5, 0,0,0);

		if(worldObj.isRemote)
		{
			ImmersiveEngineering.proxy.handleTileSound("dieselGenerator", this, active, .5f,1);
			if(active && worldObj.getTotalWorldTime()%4==0)
				worldObj.spawnParticle("largesmoke", xCoord+.5+(facing==4?1.25:facing==5?-1.25: facing==(mirrored?2:3)?.625:-.625), yCoord+2.25, zCoord+.5+(facing==2?1.25:facing==3?-1.25: facing==(mirrored?5:4)?.625:-.625), 0,0,0);
		}
		else
		{
			boolean prevActive = active;
			boolean rs = worldObj.isBlockIndirectlyGettingPowered(xCoord+(facing==4||facing==(mirrored?2:3)?1:-1), yCoord, zCoord+(facing==2||facing==(mirrored?5:4)?1:-1));

			if(!rs && tank.getFluid()!=null && tank.getFluid().getFluid()!=null)
			{
				int burnTime = DieselHandler.getBurnTime(tank.getFluid().getFluid());
				int fluidConsumed = 1000/burnTime;
				int output = Config.getInt("dieselGen_output");
				int connected = 0;
				for(int i=0; i<3; i++)
				{
					IEnergyReceiver receiver = getOutput(i==1?-1:i==2?1:0);
					if(receiver!=null && receiver.canConnectEnergy(ForgeDirection.DOWN) && receiver.receiveEnergy(ForgeDirection.DOWN,4096,true)>0)
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
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		active = nbt.getBoolean("active");

		fanRotation = nbt.getFloat("fanRotation");
		fanFadeIn = nbt.getInteger("fanFadeIn");
		fanFadeOut = nbt.getInteger("fanFadeOut");

		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
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
		if(!canDrain(from, resource!=null?resource.getFluid():null))
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


	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==31)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing==5?3: 1),yCoord-1,zCoord-(facing==3?3: 1), xCoord+(facing==4?4:2),yCoord+2,zCoord+(facing==2?4:2));

		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}
	@Override
	public float[] getBlockBounds()
	{
		boolean mirror = master()!=null?master().mirrored:mirrored;
		if(pos>=3 && pos<36)
		{
			float height = pos==24||pos==26?1: pos%9>=6&&pos>9?.375f: 1;
			if(pos==9||pos==11||pos==27||pos==29)
				return new float[]{0,0,0,1,1,1};
			else if(pos==34)
				return new float[]{(facing==4?.375f:0),0,(facing==2?.375f:0),  (facing==5?.625f:1),height,(facing==3?.625f:1)};
			else if(pos%9==0||pos%9==3||pos%9==6)
			{
				if(pos==33)
					return new float[]{(facing==2?.5f:facing==4?.375f:0),0,(facing==5?.5f:facing==2?.375f:0),  (facing==3?.5f:facing==5?.625f:1),height,(facing==4?.5f:facing==3?.625f:1)};
				else if(pos==18)
					return new float[]{(facing==2?.4375f:0),0,(facing==5?.4375f:0),  (facing==3?.5625f:1),height,(facing==4?.5625f:1)};
				else if(pos==21 && !mirror)
					return new float[]{0,0,0, 1,1,1};
				else
					return new float[]{(facing==2?.5f:0),0,(facing==5?.5f:0),  (facing==3?.5f:1),height,(facing==4?.5f:1)};
			}
			else if(pos%9==2||pos%9==5||pos%9==8)
			{
				if(pos==35)
					return new float[]{(facing==3?.5f:facing==4?.375f:0),0,(facing==4?.5f:facing==2?.375f:0),  (facing==2?.5f:facing==5?.625f:1),height,(facing==5?.5f:facing==3?.625f:1)};
				else if(pos==20)
					return new float[]{(facing==3?.4375f:0),0,(facing==4?.4375f:0),  (facing==2?.5625f:1),height,(facing==5?.5625f:1)};
				else if(pos==23 && mirror)
					return new float[]{0,0,0, 1,1,1};
				else
					return new float[]{(facing==3?.5f:0),0,(facing==4?.5f:0),  (facing==2?.5f:1),height,(facing==5?.5f:1)};
			}
			else
				return new float[]{0,0,0,  1,height,1};
		}
		else if(pos==36 || pos==38)
			return new float[]{(facing==(pos==36?3:2)?.5f:0),0,(facing==(pos==36?4:5)?.5f:0),  (facing==(pos==36?2:3)?.5f:1),1,(facing==(pos==36?5:4)?.5f:1)};
		else if(pos==37)
			return new float[]{0,.5f,0,1,1,1};
		else
			return new float[]{0,0,0,1,1,1};
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

		ImmersiveEngineering.proxy.stopTileSound("dieselGenerator", this);

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int il = pos/9;
			int ih = (pos%9/3)-1;
			int iw = (pos%3)-1;
			int startX = xCoord-(f==5?il: f==4?-il: f==2?iw: -iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==3?il: f==2?-il: f==5?iw: -iw);
			for(int l=0;l<5;l++)
				for(int w=-1;w<=1;w++)
					for(int h=-1;h<=(l==4?0:1);h++)
					{
						int xx = (f==5?l: f==4?-l: f==2?-w: w);
						int yy = h;
						int zz = (f==3?l: f==2?-l: f==5?-w: w);

						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
						if(te instanceof TileEntityDieselGenerator)
						{
							s = ((TileEntityDieselGenerator)te).getOriginalBlock();
							((TileEntityDieselGenerator)te).formed=false;
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
	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return (pos>=38&&pos<=41) && from==ForgeDirection.UP;
	}
}