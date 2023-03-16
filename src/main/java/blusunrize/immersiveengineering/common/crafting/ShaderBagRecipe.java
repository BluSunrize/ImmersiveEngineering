/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.items.ShaderBagItem;
import blusunrize.immersiveengineering.common.register.IEItems.ItemRegObject;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class ShaderBagRecipe extends CustomRecipe
{
	public ShaderBagRecipe(ResourceLocation id)
	{
		super(id, CraftingBookCategory.MISC);
	}

	@Override
	public boolean matches(CraftingContainer inv, @Nonnull Level world)
	{
		ItemStack stack = ItemStack.EMPTY;
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(stackInSlot.isEmpty())
				continue;
			if(!stack.isEmpty())
				return false;
			final Rarity rarity = stackInSlot.getRarity();
			final ItemRegObject<ShaderBagItem> shaderBagForRarity = Misc.SHADER_BAG.get(rarity);
			if(shaderBagForRarity!=null&&stackInSlot.is(shaderBagForRarity.asItem()))
				stack = stackInSlot;
			else
				return false;
		}
		return !stack.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess access)
	{
		for(int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				Rarity next = ShaderRegistry.getLowerRarity(stackInSlot.getRarity());
				ItemStack output = new ItemStack(Misc.SHADER_BAG.get(next), next!=stackInSlot.getRarity()?2: 1);
				if(next!=null)
				{
					ItemNBTHelper.putString(output, "rarity", next.toString());
					return output;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(RegistryAccess access)
	{
		return new ItemStack(Misc.SHADER_BAG.get(Rarity.COMMON), 2);
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.SHADER_BAG_SERIALIZER.get();
	}

}