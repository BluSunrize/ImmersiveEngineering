/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteEnergyData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.NetworkEvent.Context;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

public record MessageRequestEnergyUpdate(FastEither<BlockPos, Integer> pos) implements IMessage
{
	public MessageRequestEnergyUpdate(FriendlyByteBuf buf)
	{
		this(readPos(buf));
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		writePos(buf, pos);
	}

	@Override
	public void process(Context context)
	{
		context.enqueueWork(() -> {
			ServerLevel level = Objects.requireNonNull(context.getSender()).serverLevel();
			ICapabilityProvider provider;
			if(pos.isLeft())
				provider = SafeChunkUtils.getSafeBE(level, pos.leftNonnull());
			else
				provider = level.getEntity(pos.rightNonnull());
			RemoteEnergyData data = null;
			if(provider!=null)
			{
				IEnergyStorage energyCap = CapabilityUtils.getCapability(provider, Capabilities.ENERGY);
				if(energyCap!=null&&energyCap.getMaxEnergyStored() > 0)
					data = new RemoteEnergyData(
							pos, level.getGameTime(), true, energyCap.getEnergyStored(), energyCap.getMaxEnergyStored()
					);
			}
			if(data==null)
				data = new RemoteEnergyData(pos, level.getGameTime(), false, 0, 0);
			ImmersiveEngineering.packetHandler.send(
					PacketDistributor.PLAYER.with(context::getSender), new MessageStoredEnergy(data)
			);
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
}