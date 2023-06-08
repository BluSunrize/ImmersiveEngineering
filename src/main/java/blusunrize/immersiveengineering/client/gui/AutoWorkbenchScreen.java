/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonItem;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.gui.AutoWorkbenchMenu;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AutoWorkbenchScreen extends IEContainerScreen<AutoWorkbenchMenu>
{
	private final List<GuiButtonItem> selectionButtons = new ArrayList<>();

	public AutoWorkbenchScreen(AutoWorkbenchMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("auto_workbench"));
		this.imageHeight = 184;
		this.inventoryLabelY = 93;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(new EnergyInfoArea(leftPos+80, topPos+36, menu.energyStorage));
	}

	@Override
	public void init()
	{
		this.clearWidgets();
		this.selectionButtons.clear();
		super.init();
		Slot s = menu.getSlot(0);
		if(s!=null&&s.hasItem()&&s.getItem().getItem() instanceof EngineersBlueprintItem)
		{
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(
					Minecraft.getInstance().level, ItemNBTHelper.getString(s.getItem(), "blueprint")
			);
			if(recipes!=null&&recipes.length > 0)
			{
				int l = recipes.length;
				int xx = leftPos+121;
				int yy = topPos+(l > 6?59-(l-3)/3*18: l > 3?59: 68);
				for(int i = 0; i < l; i++)
					if(recipes[i]!=null&&!recipes[i].output.get().isEmpty())
					{
						GuiButtonItem button = makeSelectionButton(xx, yy, i, recipes[i].output.get().copy());
						this.selectionButtons.add(button);
						this.addRenderableWidget(button);
					}
			}
		}
	}

	private GuiButtonItem makeSelectionButton(int xx, int yy, int i, ItemStack stack)
	{
		OnPress handler = btn -> {
			int newId;
			if(i==menu.selectedRecipe.get())//disable
				newId = -1;
			else
				newId = i;
			CompoundTag message = new CompoundTag();
			message.putInt("recipe", newId);
			sendUpdateToServer(message);
		};
		return new GuiButtonItem(xx+(i%3)*18, yy+(i/3)*18, stack, false, handler);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float partialTicks, int x, int y)
	{
		for(int i = 0; i < selectionButtons.size(); ++i)
			selectionButtons.get(i).state = (i==menu.selectedRecipe.get());
		super.drawContainerBackgroundPre(graphics, partialTicks, x, y);
	}
}