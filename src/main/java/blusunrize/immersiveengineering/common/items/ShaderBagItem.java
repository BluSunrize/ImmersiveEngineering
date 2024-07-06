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
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

public class ShaderBagItem extends IEBaseItem implements IColouredItem
{
	@Nonnull
	private final Rarity rarity;

	public ShaderBagItem(Rarity rarity)
	{
		super(new Properties().component(DataComponents.RARITY, rarity));
		this.rarity = rarity;
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		return rarity.color().getColor();
	}

	@Override
	public Component getName(ItemStack stack)
	{
		return Component.translatable(Lib.DESC_INFO+"shader.rarity."+this.rarity.name().toLowerCase(Locale.US))
				.append(" ")
				.append(super.getName(stack));
	}

	@Override
	public String getDescriptionId()
	{
		return "item."+ImmersiveEngineering.MODID+".shader_bag";
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
				ItemStack shaderItem = ShaderRegistry.makeShaderStack(shader);
				Rarity shaderRarity = shaderItem.getRarity();
				if(ShaderRegistry.sortedRarityMap.indexOf(shaderRarity) <= ShaderRegistry.sortedRarityMap.indexOf(Rarity.EPIC)&&
						ShaderRegistry.sortedRarityMap.indexOf(rarity) >= ShaderRegistry.sortedRarityMap.indexOf(Rarity.COMMON))
					Utils.unlockIEAdvancement(player, "main/secret_luckofthedraw");
				stack.shrink(1);
				if(stack.getCount() <= 0)
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, shaderItem);
				if(!player.getInventory().add(shaderItem))
					player.drop(shaderItem, false, true);
			}
		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}
}