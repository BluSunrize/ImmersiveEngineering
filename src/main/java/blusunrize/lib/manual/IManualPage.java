package blusunrize.lib.manual;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import blusunrize.lib.manual.gui.GuiManual;

public interface IManualPage
{
	ManualInstance getManualHelper();
	void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons);
	void renderPage(GuiManual gui, int x, int y, int mx, int my);
	void buttonPressed(GuiManual gui, GuiButton button);
	void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button);
	boolean listForSearch(String searchTag);
	void recalculateCraftingRecipes();
}