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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemShaderBag extends ItemIEBase
{
	public ItemShaderBag()
	{
		super("shader_bag", 64);
	}

	//	@Override
//	@SideOnly(Side.CLIENT)
//	public int getColorFromItemStack(ItemStack stack, int pass)
//	{
//		EnumRarity rarity = this.getRarity(stack);
//		return ClientUtils.getFormattingColour(rarity.color);
//	}
	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		return ClientUtils.getFormattingColour(this.getRarity(stack).color);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		if(this.isInCreativeTab(tab))
			for(int i = ShaderRegistry.sortedRarityMap.size()-1; i >= 0; i--)
			{
				EnumRarity rarity = ShaderRegistry.sortedRarityMap.get(i);
				ItemStack s = new ItemStack(this);
				ItemNBTHelper.setString(s, "rarity", rarity.toString());
				list.add(s);
			}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		return getRarity(stack).rarityName+" "+super.getItemStackDisplayName(stack);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		String r = ItemNBTHelper.getString(stack, "rarity");
		for(EnumRarity rarity : EnumRarity.values())
			if(rarity.toString().equalsIgnoreCase(r))
				return rarity;
		return EnumRarity.COMMON;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			if(ShaderRegistry.totalWeight.containsKey(stack.getRarity()))
			{
				String shader = ShaderRegistry.getRandomShader(player.getName(), player.getRNG(), stack.getRarity(), true);
				if(shader==null||shader.isEmpty())
					return new ActionResult(EnumActionResult.FAIL, stack);
				ItemStack shaderItem = new ItemStack(IEContent.itemShader);
				ItemNBTHelper.setString(shaderItem, "shader_name", shader);
				if(ShaderRegistry.sortedRarityMap.indexOf(ShaderRegistry.shaderRegistry.get(shader).getRarity()) <= ShaderRegistry.sortedRarityMap.indexOf(EnumRarity.EPIC)&&ShaderRegistry.sortedRarityMap.indexOf(stack.getRarity()) >= ShaderRegistry.sortedRarityMap.indexOf(EnumRarity.COMMON))
					Utils.unlockIEAdvancement(player, "main/secret_luckofthedraw");
				stack.shrink(1);
				if(stack.getCount() <= 0)
					return new ActionResult(EnumActionResult.SUCCESS, shaderItem);
				if(!player.inventory.addItemStackToInventory(shaderItem))
					player.dropItem(shaderItem, false, true);
			}
		return new ActionResult(EnumActionResult.PASS, stack);
	}
}