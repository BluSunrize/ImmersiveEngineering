/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.IntIterators;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

//TODO Metadata and oredict are gone. Update manual entry as well.
public class SorterTileEntity extends IEBaseTileEntity implements IInteractionObjectIE
{
	public SorterInventory filter;
	public int[] sideFilter = {0, 0, 0, 0, 0, 0};//OreDict,nbt,fuzzy
	public static final int filterSlotsPerSide = 8;
	/**
	 * The positions of the routers that have been used in the current "outermost" `routeItem` call.
	 * Necessary to stop "blocks" of routers (and similar setups) from causing massive lag (using just a boolean
	 * results in every possible path to be "tested"). Using a set results in effectively a DFS.
	 */
	private static Set<BlockPos> routed = null;

	private EnumMap<Direction, CapabilityReference<IItemHandler>> neighborCaps = new EnumMap<>(Direction.class);

	public SorterTileEntity()
	{
		super(IETileTypes.SORTER.get());
		filter = new SorterInventory();
		for(Direction f : DirectionUtils.VALUES)
			neighborCaps.put(f, CapabilityReference.forNeighbor(this, ITEM_HANDLER_CAPABILITY, f));
	}


	public ItemStack routeItem(Direction inputSide, ItemStack stack, boolean simulate)
	{
		if(!world.isRemote&&canRoute())
		{
			boolean first = startRouting();
			Direction[][] validOutputs = getValidOutputs(inputSide, stack);
			stack = doInsert(stack, validOutputs[0], simulate);
			// Only if no filtered outputs were found, use unfiltered
			if(validOutputs[0].length==0)
				stack = doInsert(stack, validOutputs[1], simulate);
			if(first)
				routed = null;
		}
		return stack;
	}

	private boolean canRoute()
	{
		return routed==null||!routed.contains(pos);
	}

	private boolean startRouting()
	{
		boolean first = routed==null;
		if(first)
			routed = new HashSet<>();
		routed.add(pos);
		return first;
	}

	private ItemStack doInsert(ItemStack stack, Direction[] sides, boolean simulate)
	{
		int lengthFiltered = sides.length;
		while(lengthFiltered > 0&&!stack.isEmpty())
		{
			int rand = Utils.RAND.nextInt(lengthFiltered);
			stack = this.outputItemToInv(stack, sides[rand], simulate);
			sides[rand] = sides[lengthFiltered-1];
			lengthFiltered--;
		}
		return stack;
	}

	public boolean doOredict(int side)
	{
		if(side >= 0&&side < this.sideFilter.length)
			return (this.sideFilter[side]&1)!=0;
		return false;
	}

	public boolean doNBT(int side)
	{
		if(side >= 0&&side < this.sideFilter.length)
			return (this.sideFilter[side]&2)!=0;
		return false;
	}

