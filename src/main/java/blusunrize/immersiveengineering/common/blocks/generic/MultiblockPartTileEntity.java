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
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControlState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Set;

public abstract class MultiblockPartTileEntity<T extends MultiblockPartTileEntity<T>> extends IEBaseTileEntity
		implements TickableBlockEntity, IStateBasedDirectional, IGeneralMultiblock, IScrewdriverInteraction, IMirrorAble,
		IModelOffsetProvider
{
	public boolean formed = false;
	//Position of this block according to the BlockInfo's returned by IMultiblock#getStructure
	public BlockPos posInMultiblock = BlockPos.ZERO;
	//Offset from the master to this block (world coordinate system)
	public BlockPos offsetToMaster = BlockPos.ZERO;
	protected final IETemplateMultiblock multiblockInstance;
	// stores the world time at which this block can only be disassembled by breaking the block associated with this TE.
	// This prevents half/duplicate disassembly when working with the drill or TCon hammers
	public long onlyLocalDissassembly = -1;
	protected final Lazy<Vec3i> structureDimensions;
	protected final boolean hasRedstoneControl;
	protected boolean redstoneControlInverted = false;
	//Absent means no controlling computers
	public ComputerControlState computerControl = ComputerControlState.NO_COMPUTER;

	protected MultiblockPartTileEntity(IETemplateMultiblock multiblockInstance, BlockEntityType<? extends T> type, boolean hasRSControl)
	{
		super(type);
		this.multiblockInstance = multiblockInstance;
		this.structureDimensions = Lazy.of(() -> multiblockInstance.getSize(level));
		this.hasRedstoneControl = hasRSControl;
	}

	// This fixes compile errors with subclasses also extending IConveyorAttachable, as that also defines getFacing
	@Nonnull
	@Override
	public Direction getFacing()
	{
		return IStateBasedDirectional.super.getFacing();
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
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
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
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
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		formed = nbt.getBoolean("formed");
		posInMultiblock = NbtUtils.readBlockPos(nbt.getCompound("posInMultiblock"));
		offsetToMaster = NbtUtils.readBlockPos(nbt.getCompound("offset"));
		redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putBoolean("formed", formed);
		nbt.put("posInMultiblock", NbtUtils.writeBlockPos(new BlockPos(posInMultiblock)));
		nbt.put("offset", NbtUtils.writeBlockPos(new BlockPos(offsetToMaster)));
		nbt.putBoolean("redstoneControlInverted", redstoneControlInverted);
	}

	private EnumMap<Direction, LazyOptional<IFluidHandler>> fluidCaps = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
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
			if(fill > 0&&doFill.execute())
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

	@Override
	@Nullable
	public T master()
	{
		if(offsetToMaster.equals(Vec3i.ZERO))
			return (T)this;
		// Used to provide tile-dependant drops after disassembly
		if(tempMasterTE!=null)
			return (T)tempMasterTE;
		return getTileForPos(multiblockInstance.getMasterFromOriginOffset());
	}

	public void updateMasterBlock(BlockState state, boolean blockUpdate)
	{
		T master = master();
		if(master!=null)
		{
			master.markChunkDirty();
			if(blockUpdate)
				master.markContainingBlockForUpdate(state);
		}
	}

	@Override
	public boolean isDummy()
	{
		return !offsetToMaster.equals(Vec3i.ZERO);
	}

	public BlockState getOriginalBlock()
	{
		for(StructureBlockInfo block : multiblockInstance.getStructure(level))
			if(block.pos.equals(posInMultiblock))
				return block.state;
		return Blocks.AIR.defaultBlockState();
	}

	public void disassemble()
	{
		if(formed&&!level.isClientSide)
		{
			tempMasterTE = master();
			BlockPos startPos = getOrigin();
			multiblockInstance.disassemble(level, startPos, getIsMirrored(), multiblockInstance.untransformDirection(getFacing()));
			level.removeBlock(worldPosition, false);
		}
	}

	public BlockPos getOrigin()
	{
		return TemplateMultiblock.withSettingsAndOffset(worldPosition, BlockPos.ZERO.subtract(posInMultiblock),
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
		getWorldNonnull().setBlockAndUpdate(pos, state);
		BlockEntity tile = getWorldNonnull().getBlockEntity(pos);
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
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(this.isRedstonePos()&&hasRedstoneControl)
		{
			if(!level.isClientSide)
			{
				MultiblockPartTileEntity<T> master = master();
				if(master!=null)
				{
					master.redstoneControlInverted = !master.redstoneControlInverted;
					ChatUtils.sendServerNoSpamMessages(player, new TranslatableComponent(Lib.CHAT_INFO+"rsControl."
							+(master.redstoneControlInverted?"invertedOn": "invertedOff")));
					this.updateMasterBlock(null, true);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public boolean isRSDisabled()
	{
		Set<BlockPos> rsPositions = getRedstonePos();
		if(rsPositions==null||rsPositions.isEmpty())
			return false;
		MultiblockPartTileEntity<?> master = master();
		if(master==null)
			master = this;
		if(master.computerControl.isStillAttached())
			return !master.computerControl.isEnabled();
		else
			master.computerControl = ComputerControlState.NO_COMPUTER;
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
		BlockEntity tile = SafeChunkUtils.getSafeTE(getWorldNonnull(), target);
		if(this.getClass().isInstance(tile))
			return (T)tile;
		return null;
	}

	@Nonnull
	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		BlockPos mirroredPosInMB = posInMultiblock;
		if(size==null)
			size = multiblockInstance.getSize(level);
		if(getIsMirrored())
			mirroredPosInMB = new BlockPos(
					size.getX()-mirroredPosInMB.getX()-1,
					mirroredPosInMB.getY(),
					mirroredPosInMB.getZ()
			);
		return multiblockInstance.multiblockToModelPos(mirroredPosInMB);
	}

	public VoxelShape getShape(CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> cache)
	{
		return cache.get(posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}

	public static <T extends MultiblockPartTileEntity<?> & IComparatorOverride>
	void updateComparators(T tile, Collection<BlockPos> offsets, MutableInt cachedValue, int newValue) {
		if (newValue == cachedValue.intValue())
			return;
		cachedValue.setValue(newValue);
		final Level world = tile.getWorldNonnull();
		for (BlockPos offset : offsets) {
			final BlockPos worldPos = tile.getBlockPosForPos(offset);
			final BlockState stateAt = world.getBlockState(worldPos);
			world.updateNeighbourForOutputSignal(worldPos, stateAt.getBlock());
		}
	}
}