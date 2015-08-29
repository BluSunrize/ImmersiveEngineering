package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySheetmetalTank;

public class TileRenderSheetmetalTank extends TileEntitySpecialRenderer
{
	static IModelCustom model = ClientUtils.getModel("immersiveengineering:models/tank.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntitySheetmetalTank tank = (TileEntitySheetmetalTank)tile;
		if(!tank.formed || tank.pos!=4)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x+.5, y, z+.5);

		ClientUtils.bindTexture("immersiveengineering:textures/models/tank.png");

		model.renderAll();

		
		GL11.glPopMatrix();
	}

}