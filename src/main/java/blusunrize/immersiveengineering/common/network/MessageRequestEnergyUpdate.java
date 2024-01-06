/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteEnergyData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record MessageRequestEnergyUpdate(FastEither<BlockPos, Integer> pos) implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("request_energy_update");
	public MessageRequestEnergyUpdate(FriendlyByteBuf buf)
	{
		this(readPos(buf));
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		writePos(buf, pos);
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		context.workHandler().execute(() -> {
			Level level = context.player().orElseThrow().level();
			IEnergyStorage storage;
			if(pos.isLeft())
				storage = level.getCapability(EnergyStorage.BLOCK, pos.leftNonnull(), null);
			else
			{
				Entity entity = level.getEntity(pos.rightNonnull());
				if(entity!=null)
					storage = entity.getCapability(EnergyStorage.ENTITY, null);
				else
					storage = null;
			}
			RemoteEnergyData data = null;
			if(storage!=null&&storage.getMaxEnergyStored() > 0)
				data = new RemoteEnergyData(
						pos, level.getGameTime(), true, storage.getEnergyStored(), storage.getMaxEnergyStored()
				);
			if(data==null)
				data = new RemoteEnergyData(pos, level.getGameTime(), false, 0, 0);
			PacketDistributor.PLAYER.with(serverPlayer(context)).send(new MessageStoredEnergy(data));
		});
	}

	public static FastEither<BlockPos, Integer> readPos(FriendlyByteBuf buf)
	{
		if(buf.readBoolean())
			return FastEither.right(buf.readInt());
		else
			return FastEither.left(buf.readBlockPos());
	}

	public static void writePos(FriendlyByteBuf out, FastEither<BlockPos, Integer> pos)
	{
		out.writeBoolean(pos.isRight());
		if(pos.isRight())
			out.writeInt(pos.rightNonnull());
		else
			out.writeBlockPos(pos.leftNonnull());
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}