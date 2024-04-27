/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.gui.MaintenanceKitContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageMaintenanceKit(EquipmentSlot slot, CompoundTag nbt) implements IMessage
{
	public static final Type<MessageMaintenanceKit> ID = IMessage.createType("maintenance_kit");
	public static final StreamCodec<ByteBuf, MessageMaintenanceKit> CODEC = StreamCodec.composite(
			ByteBufCodecs.idMapper(i -> EquipmentSlot.values()[i], EquipmentSlot::ordinal), MessageMaintenanceKit::slot,
			ByteBufCodecs.COMPOUND_TAG, MessageMaintenanceKit::nbt,
			MessageMaintenanceKit::new
	);

	@Override
	public void process(IPayloadContext context)
	{
		Player player = context.player();
		context.enqueueWork(() -> {
			if(player.containerMenu instanceof MaintenanceKitContainer)
				ModWorkbenchBlockEntity.applyConfigTo(player.containerMenu.slots.get(0).getItem(), nbt);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}