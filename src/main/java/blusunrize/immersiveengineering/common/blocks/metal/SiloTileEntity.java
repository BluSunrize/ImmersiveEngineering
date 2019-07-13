/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSilo;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;

public class SiloTileEntity extends MultiblockPartTileEntity<SiloTileEntity> implements IComparatorOverride //IDeepStorageUnit
{
	public static TileEntityType<SiloTileEntity> TYPE;

	public ItemStack identStack = ItemStack.EMPTY;
	public int storageAmount = 0;
	private static final int MAX_STORAGE = 41472;
	//TODO actually implement this, it looks like a nice feature
	boolean lockItem = false;
	private int[] oldComps = new int[6];
	private int masterCompOld;

	public SiloTileEntity()
	{
		super(MultiblockSilo.instance, TYPE, true);
	}

	private EnumMap<Direction, CapabilityReference<IItemHandler>> outputCaps = new EnumMap<>(Direction.class);

	{
		for(Direction f : Direction.VALUES)
			if(f!=Direction.UP)
				outputCaps.put(f, CapabilityReference.forNeighbor(this, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f));
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);

		if(!isDummy()&&!world.isRemote&&!this.identStack.isEmpty()&&storageAmount > 0&&world.getGameTime()%8==0&&!isRSDisabled())
		{
			updateComparatorValuesPart1();
			for(Direction f : Direction.values())
				if(f!=Direction.UP)
				{
					ItemStack stack = Utils.copyStackWithAmount(identStack, 1);
					stack = Utils.insertStackIntoInventory(outputCaps.get(f), stack, false);
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
	public int[] getRedstonePos()
	{
		return new int[]{4};
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(nbt.hasKey("identStack"))
		{
			CompoundNBT t = nbt.getCompound("identStack");
			this.identStack = ItemStack.read(t);
		}
		else
			this.identStack = ItemStack.EMPTY;
		storageAmount = nbt.getInt("storageAmount");
		lockItem = nbt.getBoolean("lockItem");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.identStack.isEmpty())
		{
			CompoundNBT t = this.identStack.write(new CompoundNBT());
			nbt.put("identStack", t);
		}
		nbt.putInt("storageAmount", storageAmount);
		nbt.putBoolean("lockItem", lockItem);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock==0||posInMultiblock==2||posInMultiblock==6||posInMultiblock==8)
		{
			float xMin = (facing.getAxis()==Axis.X?(posInMultiblock > 2^facing==Direction.EAST): (posInMultiblock%3==2^facing==Direction.SOUTH))?.75f: 0;
			float xMax = (facing.getAxis()==Axis.X?(posInMultiblock < 3^facing==Direction.EAST): (posInMultiblock%3==0^facing==Direction.SOUTH))?.25f: 1;
			float zMin = (facing.getAxis()==Axis.X?(posInMultiblock%3==2^facing==Direction.EAST): (posInMultiblock < 3^facing==Direction.SOUTH))?.75f: 0;
			float zMax = (facing.getAxis()==Axis.X?(posInMultiblock%3==0^facing==Direction.EAST): (posInMultiblock > 2^facing==Direction.SOUTH))?.25f: 1;
			return new float[]{xMin, 0, zMin, xMax, 1, zMax};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public BlockPos getOrigin()
	{
		return getPos().add(-offset[0], -offset[1], -offset[2]).offset(facing.rotateYCCW()).offset(facing.getOpposite());
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(posInMultiblock==4)
				renderAABB = new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 7, 2));
			else
				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(new SiloInventoryHandler(this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if((posInMultiblock==4||posInMultiblock==58)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionHandler.cast();
		return super.getCapability(capability, facing);
	}

	public static class SiloInventoryHandler implements IItemHandler
	{
		SiloTileEntity silo;

		public SiloInventoryHandler(SiloTileEntity silo)
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
			SiloTileEntity silo = this.silo.master();
			int space = MAX_STORAGE-silo.storageAmount;
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
			SiloTileEntity silo = this.silo.master();
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

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return ItemStack.areItemsEqual(stack, silo.identStack);
		}
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(posInMultiblock==4)
			return (15*storageAmount)/MAX_STORAGE;
		SiloTileEntity master = master();
		if(offset[1] >= 1&&offset[1] <= 6&&master!=null) //6 layers of storage
		{
			int layer = offset[1]-1;
			int vol = MAX_STORAGE/6;
			int filled = master.storageAmount-layer*vol;
			int ret = Math.min(15, Math.max(0, (15*filled)/vol));
			return ret;
		}
		return 0;
	}

	private void updateComparatorValuesPart1()
	{
		int vol = MAX_STORAGE/6;
		for(int i = 0; i < 6; i++)
		{
			int filled = storageAmount-i*vol;
			oldComps[i] = Math.min(15, Math.max((15*filled)/vol, 0));
		}
		masterCompOld = (15*storageAmount)/MAX_STORAGE;
	}

	private void updateComparatorValuesPart2()
	{
		int vol = MAX_STORAGE/6;
		if((15*storageAmount)/MAX_STORAGE!=masterCompOld)
			world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
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
						world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
					}
			}
		}
	}
}
