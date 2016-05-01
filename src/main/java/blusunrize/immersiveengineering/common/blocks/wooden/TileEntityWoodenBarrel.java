package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMetalBarrel;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityWoodenBarrel extends TileEntityIEBase implements ITickable, IFluidHandler, IBlockOverlayText, IConfigurableSides, IPlayerInteraction, ITileDrop, IComparatorOverride
{
	public int[] sideConfig = {1,0};
	public FluidTank tank = new FluidTank(12000);
	public static final int IGNITION_TEMPERATURE = 573;

	@Override
	public void update()
	{
		if(worldObj.isRemote)
			return;

		boolean update = false;
		for(int i=0; i<2; i++)
			if(tank.getFluidAmount()>0 && sideConfig[i]==1)
			{
				EnumFacing f = EnumFacing.getFront(i);
				int out = Math.min(40,tank.getFluidAmount());
				TileEntity te = worldObj.getTileEntity(getPos().offset(f));
				if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(f.getOpposite(), tank.getFluid().getFluid()))
				{
					int accepted = ((IFluidHandler)te).fill(f.getOpposite(), new FluidStack(tank.getFluid().getFluid(),out), false);
					FluidStack drained = this.tank.drain(accepted, true);
					((IFluidHandler)te).fill(f.getOpposite(), drained, true);
					update = true;
				}
			}
		if(update)
		{
			this.markDirty();
			worldObj.markBlockForUpdate(getPos());
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, MovingObjectPosition mop, boolean hammer)
	{
		if(Utils.isFluidRelatedItemStack(player.getCurrentEquippedItem()))
		{
			String s = null;
			if(tank.getFluid()!=null)
				s = tank.getFluid().getLocalizedName()+": "+tank.getFluidAmount()+"mB";
			else
				s = StatCollector.translateToLocal(Lib.GUI+"empty");
			return new String[]{s};
		}
		if(hammer && Config.getBoolean("colourblindSupport") && mop.sideHit.getAxis()==Axis.Y)
		{
			int i = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.ordinal())];
			int j = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.getOpposite().ordinal())];
			return new String[]{
					StatCollector.translateToLocal(Lib.DESC_INFO+"blockSide.facing")
					+": "+StatCollector.translateToLocal(Lib.DESC_INFO+"blockSide.connectFluid."+i),
					StatCollector.translateToLocal(Lib.DESC_INFO+"blockSide.opposite")
					+": "+StatCollector.translateToLocal(Lib.DESC_INFO+"blockSide.connectFluid."+j)
			};
		}
		return null;
	}
	@Override
	public boolean useNixieFont(EntityPlayer player, MovingObjectPosition mop)
	{
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null || sideConfig.length<2)
			sideConfig = new int[]{-1,0};
		this.readTank(nbt);
	}
	public void readTank(NBTTagCompound nbt)
	{
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		this.writeTank(nbt, false);
	}
	public void writeTank(NBTTagCompound nbt, boolean toItem)
	{
		boolean write = tank.getFluidAmount()>0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if(!toItem || write)
			nbt.setTag("tank", tankTag);
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(isFluidValid(resource) && canFill(from, resource.getFluid()))
		{
			int i = tank.fill(resource, doFill);
			if(i>0)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(getPos());
			}
			return i;
		}
		return 0;
	}

	public boolean isFluidValid(FluidStack fluid)
	{
		return fluid!=null && fluid.getFluid()!=null && fluid.getFluid().getTemperature(fluid)<IGNITION_TEMPERATURE && !fluid.getFluid().isGaseous(fluid);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		return this.drain(from, resource!=null?resource.amount:0, doDrain);
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(canDrain(from,null))
		{
			FluidStack f = tank.drain(maxDrain, doDrain);
			if(f!=null && f.amount>0)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(getPos());
			}
			return f;
		}
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return (from==null || (from.ordinal()<2 && sideConfig[from.ordinal()]==0));
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return (from==null || (from.ordinal()<2 && sideConfig[from.ordinal()]==1));
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if(from!=null && from.ordinal()<2 && sideConfig[from.ordinal()]!=-1)
			return new FluidTankInfo[]{tank.getInfo()};
		return new FluidTankInfo[0];
	}

	@Override
	public SideConfig getSideConfig(int side)
	{
		if(side>1)
			return IEEnums.SideConfig.NONE;
		return IEEnums.SideConfig.values()[this.sideConfig[side]+1];
	}
	@Override
	public void toggleSide(int side)
	{
		if(side!=0&&side!=1)
			return;
		sideConfig[side]++;
		if(sideConfig[side]>1)
			sideConfig[side]=-1;
		this.markDirty();
		worldObj.markBlockForUpdate(getPos());
		worldObj.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.worldObj.markBlockForUpdate(getPos());
			return true;
		}
		return false;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		FluidStack f = Utils.getFluidFromItemStack(player.getCurrentEquippedItem());
		boolean metal = this instanceof TileEntityMetalBarrel;
		if(f!=null)
			if(!metal && f.getFluid().isGaseous(f))
				ChatUtils.sendServerNoSpamMessages(player, new ChatComponentTranslation(Lib.CHAT_INFO+"noGasAllowed"));
			else if(!metal && f.getFluid().getTemperature(f)>=TileEntityWoodenBarrel.IGNITION_TEMPERATURE)
				ChatUtils.sendServerNoSpamMessages(player, new ChatComponentTranslation(Lib.CHAT_INFO+"tooHot"));
			else if(Utils.fillFluidHandlerWithPlayerItem(worldObj, this, player))
			{
				this.markDirty();
				worldObj.markBlockForUpdate(getPos());
				return true;
			}
		if(Utils.fillPlayerItemFromFluidHandler(worldObj, this, player, this.tank.getFluid()))
		{
			this.markDirty();
			worldObj.markBlockForUpdate(getPos());
			return true;
		}
		if(player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof IFluidContainerItem)
		{
			this.markDirty();
			worldObj.markBlockForUpdate(getPos());
			return true;
		}
		return false;
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		NBTTagCompound tag = new NBTTagCompound();
		writeTank(tag, true);
		if(!tag.hasNoTags())
			stack.setTagCompound(tag);
		return stack;
	}
	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
	{
		if(stack.hasTagCompound())
			readTank(stack.getTagCompound());
	}

	@Override
	public int getComparatorInputOverride()
	{
		return (int)(15*(tank.getFluidAmount()/(float)tank.getCapacity()));
	}
}