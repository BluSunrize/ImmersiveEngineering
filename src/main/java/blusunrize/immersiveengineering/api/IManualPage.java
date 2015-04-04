package blusunrize.immersiveengineering.api;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public interface IManualPage
{
	public void initPage(GuiScreen gui, int x, int y, List<GuiButton> pageButtons);
	public void renderPage(GuiScreen gui, int x, int y, int mx, int my);
	public void buttonPressed(GuiScreen gui, GuiButton button);
}