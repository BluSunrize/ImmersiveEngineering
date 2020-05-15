/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.immersiveengineering.dummy.GlStateManager;
import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.gui.widget.button.Button;

import static blusunrize.immersiveengineering.dummy.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static blusunrize.immersiveengineering.dummy.GlStateManager.DestFactor.ZERO;
import static blusunrize.immersiveengineering.dummy.GlStateManager.SourceFactor.ONE;
import static blusunrize.immersiveengineering.dummy.GlStateManager.SourceFactor.SRC_ALPHA;

public class GuiButtonManualNavigation extends Button
{
	public int type;
	public ManualScreen gui;

	public GuiButtonManualNavigation(ManualScreen gui, int x, int y, int w, int h, int type, IPressable handler)
	{
		super(x, y, type >= 4?10: Math.min(type < 2?16: 10, w), type >= 4?10: Math.min(type < 2?10: 16, h), "", handler);
		this.gui = gui;
		this.type = type;
	}

	@Override
	public void renderButton(int mx, int my, float partial)
	{
			ManualUtils.bindTexture(gui.texture);
			GlStateManager.color3f(1.0F, 1.0F, 1.0F);
			isHovered = mx >= this.x&&mx < (this.x+this.width)&&my >= this.y&&my < (this.y+this.height);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO);
			int u = type==5?46: type==4||type==6?36: (type < 2?0: type < 3?16: 26)+(type > 1?(10-width): type==1?(16-width): 0);
			int v = 216+(type==0?0: type==1?10: type==2?(16-height): type==3?0: type==4||type==5?10: 0);
			if(isHovered)
				v += 20;
			this.blit(this.x, this.y, u, v, width, height);
	}
}