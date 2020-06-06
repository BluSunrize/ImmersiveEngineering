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
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import jdk.nashorn.internal.runtime.regexp.joni.constants.TargetInfo;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.http.pool.ConnPool;

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
			CompoundNBT link = stack.getOrCreateTag().getCompound("linkingPos");
			String dimension = stack.getOrCreateTag().getString("linkingDim");
			ConnectionPoint cp = new ConnectionPoint(link);
			tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"attachedToDim", cp.getX(), cp.getY(), cp.getZ(), dimension));
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
			if(world.isRemote)
				return ActionResultType.SUCCESS;
			TargetingInfo targetingInfo = new TargetingInfo(context);
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(null, targetingInfo);
			Vec3i delta = pos.subtract(masterPos);
			ConnectionPoint cp = ((IImmersiveConnectable)tileEntity).getTargetedPoint(targetingInfo, delta);
			if(cp==null)
				return ActionResultType.FAIL;
			if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
			{
				CompoundNBT nbt = stack.getOrCreateTag();
				nbt.putString("linkingDim", world.getDimension().getType().getRegistryName().toString());
				nbt.put("linkingPos", cp.createTag());
			}
			else
			{
				String dim = ItemNBTHelper.getString(stack, "linkingDim");
				if(dim.equals(world.getDimension().getType().getRegistryName().toString()))
				{
					ConnectionPoint linkCP = new ConnectionPoint(stack.getOrCreateTag().getCompound("linkingPos"));
					GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
					LocalWireNetwork netHere = global.getNullableLocalNet(cp);
					LocalWireNetwork netLink = global.getNullableLocalNet(linkCP);
					if(netHere==netLink&&netHere!=null)
					{
						EnergyTransferHandler energyHandler = netHere.getHandler(EnergyTransferHandler.ID,
								EnergyTransferHandler.class);
						if(energyHandler!=null)
						{
							Path energyPath = energyHandler.getPath(linkCP, cp);
							if(energyPath!=null)
								player.sendMessage(new TranslationTextComponent(
										Lib.CHAT_INFO+"averageLoss",
										Utils.formatDouble(energyPath.loss*100, "###.000")
								));
						}
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
