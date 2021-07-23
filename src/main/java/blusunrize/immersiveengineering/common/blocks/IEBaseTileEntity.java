/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.models.PrivateProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.mixin.accessors.TileEntityAccess;
import blusunrize.immersiveengineering.mixin.accessors.TileTypeAccess;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class IEBaseTileEntity extends BlockEntity implements BlockstateProvider
{
	/**
	 * Set by and for those instances of IGeneralMultiblock that need to drop their inventory
	 */
	protected IGeneralMultiblock tempMasterTE;

	private BlockState overrideBlockState = null;

	private final EnumMap<Direction, Integer> redstoneBySide = new EnumMap<>(Direction.class);

	public IEBaseTileEntity(BlockEntityType<? extends BlockEntity> type)
	{
		super(type);
	}

	@Override
	public void load(BlockState stateIn, CompoundTag nbtIn)
	{
		super.load(stateIn, nbtIn);
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
	public CompoundTag getUpdateTag()
	{
		CompoundTag nbt = super.getUpdateTag();
		writeCustomNBT(nbt, true);
		return nbt;
	}

	@Override
	public void mirror(Mirror mirrorIn)
	{
		if(mirrorIn==Mirror.FRONT_BACK&&this instanceof IDirectionalTile)
		{
			((IDirectionalTile)this).setFacing(((IDirectionalTile)this).getFacing());
			this.setChanged();
			if(this.worldPosition!=null)
				this.markBlockForUpdate(this.worldPosition, null);
		}
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

	private final Set<LazyOptional<?>> caps = new HashSet<>();
	private final Map<Direction, LazyOptional<IEnergyStorage>> energyCaps = new HashMap<>();

	protected <T> LazyOptional<T> registerConstantCap(T val)
	{
		return registerCap(() -> val);
	}

	protected <T> LazyOptional<T> registerCap(NonNullSupplier<T> cap)
	{
		return registerCap(LazyOptional.of(cap));
	}

	protected <T> LazyOptional<T> registerCap(LazyOptional<T> cap)
	{
		caps.add(cap);
		return cap;
	}

	protected <T> void unregisterCap(LazyOptional<T> cap)
	{
		cap.invalidate();
		caps.remove(cap);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==CapabilityEnergy.ENERGY&&this instanceof EnergyHelper.IIEInternalFluxConnector)
		{
			if(!energyCaps.containsKey(side))
			{
				IEForgeEnergyWrapper wrapper = ((EnergyHelper.IIEInternalFluxConnector)this).getCapabilityWrapper(side);
				if(wrapper!=null)
					energyCaps.put(side, registerConstantCap(wrapper));
				else
					energyCaps.put(side, LazyOptional.empty());
			}
			return energyCaps
					.get(side)
					.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public double getViewDistance()
	{
		double increase = IEClientConfig.increasedTileRenderdistance.get();
		return super.getViewDistance()*
				increase*increase;
	}

	@Override
	public void setRemoved()
	{
		super.setRemoved();
		for(LazyOptional<?> cap : caps)
			if(cap.isPresent())
				cap.invalidate();
		caps.clear();
	}

	@Nonnull
	public Level getWorldNonnull()
	{
		return Objects.requireNonNull(super.getLevel());
	}

	protected void checkLight()
	{
		checkLight(worldPosition);
	}

	protected void checkLight(BlockPos pos)
	{
		getWorldNonnull().getBlockTicks().scheduleTick(pos, getBlockState().getBlock(), 4);
	}

	public void setOverrideState(BlockState state)
	{
		overrideBlockState = state;
	}

	@Override
	public BlockState getBlockState()
	{
		if(level==null&&((TileEntityAccess)this).getCachedBlockStateDirectly()==null)
			return ((TileTypeAccess)getType()).getValidBlocks().iterator().next().defaultBlockState();
		if(overrideBlockState!=null)
			return overrideBlockState;
		else
			return super.getBlockState();
	}

	@Override
	public void clearCache()
	{
		BlockState old = getBlockState();
		super.clearCache();
		BlockState newState = getBlockState();
		if(old!=null&&
				newState!=null&&
				getType().isValid(old.getBlock())&&
				!getType().isValid(newState.getBlock()))
			setOverrideState(old);
	}

	@Override
	public void setState(BlockState state)
	{
		if(getWorldNonnull().getBlockState(worldPosition)==getState())
			getWorldNonnull().setBlockAndUpdate(worldPosition, state);
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
		if(this.level!=null && this.level.hasChunkAt(this.worldPosition))
			this.level.getChunkAt(this.worldPosition).markUnsaved();
	}

	@Nonnull
	@Override
	// Note: The line in the superclass javadoc about this being called off-thread is not actually correct, see
	// https://github.com/MinecraftForge/MinecraftForge/commit/06a30e9f23289a45c3c58e83d84c3dee01757e2b#r36351093 and
	// the original PR
	public IModelData getModelData()
	{
		IModelData base = super.getModelData();
		if(this instanceof IPropertyPassthrough)
			return CombinedModelData.combine(
					base, new SinglePropertyModelData<>(this, PrivateProperties.TILEENTITY_PASSTHROUGH)
			);
		else
			return base;
	}

	@Override
	public void setLevelAndPosition(Level world, BlockPos pos)
	{
		super.setLevelAndPosition(world, pos);
		this.redstoneBySide.clear();
	}

	// Based on the super version, but works around a Forge patch to World#markChunkDirty causing duplicate comparator
	// updates and only performs comparator updates if this TE actually has comparator behavior
	@Override
	public void setChanged()
	{
		if (this.level != null) {
			BlockState state = this.level.getBlockState(this.worldPosition);
			((TileEntityAccess) this).setBlockState(state);
			markChunkDirty();
			if (this instanceof IComparatorOverride && !state.isAir(this.level, this.worldPosition)) {
				this.level.updateNeighbourForOutputSignal(this.worldPosition, state.getBlock());
			}
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
		int rsStrength = getWorldNonnull().getSignal(worldPosition.relative(side), side);
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
		if(!redstoneBySide.containsKey(from))
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
