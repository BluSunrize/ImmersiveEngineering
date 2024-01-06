/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.gui.MaintenanceKitContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageMaintenanceKit implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("maintenance_kit");
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
	public void write(FriendlyByteBuf buf)
	{
		buf.writeUtf(this.slot.getName());
		buf.writeNbt(this.nbt);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		Player player = context.player().orElseThrow();
		context.workHandler().execute(() -> {
			if(player.containerMenu instanceof MaintenanceKitContainer)
				ModWorkbenchBlockEntity.applyConfigTo(player.containerMenu.slots.get(0).getItem(), nbt);
		});
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}