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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageMaintenanceKit implements IMessage
{
	EquipmentSlotType slot;
	CompoundNBT nbt;

	public MessageMaintenanceKit(EquipmentSlotType slot, CompoundNBT nbt)
	{
		this.slot = slot;
		this.nbt = nbt;
	}

	public MessageMaintenanceKit(PacketBuffer buf)
	{
		this.slot = EquipmentSlotType.fromString(buf.readString(100));
		this.nbt = buf.readCompoundTag();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeString(this.slot.getName());
		buf.writeCompoundTag(this.nbt);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ServerPlayerEntity player = ctx.getSender();
		assert player!=null;
		ctx.enqueueWork(() -> {
			if(player.openContainer instanceof MaintenanceKitContainer)
				ModWorkbenchTileEntity.applyConfigTo(player.openContainer.inventorySlots.get(0).getStack(), nbt);
		});
	}
}