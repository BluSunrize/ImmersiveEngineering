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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class IEBaseTileEntity extends TileEntity implements BlockstateProvider
{
	/**
	 * Set by and for those instances of IGeneralMultiblock that need to drop their inventory
	 */
	protected IGeneralMultiblock tempMasterTE;

	private BlockState overrideBlockState = null;

	private final EnumMap<Direction, Integer> redstoneBySide = new EnumMap<>(Direction.class);

	public IEBaseTileEntity(TileEntityType<? extends TileEntity> type)
	{
		super(type);
	}

	@Override
	public void read(BlockState stateIn, CompoundNBT nbtIn)
	{
		super.read(stateIn, nbtIn);
		this.readCustomNBT(nbtIn, false);
	}

	public abstract void readCustomNBT(CompoundNBT nbt, boolean descPacket);

	@Override
	public CompoundNBT write(CompoundNBT nbt)
	{
		super.write(nbt);
		this.writeCustomNBT(nbt, false);
		return nbt;
	}

	public abstract void writeCustomNBT(CompoundNBT nbt, boolean descPacket);

	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundNBT nbttagcompound = new CompoundNBT();
		this.writeCustomNBT(nbttagcompound, true);
		return new SUpdateTileEntityPacket(this.pos, 3, nbttagcompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		this.readCustomNBT(pkt.getNbtCompound(), true);
	}

	@Override
	public CompoundNBT getUpdateTag()
	{
		CompoundNBT nbt = super.getUpdateTag();
		writeCustomNBT(nbt, true);
		return nbt;
	}

	@Override
	public void mirror(Mirror mirrorIn)
	{
		if(mirrorIn==Mirror.FRONT_BACK&&this instanceof IDirectionalTile)
		{
			((IDirectionalTile)this).setFacing(((IDirectionalTile)this).getFacing());
			this.markDirty();
			if(this.pos!=null)
				this.markBlockForUpdate(this.pos, null);
		}
	}


	public void receiveMessageFromClient(CompoundNBT message)
	{
	}

	public void receiveMessageFromServer(CompoundNBT message)
	{
	}

	public void onEntityCollision(World world, Entity entity)
	{
	}

	@Override
	public boolean receiveClientEvent(int id, int type)
	{
		if(id==0||id==255)
		{
			markContainingBlockForUpdate(null);
			return true;
		}
		else if(id==254)
		{
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	public void markContainingBlockForUpdate(@Nullable BlockState newState)
	{
		if(this.world!=null)
			markBlockForUpdate(getPos(), newState);
	}

	public void markBlockForUpdate(BlockPos pos, @Nullable BlockState newState)
	{
		BlockState state = world.getBlockState(pos);
		if(newState==null)
			newState = state;
		world.notifyBlockUpdate(pos, state, newState, 3);
		world.notifyNeighborsOfStateChange(pos, newState.getBlock());
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
	public double getMaxRenderDistanceSquared()
	{
		double increase = IEClientConfig.increasedTileRenderdistance.get();
		return super.getMaxRenderDistanceSquared()*
				increase*increase;
	}

	@Override
	public void remove()
	{
		super.remove();
		for(LazyOptional<?> cap : caps)
			if(cap.isPresent())
				cap.invalidate();
		caps.clear();
	}

	@Nonnull
	public World getWorldNonnull()
	{
		return Objects.requireNonNull(super.getWorld());
	}

	protected void checkLight()
	{
		checkLight(pos);
	}

	protected void checkLight(BlockPos pos)
	{
		getWorldNonnull().getPendingBlockTicks().scheduleTick(pos, getBlockState().getBlock(), 4);
	}

	public void setOverrideState(BlockState state)
	{
		overrideBlockState = state;
	}

	@Override
	public BlockState getBlockState()
	{
		if(world==null&&((TileEntityAccess)this).getCachedBlockStateDirectly()==null)
			return ((TileTypeAccess)getType()).getValidBlocks().iterator().next().getDefaultState();
		if(overrideBlockState!=null)
			return overrideBlockState;
		else
			return super.getBlockState();
	}

	@Override
	public void updateContainingBlockInfo()
	{
		BlockState old = getBlockState();
		super.updateContainingBlockInfo();
		BlockState newState = getBlockState();
		if(old!=null&&
				newState!=null&&
				getType().isValidBlock(old.getBlock())&&
				!getType().isValidBlock(newState.getBlock()))
			setOverrideState(old);
	}

	@Override
	public void setState(BlockState state)
	{
		if(getWorldNonnull().getBlockState(pos)==getState())
			getWorldNonnull().setBlockState(pos, state);
	}

	@Override
	public BlockState getState()
	{
		return getBlockState();
	}

	/**
	 * Most calls to {@link TileEntity#markDirty} should be replaced by this. The vanilla mD also updates comparator
	 * states and re-caches the block state, while in most cases we just want to say "this needs to be saved to disk"
	 */
	protected void markChunkDirty()
	{
		if(this.world!=null && this.world.isBlockLoaded(this.pos))
			this.world.getChunkAt(this.pos).markDirty();
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
	public void setWorldAndPos(World world, BlockPos pos)
	{
		super.setWorldAndPos(world, pos);
		this.redstoneBySide.clear();
	}

	// Based on the super version, but works around a Forge patch to World#markChunkDirty causing duplicate comparator
	// updates and only performs comparator updates if this TE actually has comparator behavior
	@Override
	public void markDirty()
	{
		if (this.world != null) {
			BlockState state = this.world.getBlockState(this.pos);
			((TileEntityAccess) this).setCachedBlockState(state);
			markChunkDirty();
			if (this instanceof IComparatorOverride && !state.isAir(this.world, this.pos)) {
				this.world.updateComparatorOutputLevel(this.pos, state.getBlock());
			}
		}
	}

	protected void onNeighborBlockChange(BlockPos otherPos)
	{
		BlockPos delta = otherPos.subtract(pos);
		Direction side = Direction.getFacingFromVector(delta.getX(), delta.getY(), delta.getZ());
		Preconditions.checkNotNull(side);
		updateRSForSide(side);
	}

	private void updateRSForSide(Direction side)
	{
		int rsStrength = getWorldNonnull().getRedstonePower(pos.offset(side), side);
		if(rsStrength==0&&this instanceof IRedstoneOutput&&((IRedstoneOutput)this).canConnectRedstone(side))
		{
			BlockState state = SafeChunkUtils.getBlockState(world, pos.offset(side));
			if(state.getBlock()==Blocks.REDSTONE_WIRE&&state.get(RedstoneWireBlock.POWER) > rsStrength)
				rsStrength = state.get(RedstoneWireBlock.POWER);
		}
		redstoneBySide.put(side, rsStrength);
	}

	protected int getRSInput(Direction from)
	{
		if(getWorldNonnull().isRemote||!redstoneBySide.containsKey(from))
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
