package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.gui.ContainerAssembler;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class GuiAssembler extends GuiContainer
{
	public TileEntityAssembler tile;
	public GuiAssembler(InventoryPlayer inventoryPlayer, TileEntityAssembler tile)
	{
		super(new ContainerAssembler(inventoryPlayer, tile));
		this.tile=tile;
		this.xSize = 230;
		this.ySize = 218;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, guiLeft+49,guiTop+65, 10,10, TextFormatting.GRAY+"\u2716"));
		this.buttonList.add(new GuiButton(1, guiLeft+107,guiTop+65, 10,10, TextFormatting.GRAY+"\u2716"));
		this.buttonList.add(new GuiButton(2, guiLeft+165,guiTop+65, 10,10, TextFormatting.GRAY+"\u2716"));
	}
	@Override
	protected void actionPerformed(GuiButton button)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("buttonID", button.id);
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		if(mx>=guiLeft+187&&mx<guiLeft+194 && my>=guiTop+12&&my<guiTop+59)
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" RF");

		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+204,guiTop+13,16,46, 250,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+182,guiTop+70,16,46, 250,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",tooltip);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft+204,guiTop+70,16,46, 250,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",tooltip);

		for(int i=0; i<tile.patterns.length; i++)
			if(tile.inventory[18+i]==null && tile.patterns[i].inv[9]!=null)
				if(mx>=guiLeft+27+i*58&&mx<guiLeft+43+i*58 && my>=guiTop+64&&my<guiTop+80)
				{
					tooltip.add(tile.patterns[i].inv[9].getDisplayName());
					tile.patterns[i].inv[9].getItem().addInformation(tile.patterns[i].inv[9], ClientUtils.mc().thePlayer, tooltip, false);
					for(int j=0; j<tooltip.size(); j++)
						tooltip.set(j, (j==0?tile.patterns[i].inv[9].getRarity().rarityColor: TextFormatting.GRAY)+tooltip.get(j));
				}

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRendererObj, xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/assembler.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+187,guiTop+13+(46-stored), guiLeft+194,guiTop+59, 0xffb51500, 0xff600b00);

		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+204,guiTop+13,16,46, 230,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",null);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+182,guiTop+70,16,46, 230,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",null);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft+204,guiTop+70,16,46, 230,0,20,50, mx,my, "immersiveengineering:textures/gui/assembler.png",null);

		for(int i=0; i<tile.patterns.length; i++)
			if(tile.inventory[18+i]==null && tile.patterns[i].inv[9]!=null)
			{
				ItemStack stack = tile.patterns[i].inv[9];
				GL11.glPushMatrix();
				GL11.glTranslatef(0.0F, 0.0F, 32.0F);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderHelper.disableStandardItemLighting();
				this.zLevel = 200.0F;
				itemRender.zLevel = 200.0F;
				FontRenderer font = null;
				if(stack!=null)
					font = stack.getItem().getFontRenderer(stack);
				if(font==null)
					font = fontRendererObj;
				itemRender.renderItemAndEffectIntoGUI(stack, guiLeft+27+i*58, guiTop+64);
				itemRender.renderItemOverlayIntoGUI(font, stack, guiLeft+27+i*58, guiTop+64, TextFormatting.GRAY.toString()+stack.stackSize);
				this.zLevel = 0.0F;
				itemRender.zLevel = 0.0F;


				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				ClientUtils.drawColouredRect(guiLeft+27+i*58, guiTop+64, 16,16, 0x77444444);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);

				GL11.glPopMatrix();
			}
	}
}