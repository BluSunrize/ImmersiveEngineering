/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Locale;

public class ShaderBagItem extends IEBaseItem
{
	@Nonnull
	private final Rarity rarity;

	public ShaderBagItem(Rarity rarity)
	{
		super("shader_bag_"+rarity.name().toLowerCase(Locale.US).replace(':', '_'), new Properties());
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
		return rarity.color.getColor();
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack)
	{
		return new TranslationTextComponent(Lib.DESC_INFO+"shader.rarity."+this.rarity.name().toLowerCase(Locale.US))
				.appendString(" ")
				.append(super.getDisplayName(stack));
	}

	@Override
	public String getTranslationKey()
	{
		return "item."+ImmersiveEngineering.MODID+".shader_bag";
	}

	@Override
	public Rarity getRarity(ItemStack stack)
	{
		return rarity;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			if(ShaderRegistry.totalWeight.containsKey(rarity))
			{
				ResourceLocation shader = ShaderRegistry.getRandomShader(player.getUniqueID(), player.getRNG(), rarity, true);
				if(shader==null)
					return new ActionResult<>(ActionResultType.FAIL, stack);
				ItemStack shaderItem = new ItemStack(Misc.shader);
				ItemNBTHelper.putString(shaderItem, "shader_name", shader.toString());
				Rarity shaderRarity = ShaderRegistry.shaderRegistry.get(shader).getRarity();
				if(ShaderRegistry.sortedRarityMap.indexOf(shaderRarity) <= ShaderRegistry.sortedRarityMap.indexOf(Rarity.EPIC)&&
						ShaderRegistry.sortedRarityMap.indexOf(rarity) >= ShaderRegistry.sortedRarityMap.indexOf(Rarity.COMMON))
					Utils.unlockIEAdvancement(player, "main/secret_luckofthedraw");
				stack.shrink(1);
				if(stack.getCount() <= 0)
					return new ActionResult<>(ActionResultType.SUCCESS, shaderItem);
				if(!player.inventory.addItemStackToInventory(shaderItem))
					player.dropItem(shaderItem, false, true);
			}
		return new ActionResult<>(ActionResultType.PASS, stack);
	}
}