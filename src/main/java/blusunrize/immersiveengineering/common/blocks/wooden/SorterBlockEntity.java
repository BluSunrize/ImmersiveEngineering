/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Iterators;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntIterators;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

//TODO Metadata and oredict are gone. Update manual entry as well.
public class SorterBlockEntity extends IEBaseBlockEntity implements IInteractionObjectIE<SorterBlockEntity>, IBlockEntityDrop
{
	public static final int FILTER_SLOTS_PER_SIDE = 8;
	public static final int TOTAL_SLOTS = 6*SorterBlockEntity.FILTER_SLOTS_PER_SIDE;
	public static final DualCodec<ByteBuf, Map<Direction, FilterConfig>> FILTER_CODEC = IEDualCodecs.forMap(
			IEDualCodecs.forEnum(Direction.values()), FilterConfig.CODEC
	);

	public SorterInventory filter;
	public Map<Direction, FilterConfig> sideFilter = Util.make(new EnumMap<>(Direction.class), l -> {
		for(Direction d : Direction.values())
			l.put(d, FilterConfig.DEFAULT);
	});
	/**
	 * The positions of the routers that have been used in the current "outermost" `routeItem` call.
	 * Necessary to stop "blocks" of routers (and similar setups) from causing massive lag (using just a boolean
	 * results in every possible path to be "tested"). Using a set results in effectively a DFS.
	 */
	private static Set<BlockPos> routed = null;

	private final Map<Direction, IEBlockCapabilityCache<IItemHandler>> neighborCaps = IEBlockCapabilityCaches.allNeighbors(
			ItemHandler.BLOCK, this
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
					IEBlockCapabilityCache<IItemHandler> capRef = neighborCaps.get(side);
					IItemHandler itemHandler = capRef.getCapability();
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

	private static DataComponentMap getComponentsWithoutDamage(ItemStack stack)
	{
		return stack.getComponents().filter(type -> type!=DataComponents.DAMAGE);
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
				if(sideFilter.get(side).compareStackToFilterstack(stack, filterStack))
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

		Predicate<ItemStack> matchFilter = concat.isEmpty()?(stack) -> true: new Predicate<>()
		{
			final Set<ItemStack> filter = new HashSet<>(concat);
			final FilterConfig config = sideFilter.get(side0);

			@Override
			public boolean test(ItemStack stack)
			{
				for(ItemStack filterStack : filter)
					if(config.compareStackToFilterstack(stack, filterStack))
						return true;
				return false;
			}
		};

		for(ItemStack filterStack : filter.getFilterStacksOnSide(side1))
			if(!filterStack.isEmpty()&&matchFilter.test(filterStack))
				concat.add(filterStack);

		// TODO this looks dodgy
		final var filterFrom = sideFilter.get(side0);
		final var filterTo = sideFilter.get(side1);
		final boolean concatFuzzy = filterFrom.ignoreDamage||filterTo.ignoreDamage;
		final boolean concatOredict = filterFrom.allowTags||filterTo.allowTags;
		final boolean concatNBT = filterFrom.considerComponents||filterTo.considerComponents;
		final var combinedFilter = new FilterConfig(concatOredict, concatNBT, concatFuzzy);

		return concat.isEmpty()?stack -> true: stack -> {
			for(ItemStack filterStack : concat)
				if(combinedFilter.compareStackToFilterstack(stack, filterStack))
					return true;
			return false;
		};
	}

	public ItemStack outputItemToInv(ItemStack stack, Direction side, boolean simulate)
	{
		return Utils.insertStackIntoInventory(neighborCaps.get(side), stack, simulate);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		sideFilter = FILTER_CODEC.fromNBT(nbt.get("sideFilter"));
		if(!descPacket)
		{
			ListTag filterList = nbt.getList("filter", 10);
			filter = new SorterInventory();
			filter.readFromNBT(provider, filterList);
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.put("sideFilter", FILTER_CODEC.toNBT(sideFilter));
		if(!descPacket)
		{
			ListTag filterList = new ListTag();
			filter.writeToNBT(provider, filterList);
			nbt.put("filter", filterList);
		}
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		CompoundTag data = new CompoundTag();
		writeCustomNBT(data, false, context.getLevel().registryAccess());
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(data));
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final var data = ctx.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
		if(data!=null)
			readCustomNBT(data.copyTag(), false, ctx.getLevel().registryAccess());
	}

	private final EnumMap<Direction, IItemHandler> insertionHandlers = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
			insertionHandlers.put(f, new SorterInventoryHandler(this, f));
	}

	public static void registerCapabilities(BECapabilityRegistrar<SorterBlockEntity> registrar)
	{
		registrar.register(ItemHandler.BLOCK, (be, facing) -> facing!=null?be.insertionHandlers.get(facing): null);
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

		public void writeToNBT(Provider provider, ListTag list)
		{
			for(int i = 0; i < getSlots(); ++i)
			{
				ItemStack slot = getStackInSlot(i);
				if(!slot.isEmpty())
				{
					CompoundTag itemTag = new CompoundTag();
					itemTag.putByte("Slot", (byte)i);
					slot.save(provider, itemTag);
					list.add(itemTag);
				}
			}
		}

		public void readFromNBT(Provider provider, ListTag list)
		{
			for(int i = 0; i < list.size(); i++)
			{
				CompoundTag itemTag = list.getCompound(i);
				int slot = itemTag.getByte("Slot")&255;
				if(slot < getSlots())
					setStackInSlot(slot, ItemStack.parseOptional(provider, itemTag));
			}
		}
	}

	private enum EnumFilterResult
	{
		INVALID,
		VALID_FILTERED,
		VALID_UNFILTERED
	}

	public record FilterConfig(boolean allowTags, boolean considerComponents, boolean ignoreDamage)
	{
		public static final FilterConfig DEFAULT = new FilterConfig(false, false, false);
		public static final DualCodec<ByteBuf, FilterConfig> CODEC = DualCompositeCodecs.composite(
				DualCodecs.BOOL.fieldOf("allowTags"), FilterConfig::allowTags,
				DualCodecs.BOOL.fieldOf("considerComponents"), FilterConfig::considerComponents,
				DualCodecs.BOOL.fieldOf("ignoreDamage"), FilterConfig::ignoreDamage,
				FilterConfig::new
		);

		public boolean compareStackToFilterstack(ItemStack stack, ItemStack filterStack)
		{
			// "Item level" tests
			if(allowTags)
			{
				if(stack.getItem().builtInRegistryHolder().tags().noneMatch(filterStack::is))
					return false;
			}
			else if(!ItemStack.isSameItem(filterStack, stack))
				return false;
			// "NBT level" tests
			if(!ignoreDamage&&(stack.isDamageableItem()||filterStack.isDamageableItem()))
			{
				final int damageStack = stack.getDamageValue();
				final int damageFilter = filterStack.getDamageValue();
				if(damageStack!=damageFilter)
					return false;
			}
			if(considerComponents)
			{
				final var stackTag = getComponentsWithoutDamage(stack);
				final var filterTag = getComponentsWithoutDamage(filterStack);
				if(!stackTag.equals(filterTag))
					return false;
			}
			return true;
		}
	}
}