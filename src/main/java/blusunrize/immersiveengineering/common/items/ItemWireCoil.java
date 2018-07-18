/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IWireCoil;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class ItemWireCoil extends ItemIEBase implements IWireCoil
{
	public ItemWireCoil()
	{
		super("wirecoil", 64, "copper", "electrum", "hv", "rope", "structural", "redstone",
				"insulated_copper", "insulated_electrum");
	}

	@Override
	public WireType getWireType(ItemStack stack)
	{
		switch(stack.getItemDamage())
		{
			case 0:
			default:
				return WireType.COPPER;
			case 1:
				return WireType.ELECTRUM;
			case 2:
				return WireType.STEEL;
			case 3:
				return WireType.STRUCTURE_ROPE;
			case 4:
				return WireType.STRUCTURE_STEEL;
			case 5:
				return WireType.REDSTONE;
			case 6:
				return WireType.COPPER_INSULATED;
			case 7:
				return WireType.ELECTRUM_INSULATED;
		}
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		if(stack.getItemDamage()==5)
		{
			list.add(I18n.format(Lib.DESC_FLAVOUR+"coil.redstone"));
			list.add(I18n.format(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		else if(stack.getItemDamage()%6 > 2)
		{
			list.add(I18n.format(Lib.DESC_FLAVOUR+"coil.construction0"));
			list.add(I18n.format(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		if(stack.getTagCompound()!=null&&stack.getTagCompound().hasKey("linkingPos"))
		{
			int[] link = stack.getTagCompound().getIntArray("linkingPos");
			if(link!=null&&link.length > 3)
				list.add(I18n.format(Lib.DESC_INFO+"attachedToDim", link[1], link[2], link[3], link[0]));
		}
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		return ApiUtils.doCoilUse(this, player, world, pos, hand, side, hitX, hitY, hitZ);
	}
}