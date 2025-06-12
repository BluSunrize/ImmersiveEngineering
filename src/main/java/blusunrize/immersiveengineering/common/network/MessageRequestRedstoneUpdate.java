/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IRedstoneOutput;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteRedstoneData;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageRequestRedstoneUpdate(BlockPos pos) implements IMessage
{
	public static final Type<MessageRequestRedstoneUpdate> ID = IMessage.createType("request_rs_update");
	public static final StreamCodec<ByteBuf, MessageRequestRedstoneUpdate> CODEC = BlockPos.STREAM_CODEC
			.map(MessageRequestRedstoneUpdate::new, MessageRequestRedstoneUpdate::pos);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			Level level = context.player().level();
			PacketDistributor.sendToPlayer(IMessage.serverPlayer(context), new MessageRedstoneLevel(buildData(level, pos)));
		});
	}

	private static RemoteRedstoneData buildData(Level level, BlockPos pos)
	{
		BlockState blockState = level.getBlockState(pos);
		if(!blockState.isSignalSource())
			return new RemoteRedstoneData(pos, level.getGameTime(), false, (byte)0);

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof IRedstoneOutput iRedstoneOutput)
		{
			// special case for IE's own blocks
			Pair<DyeColor, Byte>[] override = iRedstoneOutput.overrideVoltmeterRead();
			if(override!=null)
				return new RemoteRedstoneData(pos, level.getGameTime(), true, override);
		}
		return new RemoteRedstoneData(pos, level.getGameTime(), true, redstoneLevel(level, pos));
	}

	public static byte redstoneLevel(Level level, BlockPos pos)
	{
		BlockState blockState = level.getBlockState(pos);
		byte redstoneLevel;
		if(blockState.isSignalSource())
		{
			redstoneLevel = blockState.getOptionalValue(RedStoneWireBlock.POWER).orElse(0).byteValue();
			for(Direction facing : Direction.values())
				redstoneLevel = (byte)Math.max(redstoneLevel, blockState.getSignal(level, pos, facing));
		}
		else
			redstoneLevel = (byte)level.getDirectSignalTo(pos);
		return redstoneLevel;
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