	public boolean doFuzzy(int side)
	{
		if(side >= 0&&side < this.sideFilter.length)
			return (this.sideFilter[side]&4)!=0;
		return false;
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return true;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return this;
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.contains("sideConfig", NBT.TAG_INT_ARRAY))
			this.sideFilter = message.getIntArray("sideConfig");
	}

	public Direction[][] getValidOutputs(Direction inputSide, ItemStack stack)
	{
		if(stack.isEmpty())
			return new Direction[][]{{}, {}, {}, {}};
		List<Direction> validFiltered = new ArrayList<>(6);
		List<Direction> validUnfiltered = new ArrayList<>(6);
		for(Direction side : Direction.values())
			if(side!=inputSide)
			{
				EnumFilterResult result = checkStackAgainstFilter(stack, side);
				if(result==EnumFilterResult.VALID_FILTERED)
					validFiltered.add(side);
				else if(result==EnumFilterResult.VALID_UNFILTERED)
					validUnfiltered.add(side);
			}

		return new Direction[][]{
				validFiltered.toArray(new Direction[0]),
				validUnfiltered.toArray(new Direction[0])
		};
	}

	public ItemStack pullItem(Direction outputSide, int amount, boolean simulate)
	{
		if(!world.isRemote&&canRoute())
		{
			boolean first = startRouting();
			for(Direction side : Direction.values())
				if(side!=outputSide)
				{
					CapabilityReference<IItemHandler> capRef = neighborCaps.get(side);
					IItemHandler itemHandler = capRef.getNullable();
					if(itemHandler!=null)
					{
						Predicate<ItemStack> concatFilter = null;
						for(int i = 0; i < itemHandler.getSlots(); i++)
						{
							ItemStack extractItem = itemHandler.extractItem(i, amount, true);
							if(!extractItem.isEmpty())
							{
								if(concatFilter==null)//Init the filter here, to save on resources
									concatFilter = this.concatFilters(outputSide, side);
								if(concatFilter.test(extractItem))
								{
									if(first)
										routed = null;
									if(!simulate)
										itemHandler.extractItem(i, amount, false);
									return extractItem;
								}
							}
						}
					}
				}
			if(first)
				routed = null;
		}
		return ItemStack.EMPTY;
	}

	private boolean compareStackToFilterstack(ItemStack stack, ItemStack filterStack, boolean fuzzy, boolean oredict, boolean nbt)
	{
		boolean b = ItemStack.areItemsEqual(filterStack, stack);
		if(!b&&fuzzy)
			b = ItemStack.areItemsEqualIgnoreDurability(filterStack, stack);
		if(!b&&oredict)
			for(ResourceLocation name : ItemTags.getCollection().getOwningTags(stack.getItem()))
				if(Utils.isInTag(filterStack, name))
				{
					b = true;
					break;
				}
		if(nbt)
			b &= Utils.compareItemNBT(filterStack, stack);
		return b;
	}

	/**
	 * @param stack the stack to check
	 * @param side  the side the filter is on
	 * @return If the stack is permitted by the given filter
	 */
	private EnumFilterResult checkStackAgainstFilter(ItemStack stack, Direction side)
	{
		boolean unmapped = true;
		for(ItemStack filterStack : filter.getFilterStacksOnSide(side))
			if(!filterStack.isEmpty())
			{
				unmapped = false;
				if(compareStackToFilterstack(stack, filterStack, doFuzzy(side.ordinal()), doOredict(side.ordinal()), doNBT(side.ordinal())))
					return EnumFilterResult.VALID_FILTERED;
			}
		if(unmapped)
			return EnumFilterResult.VALID_UNFILTERED;
		return EnumFilterResult.INVALID;
	}

	/**
	 * @return A Predicate representing the concatinated filters of two sides.<br>
	 * If one filter is empty, uses the full filter of the other side, else the matching items make up the filter
	 */
	private Predicate<ItemStack> concatFilters(Direction side0, Direction side1)
	{
		final List<ItemStack> concat = new ArrayList<>();
		for(ItemStack filterStack : filter.getFilterStacksOnSide(side0))
			if(!filterStack.isEmpty())
				concat.add(filterStack);

		Predicate<ItemStack> matchFilter = concat.isEmpty()?(stack) -> true: new Predicate<ItemStack>()
		{
			final Set<ItemStack> filter = new HashSet<>(concat);

			@Override
			public boolean test(ItemStack stack)
			{
				for(ItemStack filterStack : filter)
					if(compareStackToFilterstack(stack, filterStack, doFuzzy(side0.ordinal()), doOredict(side0.ordinal()), doNBT(side0.ordinal())))
						return true;
				return false;
			}
		};

		for(ItemStack filterStack : filter.getFilterStacksOnSide(side1))
			if(!filterStack.isEmpty()&&matchFilter.test(filterStack))
				concat.add(filterStack);

		final boolean concatFuzzy = doFuzzy(side0.ordinal())|doFuzzy(side1.ordinal());
		final boolean concatOredict = doOredict(side0.ordinal())|doOredict(side1.ordinal());
		final boolean concatNBT = doNBT(side0.ordinal())|doNBT(side1.ordinal());

		return concat.isEmpty()?stack -> true: stack -> {
			for(ItemStack filterStack : concat)
				if(compareStackToFilterstack(stack, filterStack, concatFuzzy, concatOredict, concatNBT))
					return true;
			return false;
		};
	}

	public ItemStack outputItemToInv(ItemStack stack, Direction side, boolean simulate)
	{
		return Utils.insertStackIntoInventory(neighborCaps.get(side), stack, simulate);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		sideFilter = nbt.getIntArray("sideFilter");
		if(!descPacket)
		{
			ListNBT filterList = nbt.getList("filter", 10);
			filter = new SorterInventory();
			filter.readFromNBT(filterList);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putIntArray("sideFilter", sideFilter);
		if(!descPacket)
		{
			ListNBT filterList = new ListNBT();
			filter.writeToNBT(filterList);
			nbt.put("filter", filterList);
		}
	}

	private EnumMap<Direction, LazyOptional<IItemHandler>> insertionHandlers = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
		{
			LazyOptional<IItemHandler> forSide = registerConstantCap(new SorterInventoryHandler(this, f));
			insertionHandlers.put(f, forSide);
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==ITEM_HANDLER_CAPABILITY&&facing!=null)
			return insertionHandlers.get(facing).cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		return id==0;
	}

	public static class SorterInventoryHandler implements IItemHandlerModifiable
	{
		SorterTileEntity sorter;
		Direction side;

		public SorterInventoryHandler(SorterTileEntity sorter, Direction side)
		{
			this.sorter = sorter;
			this.side = side;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			return sorter.routeItem(this.side, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return sorter.pullItem(this.side, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return true;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}

	public static class SorterInventory extends ItemStackHandler
	{
		public SorterInventory()
		{
			super(NonNullList.withSize(6*filterSlotsPerSide, ItemStack.EMPTY));
		}

		public ItemStack getStackBySideAndSlot(Direction side, int slotOnSide)
		{
			return getStackInSlot(getSlotId(side, slotOnSide));
		}

		public int getSlotId(Direction side, int slotOnSide)
		{
			return side.ordinal()*filterSlotsPerSide+slotOnSide;
		}

		public Iterable<ItemStack> getFilterStacksOnSide(Direction side)
		{
			return () -> Iterators.transform(
					IntIterators.fromTo(0, filterSlotsPerSide), i -> getStackBySideAndSlot(side, i)
			);
		}

		public void writeToNBT(ListNBT list)
		{
			for(int i = 0; i < getSlots(); ++i)
			{
				ItemStack slot = getStackInSlot(i);
				if(!slot.isEmpty())
				{
					CompoundNBT itemTag = new CompoundNBT();
					itemTag.putByte("Slot", (byte)i);
					slot.write(itemTag);
					list.add(itemTag);
				}
			}
		}

		public void readFromNBT(ListNBT list)
		{
			for(int i = 0; i < list.size(); i++)
			{
				CompoundNBT itemTag = list.getCompound(i);
				int slot = itemTag.getByte("Slot")&255;
				if(slot < getSlots())
					setStackInSlot(slot, ItemStack.read(itemTag));
			}
		}
	}

	private enum EnumFilterResult
	{
		INVALID,
		VALID_FILTERED,
		VALID_UNFILTERED
	}
}