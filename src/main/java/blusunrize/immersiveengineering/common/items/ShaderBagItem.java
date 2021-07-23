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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Locale;

public class ShaderBagItem extends IEBaseItem
{
	@Nonnull
	private final Rarity rarity;

	public ShaderBagItem(Rarity rarity)
	{
		super(new Properties());
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
	public Component getName(ItemStack stack)
	{
		return new TranslatableComponent(Lib.DESC_INFO+"shader.rarity."+this.rarity.name().toLowerCase(Locale.US))
				.append(" ")
				.append(super.getName(stack));
	}

	@Override
	public String getDescriptionId()
	{
		return "item."+ImmersiveEngineering.MODID+".shader_bag";
	}

	@Override
	public Rarity getRarity(ItemStack stack)
	{
		return rarity;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(!world.isClientSide)
			if(ShaderRegistry.totalWeight.containsKey(rarity))
			{
				ResourceLocation shader = ShaderRegistry.getRandomShader(player.getUUID(), player.getRandom(), rarity, true);
				if(shader==null)
					return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
				ItemStack shaderItem = new ItemStack(Misc.shader);
				ItemNBTHelper.putString(shaderItem, "shader_name", shader.toString());
				Rarity shaderRarity = ShaderRegistry.shaderRegistry.get(shader).getRarity();
				if(ShaderRegistry.sortedRarityMap.indexOf(shaderRarity) <= ShaderRegistry.sortedRarityMap.indexOf(Rarity.EPIC)&&
						ShaderRegistry.sortedRarityMap.indexOf(rarity) >= ShaderRegistry.sortedRarityMap.indexOf(Rarity.COMMON))
					Utils.unlockIEAdvancement(player, "main/secret_luckofthedraw");
				stack.shrink(1);
				if(stack.getCount() <= 0)
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, shaderItem);
				if(!player.inventory.add(shaderItem))
					player.drop(shaderItem, false, true);
			}
		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}
}