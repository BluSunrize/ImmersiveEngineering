/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.common.gui.ContainerMaintenanceKit;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMaintenanceKit implements IMessage
{
	EntityEquipmentSlot slot;
	NBTTagCompound nbt;

	public MessageMaintenanceKit(EntityEquipmentSlot slot, NBTTagCompound nbt)
	{
		this.slot = slot;
		this.nbt = nbt;
	}

	public MessageMaintenanceKit()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.slot = EntityEquipmentSlot.fromString(ByteBufUtils.readUTF8String(buf));
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, this.slot.getName());
		ByteBufUtils.writeTag(buf, this.nbt);
	}

	public static class Handler implements IMessageHandler<MessageMaintenanceKit, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMaintenanceKit message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				if(player.openContainer instanceof ContainerMaintenanceKit)
				{
					ItemStack tool = ((ContainerMaintenanceKit)player.openContainer).inventorySlots.get(0).getStack();
					if(!tool.isEmpty()&&tool.getItem() instanceof IConfigurableTool)
						for(String key : message.nbt.getKeySet())
						{
							if(key.startsWith("b_"))
								((IConfigurableTool)tool.getItem()).applyConfigOption(tool, key.substring(2), message.nbt.getBoolean(key));
							else if(key.startsWith("f_"))
								((IConfigurableTool)tool.getItem()).applyConfigOption(tool, key.substring(2), message.nbt.getFloat(key));
						}
				}
			});
			return null;
		}
	}
}