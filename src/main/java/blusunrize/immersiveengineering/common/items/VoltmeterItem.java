/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
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
		super("voltmeter", new Properties().maxStackSize(1));
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if(ItemNBTHelper.hasKey(stack, "linkingPos"))
		{
			int[] link = stack.getOrCreateTag().getIntArray("linkingPos");
			if(link.length > 3)
				tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"attachedToDim", link[1], link[2], link[3], link[0]));
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
			int max = 0;
			int stored = 0;
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
			if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
			{
				ItemNBTHelper.putString(stack, "linkingDim", world.getDimension().getType().toString());
				ItemNBTHelper.putIntArray(stack, "linkingPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
			}
			else
			{
				String dim = ItemNBTHelper.getString(stack, "linkingDim");
				if(dim.equals(world.getDimension().getType().toString()))
				{
					int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos");
					BlockPos linkPos = new BlockPos(array[0], array[1], array[2]);
					TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
					IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
					IImmersiveConnectable nodeLink = (IImmersiveConnectable)tileEntityLinkingPos;
					if(nodeLink!=null)
					{
						//TODO
						//Set<AbstractConnection> connections = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(nodeLink), world, true);
						//for(AbstractConnection con : connections)
						//	if(Utils.toCC(nodeHere).equals(con.end))
						//		player.sendMessage(new TranslationTextComponent(Lib.CHAT_INFO+"averageLoss", Utils.formatDouble(con.getAverageLossRate()*100, "###.000")));
					}
				}
				ItemNBTHelper.remove(stack, "linkingPos");
				ItemNBTHelper.remove(stack, "linkingDim");
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
}
