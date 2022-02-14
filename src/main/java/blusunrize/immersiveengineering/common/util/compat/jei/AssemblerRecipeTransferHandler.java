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
import blusunrize.immersiveengineering.common.gui.AssemblerContainer;
import blusunrize.immersiveengineering.common.network.MessageSetGhostSlots;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author BluSunrize - 08.09.2016
 */
public class AssemblerRecipeTransferHandler implements IRecipeTransferHandler<AssemblerContainer, CraftingRecipe>
{
	private final IRecipeTransferHandlerHelper transferHandlerHelper;

	public AssemblerRecipeTransferHandler(IRecipeTransferHandlerHelper transferHandlerHelper)
	{
		this.transferHandlerHelper = transferHandlerHelper;
	}

	@Override
	public Class<AssemblerContainer> getContainerClass()
	{
		return AssemblerContainer.class;
	}

	@Override
	public Class<CraftingRecipe> getRecipeClass()
	{
		return CraftingRecipe.class;
	}

	@Override
	@Nullable
	public IRecipeTransferError transferRecipe(AssemblerContainer container, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer)
	{
		for(int i = 0; i < 3; i++)
			if(container.tile.patterns[i].recipe==null)
			{
				if(doTransfer)
				{
					var stacks = recipeSlots.getSlotViews();
					NonNullList<ItemStack> convertedInput = NonNullList.withSize(stacks.size()-1, ItemStack.EMPTY);
					int j = 0;
					for(var ingr : stacks)
					{
						if(j > 0)
						{
							Optional<ItemStack> stackToUse = ingr.getAllIngredients()
									.filter(t -> t.getType()==VanillaTypes.ITEM)
									.map(t -> (ItemStack)t.getIngredient())
									.findFirst();
							if(stackToUse.isPresent())
								convertedInput.set(j-1, stackToUse.get());
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
		return this.transferHandlerHelper.createUserErrorWithTooltip(new TranslatableComponent(Lib.GUI+"assembler.nospace"));
	}
}
