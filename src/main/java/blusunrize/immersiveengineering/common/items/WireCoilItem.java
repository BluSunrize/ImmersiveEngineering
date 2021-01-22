/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;


public class WireCoilItem extends IEBaseItem implements IWireCoil
{

	@Nonnull
	private final WireType type;

	public WireCoilItem(WireType type)
	{
		super("wirecoil_"+type.getUniqueName().toLowerCase(Locale.US), new Properties());
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
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"coil.redstone"));
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		else if(WireType.STRUCTURE_CATEGORY.equals(type.getCategory()))
		{
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"coil.construction0"));
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"coil.construction1"));
		}
		if(WirecoilUtils.hasWireLink(stack))
		{
			WireLink link = WireLink.readFromItem(stack);
			list.add(new TranslationTextComponent(Lib.DESC_INFO+"attachedToDim", link.cp.getX(),
					link.cp.getY(), link.cp.getZ(), link.dimension));
		}
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		return WirecoilUtils.doCoilUse(this, ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getHand(), ctx.getFace(),
				(float)ctx.getHitVec().x, (float)ctx.getHitVec().y, (float)ctx.getHitVec().z);
	}
}