/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.gui.IEBaseContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageContainerUpdate implements IMessage
{
	private int windowId;
	private CompoundNBT nbt;

	//TODO get rid of NBT in packets (maybe?)
	public MessageContainerUpdate(int windowId, CompoundNBT nbt)
	{
		this.windowId = windowId;
		this.nbt = nbt;
	}

	public MessageContainerUpdate(PacketBuffer buf)
	{
		this.windowId = buf.readByte();
		this.nbt = buf.readCompoundTag();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeByte(this.windowId);
		buf.writeCompoundTag(this.nbt);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ServerPlayerEntity player = ctx.getSender();
		assert player!=null;
		ctx.enqueueWork(() -> {
			player.markPlayerActive();
			if(player.openContainer.windowId==windowId&&player.openContainer instanceof IEBaseContainer)
				((IEBaseContainer<?>)player.openContainer).receiveMessageFromScreen(nbt);
		});
	}
}