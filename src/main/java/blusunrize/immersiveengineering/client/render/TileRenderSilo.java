package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySilo;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class TileRenderSilo extends TileEntitySpecialRenderer<TileEntitySilo>
{
	@Override
	public void renderTileEntityAt(TileEntitySilo tile, double x, double y, double z, float f, int destroyStage)
	{
		if(!tile.formed || tile.pos!=4||!tile.getWorld().isBlockLoaded(tile.getPos()))
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x+.5, y, z+.5);

		if(tile.identStack!=null)
		{
			GL11.glTranslatef(0,5,0);
			float baseScale = .0625f;
			float itemScale = .75f;
			float flatScale = .001f;
			baseScale *= itemScale;
			float textScale = .375f;
			GL11.glScalef(baseScale,-baseScale,baseScale);
			ItemStack stack = Utils.copyStackWithAmount(tile.identStack, tile.storageAmount);
			String s = ""+stack.stackSize;
			float w = this.getFontRenderer().getStringWidth(s);

			float xx = -.5f*itemScale;
			float zz = 1.501f;
			xx/=baseScale;
			zz/=baseScale;
			w*=textScale;
			for(int i=0; i<4; i++)
			{
				GlStateManager.pushMatrix();
				GL11.glTranslatef(xx,0,zz);
				GL11.glScalef(1,1,flatScale);
				ClientUtils.mc().getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
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
				GlStateManager.popMatrix();
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