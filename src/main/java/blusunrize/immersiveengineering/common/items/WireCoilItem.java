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
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import blusunrize.immersiveengineering.common.network.MessageObstructedConnection;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.*;

import static blusunrize.immersiveengineering.api.wires.utils.WireUtils.findObstructingBlocks;
import static blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils.clearWireLink;
import static blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils.hasWireLink;

public class WireCoilItem extends IEBaseItem implements IWireCoil
{
	@Nonnull
	private final WireType type;

	public WireCoilItem(@Nonnull WireType type)
	{
		super(new Properties());
		this.type = type;
	}

	@Override
	public WireType getWireType(ItemStack stack)
	{
		return type;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		if(WireType.REDSTONE_CATEGORY.equals(type.getCategory()))
		{
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"coil.redstone"));
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		else if(WireType.STRUCTURE_CATEGORY.equals(type.getCategory()))
		{
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"coil.construction0"));
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		if(hasWireLink(stack))
		{
			WireLink link = WireLink.readFromItem(stack);
			String dimensionName = "";
			if(link.dimension!=null)
			{
				String s2 = link.dimension.location().getPath();
				if(s2.toLowerCase(Locale.ENGLISH).startsWith("the_"))
					s2 = s2.substring(4);
				dimensionName = Utils.toCamelCase(s2);
			}
			list.add(Component.translatable(Lib.DESC_INFO+"attachedToDim", link.cp.getX(),
					link.cp.getY(), link.cp.getZ(), dimensionName));
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		return WirecoilUtils.doCoilUse(this, ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), ctx.getHand(), ctx.getClickedFace(),
				(float)ctx.getClickLocation().x, (float)ctx.getClickLocation().y, (float)ctx.getClickLocation().z);
	}


	public static InteractionResult doCoilUse(
			IWireCoil coil, Player player, Level world, BlockPos pos, InteractionHand hand, Direction side,
			float hitX, float hitY, float hitZ
	)
	{
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if(tileEntity instanceof IImmersiveConnectable&&((IImmersiveConnectable)tileEntity).canConnect())
		{
			ItemStack stack = player.getItemInHand(hand);
			TargetingInfo targetHere = new TargetingInfo(side, hitX-pos.getX(), hitY-pos.getY(), hitZ-pos.getZ());
			WireType wire = coil.getWireType(stack);
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(wire, targetHere);
			BlockPos masterOffsetHere = pos.subtract(masterPos);
			tileEntity = world.getBlockEntity(masterPos);
			if(!(tileEntity instanceof IImmersiveConnectable iicHere)||!iicHere.canConnect())
				return InteractionResult.PASS;
			ConnectionPoint cpHere = iicHere.getTargetedPoint(targetHere, masterOffsetHere);

			if(cpHere==null||!iicHere.canConnectCable(wire, cpHere, masterOffsetHere)||
					!coil.canConnectCable(stack, tileEntity))
			{
				if(!world.isClientSide)
					player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"wrongCable"), true);
				return InteractionResult.FAIL;
			}

			if(!world.isClientSide)
			{
				if(!hasWireLink(stack))
				{
					WireLink link = WireLink.create(cpHere, world, masterOffsetHere, targetHere);
					link.writeToItem(stack);
				}
				else
				{
					final WireLink otherLink = WireLink.readFromItem(stack);
					BlockEntity tileEntityLinkingPos = world.getBlockEntity(otherLink.cp.position());
					int distanceSq = (int)Math.ceil(otherLink.cp.position().distSqr(masterPos));
					int maxLengthSq = coil.getMaxLength(stack); //not squared yet
					maxLengthSq *= maxLengthSq;
					if(!otherLink.dimension.equals(world.dimension()))
						player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"wrongDimension"), true);
					else if(otherLink.cp.position().equals(masterPos))
						player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"sameConnection"), true);
					else if(distanceSq > maxLengthSq)
						player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"tooFar"), true);
					else
					{
						if(!(tileEntityLinkingPos instanceof IImmersiveConnectable iicLink))
							player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"invalidPoint"), true);
						else
						{
							if(!iicLink.canConnectCable(wire, otherLink.cp, otherLink.offset)||
									!iicLink.getConnectionMaster(wire, otherLink.target).equals(otherLink.cp.position())||
									!coil.canConnectCable(stack, tileEntityLinkingPos))
							{
								player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"invalidPoint"), true);
							}
							else
							{
								GlobalWireNetwork net = GlobalWireNetwork.getNetwork(world);
								boolean connectionExists = false;
								LocalWireNetwork localA = net.getLocalNet(cpHere);
								LocalWireNetwork localB = net.getLocalNet(otherLink.cp);
								if(localA==localB)
								{
									Collection<Connection> outputs = localA.getConnections(cpHere);
									if(outputs!=null)
										for(Connection con : outputs)
											if(!con.isInternal()&&con.getOtherEnd(cpHere).equals(otherLink.cp))
												connectionExists = true;
								}
								if(connectionExists)
									player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"connectionExists"), true);
								else
								{
									Set<BlockPos> ignore = new HashSet<>();
									ignore.addAll(iicHere.getIgnored(iicLink));
									ignore.addAll(iicLink.getIgnored(iicHere));
									Connection conn = new Connection(wire, cpHere, otherLink.cp, net);
									Set<BlockPos> failedReasons = findObstructingBlocks(world, conn, ignore);
									if(failedReasons.isEmpty())
									{
										net.addConnection(conn);

										iicHere.connectCable(wire, cpHere, iicLink, otherLink.cp);
										iicLink.connectCable(wire, otherLink.cp, iicHere, cpHere);
										Utils.unlockIEAdvancement(player, "main/connect_wire");

										if(!player.getAbilities().instabuild)
											coil.consumeWire(stack, (int)Math.sqrt(distanceSq));
										((BlockEntity)iicHere).setChanged();
										//TODO is this needed with the new sync system?
										world.blockEvent(masterPos, ((BlockEntity)iicHere).getBlockState().getBlock(), -1, 0);
										BlockState state = world.getBlockState(masterPos);
										world.sendBlockUpdated(masterPos, state, state, 3);
										((BlockEntity)iicLink).setChanged();
										world.blockEvent(otherLink.cp.position(), tileEntityLinkingPos.getBlockState().getBlock(), -1, 0);
										state = world.getBlockState(otherLink.cp.position());
										world.sendBlockUpdated(otherLink.cp.position(), state, state, 3);
									}
									else
									{
										player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"cantSee"), true);
										PacketDistributor.PLAYER.with((ServerPlayer)player).send(
												new MessageObstructedConnection(conn, failedReasons)
										);
									}
								}
							}
						}
					}
					clearWireLink(stack);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}