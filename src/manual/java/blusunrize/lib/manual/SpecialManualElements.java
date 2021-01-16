/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SpecialManualElements extends SpecialManualElement
{
	protected ManualInstance manual;
	@Nonnull
	protected final List<ItemStack> providedItems = new ArrayList<>();

	protected ItemStack highlighted = ItemStack.EMPTY;

	public SpecialManualElements(ManualInstance manual)
	{
		this.manual = manual;
	}

	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons)
	{
		highlighted = ItemStack.EMPTY;
	}

	@Override
	public void mouseDragged(int x, int y, double clickX, double clickY, double mx, double my, double lastX, double lastY, int mouseButton)
	{
	}

	//	@Override
	//	public void buttonPressed(GuiManual gui, GuiButton button)
	//	{
	//		if(button instanceof GuiButtonManualLink && GuiManual.activeManual!=null && manual.showEntryInList(manual.getEntry(((GuiButtonManualLink)button).key)))
	//		{
	//			GuiManual.selectedEntry = ((GuiButtonManualLink)button).key;
	//			GuiManual.page = ((GuiButtonManualLink)button).pageLinked;
	//			GuiManual.activeManual.initGui();
	//		}
	//	}
	@Override
	public void recalculateCraftingRecipes()
	{
	}

	protected void renderHighlightedTooltip(MatrixStack transform, ManualScreen gui, int mx, int my)
	{
		if(!highlighted.isEmpty())
		{
			FontRenderer font = highlighted.getItem().getFontRenderer(highlighted);
			GuiUtils.preItemToolTip(highlighted);
			List<IReorderingProcessor> tooltip = LanguageMap.getInstance().func_244260_a(
					Collections.unmodifiableList(gui.getTooltipFromItem(highlighted))
			);
			gui.renderToolTip(transform, tooltip, mx, my, font!=null?font: Minecraft.getInstance().fontRenderer);
			GuiUtils.postItemToolTip();
		}
	}

	public void addProvidedItem(ItemStack s)
	{
		providedItems.add(s);
	}

	@Override
	public ItemStack[] getProvidedRecipes()
	{
		return providedItems.toArray(new ItemStack[0]);
	}

	@Override
	public ItemStack getHighlightedStack()
	{
		return highlighted;
	}

}