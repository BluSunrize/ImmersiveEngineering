package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySheetmetalTank extends TileEntityMultiblockPart<TileEntitySheetmetalTank> implements IFluidHandler, IBlockOverlayText, IPlayerInteraction, IComparatorOverride
{
	public FluidTank tank = new FluidTank(512000);
	private int[] oldComps = new int[4];
	private int masterCompOld;

	//	@Override
	//	public TileEntitySheetmetalTank master()
	//	{
	//		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
	//			return null;
	//		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
	//		return te instanceof TileEntitySheetmetalTank?(TileEntitySheetmetalTank)te : null;
	//	}

	@Override
	public String[] getOverlayText(EntityPlayer player, MovingObjectPosition mop, boolean hammer)
	{
		if(Utils.isFluidRelatedItemStack(player.getCurrentEquippedItem()))
		{
			FluidStack fs = master()!=null?master().tank.getFluid():this.tank.getFluid();
			String s = null;
			if(fs!=null)
				s = fs.getLocalizedName()+": "+fs.amount+"mB";
			else
				s = StatCollector.translateToLocal(Lib.GUI+"empty");
			return new String[]{s};
		}
		return null;
	}
	@Override
	public boolean useNixieFont(EntityPlayer player, MovingObjectPosition mop)
	{
		return false;
	}

	@Override
	public void update()
	{
		if(pos==4 && !worldObj.isRemote && worldObj.isBlockIndirectlyGettingPowered(getPos())>0)
			for(int i=0; i<6; i++)
				if(i!=1 && tank.getFluidAmount()>0)
				{
					EnumFacing f = EnumFacing.getFront(i);
					int out = Math.min(144,tank.getFluidAmount());
					TileEntity te = this.worldObj.getTileEntity(getPos().offset(f));
					if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(f.getOpposite(), tank.getFluid().getFluid()))
					{
						updateComparatorValuesPart1();
						int accepted = ((IFluidHandler)te).fill(f.getOpposite(), new FluidStack(tank.getFluid().getFluid(),out), false);
						FluidStack drained = this.tank.drain(accepted, true);
						((IFluidHandler)te).fill(f.getOpposite(), drained, true);
						worldObj.markBlockForUpdate(getPos());
						updateComparatorValuesPart2();
					}
				}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos==9)
			return new float[]{.375f,0,.375f,.625f,1,.625f};
		if(pos==0||pos==2||pos==6||pos==8)
			return new float[]{.375f,0,.375f,.625f,1,.625f};
		return new float[]{0,0,0,1,1,1};
	}
	@Override
	public float[] getSpecialCollisionBounds()
	{
		return null;
	}
	@Override
	public float[] getSpecialSelectionBounds()
	{
		return null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		return pos==0||pos==2||pos==6||pos==8?new ItemStack(IEContent.blockWoodenDecoration,1,BlockTypes_WoodenDecoration.FENCE.getMeta()):new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.IRON.getMeta());
	}

	@Override
	public void disassemble()
	{
		super.invalidate();
		if(formed && !worldObj.isRemote)
		{
			BlockPos startPos = this.getPos().add(-offset[0],-offset[1],-offset[2]);
			if(!(offset[0]==0&&offset[1]==0&&offset[2]==0) && !(worldObj.getTileEntity(startPos) instanceof TileEntitySheetmetalTank))
				return;

			for(int yy=0;yy<=4;yy++)
				for(int xx=-1;xx<=1;xx++)
					for(int zz=-1;zz<=1;zz++)
					{
						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startPos.add(xx, yy, zz));
						if(te instanceof TileEntitySheetmetalTank)
						{
							s = ((TileEntitySheetmetalTank)te).getOriginalBlock();
							((TileEntitySheetmetalTank)te).formed=false;
						}
						if(startPos.add(xx, yy, zz).equals(getPos()))
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startPos.add(xx, yy, zz).equals(getPos()))
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, getPos().getX()+.5,getPos().getY()+.5,getPos().getZ()+.5, s));
							else
							{
								if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblock)
									worldObj.setBlockToAir(startPos.add(xx, yy, zz));
								worldObj.setBlockState(startPos.add(xx, yy, zz), Block.getBlockFromItem(s.getItem()).getStateFromMeta(s.getItemDamage()));
							}
						}
					}
		}
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(!canFill(from, resource!=null?resource.getFluid():null))
			return 0;
		TileEntitySheetmetalTank master = master();
		if(master!=null)
		{
			master.updateComparatorValuesPart1();
			int f = master.tank.fill(resource, doFill);
			if(f>0 && doFill)
			{
				master.markDirty();
				worldObj.markBlockForUpdate(master.getPos());
				master.updateComparatorValuesPart2();
			}
			return f;
		}
		return 0;
	}
	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(!canDrain(from, resource!=null?resource.getFluid():null))
			return null;
		TileEntitySheetmetalTank master = master();
		if(master!=null)
			return master.drain(from,resource,doDrain);
		if(resource!=null)
			return drain(from, resource.amount, doDrain);
		return null;
	}
	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(!canDrain(from, null))
			return null;
		TileEntitySheetmetalTank master = master();
		if(master!=null)
		{
			master.updateComparatorValuesPart1();
			FluidStack fs = master.tank.drain(maxDrain, doDrain);
			if(fs!=null && fs.amount>0 && doDrain)
			{
				master.markDirty();
				worldObj.markBlockForUpdate(master.getPos());
				//Block updates for comparators
				master.updateComparatorValuesPart2();
			}
			return fs;
		}
		return null;
	}
	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return formed&&(pos==4||pos==40);
	}
	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return formed&&pos==4;
	}
	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if (pos==4||pos==40)
		{
			if (!formed)
				return new FluidTankInfo[] {};
			TileEntitySheetmetalTank master = master();
			if (master != null&&master!=this)
				return master.getTankInfo(from);
			return new FluidTankInfo[] { tank.getInfo() };
		}
		else
			return new FluidTankInfo[0];
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		TileEntitySheetmetalTank master = this.master();
		if(master!=null)
		{
			FluidStack f = Utils.getFluidFromItemStack(player.getCurrentEquippedItem());
			if(f!=null && Utils.fillFluidHandlerWithPlayerItem(worldObj, master, player))
			{
				this.markDirty();
				worldObj.markBlockForUpdate(master.getPos());
				return true;
			}
			if(Utils.fillPlayerItemFromFluidHandler(worldObj, master, player, master.tank.getFluid()))
			{
				this.markDirty();
				worldObj.markBlockForUpdate(master.getPos());
				return true;
			}
			if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(master.getPos());
				return true;
			}
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(pos==4)
				renderAABB = new AxisAlignedBB(getPos().add(-1,0,-1), getPos().add(2,5,2));
			else
				renderAABB = new AxisAlignedBB(getPos(),getPos());
		return renderAABB;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}

	public int getComparatorInputOverride()
	{
		if (pos==4)
			return (15*tank.getFluidAmount())/tank.getCapacity();
		TileEntitySheetmetalTank master = master();
		if (offset[1]>=1&&offset[1]<=4&&master!=null)//4 layers of storage
		{
			FluidTank t = master.tank;
			int layer = offset[1]-1;
			int vol = t.getCapacity()/4;
			int filled = t.getFluidAmount()-layer*vol;
			int ret = Math.min(15, Math.max(0, (15*filled)/vol));
			return ret;
		}
		return 0;
	}

	private void updateComparatorValuesPart1()
	{
		int vol = tank.getCapacity() / 4;
		for (int i = 0; i < 4; i++)
		{
			int filled = tank.getFluidAmount() - i * vol;
			oldComps[i] = Math.min(15, Math.max((15*filled)/vol, 0));
		}
		masterCompOld = (15*tank.getFluidAmount())/tank.getCapacity();
	}

	private void updateComparatorValuesPart2()
	{
		int vol = tank.getCapacity() / 6;
		if ((15*tank.getFluidAmount())/tank.getCapacity()!=masterCompOld)
			worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
		for(int i=0; i<4; i++)
		{
			int filled = tank.getFluidAmount() - i * vol;
			int now = Math.min(15, Math.max((15*filled)/vol, 0));
			if (now!=oldComps[i])
			{
				for(int x=-1; x<=1; x++)
					for(int z=-1; z<=1; z++)
					{
						BlockPos pos = getPos().add(-offset[0]+x, -offset[1]+i+1, -offset[2]+z);
						worldObj.notifyNeighborsOfStateChange(pos, worldObj.getBlockState(pos).getBlock());
					}
			}
		}
	}
}