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
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.Path;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class VoltmeterItem extends IEBaseItem implements ITool
{
	public VoltmeterItem()
	{
		super(new Properties().maxStackSize(1));
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if(WirecoilUtils.hasWireLink(stack))
		{
			WireLink link = WireLink.readFromItem(stack);
			tooltip.add(new TranslationTextComponent(
					Lib.DESC_INFO+"attachedToDim",
					link.cp.getX(),
					link.cp.getY(),
					link.cp.getZ(),
					link.dimension.toString()
			));
		}
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		Direction side = context.getFace();
		PlayerEntity player = context.getPlayer();
		ItemStack stack = context.getItem();
		TileEntity tileEntity = world.getTileEntity(pos);
		if((player==null||!player.isSneaking())&&(tileEntity instanceof IFluxReceiver||tileEntity instanceof IFluxProvider))
		{
			int max;
			int stored;
			if(tileEntity instanceof IFluxReceiver)
			{
				max = ((IFluxReceiver)tileEntity).getMaxEnergyStored(side);
				stored = ((IFluxReceiver)tileEntity).getEnergyStored(side);
			}
			else
			{
				max = ((IFluxProvider)tileEntity).getMaxEnergyStored(side);
				stored = ((IFluxProvider)tileEntity).getEnergyStored(side);
			}
			if(max > 0)
				ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"energyStorage", stored, max));
			return ActionResultType.SUCCESS;
		}
		if(player!=null&&player.isSneaking()&&tileEntity instanceof IImmersiveConnectable)
		{
			if(world.isRemote)
				return ActionResultType.SUCCESS;
			TargetingInfo targetingInfo = new TargetingInfo(context);
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(null, targetingInfo);
			BlockPos delta = pos.subtract(masterPos);
			ConnectionPoint cp = ((IImmersiveConnectable)tileEntity).getTargetedPoint(targetingInfo, delta);
			if(cp==null)
				return ActionResultType.FAIL;
			if(!WirecoilUtils.hasWireLink(stack))
			{
				WireLink link = WireLink.create(cp, world, delta, targetingInfo);
				link.writeToItem(stack);
			}
			else
			{
				WireLink link = WireLink.readFromItem(stack);
				if(link.dimension.equals(world.getDimensionKey()))
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
							player.sendMessage(new TranslationTextComponent(
									Lib.CHAT_INFO+"averageLoss",
									Utils.formatDouble(loss*100, "###.000")
							), Util.DUMMY_UUID);
						}
					}
				}
				WirecoilUtils.clearWireLink(stack);
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
}
