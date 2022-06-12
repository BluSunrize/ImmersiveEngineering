/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.gui.IEBaseContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageContainerUpdate implements IMessage
{
	private int windowId;
	private CompoundTag nbt;

	//TODO get rid of NBT in packets (maybe?)
	public MessageContainerUpdate(int windowId, CompoundTag nbt)
	{
		this.windowId = windowId;
		this.nbt = nbt;
	}

	public MessageContainerUpdate(FriendlyByteBuf buf)
	{
		this.windowId = buf.readByte();
		this.nbt = buf.readNbt();
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeByte(this.windowId);
		buf.writeNbt(this.nbt);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ServerPlayer player = ctx.getSender();
		assert player!=null;
		ctx.enqueueWork(() -> {
			player.resetLastActionTime();
			if(player.containerMenu.containerId==windowId&&player.containerMenu instanceof IEBaseContainer ieMenu)
				ieMenu.receiveMessageFromScreen(nbt);
		});
	}
}