package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntitySorter;
import blusunrize.immersiveengineering.common.gui.ContainerSorter;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiSorter extends GuiContainer
{
	TileEntitySorter tile;
	public GuiSorter(InventoryPlayer inventoryPlayer, TileEntitySorter tile)
	{
		super(new ContainerSorter(inventoryPlayer, tile));
		this.tile=tile;
		this.ySize = 244;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		for(GuiButton button : this.buttonList)
		{
			if(button instanceof ButtonSorter)
				if(mx>button.xPosition&&mx<button.xPosition+18 && my>button.yPosition&&my<button.yPosition+18)
				{
					ArrayList<String> tooltip = new ArrayList<String>();
					int type = ((ButtonSorter)button).type;
					String[] split = I18n.format(Lib.DESC_INFO + "filter." + (type == 0 ? "oreDict" : type == 1 ? "nbt" : "fuzzy")).split("<br>");
					for(int i=0; i<split.length; i++)
						tooltip.add((i==0? TextFormatting.WHITE: TextFormatting.GRAY)+split[i]);
					ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize,-1);
					RenderHelper.enableGUIStandardItemLighting();
				}
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/sorter.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);
		for(int side=0; side<6; side++)
		{
			int x = guiLeft+ 30+ (side/2)*58;
			int y = guiTop+ 44+ (side%2)*76;
			String s = I18n.format("desc.immersiveengineering.info.blockSide."+EnumFacing.getFront(side).toString()).substring(0, 1);
			GL11.glEnable(3042);
			ClientUtils.font().drawString(s, x-(ClientUtils.font().getStringWidth(s)/2), y, 0xaacccccc, true);
		}
		ClientUtils.bindTexture("immersiveengineering:textures/gui/sorter.png");
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		for(int side=0; side<6; side++)
			for(int i=0; i<3; i++)
			{
				int x = guiLeft+3+ (side/2)*58 +i*18;
				int y = guiTop+3+ (side%2)*76;
				ButtonSorter b = new ButtonSorter(side*3+i, x,y, i);
				b.active = i==0?this.tile.doOredict(side): i==1?this.tile.doNBT(side): this.tile.doFuzzy(side);
				this.buttonList.add(b);
			}
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(button instanceof ButtonSorter && FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
		{
			int side = button.id/3;
			int bit = button.id%3;
			int mask = (1<<bit);
			this.tile.sideFilter[side] = this.tile.sideFilter[side]^mask;

			NBTTagCompound tag = new NBTTagCompound();
			tag.setIntArray("sideConfig", this.tile.sideFilter);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
			this.initGui();
		}
	}

	public static class ButtonSorter extends GuiButton
	{
		int type; 
		boolean active = false;
		public ButtonSorter(int id, int x, int y, int type)
		{
			super(id, x, y, 18,18, "");
			this.type = type;
		}

		@Override
		public void drawButton(Minecraft mc, int mx, int my)
		{
			if (this.visible)
			{
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.hovered = mx >= this.xPosition && my >= this.yPosition && mx < this.xPosition + this.width && my < this.yPosition + this.height;
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				this.drawTexturedModalRect(this.xPosition, this.yPosition, 176+type*18,(active?3:21), this.width, this.height);
				this.mouseDragged(mc, mx, my);
			}
		}
	}
}
