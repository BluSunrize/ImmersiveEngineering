/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.Objects;
import java.util.function.Supplier;

public class MessageTileSync implements IMessage
{
	private BlockPos pos;
	private CompoundNBT nbt;

	//TODO get rid of NBT in packets
	public MessageTileSync(IEBaseTileEntity tile, CompoundNBT nbt)
	{
		this.pos = tile.getPos();
		this.nbt = nbt;
	}

	public MessageTileSync(PacketBuffer buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.nbt = buf.readCompoundTag();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
		buf.writeCompoundTag(this.nbt);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		if(ctx.getDirection().getReceptionSide()==LogicalSide.SERVER)
			ctx.enqueueWork(() -> {
				ServerWorld world = Objects.requireNonNull(ctx.getSender()).getServerWorld();
				if(world.isAreaLoaded(pos, 1))
				{
					TileEntity tile = world.getTileEntity(pos);
					if(tile instanceof IEBaseTileEntity)
						((IEBaseTileEntity)tile).receiveMessageFromClient(nbt);
				}
			});
		else
			ctx.enqueueWork(() -> {
				World world = ImmersiveEngineering.proxy.getClientWorld();
				if(world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					TileEntity tile = world.getTileEntity(pos);
					if(tile instanceof IEBaseTileEntity)
						((IEBaseTileEntity)tile).receiveMessageFromServer(nbt);
				}
			});
	}
}