package blusunrize.lib.manual;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import blusunrize.lib.manual.gui.GuiManual;

public interface IManualPage
{
	public ManualInstance getManualHelper();
	public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons);
	public void renderPage(GuiManual gui, int x, int y, int mx, int my);
	public void buttonPressed(GuiManual gui, GuiButton button);
}