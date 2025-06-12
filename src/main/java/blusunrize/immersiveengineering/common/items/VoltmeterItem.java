/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.Path;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.common.network.MessageRequestRedstoneUpdate;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class VoltmeterItem extends IEBaseItem
{
	public static RemoteEnergyData lastEnergyUpdate = new RemoteEnergyData(
			Either.left(BlockPos.ZERO), 0, false, 0, 0
	);
	public static RemoteRedstoneData lastRedstoneUpdate = new RemoteRedstoneData(
			BlockPos.ZERO, 0, false, (byte)0
	);

	public VoltmeterItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack, ctx, tooltip, flagIn);
		WireLink link = stack.get(IEApiDataComponents.WIRE_LINK);
		if(link!=null)
			tooltip.add(Component.translatable(
					Lib.DESC_INFO+"attachedToDim",
					link.cp().getX(),
					link.cp().getY(),
					link.cp().getZ(),
					link.dimension().toString()
			));
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if((player==null||!player.isShiftKeyDown()))
		{
			IEnergyStorage energyCap = level.getCapability(EnergyStorage.BLOCK, pos, null);
			if(energyCap!=null)
			{
				int max = energyCap.getMaxEnergyStored();
				int stored = energyCap.getEnergyStored();
				if(max > 0)
					ChatUtils.sendServerNoSpamMessages(player, Component.translatable(Lib.CHAT_INFO+"energyStorage", stored, max));
				return InteractionResult.SUCCESS;
			}
		}
		if(player!=null&&player.isShiftKeyDown())
		{
			if(level.isClientSide)
				return InteractionResult.SUCCESS;
			// either measure loss
			if(!measureLoss(level, pos, player, context))
			{
				// or measure redstone level
				ChatUtils.sendServerNoSpamMessages(player, Component.translatable(
						Lib.CHAT_INFO+"redstone_level",
						MessageRequestRedstoneUpdate.redstoneLevel(level, pos))
				);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private static boolean measureLoss(Level level, BlockPos pos, Player player, UseOnContext context)
	{
		ItemStack stack = context.getItemInHand();
		BlockEntity bEntity = level.getBlockEntity(pos);
		if(!(bEntity instanceof IImmersiveConnectable))
			return false;

		// Find connection point or exit
		TargetingInfo targetingInfo = new TargetingInfo(context);
		BlockPos masterPos = ((IImmersiveConnectable)bEntity).getConnectionMaster(null, targetingInfo);
		BlockPos delta = pos.subtract(masterPos);
		ConnectionPoint cp = ((IImmersiveConnectable)bEntity).getTargetedPoint(targetingInfo, delta);
		if(cp==null)
			return false;

		// Find energy handler or exit
		GlobalWireNetwork global = GlobalWireNetwork.getNetwork(level);
		LocalWireNetwork netHere = global.getNullableLocalNet(cp);
		EnergyTransferHandler energyHandler = netHere.getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
		if(energyHandler==null)
			return false;

		// Store position or check loss if position is present
		if(!stack.has(IEApiDataComponents.WIRE_LINK))
			stack.set(IEApiDataComponents.WIRE_LINK, WireLink.create(cp, level, delta, targetingInfo));
		else
		{
			WireLink link = stack.remove(IEApiDataComponents.WIRE_LINK);
			if(link.dimension().equals(level.dimension()))
			{
				LocalWireNetwork netLink = global.getNullableLocalNet(link.cp());
				// check for same network
				if(netHere==netLink)
				{
					Path energyPath = energyHandler.getPath(link.cp(), cp);
					double loss;
					if(energyPath!=null)
						loss = energyPath.loss;
					else
						loss = 1;
					player.sendSystemMessage(Component.translatable(
							Lib.CHAT_INFO+"averageLoss", Utils.formatDouble(loss*100, "###.000")
					));
				}
			}
		}
		return true;
	}

	public record RemoteEnergyData(
			Either<BlockPos, Integer> pos, long measuredInTick, boolean isValid, int stored, int capacity
	)
	{
		public static final StreamCodec<ByteBuf, RemoteEnergyData> CODEC = StreamCodec.composite(
				ByteBufCodecs.either(BlockPos.STREAM_CODEC, ByteBufCodecs.INT), RemoteEnergyData::pos,
				ByteBufCodecs.VAR_LONG, RemoteEnergyData::measuredInTick,
				ByteBufCodecs.BOOL, RemoteEnergyData::isValid,
				ByteBufCodecs.INT, RemoteEnergyData::stored,
				ByteBufCodecs.INT, RemoteEnergyData::capacity,
				RemoteEnergyData::new
		);
	}

	public record RemoteRedstoneData(
			BlockPos pos, long measuredInTick, boolean isSignalSource, FastEither<Byte, Pair<DyeColor, Byte>[]> rsLevels
	)
	{
		public RemoteRedstoneData(BlockPos pos, long measuredInTick, boolean isSignalSource, byte rsLevel)
		{
			this(pos, measuredInTick, isSignalSource, FastEither.left(rsLevel));
		}

		public RemoteRedstoneData(BlockPos pos, long measuredInTick, boolean isSignalSource, Pair<DyeColor, Byte>[] rsLevel)
		{
			this(pos, measuredInTick, isSignalSource, FastEither.right(rsLevel));
		}

		private static final StreamCodec<ByteBuf, Pair<DyeColor, Byte>[]> LEVEL_ARRAY_CODEC = StreamCodec.of(
				(buffer, pairs) -> {
					ByteBufCodecs.writeCount(buffer, pairs.length, 16);
					for(Pair<DyeColor, Byte> v : pairs)
					{
						DyeColor.STREAM_CODEC.encode(buffer, v.getFirst());
						ByteBufCodecs.BYTE.encode(buffer, v.getSecond());
					}
				},
				(buffer) -> {
					int readCount = ByteBufCodecs.readCount(buffer, 16);
					Pair<DyeColor, Byte>[] array = new Pair[readCount];
					for(int j = 0; j < readCount; j++)
						array[j] = Pair.of(
								DyeColor.STREAM_CODEC.decode(buffer),
								ByteBufCodecs.BYTE.decode(buffer)
						);
					return array;
				}
		);

		public static final StreamCodec<ByteBuf, RemoteRedstoneData> STREAM_CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, RemoteRedstoneData::pos,
				ByteBufCodecs.VAR_LONG, RemoteRedstoneData::measuredInTick,
				ByteBufCodecs.BOOL, RemoteRedstoneData::isSignalSource,
				FastEither.streamCodec(ByteBufCodecs.BYTE, LEVEL_ARRAY_CODEC), RemoteRedstoneData::rsLevels,
				RemoteRedstoneData::new
		);
	}
}
