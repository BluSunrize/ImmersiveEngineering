package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySilo;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileRenderSilo extends TileEntitySpecialRenderer
{
	static IModelCustom model = ClientUtils.getModel("immersiveengineering:models/silo.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntitySilo silo = (TileEntitySilo)tile;
		if(!silo.formed || silo.pos!=4)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x+.5, y, z+.5);

		ClientUtils.bindTexture("immersiveengineering:textures/models/silo.png");

		model.renderAll();

		if(silo.identStack!=null)
		{
			GL11.glTranslatef(0,5,0);
			float baseScale = .0625f;
			float itemScale = .75f;
			float flatScale = .001f;
			baseScale *= itemScale;
			float textScale = .375f;
			GL11.glScalef(baseScale,-baseScale,baseScale);
			ItemStack stack = Utils.copyStackWithAmount(silo.identStack, silo.storageAmount);
			String s = ""+stack.stackSize;
			float w = this.func_147498_b().getStringWidth(s);
			RenderItem ri = RenderItem.getInstance();

			float xx = -.5f*itemScale;
			float zz = 1.5001f;
			xx/=baseScale;
			zz/=baseScale;
			w*=textScale;
			for(int i=0; i<4; i++)
			{
				GL11.glTranslatef(xx,0,zz);
				GL11.glScalef(1,1,flatScale);
				if(!ForgeHooksClient.renderInventoryItem(RenderBlocks.getInstance(), ClientUtils.mc().getTextureManager(), stack, true, 0.0F, 0.0F, 0.0F))
					ri.renderItemIntoGUI(ClientUtils.font(), ClientUtils.mc().getTextureManager(), stack, 0, 0);
				GL11.glScalef(1,1,1/flatScale);
				
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDepthMask(false);
				GL11.glTranslatef(8-w/2,17,.001f);
				GL11.glScalef(textScale,textScale,1);
				ClientUtils.font().drawString(""+stack.stackSize, 0,0,0x888888, true);
				GL11.glScalef(1/textScale,1/textScale,1);
				GL11.glTranslatef(-(8-w/2),-17,-.001f);
				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_LIGHTING);

				GL11.glTranslatef(-xx,0,-zz);
				GL11.glRotatef(90, 0,1,0);

				GL11.glEnable(3008);
				GL11.glAlphaFunc(516, 0.1F);
				GL11.glEnable(3042);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			}
		}
		GL11.glPopMatrix();
	}

}