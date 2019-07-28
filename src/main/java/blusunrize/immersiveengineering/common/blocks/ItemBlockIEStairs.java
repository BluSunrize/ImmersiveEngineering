/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
/*
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.List;
*/

//TODO This class should be obsolste by now. The tooltip can be handled in `IEStairsBlock`.
// 		 Commented out for main author review/removal.
public class ItemBlockIEStairs extends BlockItem
{
	public ItemBlockIEStairs(Block block, Item.Properties builder)
	{
		super(block, builder);
	}

	/*
	@Override
	public int getMetadata(int damageValue)
	{
		return damageValue;
	}

	@Override
	public void fillItemGroup(CreativeTabs tab, NonNullList<ItemStack> itemList)
	{
		if(this.isInCreativeTab(tab))
			this.block.getSubBlocks(tab, itemList);
	}

	@Override
	public String getTranslationKey(ItemStack itemstack)
	{
		return super.getTranslationKey(itemstack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		if(((IEStairsBlock)block).hasFlavour)
			tooltip.add(I18n.format(Lib.DESC_FLAVOUR+((IEStairsBlock)block).name));
	}
	*/
}