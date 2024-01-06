/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers.DataPair;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;

public class MessageContainerData implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("container_data");
	private final List<Pair<Integer, DataPair<?>>> synced;

	public MessageContainerData(List<Pair<Integer, DataPair<?>>> synced)
	{
		this.synced = synced;
	}

	public MessageContainerData(FriendlyByteBuf buf)
	{
		this(PacketUtils.readList(buf, pb -> Pair.of(pb.readVarInt(), GenericDataSerializers.read(pb))));
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		PacketUtils.writeList(buf, synced, (pair, b) -> {
			b.writeVarInt(pair.getFirst());
			pair.getSecond().write(b);
		});
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		context.workHandler().execute(() -> {
			AbstractContainerMenu currentContainer = ImmersiveEngineering.proxy.getClientPlayer().containerMenu;
			if(currentContainer instanceof IEContainerMenu ieContainer)
				ieContainer.receiveSync(synced);
		});
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
