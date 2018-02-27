/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.IWireCoil;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
		if(stack.getItemDamage() == 5)
		{
			list.add(I18n.format(Lib.DESC_FLAVOUR + "coil.redstone"));
			list.add(I18n.format(Lib.DESC_FLAVOUR + "coil.construction1"));
		} else if(stack.getItemDamage()%6 > 2)
		{
			list.add(I18n.format(Lib.DESC_FLAVOUR+"coil.construction0"));
			list.add(I18n.format(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		if(stack.getTagCompound()!=null && stack.getTagCompound().hasKey("linkingPos"))
		{
			int[] link = stack.getTagCompound().getIntArray("linkingPos");
			if(link!=null&&link.length>3)
				list.add(I18n.format(Lib.DESC_INFO+"attachedToDim", link[1],link[2],link[3],link[0]));
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
//	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		return ApiUtils.doCoilUse(this, player, world, pos, hand, side, hitX, hitY, hitZ);
	}
}