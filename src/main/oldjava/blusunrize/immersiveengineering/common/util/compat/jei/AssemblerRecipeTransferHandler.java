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
import blusunrize.immersiveengineering.common.gui.AssemblerMenu;
import blusunrize.immersiveengineering.common.network.MessageSetGhostSlots;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author BluSunrize - 08.09.2016
 */
public class AssemblerRecipeTransferHandler implements IRecipeTransferHandler<AssemblerMenu, CraftingRecipe>
{
	private final IRecipeTransferHandlerHelper transferHandlerHelper;

	public AssemblerRecipeTransferHandler(IRecipeTransferHandlerHelper transferHandlerHelper)
	{
		this.transferHandlerHelper = transferHandlerHelper;
	}

	@Override
	public Class<AssemblerMenu> getContainerClass()
	{
		return AssemblerMenu.class;
	}

	@Override
	public Optional<MenuType<AssemblerMenu>> getMenuType()
	{
		return Optional.of(IEMenuTypes.ASSEMBLER.getType());
	}

	@Override
	public RecipeType<CraftingRecipe> getRecipeType()
	{
		return RecipeTypes.CRAFTING;
	}

	@Override
	@Nullable
	public IRecipeTransferError transferRecipe(AssemblerMenu container, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer)
	{
		for(int i = 0; i < 3; i++)
			if(container.patterns.get(i).getStackInSlot(9).isEmpty())
			{
				if(doTransfer)
				{
					List<IRecipeSlotView> stacks = recipeSlots.getSlotViews();
					NonNullList<ItemStack> convertedInput = NonNullList.withSize(stacks.size()-1, ItemStack.EMPTY);
					int j = 0;
					for(IRecipeSlotView ingr : stacks)
					{
						if(j > 0)
						{
							Optional<ItemStack> stackToUse = ingr.getAllIngredients()
									.filter(t -> t.getType()==VanillaTypes.ITEM_STACK)
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
		return this.transferHandlerHelper.createUserErrorWithTooltip(Component.translatable(Lib.GUI+"assembler.nospace"));
	}
}
