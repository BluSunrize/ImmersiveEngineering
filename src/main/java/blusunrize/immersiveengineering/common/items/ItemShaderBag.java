/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemShaderBag extends ItemIEBase
{
	@Nonnull
	private final EnumRarity rarity;

	public ItemShaderBag(EnumRarity rarity)
	{
		super("shader_bag_"+rarity.name().toLowerCase(), new Properties());
		this.rarity = rarity;
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		return ClientUtils.getFormattingColour(rarity.color);
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack)
	{
		return new TextComponentString(rarity.name()+" ").appendSibling(super.getDisplayName(stack));
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		return rarity;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			if(ShaderRegistry.totalWeight.containsKey(rarity))
			{
				String shader = ShaderRegistry.getRandomShader(player.getUniqueID(), player.getRNG(), rarity, true);
				if(shader==null||shader.isEmpty())
					return new ActionResult<>(EnumActionResult.FAIL, stack);
				ItemStack shaderItem = new ItemStack(Misc.shader);
				ItemNBTHelper.setString(shaderItem, "shader_name", shader);
				EnumRarity shaderRarity = ShaderRegistry.shaderRegistry.get(shader).getRarity();
				if(ShaderRegistry.sortedRarityMap.indexOf(shaderRarity) <= ShaderRegistry.sortedRarityMap.indexOf(EnumRarity.EPIC)&&
						ShaderRegistry.sortedRarityMap.indexOf(rarity) >= ShaderRegistry.sortedRarityMap.indexOf(EnumRarity.COMMON))
					Utils.unlockIEAdvancement(player, "main/secret_luckofthedraw");
				stack.shrink(1);
				if(stack.getCount() <= 0)
					return new ActionResult<>(EnumActionResult.SUCCESS, shaderItem);
				if(!player.inventory.addItemStackToInventory(shaderItem))
					player.dropItem(shaderItem, false, true);
			}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}
}