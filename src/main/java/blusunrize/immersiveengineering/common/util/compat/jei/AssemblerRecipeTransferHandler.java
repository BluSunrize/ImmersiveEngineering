/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.gui.ContainerAssembler;
import blusunrize.immersiveengineering.common.util.network.MessageSetGhostSlots;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author BluSunrize - 08.09.2016
 */
public class AssemblerRecipeTransferHandler implements IRecipeTransferHandler<ContainerAssembler>
{
	@Override
	public Class getContainerClass()
	{
		return ContainerAssembler.class;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(@Nonnull ContainerAssembler container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer)
	{
		for(int i = 0; i < 3; i++)
			if(container.tile.patterns[i].recipe==null)
			{
				if(doTransfer)
				{
					IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
					NonNullList<ItemStack> convertedInput = NonNullList.withSize(stacks.getGuiIngredients().size()-1, ItemStack.EMPTY);
					int j = 0;
					for(IGuiIngredient<ItemStack> ingr : stacks.getGuiIngredients().values())
					{
						if(j > 0)
						{
							List<ItemStack> list = ingr.getAllIngredients();
							if(list!=null&&list.size() > 0)
								convertedInput.set(j-1, list.get(0));
						}
						j++;
					}
					Int2ObjectMap<ItemStack> changes = new Int2ObjectOpenHashMap<>();
					for(int slot = 0; slot < Math.min(convertedInput.size(), 9); slot++)
						changes.put(i*10+slot, convertedInput.get(slot));
					ImmersiveEngineering.packetHandler.sendToServer(new MessageSetGhostSlots(changes));
				}
				return null;
			}
		return JEIHelper.jeiHelpers.recipeTransferHandlerHelper().createUserErrorWithTooltip(I18n.translateToLocal(Lib.GUI+"assembler.nospace"));
	}
}
