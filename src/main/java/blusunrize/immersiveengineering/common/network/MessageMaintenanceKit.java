/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.gui.MaintenanceKitContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageMaintenanceKit implements IMessage
{
	EquipmentSlot slot;
	CompoundTag nbt;

	public MessageMaintenanceKit(EquipmentSlot slot, CompoundTag nbt)
	{
		this.slot = slot;
		this.nbt = nbt;
	}

	public MessageMaintenanceKit(FriendlyByteBuf buf)
	{
		this.slot = EquipmentSlot.byName(buf.readUtf(100));
		this.nbt = buf.readNbt();
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeUtf(this.slot.getName());
		buf.writeNbt(this.nbt);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ServerPlayer player = ctx.getSender();
		assert player!=null;
		ctx.enqueueWork(() -> {
			if(player.containerMenu instanceof MaintenanceKitContainer)
				ModWorkbenchTileEntity.applyConfigTo(player.containerMenu.slots.get(0).getItem(), nbt);
		});
	}
}