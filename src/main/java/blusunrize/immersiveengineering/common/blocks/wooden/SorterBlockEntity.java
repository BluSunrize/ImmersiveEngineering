/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.IntIterators;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

//TODO Metadata and oredict are gone. Update manual entry as well.
public class SorterBlockEntity extends IEBaseBlockEntity implements IInteractionObjectIE<SorterBlockEntity>, IBlockEntityDrop
{
	public static final int FILTER_SLOTS_PER_SIDE = 8;
	public static final int TOTAL_SLOTS = 6*SorterBlockEntity.FILTER_SLOTS_PER_SIDE;

	public SorterInventory filter;
	public int[] sideFilter = {0, 0, 0, 0, 0, 0};//OreDict,nbt,fuzzy
	/**
	 * The positions of the routers that have been used in the current "outermost" `routeItem` call.
	 * Necessary to stop "blocks" of routers (and similar setups) from causing massive lag (using just a boolean
	 * results in every possible path to be "tested"). Using a set results in effectively a DFS.
	 */
	private static Set<BlockPos> routed = null;

	private final Map<Direction, CapabilityReference<IItemHandler>> neighborCaps = CapabilityReference.forAllNeighbors(
			this, ITEM_HANDLER
	);

	public SorterBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.SORTER.get(), pos, state);
		filter = new SorterInventory();
	}


	public ItemStack routeItem(Direction inputSide, ItemStack stack, boolean simulate)
	{
		if(!level.isClientSide&&canRoute())
		{
			boolean first = startRouting();
			Direction[][] validOutputs = getValidOutputs(inputSide, stack);
			stack = doInsert(stack, validOutputs[0], simulate);
			// Only if no filtered outputs were found, use unfiltered
			if(validOutputs[0].length==0||!stack.isEmpty())
				stack = doInsert(stack, validOutputs[1], simulate);
			if(first)
				routed = null;
		}
		return stack;
	}

	private boolean canRoute()
	{
		return routed==null||!routed.contains(worldPosition);
	}

	private boolean startRouting()
	{
		boolean first = routed==null;
		if(first)
			routed = new HashSet<>();
		routed.add(worldPosition);
		return first;
	}

	private ItemStack doInsert(ItemStack stack, Direction[] sides, boolean simulate)
	{
		int lengthFiltered = sides.length;
		while(lengthFiltered > 0&&!stack.isEmpty())
		{
			int rand = ApiUtils.RANDOM.nextInt(lengthFiltered);
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
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public SorterBlockEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public ArgContainer<SorterBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.SORTER;
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
		if(!level.isClientSide&&canRoute())
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

	private static final CompoundTag EMPTY_NBT = new CompoundTag();

	private boolean compareStackToFilterstack(ItemStack stack, ItemStack filterStack, boolean fuzzy, boolean oredict, boolean nbt)
	{
		// "Item level" tests
		if(oredict)
		{
			if(!stack.getItem().builtInRegistryHolder().tags().anyMatch(filterStack::is))
				return false;
		}
		else if(!ItemStack.isSameItem(filterStack, stack))
			return false;
		// "NBT level" tests
		if(!fuzzy&&(stack.isDamageableItem()||filterStack.isDamageableItem()))
		{
			final int damageStack = stack.getDamageValue();
			final int damageFilter = filterStack.getDamageValue();
			if(damageStack!=damageFilter)
				return false;
		}
		if(nbt)
		{
			final CompoundTag stackTag = getTagWithoutDamage(stack);
			final CompoundTag filterTag = getTagWithoutDamage(filterStack);
			if(!stackTag.equals(filterTag))
				return false;
		}
		return true;
	}

	private static CompoundTag getTagWithoutDamage(ItemStack stack)
	{
		final CompoundTag directTag = stack.getTag();
		if(directTag==null)
			return EMPTY_NBT;
		final CompoundTag tagCopy = directTag.copy();
		tagCopy.remove(ItemStack.TAG_DAMAGE);
		return tagCopy;
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
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		sideFilter = nbt.getIntArray("sideFilter");
		if(!descPacket)
		{
			ListTag filterList = nbt.getList("filter", 10);
			filter = new SorterInventory();
			filter.readFromNBT(filterList);
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putIntArray("sideFilter", sideFilter);
		if(!descPacket)
		{
			ListTag filterList = new ListTag();
			filter.writeToNBT(filterList);
			nbt.put("filter", filterList);
		}
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		writeCustomNBT(stack.getOrCreateTag(), false);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		if(stack.hasTag())
			readCustomNBT(stack.getOrCreateTag(), false);
	}

	private final EnumMap<Direction, ResettableCapability<IItemHandler>> insertionHandlers = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
		{
			ResettableCapability<IItemHandler> forSide = registerCapability(new SorterInventoryHandler(this, f));
			insertionHandlers.put(f, forSide);
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==ITEM_HANDLER&&facing!=null)
			return insertionHandlers.get(facing).cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		return id==0;
	}

	public static class SorterInventoryHandler implements IItemHandlerModifiable
	{
		SorterBlockEntity sorter;
		Direction side;

		public SorterInventoryHandler(SorterBlockEntity sorter, Direction side)
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
			super(NonNullList.withSize(6*FILTER_SLOTS_PER_SIDE, ItemStack.EMPTY));
		}

		public ItemStack getStackBySideAndSlot(Direction side, int slotOnSide)
		{
			return getStackInSlot(getSlotId(side, slotOnSide));
		}

		public int getSlotId(Direction side, int slotOnSide)
		{
			return side.ordinal()*FILTER_SLOTS_PER_SIDE+slotOnSide;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 1;
		}

		public Iterable<ItemStack> getFilterStacksOnSide(Direction side)
		{
			return () -> Iterators.transform(
					IntIterators.fromTo(0, FILTER_SLOTS_PER_SIDE), i -> getStackBySideAndSlot(side, i)
			);
		}

		public void writeToNBT(ListTag list)
		{
			for(int i = 0; i < getSlots(); ++i)
			{
				ItemStack slot = getStackInSlot(i);
				if(!slot.isEmpty())
				{
					CompoundTag itemTag = new CompoundTag();
					itemTag.putByte("Slot", (byte)i);
					slot.save(itemTag);
					list.add(itemTag);
				}
			}
		}

		public void readFromNBT(ListTag list)
		{
			for(int i = 0; i < list.size(); i++)
			{
				CompoundTag itemTag = list.getCompound(i);
				int slot = itemTag.getByte("Slot")&255;
				if(slot < getSlots())
					setStackInSlot(slot, ItemStack.of(itemTag));
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