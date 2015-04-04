package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelWindmillAdvanced;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;

public class TileRenderWindmillAdvanced extends TileEntitySpecialRenderer
{
	static ModelWindmillAdvanced model = new ModelWindmillAdvanced();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityWindmillAdvanced mill = (TileEntityWindmillAdvanced)tile;
		GL11.glPushMatrix();
		GL11.glTranslated(x+.5, y+.5, z+.5);

		switch(mill.facing)
		{
		case 2:
			break;
		case 3:
			GL11.glRotated(180, 0, 1, 0);
			break;
		case 4:
			GL11.glRotated(90, 0, 1, 0);
			break;
		case 5:
			GL11.glRotated(270, 0, 1, 0);
			break;
		}
		
		
		if(mill.getWorldObj()!=null)
			GL11.glRotated(-360*mill.rotation, 0, 0, 1);
		ClientUtils.bindTexture("immersiveengineering:textures/models/windmillAdvanced.png");
		model.render(EntitySheep.fleeceColorTable[15-mill.dye], .0625f);

		GL11.glPopMatrix();
	}

}