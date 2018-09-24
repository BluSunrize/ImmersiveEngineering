/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
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
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntitySilo extends TileEntityMultiblockPart<TileEntitySilo> implements IComparatorOverride //IDeepStorageUnit
{
	public ItemStack identStack = ItemStack.EMPTY;
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

	public TileEntitySilo()
	{
		super(size);
	}

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);

		if(pos==4&&!world.isRemote&&!this.identStack.isEmpty()&&storageAmount > 0&&world.getRedstonePowerFromNeighbors(getPos()) > 0&&world.getTotalWorldTime()%8==0)
		{
			updateComparatorValuesPart1();
			for(EnumFacing f : EnumFacing.values())
				if(f!=EnumFacing.UP)
				{
					TileEntity inventory = Utils.getExistingTileEntity(world, getPos().offset(f));
					ItemStack stack = Utils.copyStackWithAmount(identStack, 1);
					stack = Utils.insertStackIntoInventory(inventory, stack, f.getOpposite());
					if(stack.isEmpty())
					{
						storageAmount--;
						if(storageAmount <= 0)
							identStack = ItemStack.EMPTY;
						this.markDirty();
						markContainingBlockForUpdate(null);
						if(storageAmount <= 0)
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
			this.identStack = new ItemStack(t);
		}
		else
			this.identStack = ItemStack.EMPTY;
		storageAmount = nbt.getInteger("storageAmount");
		lockItem = nbt.getBoolean("lockItem");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.identStack.isEmpty())
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
			float xMin = (facing.getAxis()==Axis.X?(pos > 2^facing==EnumFacing.EAST): (pos%3==2^facing==EnumFacing.SOUTH))?.75f: 0;
			float xMax = (facing.getAxis()==Axis.X?(pos < 3^facing==EnumFacing.EAST): (pos%3==0^facing==EnumFacing.SOUTH))?.25f: 1;
			float zMin = (facing.getAxis()==Axis.X?(pos%3==2^facing==EnumFacing.EAST): (pos < 3^facing==EnumFacing.SOUTH))?.75f: 0;
			float zMax = (facing.getAxis()==Axis.X?(pos%3==0^facing==EnumFacing.EAST): (pos > 2^facing==EnumFacing.SOUTH))?.25f: 1;
			return new float[]{xMin, 0, zMin, xMax, 1, zMax};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		return pos==0||pos==2||pos==6||pos==8?new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta()): new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
	}

	@Override
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
				renderAABB = new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 7, 2));
			else
				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
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
		if((pos==4||pos==58)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		//		if(pos>30&&pos<44 && pos%5>0&&pos%5<4 )
		//			return true;
		return super.hasCapability(capability, facing);
	}

	IItemHandler insertionHandler = new SiloInventoryHandler(this);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if((pos==4||pos==58)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T)insertionHandler;
		return super.getCapability(capability, facing);
	}

	public static class SiloInventoryHandler implements IItemHandler
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
			if(slot==0)
				return ItemStack.EMPTY;
			else
			{
				int maxSize = Math.min(silo.storageAmount, silo.identStack.getMaxStackSize());
				return ApiUtils.copyStackWithAmount(silo.identStack, maxSize);
			}
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			stack = stack.copy();
			TileEntitySilo silo = this.silo.master();
			int space = maxStorage-silo.storageAmount;
			if(slot!=0||space < 1||stack.isEmpty()||(!silo.identStack.isEmpty()&&!ItemHandlerHelper.canItemStacksStack(silo.identStack, stack)))
				return stack;
			int accepted = Math.min(space, stack.getCount());
			if(!simulate)
			{
				silo.updateComparatorValuesPart1();
				silo.storageAmount += accepted;
				if(silo.identStack.isEmpty())
					silo.identStack = stack.copy();
				silo.markDirty();
				silo.markContainingBlockForUpdate(null);
				silo.updateComparatorValuesPart2();
			}
			stack.shrink(accepted);
			if(stack.getCount() < 1)
				stack = ItemStack.EMPTY;
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			TileEntitySilo silo = this.silo.master();
			if(slot!=1||silo.storageAmount < 1||amount < 1||silo.identStack.isEmpty())
				return ItemStack.EMPTY;
			ItemStack out;
			if(silo.storageAmount >= amount)
				out = Utils.copyStackWithAmount(silo.identStack, amount);
			else
				out = Utils.copyStackWithAmount(silo.identStack, silo.storageAmount);
			if(!simulate)
			{
				silo.updateComparatorValuesPart1();
				silo.storageAmount -= out.getCount();
				if(silo.storageAmount <= 0&&!silo.lockItem)
					silo.identStack = ItemStack.EMPTY;
				silo.markDirty();
				silo.markContainingBlockForUpdate(null);
				silo.updateComparatorValuesPart2();
			}
			return out;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(pos==4)
			return (15*storageAmount)/maxStorage;
		TileEntitySilo master = master();
		if(offset[1] >= 1&&offset[1] <= 6&&master!=null) //6 layers of storage
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
		int vol = maxStorage/6;
		for(int i = 0; i < 6; i++)
		{
			int filled = storageAmount-i*vol;
			oldComps[i] = Math.min(15, Math.max((15*filled)/vol, 0));
		}
		masterCompOld = (15*storageAmount)/maxStorage;
	}

	private void updateComparatorValuesPart2()
	{
		int vol = maxStorage/6;
		if((15*storageAmount)/maxStorage!=masterCompOld)
			world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
		for(int i = 0; i < 6; i++)
		{
			int filled = storageAmount-i*vol;
			int now = Math.min(15, Math.max((15*filled)/vol, 0));
			if(now!=oldComps[i])
			{
				for(int x = -1; x <= 1; x++)
					for(int z = -1; z <= 1; z++)
					{
						BlockPos pos = getPos().add(-offset[0]+x, -offset[1]+i+1, -offset[2]+z);
						world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
					}
			}
		}
	}
}
