package blusunrize.immersiveengineering.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.GuiManual;

public class ManualPageMultiblock extends ManualPages
{
	IMultiblock multiblock;
	boolean canTick = true;
	boolean showCompleted = false;
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
	float rotX=0;
	float rotY=0;
	float rotZ=0;
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
			rotX=25;
			rotY=40;
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
			boolean canRenderFormed = multiblock.canRenderFormedStructure();
			//			yOff = (structureHeight-1)*12+structureWidth*5+structureLength*5+16;
			//			yOff = Math.max(48, yOff);
			yOff = (int)(multiblock.getManualScale()*Math.sqrt(structureHeight*structureHeight + structureWidth*structureWidth + structureLength*structureLength));
			yOff = Math.max(10+(canRenderFormed?12:0)+(structureHeight>1?36:0), yOff);
			pageButtons.add(new GuiButtonManualNavigation(gui, 100, x+4,y+yOff/2-(canRenderFormed?11:5), 10,10, 4));
			if(canRenderFormed)
				pageButtons.add(new GuiButtonManualNavigation(gui, 103, x+4,y+yOff/2+1, 10,10, 6));
			if(structureHeight>1)
			{
				pageButtons.add(new GuiButtonManualNavigation(gui, 101, x+4,y+yOff/2-(canRenderFormed?14:8)-16, 10,16, 3));
				pageButtons.add(new GuiButtonManualNavigation(gui, 102, x+4,y+yOff/2+(canRenderFormed?14:8), 10,16, 2));
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

			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glPushMatrix();
			//			RenderHelper.enableGUIStandardItemLighting();
			RenderHelper.disableStandardItemLighting();
			//			GL11.glEnable(GL11.GL_DEPTH_TEST);
			//			GL11.glDepthFunc(GL11.GL_ALWAYS);
			//			GL11.glDisable(GL11.GL_CULL_FACE);
			int i=0;
			ItemStack highlighted = null;
			RenderBlocks blockRender = RenderBlocks.getInstance();

			float f = (float)Math.sqrt(structureHeight*structureHeight + structureWidth*structureWidth + structureLength*structureLength);
			//			GL11.glTranslatef(x+(structureWidth/2+structureLength/2)/2*30, y+structureHeight/2*30, structureLength);
			float scale = multiblock.getManualScale();
			yOffTotal = Math.max(10+(multiblock.canRenderFormedStructure()?12:0)+(structureHeight>1?36:0), (int) (f*scale));
			GL11.glTranslatef(x+60,y+f*scale/2, Math.max(structureHeight, Math.max(structureWidth,structureLength)));
			//						GL11.glScalef(12,-12,1);
			GL11.glScalef(scale,-scale,1);
			GL11.glRotatef(rotX, 1, 0, 0);
			GL11.glRotatef(rotY, 0, 1, 0);
			if(showCompleted)
				multiblock.renderFormedStructure();
			else
			{
				if(structureWidth%2==1)
					GL11.glTranslatef(-.5f,0,0);
				for(int h=0; h<structure.length; h++)
					if(showLayer==-1 || h<=showLayer)
					{
						ItemStack[][] level = structure[h];
						for(int l=0; l<level.length; l++)
						{
							ItemStack[] row = level[l];
							for(int w=row.length-1; w>=0; w--)
							{
								int xx = 60 +xHalf -10*w +10*l -7;
								int yy = yOffPartial - 5*w - 5*l -12*h;

								//							GL11.glTranslated(0, 0, 1);
								if(row[w]!=null && i<=limiter)
								{
									i++;
									Block b = Block.getBlockFromItem(row[w].getItem());
									if(b != null)
									{
										ClientUtils.bindAtlas(0);
										GL11.glPushMatrix();
										GL11.glTranslatef(w-structureWidth/2, h-structureHeight/2, l-structureLength/2);
										if(!multiblock.overwriteBlockRender(row[w]))
											blockRender.renderBlockAsItem(b, row[w].getItemDamage(), 0.8F);
										GL11.glPopMatrix();
									}

									//								RenderItem.getInstance().renderItemIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, row[w], x+xx, y+yy);
									if(mx>=x+xx&&mx<x+xx+16 && my>=y+yy&&my<y+yy+16)
										highlighted = row[w];
								}
							}
						}
					}
			}
			//			GL11.glTranslated(0, 0, -i);
			GL11.glPopMatrix();

			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			//			GL11.glEnable(GL11.GL_BLEND);
			//			GL11.glEnable(GL11.GL_DEPTH_TEST);


			manual.fontRenderer.setUnicodeFlag(false);
			if(this.multiblock.getTotalMaterials()!=null)
				manual.fontRenderer.drawString("?", x+116, y+yOffTotal/2-4, manual.getTextColour(), false);
			//			if(highlighted!=null)
			//				gui.renderToolTip(highlighted, mx, my);
			//			else 
			if(this.multiblock.getTotalMaterials()!=null && mx>=x+116&&mx<x+122 && my>=y+yOffTotal/2-4&&my<y+yOffTotal/2+4)
			{
				ArrayList<String> components = new ArrayList();
				components.add(StatCollector.translateToLocal("desc.ImmersiveEngineering.info.reqMaterial"));
				int maxOff = 1;
				for(ItemStack ss : this.multiblock.getTotalMaterials())
					if((""+ss.stackSize).length()>maxOff)
						maxOff = (""+ss.stackSize).length();
				for(ItemStack ss : this.multiblock.getTotalMaterials())
					if(ss!=null)
					{
						int indent = 0;
						if(maxOff>(""+ss.stackSize).length())
							indent = maxOff-(""+ss.stackSize).length();
						String sIndent = "";
						if(indent>0)
							for(int ii=0;ii<indent;ii++)
								sIndent+="0";
						components.add(""+EnumChatFormatting.GRAY+sIndent+ss.stackSize+"x "+EnumChatFormatting.RESET+ss.getRarity().rarityColor+ss.getDisplayName());
					}
				gui.drawHoveringText(components, mx, my, manual.fontRenderer);
			}
			RenderHelper.disableStandardItemLighting();

			manual.fontRenderer.setUnicodeFlag(true);
			if(localizedText!=null&&!localizedText.isEmpty())
				manual.fontRenderer.drawSplitString(localizedText, x,y+yOffTotal, 120, manual.getTextColour());
		}
	}

	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button)
	{
		if((clickX>=40 && clickX<144 && mx>=40 && mx<144)&&(clickY>=30 && clickY<130 && my>=30 && my<130))
		{
			int dx = mx-lastX;
			int dy = my-lastY;
			rotY = rotY+(dx/104f)*80;
			rotX = rotX+(dy/100f)*80;
		}
	}

	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		if(button.id==100)
		{
			canTick = !canTick;
			((GuiButtonManualNavigation)button).type = ((GuiButtonManualNavigation)button).type==4?5:4; 
		}
		else if(button.id==101)
		{
			showLayer = Math.min(showLayer+1, structureHeight-1);
			tick= (countPerLevel[showLayer])*40;
		}
		else if(button.id==102)
		{
			showLayer = Math.max(showLayer-1, -1);
			tick= (showLayer==-1?blockCount:countPerLevel[showLayer])*40;
		}
		else if(button.id==103)
			showCompleted = !showCompleted;
		super.buttonPressed(gui, button);
	}

}
