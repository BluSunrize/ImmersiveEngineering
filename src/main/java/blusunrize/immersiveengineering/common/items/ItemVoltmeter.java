package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ItemVoltmeter extends ItemIEBase implements ITool
{
	public final static String NAME = "voltmeter";

	public ItemVoltmeter()
	{
		super(NAME, 1);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side,
									  float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		TileEntity tileEntity = world.getTileEntity(pos);
		if(!world.isRemote)
		{
			if(!player.isSneaking() && (tileEntity instanceof IFluxReceiver || tileEntity instanceof IFluxProvider))
			{
				int max = 0;
				int stored = 0;
				if(tileEntity instanceof IFluxReceiver)
				{
					max = ((IFluxReceiver) tileEntity).getMaxEnergyStored(side);
					stored = ((IFluxReceiver) tileEntity).getEnergyStored(side);
				}
				else
				{
					max = ((IFluxProvider) tileEntity).getMaxEnergyStored(side);
					stored = ((IFluxProvider) tileEntity).getEnergyStored(side);
				}
				if(max > 0)
					ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO + "energyStorage", stored, max));
				return EnumActionResult.SUCCESS;
			}
			if(player.isSneaking() && tileEntity instanceof IImmersiveConnectable)
			{
				if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
					ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ()});
				else
				{
					int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos");
					BlockPos linkPos = new BlockPos(array[1], array[2], array[3]);
					TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
					if(array[0] == world.provider.getDimension())
					{
						IImmersiveConnectable nodeHere = (IImmersiveConnectable) tileEntity;
						IImmersiveConnectable nodeLink = (IImmersiveConnectable) tileEntityLinkingPos;
						if(nodeLink != null)
						{
							Set<ImmersiveNetHandler.AbstractConnection> connections = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(nodeLink), world, true);
							for(ImmersiveNetHandler.AbstractConnection con : connections)
								if(Utils.toCC(nodeHere).equals(con.end))
									player.sendMessage(new TextComponentTranslation(Lib.CHAT_INFO + "averageLoss", Utils.formatDouble(con.getAverageLossRate() * 100, "###.000")));
						}
					}
					ItemNBTHelper.remove(stack, "linkingPos");
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		if(ItemNBTHelper.hasKey(stack, "linkingPos"))
		{
			int[] link = ItemNBTHelper.getIntArray(stack, "linkingPos");
			if(link!=null&&link.length>3)
				list.add(I18n.format(Lib.DESC_INFO+"attachedToDim", link[1],link[2],link[3],link[0]));
		}
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}
