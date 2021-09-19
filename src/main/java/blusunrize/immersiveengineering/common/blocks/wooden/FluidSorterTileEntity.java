/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

/**
 * @author BluSunrize - 02.03.2017
 */
public class FluidSorterTileEntity extends IEBaseTileEntity implements IInteractionObjectIE
{
	public byte[] sortWithNBT = {1, 1, 1, 1, 1, 1};
	//	public static final int filterSlotsPerSide = 8;
	public FluidStack[][] filters = new FluidStack[6][8];
	/**
	 * The positions of the routers that have been used in the current "outermost" `routeFluid` call.
	 * Necessary to stop "blocks" of routers (and similar setups) from causing massive lag (using just a boolean
	 * results in every possible path to be "tested"). Using a set results in effectively a DFS.
	 */
	private static Set<BlockPos> usedRouters = null;
	private EnumMap<Direction, CapabilityReference<IFluidHandler>> neighborCaps = new EnumMap<>(Direction.class);

	public FluidSorterTileEntity()
	{
		super(IETileTypes.FLUID_SORTER.get());
		for(Direction f : DirectionUtils.VALUES)
			neighborCaps.put(f, CapabilityReference.forNeighbor(this, FLUID_HANDLER_CAPABILITY, f));
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
			int rand = Utils.RAND.nextInt(lengthFiltered);
			Direction currentSide = sides[rand];
			CapabilityReference<IFluidHandler> capRef = neighborCaps.get(currentSide);
			IFluidHandler fluidOut = capRef.getNullable();
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
	public IInteractionObjectIE getGuiMaster()
	{
		return this;
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("sideConfig", NBT.TAG_BYTE_ARRAY))
			this.sortWithNBT = message.getByteArray("sideConfig");
		if(message.contains("filter_side", NBT.TAG_INT))
		{
			int side = message.getInt("filter_side");
			int slot = message.getInt("filter_slot");
			this.filters[side][slot] = FluidStack.loadFluidStackFromNBT(message.getCompound("filter"));
		}
		this.setChanged();
	}

	public Direction[][] getValidOutputs(Direction inputSide, @Nullable FluidStack fluidStack)
	{
		if(fluidStack==null)
			return new Direction[2][0];
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
						if(filterStack!=null)
						{
							unmapped = false;
							boolean b = filterStack.getFluid()==fluidStack.getFluid();
							if(doNBT(side.ordinal()))
								b &= FluidStack.areFluidStackTagsEqual(filterStack, fluidStack);
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
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		sortWithNBT = nbt.getByteArray("sortWithNBT");
		for(int side = 0; side < 6; side++)
		{
			ListTag filterList = nbt.getList("filter_"+side, 10);
			for(int i = 0; i < filterList.size(); i++)
				filters[side][i] = FluidStack.loadFluidStackFromNBT(filterList.getCompound(i));
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putByteArray("sortWithNBT", sortWithNBT);
		for(int side = 0; side < 6; side++)
		{
			ListTag filterList = new ListTag();
			for(int i = 0; i < filters[side].length; i++)
			{
				CompoundTag tag = new CompoundTag();
				if(filters[side][i]!=null)
					filters[side][i].writeToNBT(tag);
				filterList.add(tag);
			}
			nbt.put("filter_"+side, filterList);
		}
	}


	private EnumMap<Direction, LazyOptional<IFluidHandler>> insertionHandlers = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
		{
			LazyOptional<IFluidHandler> forSide = registerConstantCap(new SorterFluidHandler(this, f));
			insertionHandlers.put(f, forSide);
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==FLUID_HANDLER_CAPABILITY&&facing!=null)
			return insertionHandlers.get(facing).cast();
		return super.getCapability(capability, facing);
	}

	static class SorterFluidHandler implements IFluidHandler
	{
		FluidSorterTileEntity tile;
		Direction facing;

		SorterFluidHandler(FluidSorterTileEntity tile, Direction facing)
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
			return FluidAttributes.BUCKET_VOLUME;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return true;
		}
	}
}
