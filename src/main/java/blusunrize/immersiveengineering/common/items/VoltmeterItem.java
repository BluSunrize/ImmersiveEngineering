/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.Path;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import blusunrize.immersiveengineering.common.network.MessageRequestEnergyUpdate;
import blusunrize.immersiveengineering.common.network.MessageRequestRedstoneUpdate;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

public class VoltmeterItem extends IEBaseItem
{
	public static RemoteEnergyData lastEnergyUpdate = new RemoteEnergyData(
			FastEither.left(BlockPos.ZERO), 0, false, 0, 0
	);
	public static RemoteRedstoneData lastRedstoneUpdate = new RemoteRedstoneData(
			BlockPos.ZERO, 0, false, (byte)0
	);

	public VoltmeterItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if(WirecoilUtils.hasWireLink(stack))
		{
			WireLink link = WireLink.readFromItem(stack);
			tooltip.add(Component.translatable(
					Lib.DESC_INFO+"attachedToDim",
					link.cp.getX(),
					link.cp.getY(),
					link.cp.getZ(),
					link.dimension.toString()
			));
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();
		BlockEntity bEntity = world.getBlockEntity(pos);
		if((player==null||!player.isShiftKeyDown())&&bEntity!=null)
		{
			IEnergyStorage energyCap = CapabilityUtils.getCapability(bEntity, CapabilityEnergy.ENERGY);
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
			if(bEntity instanceof IImmersiveConnectable)
			{
				if(world.isClientSide)
					return InteractionResult.SUCCESS;
				TargetingInfo targetingInfo = new TargetingInfo(context);
				BlockPos masterPos = ((IImmersiveConnectable)bEntity).getConnectionMaster(null, targetingInfo);
				BlockPos delta = pos.subtract(masterPos);
				ConnectionPoint cp = ((IImmersiveConnectable)bEntity).getTargetedPoint(targetingInfo, delta);
				if(cp==null)
					return InteractionResult.FAIL;
				if(!WirecoilUtils.hasWireLink(stack))
				{
					WireLink link = WireLink.create(cp, world, delta, targetingInfo);
					link.writeToItem(stack);
				}
				else
				{
					WireLink link = WireLink.readFromItem(stack);
					if(link.dimension.equals(world.dimension()))
					{
						GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
						LocalWireNetwork netHere = global.getNullableLocalNet(cp);
						LocalWireNetwork netLink = global.getNullableLocalNet(link.cp);
						if(netHere==netLink&&netHere!=null)
						{
							EnergyTransferHandler energyHandler = netHere.getHandler(EnergyTransferHandler.ID,
									EnergyTransferHandler.class);
							if(energyHandler!=null)
							{
								Path energyPath = energyHandler.getPath(link.cp, cp);
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
					WirecoilUtils.clearWireLink(stack);
				}
				return InteractionResult.SUCCESS;
			}
			else
			{
				if(!world.isClientSide)
					ChatUtils.sendServerNoSpamMessages(player, Component.translatable(Lib.CHAT_INFO+"redstoneLevel", MessageRequestRedstoneUpdate.redstoneLevel(world, pos)));
			}
		}
		return InteractionResult.PASS;
	}

	public static record RemoteEnergyData(
			FastEither<BlockPos, Integer> pos, long measuredInTick, boolean isValid, int stored, int capacity
	)
	{
		public static RemoteEnergyData read(FriendlyByteBuf in)
		{
			FastEither<BlockPos, Integer> pos = MessageRequestEnergyUpdate.readPos(in);
			long measuredInTick = in.readVarLong();
			boolean isValid = in.readBoolean();
			final int stored;
			final int capacity;
			if(isValid)
			{
				stored = in.readVarInt();
				capacity = in.readVarInt();
			}
			else
				stored = capacity = 0;
			return new RemoteEnergyData(pos, measuredInTick, isValid, stored, capacity);
		}

		public void write(FriendlyByteBuf out)
		{
			MessageRequestEnergyUpdate.writePos(out, pos);
			out.writeVarLong(measuredInTick)
					.writeBoolean(isValid);
			if(isValid)
				out.writeVarInt(stored).writeVarInt(capacity);
		}
	}

	public static record RemoteRedstoneData(
			BlockPos pos, long measuredInTick, boolean isSignalSource, byte rsLevel
	)
	{
		public static RemoteRedstoneData read(FriendlyByteBuf in)
		{
			BlockPos pos = in.readBlockPos();
			long measuredInTick = in.readVarLong();
			boolean isSignalSource = in.readBoolean();
			final byte rsLevel;
			rsLevel = in.readByte();
			return new RemoteRedstoneData(pos, measuredInTick, isSignalSource, rsLevel);
		}

		public void write(FriendlyByteBuf out)
		{
			out.writeBlockPos(pos);
			out.writeVarLong(measuredInTick)
					.writeBoolean(isSignalSource);
			out.writeByte(rsLevel);
		}
	}
}
