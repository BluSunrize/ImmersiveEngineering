/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.RevolverItem.RevolverCooldowns;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.IESounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageSpeedloaderSync(int slot, InteractionHand hand) implements IMessage
{
	public static final Type<MessageSpeedloaderSync> ID = IMessage.createType("speedloader_sync");
	public static final StreamCodec<ByteBuf, MessageSpeedloaderSync> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MessageSpeedloaderSync::slot,
			ByteBufCodecs.idMapper(i -> InteractionHand.values()[i], InteractionHand::ordinal), MessageSpeedloaderSync::hand,
			MessageSpeedloaderSync::new
	);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			Player player = ImmersiveEngineering.proxy.getClientPlayer();
			if(player!=null)
			{
				if(player.getItemInHand(hand).getItem() instanceof RevolverItem)
				{
					player.playSound(IESounds.revolverReload.value(), 1f, 1f);
					player.getItemInHand(hand).set(IEDataComponents.REVOLVER_COOLDOWN, new RevolverCooldowns(60, 0));
				}
				player.getInventory().setItem(slot, new ItemStack(Weapons.SPEEDLOADER));
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}