package blusunrize.immersiveengineering.api;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.GuiManual;

public class ManualPageMultiblock extends ManualPages
{
	IMultiblock multiblock;
	boolean canTick = true;
	int tick = 0;
	int showLayer = -1;

	public ManualPageMultiblock(ManualInstance manual, String text, IMultiblock multiblock)
	{
		super(manual, text);
		this.multiblock = multiblock;
	}


	int blockCount=0;
	int[] countPerLevel;
	int structureHeight = 0;
	int structureLength = 0;
	int structureWidth = 0;
	@Override
	public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		int yOff = 0;
		if(multiblock.getStructureManual()!=null)
		{
			ItemStack[][][] structure = multiblock.getStructureManual();
			structureHeight = structure.length;
			structureWidth=0;
			structureLength=0;
			countPerLevel = new int[structureHeight];
			blockCount=0;
			for(int h=0; h<structure.length; h++)
			{
				if(structure[h].length-1>structureLength)
					structureLength = structure[h].length-1;
				int perLvl=0;
				for(int l=0; l<structure[h].length; l++)
				{
					if(structure[h][l].length-1>structureWidth)
						structureWidth = structure[h][l].length-1;
					for(ItemStack ss : structure[h][l])
						if(ss!=null)
							perLvl++;
				}
				countPerLevel[h] = perLvl;
				blockCount += perLvl; 
			}
			tick= (showLayer==-1?blockCount:countPerLevel[showLayer])*40;
			yOff = (structureHeight-1)*12+structureWidth*5+structureLength*5+16;
			yOff = Math.max(48, yOff);
			pageButtons.add(new GuiButtonManualNavigation(gui, 100, x+4,y+yOff/2-5, 10,10, 4));
			if(structureHeight>1)
			{
				pageButtons.add(new GuiButtonManualNavigation(gui, 101, x+4,y+yOff/2-8-16, 10,16, 3));
				pageButtons.add(new GuiButtonManualNavigation(gui, 102, x+4,y+yOff/2+8, 10,16, 2));
			}
		}
		super.initPage(gui, x, y+yOff, pageButtons);
	}

	@Override
	public void renderPage(GuiManual gui, int x, int y, int mx, int my)
	{
		if(multiblock.getStructureManual()!=null)
		{
			if(canTick)
				tick++;

			ItemStack[][][] structure = multiblock.getStructureManual();
			int prevLayers = 0;
			if(showLayer!=-1)
				for(int ll=0; ll<showLayer; ll++)
					prevLayers+=countPerLevel[ll];
			int limiter = prevLayers+ (tick/40)% ((showLayer==-1?blockCount:countPerLevel[showLayer])+4);			

			int xHalf = (structureWidth*5 - structureLength*5);
			int yOffPartial = (structureHeight-1)*12+structureWidth*5+structureLength*5;
			int yOffTotal = Math.max(48, yOffPartial+16);

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();
			RenderItem.getInstance().renderWithColor=true;
			int i=0;
			ItemStack highlighted = null;
			for(int h=0; h<structure.length; h++)
				if(showLayer==-1 || h<=showLayer)
				{
					ItemStack[][] level = structure[h];
					for(int l=level.length-1; l>=0; l--)
					{
						ItemStack[] row = level[l];
						for(int w=row.length-1; w>=0; w--)
						{
							int xx = 60 +xHalf -10*w +10*l -7;
							int yy = yOffPartial - 5*w - 5*l -12*h;
							GL11.glTranslated(0, 0, 1);
							if(row[w]!=null && i<=limiter)
							{
								i++;
								RenderItem.getInstance().renderItemIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, row[w], x+xx, y+yy);
								if(mx>=x+xx&&mx<x+xx+16 && my>=y+yy&&my<y+yy+16)
									highlighted = row[w];
							}
						}
					}
				}
			GL11.glTranslated(0, 0, -i);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			manual.fontRenderer.setUnicodeFlag(false);
			if(highlighted!=null)
				gui.renderToolTip(highlighted, mx, my);
			RenderHelper.disableStandardItemLighting();

			manual.fontRenderer.setUnicodeFlag(true);
			if(localizedText!=null&&!localizedText.isEmpty())
				manual.fontRenderer.drawSplitString(localizedText, x,y+yOffTotal, 120, manual.getTextColour());
		}
	}

	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		if(button.id==100)
			canTick = !canTick;
		if(button.id==101)
		{
			showLayer = Math.min(showLayer+1, structureHeight-1);
			tick= (countPerLevel[showLayer])*40;
		}
		if(button.id==102)
		{
			showLayer = Math.max(showLayer-1, -1);
			tick= (showLayer==-1?blockCount:countPerLevel[showLayer])*40;
		}
		super.buttonPressed(gui, button);
	}

}
