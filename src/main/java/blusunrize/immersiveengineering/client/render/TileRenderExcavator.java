package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderExcavator extends TileEntitySpecialRenderer
{
	static WavefrontObject model = ClientUtils.getModel("immersiveengineering:models/excavator.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityExcavator excavator = (TileEntityExcavator)tile;
		if(!excavator.formed || excavator.pos!=4)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x+.5, y+.5, z+.5);
		GL11.glRotatef(excavator.facing==4?180: excavator.facing==3?-90: excavator.facing==2?90: 0, 0,1,0);
		GL11.glTranslated(-4,0,0);
		if(excavator.mirrored)
		{
			GL11.glScalef(1,1,-1);
			GL11.glDisable(GL11.GL_CULL_FACE);
		}

		ClientUtils.bindTexture("immersiveengineering:textures/models/excavator.png");


		Tessellator.instance.startDrawingQuads();
		ClientUtils.renderWavefrontModelWithModifications(model, Tessellator.instance, new Matrix4(), new Matrix4(), false);
		Tessellator.instance.draw();

		if(excavator.mirrored)
		{
			GL11.glScalef(1,1,-1);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		GL11.glPopMatrix();
	}

}