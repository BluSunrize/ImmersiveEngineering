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
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControlState;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControllable;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public abstract class MultiblockPartBlockEntity<T extends MultiblockPartBlockEntity<T>> extends IEBaseBlockEntity
		implements IEServerTickableBE, IStateBasedDirectional, IGeneralMultiblock, IScrewdriverInteraction, IMirrorAble,
		IModelOffsetProvider, ComputerControllable
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
	public final ComputerControlState computerControl = new ComputerControlState();

	protected MultiblockPartBlockEntity(
			IETemplateMultiblock multiblockInstance, BlockEntityType<? extends T> type, boolean hasRSControl,
			BlockPos pos, BlockState state
	)
	{
		super(type, pos, state);
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
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
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

	@Override
	@Nullable
	public T master()
	{
		if(offsetToMaster.equals(Vec3i.ZERO))
			return (T)this;
		// Used to provide tile-dependant drops after disassembly
		if(tempMasterBE!=null)
			return (T)tempMasterBE;
		return getEntityForPos(multiblockInstance.getMasterFromOriginOffset());
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
			tempMasterBE = master();
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
				MultiblockPartBlockEntity<T> master = master();
				if(master!=null)
				{
					master.redstoneControlInverted = !master.redstoneControlInverted;
					player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"rsControl."
							+(master.redstoneControlInverted?"invertedOn": "invertedOff")), true);
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
		for(BlockPos rsPos : rsPositions)
		{
			T tile = this.getEntityForPos(rsPos);
			if(tile!=null)
			{
				if(tile.computerControl.isAttached())
					return !tile.computerControl.isEnabled();
				boolean b = tile.isRSPowered();
				if(redstoneControlInverted!=b)
					return true;
			}
		}
		return false;
	}

	@Nullable
	public T getEntityForPos(BlockPos targetPosInMB)
	{
		BlockPos target = getBlockPosForPos(targetPosInMB);
		BlockEntity tile = SafeChunkUtils.getSafeBE(getLevelNonnull(), target);
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
		return multiblockInstance.multiblockToModelPos(mirroredPosInMB, level);
	}

	public VoxelShape getShape(CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> cache)
	{
		return cache.get(posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}

	@Override
	public Stream<ComputerControlState> getAllComputerControlStates()
	{
		return Stream.of(computerControl);
	}

	public static <T extends MultiblockPartBlockEntity<?> & IComparatorOverride>
	void updateComparators(T tile, Collection<BlockPos> offsets, MutableInt cachedValue, int newValue)
	{
		if(newValue==cachedValue.intValue())
			return;
		cachedValue.setValue(newValue);
		final Level world = tile.getLevelNonnull();
		for(BlockPos offset : offsets)
		{
			final BlockPos worldPos = tile.getBlockPosForPos(offset);
			final BlockState stateAt = world.getBlockState(worldPos);
			world.updateNeighbourForOutputSignal(worldPos, stateAt.getBlock());
		}
	}
}