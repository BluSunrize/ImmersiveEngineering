/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SiloBlockEntity extends MultiblockPartBlockEntity<SiloBlockEntity> implements IComparatorOverride,
		IBlockBounds
{
	public ItemStack identStack = ItemStack.EMPTY;
	public int storageAmount = 0;
	private static final int MAX_STORAGE = 41472;
	//TODO actually implement this, it looks like a nice feature
	boolean lockItem = false;
	private final LayeredComparatorOutput comparatorHelper = new LayeredComparatorOutput(
			MAX_STORAGE,
			6,
			() -> level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock()),
			layer -> {
				BlockPos masterPos = worldPosition.subtract(offsetToMaster);
				for(int x = -1; x <= 1; x++)
					for(int z = -1; z <= 1; z++)
					{
						BlockPos pos = masterPos.offset(x, layer+1, z);
						level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
					}
			}
	);

	public SiloBlockEntity(BlockEntityType<SiloBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.SILO, type, true, pos, state);
		// Silos should not output by default
		this.redstoneControlInverted = true;
	}

	private final List<CapabilityReference<IItemHandler>> outputCaps = new ArrayList<>();

	{
		for(Direction f : DirectionUtils.VALUES)
			if(f!=Direction.UP)
				outputCaps.add(CapabilityReference.forNeighbor(this, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f));
	}

	@Override
	public void tickServer()
	{
		if(!this.identStack.isEmpty()&&storageAmount > 0&&level.getGameTime()%8==0&&!isRSDisabled())
		{
			for(CapabilityReference<IItemHandler> output : outputCaps)
			{
				ItemStack stack = ItemHandlerHelper.copyStackWithSize(identStack, 1);
				stack = Utils.insertStackIntoInventory(output, stack, false);
				if(stack.isEmpty())
				{
					storageAmount--;
					if(storageAmount <= 0)
						identStack = ItemStack.EMPTY;
					this.setChanged();
					markContainingBlockForUpdate(null);
					if(storageAmount <= 0)
						break;
				}
			}
		}
		comparatorHelper.update(storageAmount);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(1, 0, 1)
		);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(nbt.contains("identStack", NBT.TAG_COMPOUND))
		{
			CompoundTag t = nbt.getCompound("identStack");
			this.identStack = ItemStack.of(t);
		}
		else
			this.identStack = ItemStack.EMPTY;
		storageAmount = nbt.getInt("storageAmount");
		lockItem = nbt.getBoolean("lockItem");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.identStack.isEmpty())
		{
			CompoundTag t = this.identStack.save(new CompoundTag());
			nbt.put("identStack", t);
		}
		nbt.putInt("storageAmount", storageAmount);
		nbt.putBoolean("lockItem", lockItem);
	}

	private static final CachedShapesWithTransform<BlockPos, Direction> BLOCK_BOUNDS = CachedShapesWithTransform.createDirectional(
			pos -> {
				if(pos.getX()%2==0&&pos.getY()==0&&pos.getZ()%2==0)
				{
					float xMin = pos.getX()==2?.75f: 0;
					float xMax = pos.getX()==0?.25f: 1;
					float zMin = pos.getZ()==2?.75f: 0;
					float zMax = pos.getZ()==0?.25f: 1;
					return ImmutableList.of(new AABB(xMin, 0, zMin, xMax, 1, zMax));
				}
				return ImmutableList.of(new AABB(0, 0, 0, 1, 1, 1));
			}
	);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return BLOCK_BOUNDS.get(posInMultiblock, getFacing());
	}

	private AABB renderAABB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(offsetToMaster.equals(BlockPos.ZERO))
				renderAABB = new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(2, 7, 2));
			else
				renderAABB = new AABB(getBlockPos(), getBlockPos());
		return renderAABB;
	}

	private final ResettableCapability<IItemHandler> insertionHandler = registerCapability(new SiloInventoryHandler(this));

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

	@Override
	public int getComparatorInputOverride()
	{
		if(bottomIoOffset.equals(posInMultiblock))
			return comparatorHelper.getCurrentMasterOutput();
		SiloBlockEntity master = master();
		if(offsetToMaster.getY() >= 1&&master!=null)
			return comparatorHelper.getLayerOutput(offsetToMaster.getY()-1);
		return 0;
	}

	public static class SiloInventoryHandler implements IItemHandler
	{
		SiloBlockEntity silo;

		public SiloInventoryHandler(SiloBlockEntity silo)
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
				return ItemHandlerHelper.copyStackWithSize(silo.identStack, silo.storageAmount);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			stack = stack.copy();
			SiloBlockEntity silo = this.silo.master();
			int space = MAX_STORAGE-silo.storageAmount;
			if(slot!=0||space < 1||stack.isEmpty()||(!silo.identStack.isEmpty()&&!ItemHandlerHelper.canItemStacksStack(silo.identStack, stack)))
				return stack;
			int accepted = Math.min(space, stack.getCount());
			if(!simulate)
			{
				silo.storageAmount += accepted;
				if(silo.identStack.isEmpty())
					silo.identStack = stack.copy();
				silo.setChanged();
				silo.markContainingBlockForUpdate(null);
			}
			stack.shrink(accepted);
			if(stack.getCount() < 1)
				stack = ItemStack.EMPTY;
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			SiloBlockEntity silo = this.silo.master();
			if(slot!=1||silo.storageAmount < 1||amount < 1||silo.identStack.isEmpty())
				return ItemStack.EMPTY;
			int returned = Math.min(Math.min(silo.storageAmount, amount), silo.identStack.getMaxStackSize());
			ItemStack out = ItemHandlerHelper.copyStackWithSize(silo.identStack, returned);
			if(!simulate)
			{
				silo.storageAmount -= out.getCount();
				if(silo.storageAmount <= 0&&!silo.lockItem)
					silo.identStack = ItemStack.EMPTY;
				silo.setChanged();
				silo.markContainingBlockForUpdate(null);
			}
			return out;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return MAX_STORAGE;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return slot==0&&ItemStack.isSame(stack, silo.identStack);
		}
	}
}
