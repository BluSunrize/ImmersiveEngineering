/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 02.03.2017
 */
public class FluidSorterBlockEntity extends IEBaseBlockEntity implements IInteractionObjectIE<FluidSorterBlockEntity>, IFluidPipe, IBlockEntityDrop
{
	public byte[] sortWithNBT = {1, 1, 1, 1, 1, 1};
	public static final int FILTER_SLOTS_PER_SIDE = 8;
	public FluidStack[][] filters = makeFilterArray();
	/**
	 * The positions of the routers that have been used in the current "outermost" `routeFluid` call.
	 * Necessary to stop "blocks" of routers (and similar setups) from causing massive lag (using just a boolean
	 * results in every possible path to be "tested"). Using a set results in effectively a DFS.
	 */
	private static Set<BlockPos> usedRouters = null;
	private final Map<Direction, IEBlockCapabilityCache<IFluidHandler>> neighborCaps = IEBlockCapabilityCaches.allNeighbors(
			FluidHandler.BLOCK, this
	);

	public FluidSorterBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.FLUID_SORTER.get(), pos, state);
	}

	public int routeFluid(Direction inputSide, FluidStack stack, FluidAction doFill)
	{
		int ret = 0;
		if(!level.isClientSide&&canRoute())
		{
			boolean first = startRouting();
			Direction[][] validOutputs = getValidOutputs(inputSide, stack);
			ret += doInsert(stack, validOutputs[0], doFill);
			// Only if no filtered outputs were found, use unfiltered
			if(validOutputs[0].length==0)
				ret += doInsert(stack, validOutputs[1], doFill);
			if(first)
				usedRouters = null;
		}
		return ret;
	}

	private boolean canRoute()
	{
		return usedRouters==null||!usedRouters.contains(worldPosition);
	}

	private boolean startRouting()
	{
		boolean first = usedRouters==null;
		if(first)
			usedRouters = new HashSet<>();
		usedRouters.add(worldPosition);
		return first;
	}

	private int doInsert(FluidStack stack, Direction[] sides, FluidAction doFill)
	{
		int ret = 0;
		FluidStack available = stack.copy();
		int lengthFiltered = sides.length;
		while(lengthFiltered > 0&&available.getAmount() > 0)
		{
			int rand = ApiUtils.RANDOM.nextInt(lengthFiltered);
			Direction currentSide = sides[rand];
			IFluidHandler fluidOut = neighborCaps.get(currentSide).getCapability();
			if(fluidOut!=null)
			{
				int filledHere = fluidOut.fill(available, doFill);
				available.shrink(filledHere);
				ret += filledHere;
			}
			sides[rand] = sides[lengthFiltered-1];
			lengthFiltered--;
		}
		return ret;
	}


	public boolean doNBT(int side)
	{
		if(side >= 0&&side < this.sortWithNBT.length)
			return this.sortWithNBT[side]==1;
		return false;
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public FluidSorterBlockEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public ArgContainer<FluidSorterBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.FLUID_SORTER;
	}

	public Direction[][] getValidOutputs(Direction inputSide, @Nullable FluidStack fluidStack)
	{
		if(fluidStack==null||fluidStack.isEmpty())
			return new Direction[2][0];
		// Strip pressure tag, since it confuses the sorting
		fluidStack = fluidStack.copyWithAmount(1);
		fluidStack.remove(IEApiDataComponents.FLUID_PRESSURIZED);

		ArrayList<Direction> validFilteredInvOuts = new ArrayList<>(6);
		ArrayList<Direction> validUnfilteredInvOuts = new ArrayList<>(6);
		for(Direction side : Direction.values())
			if(side!=inputSide&&level.hasChunkAt(getBlockPos().relative(side)))
			{
				boolean unmapped = true;
				boolean allowed = false;
				filterIteration:
				{
					for(FluidStack filterStack : filters[side.ordinal()])
						if(!filterStack.isEmpty())
						{
							unmapped = false;
							boolean b = filterStack.getFluid()==fluidStack.getFluid();
							if(doNBT(side.ordinal()))
								b &= filterStack.getComponents().equals(fluidStack.getComponents());
							if(b)
							{
								allowed = true;
								break filterIteration;
							}
						}
				}
				if(allowed)
					validFilteredInvOuts.add(side);
				else if(unmapped)
					validUnfilteredInvOuts.add(side);
			}
		return new Direction[][]{
				validFilteredInvOuts.toArray(new Direction[0]),
				validUnfilteredInvOuts.toArray(new Direction[0])
		};
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		sortWithNBT = nbt.getByteArray("sortWithNBT");
		for(int side = 0; side < 6; side++)
		{
			ListTag filterList = nbt.getList("filter_"+side, 10);
			for(int i = 0; i < filterList.size(); i++)
				filters[side][i] = FluidStack.parseOptional(provider, filterList.getCompound(i));
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.putByteArray("sortWithNBT", sortWithNBT);
		for(int side = 0; side < 6; side++)
		{
			ListTag filterList = new ListTag();
			for(int i = 0; i < filters[side].length; i++)
				filterList.add(filters[side][i].saveOptional(provider));
			nbt.put("filter_"+side, filterList);
		}
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundTag data = new CompoundTag();
		writeCustomNBT(data, false, context.getLevel().registryAccess());
		BlockItem.setBlockEntityData(stack, this.getType(), data);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		var data = ctx.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
		if(data!=null)
			readCustomNBT(data.copyTag(), false, ctx.getLevel().registryAccess());
	}


	private final EnumMap<Direction, IFluidHandler> insertionHandlers = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
			insertionHandlers.put(f, new SorterFluidHandler(this, f));
	}

	public static void registerCapabilities(BECapabilityRegistrar<FluidSorterBlockEntity> registrar)
	{
		registrar.register(FluidHandler.BLOCK, (be, facing) -> facing!=null?be.insertionHandlers.get(facing): null);
	}

	public static FluidStack[][] makeFilterArray()
	{
		FluidStack[][] filters = new FluidStack[DirectionUtils.VALUES.length][FILTER_SLOTS_PER_SIDE];
		for(FluidStack[] sideFilter : filters)
			Arrays.fill(sideFilter, FluidStack.EMPTY);
		return filters;
	}

	static class SorterFluidHandler implements IFluidHandler
	{
		FluidSorterBlockEntity tile;
		Direction facing;

		SorterFluidHandler(FluidSorterBlockEntity tile, Direction facing)
		{
			this.tile = tile;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action)
		{
			if(resource.isEmpty())
				return 0;
			return tile.routeFluid(facing, resource, action);
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			return FluidStack.EMPTY;
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			return FluidStack.EMPTY;
		}

		@Override
		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank)
		{
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return FluidType.BUCKET_VOLUME;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return true;
		}
	}
}
