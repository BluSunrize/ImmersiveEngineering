/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteRedstoneData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

public record MessageRequestRedstoneUpdate(BlockPos pos) implements IMessage
{
	public MessageRequestRedstoneUpdate(FriendlyByteBuf buf)
	{
		this(readPos(buf));
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		writePos(buf, pos);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerLevel level = Objects.requireNonNull(ctx.getSender()).serverLevel();
			ICapabilityProvider provider;
			BlockState blockState = level.getBlockState(pos);
			RemoteRedstoneData data = new RemoteRedstoneData(pos, level.getGameTime(), blockState.isSignalSource(), redstoneLevel(level, pos));
			ImmersiveEngineering.packetHandler.send(
					PacketDistributor.PLAYER.with(ctx::getSender), new MessageRedstoneLevel(data)
			);
		});
	}

	public static byte redstoneLevel(Level level, BlockPos pos)
	{
		BlockState blockState = level.getBlockState(pos);
		byte redstoneLevel = 0;
		if(blockState!=null)
		{
			if(blockState.isSignalSource())
			{
				redstoneLevel = blockState.getOptionalValue(RedStoneWireBlock.POWER).orElse(0).byteValue();
				for(Direction facing : Direction.values())
					redstoneLevel = (byte)Math.max(redstoneLevel, blockState.getSignal(level, pos, facing));
			}
			else
				redstoneLevel = (byte)Math.max(redstoneLevel, level.getDirectSignalTo(pos));
		}
		return redstoneLevel;
	}

	public static BlockPos readPos(FriendlyByteBuf buf)
	{
		return buf.readBlockPos();
	}

	public static void writePos(FriendlyByteBuf out, BlockPos pos)
	{
		out.writeBlockPos(pos);
	}
}
