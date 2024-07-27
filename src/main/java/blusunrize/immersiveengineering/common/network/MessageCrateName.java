/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageCrateName(BlockPos pos, String name) implements IMessage
{
	public static final Type<MessageCrateName> ID = IMessage.createType("crate_name");
	public static final StreamCodec<ByteBuf, MessageCrateName> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, MessageCrateName::pos,
			ByteBufCodecs.STRING_UTF8, MessageCrateName::name,
			MessageCrateName::new
	);

	public MessageCrateName(BlockEntity tile, String name)
	{
		this(tile.getBlockPos(), name);
	}

	@Override
	public void process(IPayloadContext context)
	{
		if(context.flow().getReceptionSide()==LogicalSide.SERVER)
			context.enqueueWork(() -> {
				Level world = context.player().level();
				if(world.isAreaLoaded(pos, 1))
				{
					BlockEntity tile = world.getBlockEntity(pos);
					if(tile instanceof WoodenCrateBlockEntity crate)
						crate.setCustomName(Component.literal(name));
				}
			});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}