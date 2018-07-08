/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;

public abstract class TileEntityIEBase extends TileEntity
{
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.readCustomNBT(nbt, false);
	}

	public abstract void readCustomNBT(NBTTagCompound nbt, boolean descPacket);

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		this.writeCustomNBT(nbt, false);
		return nbt;
	}

	public abstract void writeCustomNBT(NBTTagCompound nbt, boolean descPacket);

	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeCustomNBT(nbttagcompound, true);
		return new SPacketUpdateTileEntity(this.pos, 3, nbttagcompound);
	}

	@Override
	public NBTTagCompound getUpdateTag()
	{
		NBTTagCompound nbt = super.writeToNBT(new NBTTagCompound());
		writeCustomNBT(nbt, true);
		return nbt;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		this.readCustomNBT(pkt.getNbtCompound(), true);
	}


	@Override
	public void rotate(Rotation rot)
	{
		if(rot!=Rotation.NONE&&this instanceof IDirectionalTile&&((IDirectionalTile)this).canRotate(EnumFacing.UP))
		{
			EnumFacing f = ((IDirectionalTile)this).getFacing();
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


	public void receiveMessageFromClient(NBTTagCompound message)
	{
	}

	public void receiveMessageFromServer(NBTTagCompound message)
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
			IBlockState state = world.getBlockState(pos);
			if(state instanceof IExtendedBlockState)
				ImmersiveEngineering.proxy.removeStateFromSmartModelCache((IExtendedBlockState)state);
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		if(world.isBlockLoaded(pos))
			newState = world.getBlockState(pos);
		if(oldState.getBlock()!=newState.getBlock()||!(oldState.getBlock() instanceof BlockIEBase)||!(newState.getBlock() instanceof BlockIEBase))
			return true;
		IProperty type = ((BlockIEBase)oldState.getBlock()).getMetaProperty();
		return oldState.getValue(type)!=newState.getValue(type);
	}

	public void markContainingBlockForUpdate(@Nullable IBlockState newState)
	{
		markBlockForUpdate(getPos(), newState);
	}

	public void markBlockForUpdate(BlockPos pos, @Nullable IBlockState newState)
	{
		IBlockState state = world.getBlockState(pos);
		if(newState==null)
			newState = state;
		world.notifyBlockUpdate(pos, state, newState, 3);
		world.notifyNeighborsOfStateChange(pos, newState.getBlock(), true);
	}


	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityEnergy.ENERGY&&this instanceof EnergyHelper.IIEInternalFluxConnector)
			return ((EnergyHelper.IIEInternalFluxConnector)this).getCapabilityWrapper(facing)!=null;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityEnergy.ENERGY&&this instanceof EnergyHelper.IIEInternalFluxConnector)
			return (T)((EnergyHelper.IIEInternalFluxConnector)this).getCapabilityWrapper(facing);
		return super.getCapability(capability, facing);
	}

	@Override
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*
				Config.IEConfig.increasedTileRenderdistance*Config.IEConfig.increasedTileRenderdistance;
	}
}