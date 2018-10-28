/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityMultiblockPart<T extends TileEntityMultiblockPart<T>> extends TileEntityIEBase
		implements ITickable, IDirectionalTile, IBlockBounds, IGeneralMultiblock
{
	public boolean formed = false;
	public int pos = -1;
	public int[] offset = {0, 0, 0};
	public boolean mirrored = false;
	public EnumFacing facing = EnumFacing.NORTH;
	// stores the world time at which this block can only be disassembled by breaking the block associated with this TE.
	// This prevents half/duplicate disassembly when working with the drill or TCon hammers
	public long onlyLocalDissassembly = -1;
	/**
	 * H L W
	 */
	protected final int[] structureDimensions;

	protected TileEntityMultiblockPart(int[] structureDimensions)
	{
		this.structureDimensions = structureDimensions;
	}

	@Override
	public EnumFacing getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}


	//	=================================
	//		DATA MANAGEMENT
	//	=================================
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		formed = nbt.getBoolean("formed");
		pos = nbt.getInteger("pos");
		offset = nbt.getIntArray("offset");
		mirrored = nbt.getBoolean("mirrored");
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("formed", formed);
		nbt.setInteger("pos", pos);
		nbt.setIntArray("offset", offset);
		nbt.setBoolean("mirrored", mirrored);
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&facing!=null&&this.getAccessibleFluidTanks(facing).length > 0)
			return true;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&facing!=null&&this.getAccessibleFluidTanks(facing).length > 0)
			return (T)new MultiblockFluidWrapper(this, facing);
		return super.getCapability(capability, facing);
	}

	//	=================================
	//		FLUID MANAGEMENT
	//	=================================
	@Nonnull
	protected abstract IFluidTank[] getAccessibleFluidTanks(EnumFacing side);

	protected abstract boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource);

	protected abstract boolean canDrainTankFrom(int iTank, EnumFacing side);

	public static class MultiblockFluidWrapper implements IFluidHandler
	{
		final TileEntityMultiblockPart multiblock;
		final EnumFacing side;

		public MultiblockFluidWrapper(TileEntityMultiblockPart multiblock, EnumFacing side)
		{
			this.multiblock = multiblock;
			this.side = side;
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			if(!this.multiblock.formed)
				return new IFluidTankProperties[0];
			IFluidTank[] tanks = this.multiblock.getAccessibleFluidTanks(side);
			IFluidTankProperties[] array = new IFluidTankProperties[tanks.length];
			for(int i = 0; i < tanks.length; i++)
				array[i] = new FluidTankProperties(tanks[i].getFluid(), tanks[i].getCapacity());
			return array;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			if(!this.multiblock.formed||resource==null)
				return 0;
			IFluidTank[] tanks = this.multiblock.getAccessibleFluidTanks(side);
			int fill = -1;
			for(int i = 0; i < tanks.length; i++)
			{
				IFluidTank tank = tanks[i];
				if(tank!=null&&this.multiblock.canFillTankFrom(i, side, resource)&&tank.getFluid()!=null&&tank.getFluid().isFluidEqual(resource))
				{
					fill = tank.fill(resource, doFill);
					if(fill > 0)
						break;
				}
			}
			if(fill==-1)
				for(int i = 0; i < tanks.length; i++)
				{
					IFluidTank tank = tanks[i];
					if(tank!=null&&this.multiblock.canFillTankFrom(i, side, resource))
					{
						fill = tank.fill(resource, doFill);
						if(fill > 0)
							break;
					}
				}
			if(fill > 0)
				this.multiblock.updateMasterBlock(null, true);
			return fill < 0?0: fill;
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain)
		{
			if(!this.multiblock.formed||resource==null)
				return null;
			IFluidTank[] tanks = this.multiblock.getAccessibleFluidTanks(side);
			FluidStack drain = null;
			for(int i = 0; i < tanks.length; i++)
			{
				IFluidTank tank = tanks[i];
				if(tank!=null&&this.multiblock.canDrainTankFrom(i, side))
				{
					if(tank instanceof IFluidHandler)
						drain = ((IFluidHandler)tank).drain(resource, doDrain);
					else
						drain = tank.drain(resource.amount, doDrain);
					if(drain!=null)
						break;
				}
			}
			if(drain!=null)
				this.multiblock.updateMasterBlock(null, true);
			return drain;
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain)
		{
			if(!this.multiblock.formed||maxDrain==0)
				return null;
			IFluidTank[] tanks = this.multiblock.getAccessibleFluidTanks(side);
			FluidStack drain = null;
			for(int i = 0; i < tanks.length; i++)
			{
				IFluidTank tank = tanks[i];
				if(tank!=null&&this.multiblock.canDrainTankFrom(i, side))
				{
					drain = tank.drain(maxDrain, doDrain);
					if(drain!=null)
						break;
				}
			}
			if(drain!=null)
				this.multiblock.updateMasterBlock(null, true);
			return drain;
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Nullable
	public T master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return (T)this;
		BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(T)te: null;
	}

	public void updateMasterBlock(IBlockState state, boolean blockUpdate)
	{
		T master = master();
		if(master!=null)
		{
			master.markDirty();
			if(blockUpdate)
				master.markContainingBlockForUpdate(state);
		}
	}

	public boolean isDummy()
	{
		return offset[0]!=0||offset[1]!=0||offset[2]!=0;
	}

	@Override
	public boolean isLogicDummy()
	{
		return isDummy();
	}

	public abstract ItemStack getOriginalBlock();

	public void disassemble()
	{
		if(formed&&!world.isRemote)
		{
			BlockPos startPos = getOrigin();
			BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
			long time = world.getTotalWorldTime();
			for(int yy = 0; yy < structureDimensions[0]; yy++)
				for(int ll = 0; ll < structureDimensions[1]; ll++)
					for(int ww = 0; ww < structureDimensions[2]; ww++)
					{
						int w = mirrored?-ww: ww;
						BlockPos pos = startPos.offset(facing, ll).offset(facing.rotateY(), w).add(0, yy, 0);
						ItemStack s = ItemStack.EMPTY;

						TileEntity te = world.getTileEntity(pos);
						if(te instanceof TileEntityMultiblockPart)
						{
							TileEntityMultiblockPart part = (TileEntityMultiblockPart)te;
							Vec3i diff = pos.subtract(masterPos);
							if(part.offset[0]!=diff.getX()||part.offset[1]!=diff.getY()||part.offset[2]!=diff.getZ())
								continue;
							else if(time!=part.onlyLocalDissassembly)
							{
								s = part.getOriginalBlock();
								part.formed = false;
							}
						}
						if(pos.equals(getPos()))
							s = this.getOriginalBlock();
						IBlockState state = Utils.getStateFromItemStack(s);
						if(state!=null)
						{
							if(pos.equals(getPos()))
								world.spawnEntity(new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, s));
							else
								replaceStructureBlock(pos, state, s, yy, ll, ww);
						}
					}
		}
	}

	public BlockPos getOrigin()
	{
		return getBlockPosForPos(0);
	}

	public BlockPos getBlockPosForPos(int targetPos)
	{
		int blocksPerLevel = structureDimensions[1]*structureDimensions[2];
		// dist = target position - current position
		int distH = (targetPos/blocksPerLevel)-(pos/blocksPerLevel);
		int distL = (targetPos%blocksPerLevel/structureDimensions[2])-(pos%blocksPerLevel/structureDimensions[2]);
		int distW = (targetPos%structureDimensions[2])-(pos%structureDimensions[2]);
		int w = mirrored?-distW: distW;
		return getPos().offset(facing, distL).offset(facing.rotateY(), w).add(0, distH, 0);
	}

	public void replaceStructureBlock(BlockPos pos, IBlockState state, ItemStack stack, int h, int l, int w)
	{
		if(state.getBlock()==this.getBlockType())
			world.setBlockToAir(pos);
		world.setBlockState(pos, state);
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof ITileDrop)
			((ITileDrop)tile).readOnPlacement(null, stack);
	}
}