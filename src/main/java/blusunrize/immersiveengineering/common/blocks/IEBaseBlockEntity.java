/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.energy.WrappingEnergyStorage;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.BlockstateProvider;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IRedstoneOutput;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public abstract class IEBaseBlockEntity extends BlockEntity implements BlockstateProvider
{
	/**
	 * Set by and for those instances of IGeneralMultiblock that need to drop their inventory
	 */
	protected IGeneralMultiblock tempMasterBE;

	@Nullable
	private BlockState overrideBlockState = null;

	private final EnumMap<Direction, Integer> redstoneBySide = new EnumMap<>(Direction.class);

	public IEBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void load(CompoundTag nbtIn)
	{
		super.load(nbtIn);
		this.readCustomNBT(nbtIn, false);
	}

	public abstract void readCustomNBT(CompoundTag nbt, boolean descPacket);

	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		super.save(nbt);
		this.writeCustomNBT(nbt, false);
		return nbt;
	}

	public abstract void writeCustomNBT(CompoundTag nbt, boolean descPacket);

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		CompoundTag nbttagcompound = new CompoundTag();
		this.writeCustomNBT(nbttagcompound, true);
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, nbttagcompound);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
	{
		this.readCustomNBT(pkt.getTag(), true);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag)
	{
		this.readCustomNBT(tag, true);
	}

	@Override
	public CompoundTag getUpdateTag()
	{
		CompoundTag nbt = super.getUpdateTag();
		writeCustomNBT(nbt, true);
		return nbt;
	}

	public void receiveMessageFromClient(CompoundTag message)
	{
	}

	public void receiveMessageFromServer(CompoundTag message)
	{
	}

	public void onEntityCollision(Level world, Entity entity)
	{
	}

	@Override
	public boolean triggerEvent(int id, int type)
	{
		if(id==0||id==255)
		{
			markContainingBlockForUpdate(null);
			return true;
		}
		else if(id==254)
		{
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 3);
			return true;
		}
		return super.triggerEvent(id, type);
	}

	public void markContainingBlockForUpdate(@Nullable BlockState newState)
	{
		if(this.level!=null)
			markBlockForUpdate(getBlockPos(), newState);
	}

	public void markBlockForUpdate(BlockPos pos, @Nullable BlockState newState)
	{
		BlockState state = level.getBlockState(pos);
		if(newState==null)
			newState = state;
		level.sendBlockUpdated(pos, state, newState, 3);
		level.updateNeighborsAt(pos, newState.getBlock());
	}

	private final List<ResettableCapability<?>> caps = new ArrayList<>();

	protected <T> ResettableCapability<T> registerCapability(T val)
	{
		ResettableCapability<T> cap = new ResettableCapability<>(val);
		caps.add(cap);
		return cap;
	}

	protected ResettableCapability<IEnergyStorage> registerEnergyInput(IEnergyStorage directStorage)
	{
		return registerCapability(new WrappingEnergyStorage(directStorage, true, false, this::setChanged));
	}

	protected ResettableCapability<IEnergyStorage> registerEnergyOutput(IEnergyStorage directStorage)
	{
		return registerCapability(new WrappingEnergyStorage(directStorage, false, true, this::setChanged));
	}

	private ResettableCapability<IFluidHandler> registerFluidHandler(IFluidTank[] tanks, boolean allowDrain, boolean allowFill)
	{
		return registerCapability(new ArrayFluidHandler(
				// TODO the global forced update is a hack and should be replaced by updates on the machines that render
				//  the fluid in world and screen sync for those that do not
				tanks, allowDrain, allowFill, () -> markContainingBlockForUpdate(null)
		));
	}

	protected final ResettableCapability<IFluidHandler> registerFluidHandler(IFluidTank... tanks)
	{
		return registerFluidHandler(tanks, true, true);
	}

	protected final ResettableCapability<IFluidHandler> registerFluidInput(IFluidTank... tanks)
	{
		return registerFluidHandler(tanks, false, true);
	}

	protected final ResettableCapability<IFluidHandler> registerFluidOutput(IFluidTank... tanks)
	{
		return registerFluidHandler(tanks, true, false);
	}

	protected final ResettableCapability<IFluidHandler> registerFluidView(IFluidTank... tanks)
	{
		return registerFluidHandler(tanks, false, false);
	}

	@Override
	public final void setRemoved()
	{
		if(!isUnloaded)
			setRemovedIE();
		super.setRemoved();
	}

	@Override
	public void invalidateCaps()
	{
		super.invalidateCaps();
		resetAllCaps();
		caps.clear();
	}

	protected void resetAllCaps()
	{
		caps.forEach(ResettableCapability::reset);
	}

	private boolean isUnloaded = false;

	@Override
	public void onLoad()
	{
		super.onLoad();
		isUnloaded = false;
	}

	@Override
	public void onChunkUnloaded()
	{
		super.onChunkUnloaded();
		isUnloaded = true;
	}

	public void setRemovedIE()
	{
	}

	@Nonnull
	public Level getLevelNonnull()
	{
		return Objects.requireNonNull(super.getLevel());
	}

	protected void checkLight()
	{
		checkLight(worldPosition);
	}

	protected void checkLight(BlockPos pos)
	{
		getLevelNonnull().getBlockTicks().scheduleTick(pos, getBlockState().getBlock(), 4);
	}

	public void setOverrideState(@Nullable BlockState state)
	{
		overrideBlockState = state;
	}

	@Override
	public BlockState getBlockState()
	{
		if(overrideBlockState!=null)
			return overrideBlockState;
		else
			return super.getBlockState();
	}

	@Override
	@Deprecated
	public void setBlockState(BlockState newState)
	{
		BlockState old = getBlockState();
		super.setBlockState(newState);
		if(getType().isValid(old)&&!getType().isValid(newState))
			setOverrideState(old);
		else if(getType().isValid(newState))
			setOverrideState(null);
		// Reset caps after e.g. rotating a block, so users get the cap for the logical side of the block now facing
		// them
		resetAllCaps();
	}

	@Override
	public void setState(BlockState state)
	{
		if(getLevelNonnull().getBlockState(worldPosition)==getState())
			getLevelNonnull().setBlockAndUpdate(worldPosition, state);
	}

	@Override
	public BlockState getState()
	{
		return getBlockState();
	}

	/**
	 * Most calls to {@link BlockEntity#setChanged} should be replaced by this. The vanilla mD also updates comparator
	 * states and re-caches the block state, while in most cases we just want to say "this needs to be saved to disk"
	 */
	protected void markChunkDirty()
	{
		if(this.level!=null&&this.level.hasChunkAt(this.worldPosition))
			this.level.getChunkAt(this.worldPosition).markUnsaved();
	}

	@Override
	public void setLevel(Level world)
	{
		super.setLevel(world);
		this.redstoneBySide.clear();
	}

	// Based on the super version, but works around a Forge patch to World#markChunkDirty causing duplicate comparator
	// updates and only performs comparator updates if this TE actually has comparator behavior
	@Override
	public void setChanged()
	{
		if(this.level!=null)
		{
			markChunkDirty();
			BlockState state = getBlockState();
			if(state.hasAnalogOutputSignal())
				this.level.updateNeighbourForOutputSignal(this.worldPosition, state.getBlock());
		}
	}

	protected void onNeighborBlockChange(BlockPos otherPos)
	{
		BlockPos delta = otherPos.subtract(worldPosition);
		Direction side = Direction.getNearest(delta.getX(), delta.getY(), delta.getZ());
		Preconditions.checkNotNull(side);
		updateRSForSide(side);
	}

	private void updateRSForSide(Direction side)
	{
		int rsStrength = getLevelNonnull().getSignal(worldPosition.relative(side), side);
		if(rsStrength==0&&this instanceof IRedstoneOutput&&((IRedstoneOutput)this).canConnectRedstone(side))
		{
			BlockState state = SafeChunkUtils.getBlockState(level, worldPosition.relative(side));
			if(state.getBlock()==Blocks.REDSTONE_WIRE&&state.getValue(RedStoneWireBlock.POWER) > rsStrength)
				rsStrength = state.getValue(RedStoneWireBlock.POWER);
		}
		redstoneBySide.put(side, rsStrength);
	}

	protected int getRSInput(Direction from)
	{
		if(level.isClientSide||!redstoneBySide.containsKey(from))
			updateRSForSide(from);
		return redstoneBySide.get(from);
	}

	protected int getMaxRSInput()
	{
		int ret = 0;
		for(Direction d : DirectionUtils.VALUES)
			ret = Math.max(ret, getRSInput(d));
		return ret;
	}

	protected boolean isRSPowered()
	{
		for(Direction d : DirectionUtils.VALUES)
			if(getRSInput(d) > 0)
				return true;
		return false;
	}
}
