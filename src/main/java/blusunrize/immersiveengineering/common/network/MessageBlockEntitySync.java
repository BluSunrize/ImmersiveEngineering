/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.Objects;
import java.util.function.Supplier;

public class MessageBlockEntitySync implements IMessage
{
	private final BlockPos pos;
	private final CompoundTag nbt;

	//TODO get rid of NBT in packets
	public MessageBlockEntitySync(IEBaseBlockEntity tile, CompoundTag nbt)
	{
		this.pos = tile.getBlockPos();
		this.nbt = nbt;
	}

	public MessageBlockEntitySync(FriendlyByteBuf buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.nbt = buf.readNbt();
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
		buf.writeNbt(this.nbt);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		if(ctx.getDirection().getReceptionSide()==LogicalSide.SERVER)
			ctx.enqueueWork(() -> {
				ServerLevel world = Objects.requireNonNull(ctx.getSender()).serverLevel();
				if(world.isAreaLoaded(pos, 1))
				{
					BlockEntity tile = world.getBlockEntity(pos);
					if(tile instanceof IEBaseBlockEntity)
						((IEBaseBlockEntity)tile).receiveMessageFromClient(nbt);
				}
			});
		else
			ctx.enqueueWork(() -> {
				Level world = ImmersiveEngineering.proxy.getClientWorld();
				if(world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					BlockEntity tile = world.getBlockEntity(pos);
					if(tile instanceof IEBaseBlockEntity)
						((IEBaseBlockEntity)tile).receiveMessageFromServer(nbt);
				}
			});
	}
}