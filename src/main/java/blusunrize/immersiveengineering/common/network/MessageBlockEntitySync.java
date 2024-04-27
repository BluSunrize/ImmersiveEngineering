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
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MessageBlockEntitySync(BlockPos pos, CompoundTag nbt) implements IMessage
{
	public static final Type<MessageBlockEntitySync> ID = IMessage.createType("be_sync");
	public static final StreamCodec<ByteBuf, MessageBlockEntitySync> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, MessageBlockEntitySync::pos,
			ByteBufCodecs.COMPOUND_TAG, MessageBlockEntitySync::nbt,
			MessageBlockEntitySync::new
	);

	@Override
	public void process(IPayloadContext context)
	{
		if(context.flow().getReceptionSide()==LogicalSide.SERVER)
			context.enqueueWork(() -> {
				Level world = context.player().level();
				if(world.isAreaLoaded(pos, 1))
				{
					BlockEntity tile = world.getBlockEntity(pos);
					if(tile instanceof IEBaseBlockEntity)
						((IEBaseBlockEntity)tile).receiveMessageFromClient(nbt);
				}
			});
		else
			context.enqueueWork(() -> {
				Level world = ImmersiveEngineering.proxy.getClientWorld();
				if(world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					BlockEntity tile = world.getBlockEntity(pos);
					if(tile instanceof IEBaseBlockEntity)
						((IEBaseBlockEntity)tile).receiveMessageFromServer(nbt);
				}
			});
	}

	@Override
	@NotNull
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}