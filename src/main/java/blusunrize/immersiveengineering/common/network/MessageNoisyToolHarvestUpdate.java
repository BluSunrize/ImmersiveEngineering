/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.sound.NoisyToolSoundHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageNoisyToolHarvestUpdate(int holderID, byte actionOrdinal, BlockPos targetPos) implements IMessage
{
	public static final Type<MessageNoisyToolHarvestUpdate> ID = IMessage.createType("noisy_tool_harvesting_update");
	public static final StreamCodec<ByteBuf, MessageNoisyToolHarvestUpdate> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MessageNoisyToolHarvestUpdate::holderID,
			ByteBufCodecs.BYTE, MessageNoisyToolHarvestUpdate::actionOrdinal,
			BlockPos.STREAM_CODEC, MessageNoisyToolHarvestUpdate::targetPos,
			MessageNoisyToolHarvestUpdate::new
	);

	public MessageNoisyToolHarvestUpdate(LivingEntity holder, LeftClickBlock.Action action, BlockPos targetPos)
	{
		this(holder.getId(), (byte)action.ordinal(), targetPos);
	}

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null)
			{
				Entity entity = world.getEntity(holderID);
				if(entity instanceof LivingEntity holder)
					NoisyToolSoundHandler.handleHarvestAction(holder, LeftClickBlock.Action.class.getEnumConstants()[actionOrdinal], targetPos);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
