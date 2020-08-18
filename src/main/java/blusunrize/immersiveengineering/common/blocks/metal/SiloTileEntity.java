/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Set;

public class SiloTileEntity extends MultiblockPartTileEntity<SiloTileEntity> implements IComparatorOverride, IBlockBounds
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
		super(IEMultiblocks.SILO, TYPE, true);
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
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(1, 0, 1)
		);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(nbt.contains("identStack", NBT.TAG_COMPOUND))
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

	private static final CachedShapesWithTransform<BlockPos, Direction> BLOCK_BOUNDS = CachedShapesWithTransform.createDirectional(
			pos -> {
				if(pos.getX()%2==0&&pos.getY()==0&&pos.getZ()%2==0)
				{
					float xMin = pos.getX()==2?.75f: 0;
					float xMax = pos.getX()==0?.25f: 1;
					float zMin = pos.getZ()==2?.75f: 0;
					float zMax = pos.getZ()==0?.25f: 1;
					return ImmutableList.of(new AxisAlignedBB(xMin, 0, zMin, xMax, 1, zMax));
				}
				return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
			}
	);

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return BLOCK_BOUNDS.get(posInMultiblock, getFacing());
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(offsetToMaster.equals(BlockPos.ZERO))
				renderAABB = new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 7, 2));
			else
				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(new SiloInventoryHandler(this));

	private static final BlockPos bottomIoOffset = new BlockPos(1, 0, 1);
	private static final BlockPos topIoOffset = new BlockPos(1, 6, 1);
	private static final Set<BlockPos> ioOffsets = ImmutableSet.of(bottomIoOffset, topIoOffset);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(ioOffsets.contains(posInMultiblock)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
				return ItemUtils.copyStackWithAmount(silo.identStack, maxSize);
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
		if(bottomIoOffset.equals(posInMultiblock))
			return (15*storageAmount)/MAX_STORAGE;
		SiloTileEntity master = master();
		if(offsetToMaster.getY() >= 1&&master!=null) //6 layers of storage
		{
			int layer = offsetToMaster.getY()-1;
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
		BlockPos masterPos = pos.subtract(offsetToMaster);
		for(int i = 0; i < 6; i++)
		{
			int filled = storageAmount-i*vol;
			int now = Math.min(15, Math.max((15*filled)/vol, 0));
			if(now!=oldComps[i])
			{
				for(int x = -1; x <= 1; x++)
					for(int z = -1; z <= 1; z++)
					{
						BlockPos pos = masterPos.add(x, i+1, z);
						world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
					}
			}
		}
	}
}
