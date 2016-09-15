package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

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
		if(id == 0 || id == 255)
		{
			markContainingBlockForUpdate(null);
			return true;
		} else if(id == 254)
		{
			IBlockState state = worldObj.getBlockState(pos);
			if(state instanceof IExtendedBlockState)
				ImmersiveEngineering.proxy.removeStateFromSmartModelCache((IExtendedBlockState) state);
			worldObj.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		return super.receiveClientEvent(id, type);
	}
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		if (world.isBlockLoaded(pos))
			newState = world.getBlockState(pos);
		if (oldState.getBlock()!=newState.getBlock()||!(oldState.getBlock() instanceof BlockIEBase)||!(newState.getBlock() instanceof BlockIEBase))
			return true;
		IProperty type = ((BlockIEBase)oldState.getBlock()).getMetaProperty();
		return oldState.getValue(type) != newState.getValue(type);
	}

	public void markContainingBlockForUpdate(IBlockState newState)
	{
		markBlockForUpdate(getPos(), newState);
	}
	public void markBlockForUpdate(BlockPos pos, IBlockState newState)
	{
		IBlockState state = worldObj.getBlockState(getPos());
		if(newState==null)
			newState = state;
		worldObj.notifyBlockUpdate(pos,state,newState,3);
		worldObj.notifyNeighborsOfStateChange(pos, newState.getBlock());
	}
}