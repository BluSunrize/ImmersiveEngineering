/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiButtonManualLink;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.GuiManual;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.*;

public abstract class SpecialManualElements extends SpecialManualElement
{
	protected ManualInstance manual;
	protected List<ItemStack> providedItems;

	protected ItemStack highlighted = ItemStack.EMPTY;

	public SpecialManualElements(ManualInstance manual)
	{
		this.manual = manual;
	}

	@Override
	public void onOpened(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		highlighted = ItemStack.EMPTY;
	}

	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		if(button instanceof GuiButtonManualLink)
			((GuiButtonManualLink)button).link.changePage(gui, true);
	}

	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mouseX, int mouseY, int lastX, int lastY, GuiButton button)
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

	public void addProvidedItem(ItemStack s)
	{
		if(providedItems==null)
			providedItems = new ArrayList<>(1);
		providedItems.add(s);
	}

	@Override
	public ItemStack[] getProvidedRecipes()
	{
		return providedItems!=null?providedItems.toArray(new ItemStack[providedItems.size()]): new ItemStack[0];
	}

	@Override
	public ItemStack getHighlightedStack()
	{
		return highlighted;
	}

}