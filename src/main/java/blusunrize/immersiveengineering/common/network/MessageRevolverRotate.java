/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.RevolverItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageRevolverRotate(boolean forward) implements IMessage
{
	public static final Type<MessageRevolverRotate> ID = IMessage.createType("revolver_rotate");
	public static final StreamCodec<ByteBuf, MessageRevolverRotate> CODEC = ByteBufCodecs.BOOL
			.map(MessageRevolverRotate::new, MessageRevolverRotate::forward);

	@Override
	public void process(IPayloadContext context)
	{
		Player player = context.player();
		context.enqueueWork(() -> {
			ItemStack equipped = player.getItemInHand(InteractionHand.MAIN_HAND);
			if(equipped.getItem() instanceof RevolverItem)
				((RevolverItem)equipped.getItem()).rotateCylinder(equipped, player, forward);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}