/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.items.VoltmeterItem;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteRedstoneData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record MessageRedstoneLevel(VoltmeterItem.RemoteRedstoneData data) implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("redstone_level");

	public MessageRedstoneLevel(FriendlyByteBuf in)
	{
		this(RemoteRedstoneData.read(in));
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		data.write(buf);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		context.workHandler().execute(() -> VoltmeterItem.lastRedstoneUpdate = data);
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
