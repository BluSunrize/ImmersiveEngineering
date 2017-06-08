package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntitySilo extends TileEntityMultiblockPart<TileEntitySilo> implements IComparatorOverride //IDeepStorageUnit
{
	public ItemStack identStack;
	public int storageAmount = 0;
	static int maxStorage = 41472;
	//	ItemStack inputStack;
//	ItemStack outputStack;
//	ItemStack prevInputStack;
//	ItemStack prevOutputStack;
	boolean lockItem = false;
	private int[] oldComps = new int[6];
	private int masterCompOld;
	private boolean forceUpdate = false;

	private static final int[] size = {7, 3, 3};
	public TileEntitySilo() {
		super(size);
	}
	@Override
	public void update()
	{
//		if(pos==4 && !worldObj.isRemote && this.outputStack==null && storageAmount>0 && identStack!=null)
//			this.markDirty();

		if(pos==4 && !worldObj.isRemote && this.identStack!=null && storageAmount>0 && worldObj.isBlockIndirectlyGettingPowered(getPos())>0 && worldObj.getTotalWorldTime()%8==0)
		{
			updateComparatorValuesPart1();
			for(EnumFacing f : EnumFacing.values())
				if(f!=EnumFacing.UP)
				{
					TileEntity inventory = this.worldObj.getTileEntity(getPos().offset(f));
					ItemStack stack = Utils.copyStackWithAmount(identStack,1);
					stack = Utils.insertStackIntoInventory(inventory, stack, f.getOpposite());
					if(stack==null)
					{
						storageAmount--;
						if(storageAmount<=0)
							identStack = null;
						this.markDirty();
						markContainingBlockForUpdate(null);
						if(storageAmount<=0)
							break;
					}
				}
			updateComparatorValuesPart2();
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(nbt.hasKey("identStack"))
		{
			NBTTagCompound t = nbt.getCompoundTag("identStack");
			this.identStack = ItemStack.loadItemStackFromNBT(t);
		}
		else
			this.identStack = null;
		storageAmount = nbt.getInteger("storageAmount");
		lockItem = nbt.getBoolean("lockItem");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(this.identStack!=null)
		{
			NBTTagCompound t = this.identStack.writeToNBT(new NBTTagCompound());
			nbt.setTag("identStack", t);
		}
		nbt.setInteger("storageAmount", storageAmount);
		nbt.setBoolean("lockItem", lockItem);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new FluidTank[0];
	}
	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}
	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos==0||pos==2||pos==6||pos==8)
		{
			float xMin = (facing.getAxis() == Axis.X ? (pos > 2 ^ facing == EnumFacing.EAST) : (pos % 3 == 2 ^ facing == EnumFacing.SOUTH)) ? .75f : 0;
			float xMax = (facing.getAxis() == Axis.X ? (pos < 3 ^ facing == EnumFacing.EAST) : (pos % 3 == 0 ^ facing == EnumFacing.SOUTH)) ? .25f : 1;
			float zMin = (facing.getAxis() == Axis.X ? (pos % 3 == 2 ^ facing == EnumFacing.EAST) : (pos < 3 ^ facing == EnumFacing.SOUTH)) ? .75f : 0;
			float zMax = (facing.getAxis() == Axis.X ? (pos % 3 == 0 ^ facing == EnumFacing.EAST) : (pos > 2 ^ facing == EnumFacing.SOUTH)) ? .25f : 1;
			return new float[]{xMin, 0, zMin, xMax, 1, zMax};
		}
		return new float[]{0,0,0,1,1,1};
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		return pos==0||pos==2||pos==6||pos==8?new ItemStack(IEContent.blockWoodenDecoration,1,BlockTypes_WoodenDecoration.FENCE.getMeta()):new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.IRON.getMeta());
	}

	public BlockPos getOrigin()
	{
		return getPos().add(-offset[0], -offset[1], -offset[2]).offset(facing.rotateYCCW()).offset(facing.getOpposite());
	}
	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(pos==4)
				renderAABB = new AxisAlignedBB(getPos().add(-1,0,-1), getPos().add(2,7,2));
			else
				renderAABB = new AxisAlignedBB(getPos(),getPos());
		return renderAABB;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()* IEConfig.increasedTileRenderdistance;
	}

	/*
	//DEEP STORAGE
	@Override
	public ItemStack getStoredItemType()
	{
		TileEntitySilo mast = master();
		if (mast!=null)
			return mast.getStoredItemType();
		if(this.identStack != null)
			return Utils.copyStackWithAmount(identStack, storageAmount);
		return null;
	}

	@Override
	public void setStoredItemCount(int amount)
	{
		TileEntitySilo mast = master();
		if (mast!=null)
		{
			mast.setStoredItemCount(amount);
			return;
		}
		updateComparatorValuesPart1();
		if(amount > maxStorage)
			amount = maxStorage;
		this.storageAmount = amount;
		this.forceUpdate = true;
		this.markDirty();
		updateComparatorValuesPart2();
	}

	@Override
	public void setStoredItemType(ItemStack type, int amount)
	{
		TileEntitySilo mast = master();
		if (mast!=null)
		{
			mast.setStoredItemType(type, amount);
			return;
		}
		updateComparatorValuesPart1();
		this.identStack = Utils.copyStackWithAmount(type, 0);
		if(amount > maxStorage)
			amount = maxStorage;
		this.storageAmount = amount;
		this.forceUpdate = true;
		this.markDirty();
		updateComparatorValuesPart2();
	}

	@Override
	public int getMaxStoredCount()
	{
		return maxStorage;
	}
	 */


	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if((pos==4||pos==58) && capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		//		if(pos>30&&pos<44 && pos%5>0&&pos%5<4 )
		//			return true;
		return super.hasCapability(capability, facing);
	}
	IItemHandler insertionHandler = new SiloInventoryHandler(this);
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if((pos==4||pos==58) && capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T)insertionHandler;
		return super.getCapability(capability, facing);
	}

	public static class SiloInventoryHandler implements IItemHandlerModifiable
	{
		TileEntitySilo silo;
		public SiloInventoryHandler(TileEntitySilo silo)
		{
			this.silo = silo;
		}

		@Override
		public int getSlots()
		{
			return 2;
		}
		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return slot==0?Utils.copyStackWithAmount(silo.identStack,Math.min(silo.storageAmount,1)):Utils.copyStackWithAmount(silo.identStack,Math.min(silo.storageAmount,64));
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			stack = stack.copy();
			TileEntitySilo silo = this.silo.master();
			int space = maxStorage-silo.storageAmount;
			if(slot!=0 || space<1 || stack==null || (silo.identStack!=null && !ItemHandlerHelper.canItemStacksStack(silo.identStack,stack)))
				return stack;
			int accepted = Math.min(space, stack.stackSize);
			if(!simulate)
			{
				silo.updateComparatorValuesPart1();
				silo.storageAmount += accepted;
				if(silo.identStack==null)
					silo.identStack = stack.copy();
				silo.markDirty();
				silo.markContainingBlockForUpdate(null);
				silo.updateComparatorValuesPart2();
			}
			stack.stackSize -= accepted;
			if(stack.stackSize<1)
				stack = null;
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			TileEntitySilo silo = this.silo.master();
			if(slot!=1 || silo.storageAmount<1 || amount<1 || silo.identStack==null)
				return null;
			ItemStack out;
			if(silo.identStack.stackSize>amount)
				out = Utils.copyStackWithAmount(silo.identStack, amount);
			else
				out = silo.identStack.copy();
			if(!simulate)
			{
				silo.updateComparatorValuesPart1();
				silo.storageAmount -= out.stackSize;
				if(silo.storageAmount<=0 && !silo.lockItem)
					silo.identStack = null;
				silo.markDirty();
				silo.markContainingBlockForUpdate(null);
				silo.updateComparatorValuesPart2();
			}
			return out;
		}
		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(pos==4)
			return (15*storageAmount)/maxStorage;
		TileEntitySilo master = master();
		if (offset[1]>=1&&offset[1]<=6&&master!=null) //6 layers of storage
		{
			int layer = offset[1]-1;
			int vol = maxStorage/6;
			int filled = master.storageAmount-layer*vol;
			int ret = Math.min(15, Math.max(0, (15*filled)/vol));
			return ret;
		}
		return 0;
	}
	private void updateComparatorValuesPart1()
	{
		int vol = maxStorage / 6;
		for(int i=0; i<6; i++)
		{
			int filled = storageAmount - i * vol;
			oldComps[i] = Math.min(15, Math.max((15*filled)/vol, 0));
		}
		masterCompOld = (15*storageAmount)/maxStorage;
	}
	private void updateComparatorValuesPart2()
	{
		int vol = maxStorage / 6;
		if((15*storageAmount)/maxStorage!=masterCompOld)
			worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
		for(int i=0; i<6; i++)
		{
			int filled = storageAmount - i * vol;
			int now = Math.min(15, Math.max((15*filled)/vol, 0));
			if(now!=oldComps[i])
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
