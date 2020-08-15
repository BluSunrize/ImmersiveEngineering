/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.SafeChunkUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Set;

public abstract class MultiblockPartTileEntity<T extends MultiblockPartTileEntity<T>> extends IEBaseTileEntity
		implements ITickableTileEntity, IDirectionalTile, IGeneralMultiblock, IScrewdriverInteraction, IMirrorAble
{
	public boolean formed = false;
	//Position of this block according to the BlockInfo's returned by IMultiblock#getStructure
	public BlockPos posInMultiblock = BlockPos.ZERO;
	//Offset from the master to this block (world coordinate system)
	public BlockPos offsetToMaster = BlockPos.ZERO;
	private final IETemplateMultiblock multiblockInstance;
	// stores the world time at which this block can only be disassembled by breaking the block associated with this TE.
	// This prevents half/duplicate disassembly when working with the drill or TCon hammers
	public long onlyLocalDissassembly = -1;
	protected final Vec3i structureDimensions;
	protected final boolean hasRedstoneControl;
	protected boolean redstoneControlInverted = false;
	//Absent means no controlling computers
	public Optional<Boolean> computerOn = Optional.empty();

	protected MultiblockPartTileEntity(IETemplateMultiblock multiblockInstance, TileEntityType<? extends T> type, boolean hasRSControl)
	{
		super(type);
		this.multiblockInstance = multiblockInstance;
		this.structureDimensions = multiblockInstance.getSize();
		this.hasRedstoneControl = hasRSControl;
	}

	@Override
	public Direction getFacing()
	{
		BlockState state = getBlockState();
		return state.get(IEProperties.FACING_HORIZONTAL);
	}

	@Override
	public void setFacing(Direction facing)
	{
		BlockState oldState = getWorldNonnull().getBlockState(pos);
		BlockState newState = oldState.with(IEProperties.FACING_HORIZONTAL, facing);
		getWorldNonnull().setBlockState(pos, newState);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}


	//	=================================
	//		DATA MANAGEMENT
	//	=================================
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		formed = nbt.getBoolean("formed");
		posInMultiblock = NBTUtil.readBlockPos(nbt.getCompound("posInMultiblock"));
		offsetToMaster = NBTUtil.readBlockPos(nbt.getCompound("offset"));
		redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putBoolean("formed", formed);
		nbt.put("posInMultiblock", NBTUtil.writeBlockPos(new BlockPos(posInMultiblock)));
		nbt.put("offset", NBTUtil.writeBlockPos(new BlockPos(offsetToMaster)));
		nbt.putBoolean("redstoneControlInverted", redstoneControlInverted);
	}

	private EnumMap<Direction, LazyOptional<IFluidHandler>> fluidCaps = new EnumMap<>(Direction.class);

	{
		for(Direction f : Direction.VALUES)
		{
			LazyOptional<IFluidHandler> forSide = registerConstantCap(new MultiblockFluidWrapper(this, f));
			fluidCaps.put(f, forSide);
		}
	}
	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&facing!=null&&
				this.getAccessibleFluidTanks(facing).length > 0)
			return fluidCaps.get(facing).cast();
		return super.getCapability(capability, facing);
	}

	//	=================================
	//		FLUID MANAGEMENT
	//	=================================
	@Nonnull
	protected abstract IFluidTank[] getAccessibleFluidTanks(Direction side);

	protected abstract boolean canFillTankFrom(int iTank, Direction side, FluidStack resource);

	protected abstract boolean canDrainTankFrom(int iTank, Direction side);

	public static class MultiblockFluidWrapper implements IFluidHandler
	{
		final MultiblockPartTileEntity multiblock;
		final Direction side;

		public MultiblockFluidWrapper(MultiblockPartTileEntity multiblock, Direction side)
		{
			this.multiblock = multiblock;
			this.side = side;
		}

		@Override
		public int getTanks()
		{
			return multiblock.getAccessibleFluidTanks(side).length;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank)
		{
			return multiblock.getAccessibleFluidTanks(side)[tank].getFluid();
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return multiblock.getAccessibleFluidTanks(side)[tank].getCapacity();
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return multiblock.getAccessibleFluidTanks(side)[tank].isFluidValid(stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction doFill)
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
			return Math.max(fill, 0);
		}

		@Nonnull
		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			if(!this.multiblock.formed||resource==null)
				return FluidStack.EMPTY;
			IFluidTank[] tanks = this.multiblock.getAccessibleFluidTanks(side);
			FluidStack drain = FluidStack.EMPTY;
			for(int i = 0; i < tanks.length; i++)
			{
				IFluidTank tank = tanks[i];
				if(tank!=null&&this.multiblock.canDrainTankFrom(i, side))
				{
					if(tank instanceof IFluidHandler)
						drain = ((IFluidHandler)tank).drain(resource, doDrain);
					else
						drain = tank.drain(resource.getAmount(), doDrain);
					if(!drain.isEmpty())
						break;
				}
			}
			if(!drain.isEmpty())
				this.multiblock.updateMasterBlock(null, true);
			return drain;
		}

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			if(!this.multiblock.formed||maxDrain==0)
				return FluidStack.EMPTY;
			IFluidTank[] tanks = this.multiblock.getAccessibleFluidTanks(side);
			FluidStack drain = FluidStack.EMPTY;
			for(int i = 0; i < tanks.length; i++)
			{
				IFluidTank tank = tanks[i];
				if(tank!=null&&this.multiblock.canDrainTankFrom(i, side))
				{
					drain = tank.drain(maxDrain, doDrain);
					if(!drain.isEmpty())
						break;
				}
			}
			if(!drain.isEmpty())
				this.multiblock.updateMasterBlock(null, true);
			return drain;
		}
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	@Nullable
	public T master()
	{
		if(offsetToMaster.equals(Vec3i.NULL_VECTOR))
			return (T)this;
		// Used to provide tile-dependant drops after disassembly
		if(tempMasterTE!=null)
			return (T)tempMasterTE;
		BlockPos masterPos = getPos().subtract(offsetToMaster);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(T)te: null;
	}

	public void updateMasterBlock(BlockState state, boolean blockUpdate)
	{
		T master = master();
		if(master!=null)
		{
			master.markDirty();
			if(blockUpdate)
				master.markContainingBlockForUpdate(state);
		}
	}

	@Override
	public boolean isDummy()
	{
		return !offsetToMaster.equals(Vec3i.NULL_VECTOR);
	}

	public BlockState getOriginalBlock()
	{
		for(BlockInfo block : multiblockInstance.getStructure())
			if(block.pos.equals(posInMultiblock))
				return block.state;
		return Blocks.AIR.getDefaultState();
	}

	public void disassemble()
	{
		if(formed&&!world.isRemote)
		{
			tempMasterTE = master();
			BlockPos startPos = getOrigin();
			multiblockInstance.disassemble(world, startPos, getIsMirrored(), multiblockInstance.untransformDirection(getFacing()));
			world.removeBlock(pos, false);
		}
	}

	public BlockPos getOrigin()
	{
		return TemplateMultiblock.withSettingsAndOffset(pos, BlockPos.ZERO.subtract(posInMultiblock),
				getIsMirrored(), multiblockInstance.untransformDirection(getFacing()));
	}

	public BlockPos getBlockPosForPos(BlockPos targetPos)
	{
		BlockPos origin = getOrigin();
		return TemplateMultiblock.withSettingsAndOffset(origin, targetPos, getIsMirrored(), multiblockInstance.untransformDirection(getFacing()));
	}

	public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w)
	{
		if(state.getBlock()==this.getBlockState().getBlock())
			getWorldNonnull().removeBlock(pos, false);
		getWorldNonnull().setBlockState(pos, state);
		TileEntity tile = getWorldNonnull().getTileEntity(pos);
		if(tile instanceof IReadOnPlacement)
			((IReadOnPlacement)tile).readOnPlacement(null, stack);
	}

	//	=================================
	//		REDSTONE CONTROL
	//	=================================
	public Set<BlockPos> getRedstonePos()
	{
		throw new UnsupportedOperationException("Tried to get RS position for a multiblock without RS control!");
	}

	public boolean isRedstonePos()
	{
		if(!hasRedstoneControl||getRedstonePos()==null)
			return false;
		for(BlockPos i : getRedstonePos())
			if(posInMultiblock.equals(i))
				return true;
		return false;
	}

	@Override
	public boolean screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, Vec3d hitVec)
	{
		if(!world.isRemote)
		{
			if(this.isRedstonePos()&&hasRedstoneControl)
			{
				MultiblockPartTileEntity<T> master = master();
				if(master!=null)
				{
					master.redstoneControlInverted = !master.redstoneControlInverted;
					ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"rsControl."
							+(master.redstoneControlInverted?"invertedOn": "invertedOff")));
					this.updateMasterBlock(null, true);
					return true;
				}
			}
		}
		return false;
	}

	public boolean isRSDisabled()
	{
		if(computerOn.isPresent())
			return !computerOn.get();
		Set<BlockPos> rsPositions = getRedstonePos();
		if(rsPositions==null||rsPositions.isEmpty())
			return false;
		for(BlockPos rsPos : rsPositions)
		{
			T tile = this.getTileForPos(rsPos);
			if(tile!=null)
			{
				boolean b = tile.isRSPowered();
				if(redstoneControlInverted!=b)
					return true;
			}
		}
		return false;
	}

	@Nullable
	public T getTileForPos(BlockPos targetPosInMB)
	{
		BlockPos target = getBlockPosForPos(targetPosInMB);
		TileEntity tile = SafeChunkUtils.getSafeTE(getWorldNonnull(), target);
		if(this.getClass().isInstance(tile))
			return (T)tile;
		return null;
	}
}