/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author BluSunrize - 02.03.2017
 */
public class TileEntityFluidSorter extends TileEntityIEBase implements IGuiTile
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

	public int routeFluid(EnumFacing inputSide, FluidStack stack, boolean doFill)
	{
		int ret = 0;
		if(!world.isRemote&&canRoute())
		{
			boolean first = startRouting();
			EnumFacing[][] validOutputs = getValidOutputs(inputSide, stack);
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

	private int doInsert(FluidStack stack, EnumFacing[] sides, boolean doFill)
	{
		int ret = 0;
		int lengthFiltered = sides.length;
		while(lengthFiltered > 0&&stack.amount>0)
		{
			int rand = Utils.RAND.nextInt(lengthFiltered);
			EnumFacing currentSide = sides[rand];
			TileEntity te = Utils.getExistingTileEntity(world, pos.offset(currentSide));
			if (te!=null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, currentSide.getOpposite()))
			{
				IFluidHandler fluidOut = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
						currentSide.getOpposite());
				assert fluidOut!=null;
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
	public boolean canOpenGui()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return Lib.GUIID_FluidSorter;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		if(message.hasKey("sideConfig"))
			this.sortWithNBT = message.getByteArray("sideConfig");
		if(message.hasKey("filter_side"))
		{
			int side = message.getInteger("filter_side");
			int slot = message.getInteger("filter_slot");
			this.filters[side][slot] = FluidStack.loadFluidStackFromNBT(message.getCompoundTag("filter"));
		}
		this.markDirty();
	}

	public EnumFacing[][] getValidOutputs(EnumFacing inputSide, @Nullable FluidStack fluidStack)
	{
		if(fluidStack==null)
			return new EnumFacing[2][0];
		ArrayList<EnumFacing> validFilteredInvOuts = new ArrayList<>(6);
		ArrayList<EnumFacing> validUnfilteredInvOuts = new ArrayList<>(6);
		for(EnumFacing side : EnumFacing.values())
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
		return new EnumFacing[][]{
				validFilteredInvOuts.toArray(new EnumFacing[0]),
				validUnfilteredInvOuts.toArray(new EnumFacing[0])
		};
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sortWithNBT = nbt.getByteArray("sortWithNBT");
		for(int side = 0; side < 6; side++)
		{
			NBTTagList filterList = nbt.getTagList("filter_"+side, 10);
			for(int i = 0; i < filterList.tagCount(); i++)
				filters[side][i] = FluidStack.loadFluidStackFromNBT(filterList.getCompoundTagAt(i));
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setByteArray("sortWithNBT", sortWithNBT);
		for(int side = 0; side < 6; side++)
		{
			NBTTagList filterList = new NBTTagList();
			for(int i = 0; i < filters[side].length; i++)
			{
				NBTTagCompound tag = new NBTTagCompound();
				if(filters[side][i]!=null)
					filters[side][i].writeToNBT(tag);
				filterList.appendTag(tag);
			}
			nbt.setTag("filter_"+side, filterList);
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&facing!=null)
			return true;
		return super.hasCapability(capability, facing);
	}

	IFluidHandler[] insertionHandlers = {
			new SorterFluidHandler(this, EnumFacing.DOWN),
			new SorterFluidHandler(this, EnumFacing.UP),
			new SorterFluidHandler(this, EnumFacing.NORTH),
			new SorterFluidHandler(this, EnumFacing.SOUTH),
			new SorterFluidHandler(this, EnumFacing.WEST),
			new SorterFluidHandler(this, EnumFacing.EAST)};

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&facing!=null)
			return (T)insertionHandlers[facing.ordinal()];
		return super.getCapability(capability, facing);
	}

	static class SorterFluidHandler implements IFluidHandler
	{
		TileEntityFluidSorter tile;
		EnumFacing facing;

		SorterFluidHandler(TileEntityFluidSorter tile, EnumFacing facing)
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
