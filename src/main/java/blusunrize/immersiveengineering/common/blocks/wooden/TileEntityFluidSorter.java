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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;

/**
 * @author BluSunrize - 02.03.2017
 */
public class TileEntityFluidSorter extends TileEntityIEBase implements IGuiTile
{
	public byte[] sortWithNBT = {1,1,1,1,1,1};
	//	public static final int filterSlotsPerSide = 8;
	public FluidStack[][] filters = new FluidStack[6][8];
	private boolean isRouting = false;

	public int routeFluid(EnumFacing inputSide, FluidStack stack, boolean doFill)
	{
		if(!world.isRemote&&!isRouting)
		{
			this.isRouting = true;
			IFluidHandler[][] validOutputs = getValidOutputs(inputSide, stack, true);
			if(validOutputs[0].length>0)
			{
				int rand = Utils.RAND.nextInt(validOutputs[0].length);
				int accepted = validOutputs[0][rand].fill(stack.copy(), doFill);
				if(accepted>0)
				{
					isRouting = false;
					return accepted;
				}
			}
			if(validOutputs[1].length>0)
			{
				int rand = Utils.RAND.nextInt(validOutputs[1].length);
				int accepted = validOutputs[1][rand].fill(stack.copy(), doFill);
				if(accepted>0)
				{
					isRouting = false;
					return accepted;
				}
			}
			isRouting = false;
		}
		return 0;
	}

	public boolean doNBT(int side)
	{
		if(side>=0 && side<this.sortWithNBT.length)
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

	public IFluidHandler[][] getValidOutputs(EnumFacing inputSide, FluidStack fluidStack, boolean allowUnmapped)
	{
		if(fluidStack==null)
			return new IFluidHandler[2][0];
		ArrayList<IFluidHandler> validFilteredInvOuts = new ArrayList<IFluidHandler>(6);
		ArrayList<IFluidHandler> validUnfilteredInvOuts = new ArrayList<IFluidHandler>(6);
		for(EnumFacing side : EnumFacing.values())
			if(side!=inputSide && world.isBlockLoaded(getPos().offset(side)))
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
								allowed=true;
								break filterIteration;
							}
						}
				}
				if(allowed || (allowUnmapped&&unmapped))
				{
					TileEntity tile = Utils.getExistingTileEntity(world, getPos().offset(side));
					if(tile!=null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()))
					{
						IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
						if(handler.fill(fluidStack.copy(), false) > 0)
							if(allowed)
								validFilteredInvOuts.add(handler);
							else
								validUnfilteredInvOuts.add(handler);
					}
				}
			}
		return new IFluidHandler[][]{
				validFilteredInvOuts.toArray(new IFluidHandler[validFilteredInvOuts.size()]),
				validUnfilteredInvOuts.toArray(new IFluidHandler[validUnfilteredInvOuts.size()]),
		};
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sortWithNBT = nbt.getByteArray("sortWithNBT");
		for(int side=0; side<6; side++)
		{
			NBTTagList filterList = nbt.getTagList("filter_"+side, 10);
			for(int i=0; i<filterList.tagCount(); i++)
				filters[side][i] = FluidStack.loadFluidStackFromNBT(filterList.getCompoundTagAt(i));
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setByteArray("sortWithNBT", sortWithNBT);
		for(int side=0; side<6; side++)
		{
			NBTTagList filterList = new NBTTagList();
			for(int i=0; i<filters[side].length; i++)
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
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing!=null)
			return true;
		return super.hasCapability(capability, facing);
	}
	IFluidHandler[] insertionHandlers = {
			new SorterFluidHandler(this,EnumFacing.DOWN),
			new SorterFluidHandler(this,EnumFacing.UP),
			new SorterFluidHandler(this,EnumFacing.NORTH),
			new SorterFluidHandler(this,EnumFacing.SOUTH),
			new SorterFluidHandler(this,EnumFacing.WEST),
			new SorterFluidHandler(this,EnumFacing.EAST)};

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing!=null)
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
			if(resource == null)
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
			return new IFluidTankProperties[]{new FluidTankProperties(null,0)};
		}
	}
}
