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
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class ItemWireCoil extends ItemIEBase implements IWireCoil
{

	@Nonnull
	private final WireType type;

	public ItemWireCoil(WireType type)
	{
		super("wirecoil_"+type.getUniqueName().toLowerCase(), new Properties());
		this.type = type;
	}

	@Override
	public WireType getWireType(ItemStack stack)
	{
		return type;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		if(WireType.REDSTONE_CATEGORY.equals(type.getCategory()))
		{
			list.add(new TextComponentTranslation(Lib.DESC_FLAVOUR+"coil.redstone"));
			list.add(new TextComponentTranslation(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		else if(WireType.STRUCTURE_CATEGORY.equals(type.getCategory()))
		{
			list.add(new TextComponentTranslation(Lib.DESC_FLAVOUR+"coil.construction0"));
			list.add(new TextComponentTranslation(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		if(stack.hasTag()&&stack.getOrCreateTag().contains("linkingPos", NBT.TAG_INT_ARRAY))
		{
			int[] link = stack.getOrCreateTag().getIntArray("linkingPos");
			if(link.length > 3)
				list.add(new TextComponentTranslation(Lib.DESC_INFO+"attachedToDim", link[1], link[2], link[3], link[0]));
		}
	}

	@Override
	public EnumActionResult onItemUse(ItemUseContext ctx)
	{
		return ApiUtils.doCoilUse(this, ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getItem(), ctx.getFace(),
				ctx.getHitX(), ctx.getHitY(), ctx.getHitZ());
	}
}