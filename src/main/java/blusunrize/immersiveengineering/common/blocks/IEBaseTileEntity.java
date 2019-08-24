/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class IEBaseTileEntity extends TileEntity
{
	public IEBaseTileEntity(TileEntityType<? extends TileEntity> type)
	{
		super(type);
	}

	@Override
	public void read(CompoundNBT nbt)
	{
		super.read(nbt);
		this.readCustomNBT(nbt, false);
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
	public CompoundNBT getUpdateTag()
	{
		CompoundNBT nbt = super.getUpdateTag();
		writeCustomNBT(nbt, true);
		return nbt;
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		this.readCustomNBT(pkt.getNbtCompound(), true);
	}


	@Override
	public void rotate(Rotation rot)
	{
		if(rot!=Rotation.NONE&&this instanceof IDirectionalTile&&((IDirectionalTile)this).canRotate(Direction.UP))
		{
			Direction f = ((IDirectionalTile)this).getFacing();
			switch(rot)
			{
				case CLOCKWISE_90:
					f = f.rotateY();
					break;
				case CLOCKWISE_180:
					f = f.getOpposite();
					break;
				case COUNTERCLOCKWISE_90:
					f = f.rotateYCCW();
					break;
			}
			((IDirectionalTile)this).setFacing(f);
			this.markDirty();
			if(this.pos!=null)
				this.markBlockForUpdate(this.pos, null);
		}
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
	private final EnumMap<Direction, LazyOptional<IEnergyStorage>> energyCaps = new EnumMap<>(Direction.class);

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
			return energyCaps
					.computeIfAbsent(side, (f) ->
							registerCap(() -> ((EnergyHelper.IIEInternalFluxConnector)this).getCapabilityWrapper(f)))
					.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public double getMaxRenderDistanceSquared()
	{
		double increase = IEConfig.GENERAL.increasedTileRenderdistance.get();
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
	}

	@Nonnull
	@Override
	public World getWorld()
	{
		return Objects.requireNonNull(super.getWorld());
	}

	protected void checkLight()
	{
		checkLight(pos);
	}

	protected void checkLight(BlockPos pos)
	{
		getWorld().getProfiler().startSection("queueCheckLight");
		getWorld().getChunkProvider().getLightManager().checkBlock(pos);
		getWorld().getProfiler().endSection();
	}
}