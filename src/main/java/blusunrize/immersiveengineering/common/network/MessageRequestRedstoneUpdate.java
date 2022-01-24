package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteRedstoneData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
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
			ServerLevel level = Objects.requireNonNull(ctx.getSender()).getLevel();
			ICapabilityProvider provider;
			BlockState blockState = level.getBlockState(pos);
			RemoteRedstoneData data = null;
			if(blockState!=null)//&&blockState.isSignalSource())
			{
				byte redstoneLevel = 0;
				if(blockState.isSignalSource())
				{
					redstoneLevel = blockState.getOptionalValue(RedStoneWireBlock.POWER).orElse(0).byteValue();
				/*Direction facing = null;
				for(Property prop : blockState.getProperties())
				{
					if(prop instanceof DirectionProperty dirProp&&prop.getName().equals("facing"))
					{
						facing = blockState.getOptionalValue(dirProp).orElse(null);
					}
				}
				if(facing!=null)
					redstoneLevel = Math.max(redstoneLevel, blockState.getSignal(level, pos, facing));*/
					for(Direction facing : Direction.values())
					{
						redstoneLevel = (byte)Math.max(redstoneLevel, blockState.getSignal(level, pos, facing));
					}
				}
				else
					redstoneLevel = (byte)Math.max(redstoneLevel, level.getDirectSignalTo(pos));
				data = new RemoteRedstoneData(
						pos, level.getGameTime(), true, blockState.isSignalSource(), redstoneLevel
				);
			}
			if(data==null)
				data = new RemoteRedstoneData(pos, level.getGameTime(), false, false, (byte)0);
			ImmersiveEngineering.packetHandler.send(
					PacketDistributor.PLAYER.with(ctx::getSender), new MessageRedstoneLevel(data)
			);
		});
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
