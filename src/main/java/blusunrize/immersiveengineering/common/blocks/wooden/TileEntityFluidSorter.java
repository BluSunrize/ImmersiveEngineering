/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

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
public class TileEntityFluidSorter extends TileEntityIEBase implements IInteractionObjectIE
{
	public static TileEntityType<TileEntityFluidSorter> TYPE;

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

	public TileEntityFluidSorter()
	{
		super(TYPE);
		for(Direction f : Direction.VALUES)
			neighborCaps.put(f, CapabilityReference.forNeighbor(this, FLUID_HANDLER_CAPABILITY, f));
	}

	public int routeFluid(Direction inputSide, FluidStack stack, boolean doFill)
	{
		int ret = 0;
		if(!world.isRemote&&canRoute())
		{
			boolean first = startRouting();
			Direction[][] validOutputs = getValidOutputs(inputSide, stack);
			ret += doInsert(stack, validOutputs[0], doFill);
			ret += doInsert(stack, validOutputs[1], doFill);
			if(first)
				usedRouters = null;
		}
		return ret;
	}

	private boolean canRoute()
	{
		return usedRouters==null||!usedRouters.contains(pos);
	}

	private boolean startRouting()
	{
		boolean first = usedRouters==null;
		if(first)
			usedRouters = new HashSet<>();
		usedRouters.add(pos);
		return first;
	}

	private int doInsert(FluidStack stack, Direction[] sides, boolean doFill)
	{
		int ret = 0;
		int lengthFiltered = sides.length;
		while(lengthFiltered > 0&&stack.amount>0)
		{
			int rand = Utils.RAND.nextInt(lengthFiltered);
			Direction currentSide = sides[rand];
			CapabilityReference<IFluidHandler> capRef = neighborCaps.get(currentSide);
			IFluidHandler fluidOut = capRef.get();
			if(fluidOut!=null)
			{
				int filledHere = fluidOut.fill(stack, doFill);
				stack.amount -= filledHere;
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
	public boolean canUseGui(EntityPlayer player)
	{
		return true;
	}

	@Override
	public ResourceLocation getGuiName()
	{
		return Lib.GUIID_FluidSorter;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return this;
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.hasKey("sideConfig"))
			this.sortWithNBT = message.getByteArray("sideConfig");
		if(message.hasKey("filter_side"))
		{
			int side = message.getInt("filter_side");
			int slot = message.getInt("filter_slot");
			this.filters[side][slot] = FluidStack.loadFluidStackFromNBT(message.getCompound("filter"));
		}
		this.markDirty();
	}

	public Direction[][] getValidOutputs(Direction inputSide, @Nullable FluidStack fluidStack)
	{
		if(fluidStack==null)
			return new Direction[2][0];
		ArrayList<Direction> validFilteredInvOuts = new ArrayList<>(6);
		ArrayList<Direction> validUnfilteredInvOuts = new ArrayList<>(6);
		for(Direction side : Direction.values())
			if(side!=inputSide&&world.isBlockLoaded(getPos().offset(side)))
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
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		sortWithNBT = nbt.getByteArray("sortWithNBT");
		for(int side = 0; side < 6; side++)
		{
			ListNBT filterList = nbt.getList("filter_"+side, 10);
			for(int i = 0; i < filterList.size(); i++)
				filters[side][i] = FluidStack.loadFluidStackFromNBT(filterList.getCompound(i));
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.setByteArray("sortWithNBT", sortWithNBT);
		for(int side = 0; side < 6; side++)
		{
			ListNBT filterList = new ListNBT();
			for(int i = 0; i < filters[side].length; i++)
			{
				CompoundNBT tag = new CompoundNBT();
				if(filters[side][i]!=null)
					filters[side][i].writeToNBT(tag);
				filterList.add(tag);
			}
			nbt.setTag("filter_"+side, filterList);
		}
	}


	private EnumMap<Direction, LazyOptional<IFluidHandler>> insertionHandlers = new EnumMap<>(Direction.class);

	{
		for(Direction f : Direction.VALUES)
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
		TileEntityFluidSorter tile;
		Direction facing;

		SorterFluidHandler(TileEntityFluidSorter tile, Direction facing)
		{
			this.tile = tile;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			if(resource==null)
				return 0;
			return tile.routeFluid(facing, resource, doFill);
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain)
		{
			return null;
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain)
		{
			return null;
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			return new IFluidTankProperties[]{new FluidTankProperties(null, 0)};
		}
	}
}
