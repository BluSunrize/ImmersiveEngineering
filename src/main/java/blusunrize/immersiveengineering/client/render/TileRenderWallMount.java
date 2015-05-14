package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelWallMount;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityWallMountSteel;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallMount;

public class TileRenderWallMount extends TileEntitySpecialRenderer
{
	static ModelWallMount model = new ModelWallMount();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		if(GuiScreen.isShiftKeyDown())
			model = new ModelWallMount();
		TileEntityWallMount arm = (TileEntityWallMount)tile;

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);

		String s = arm instanceof TileEntityWallMountSteel?"steel":"wood";
		ClientUtils.bindTexture("immersiveengineering:textures/models/wallmount_"+s+".png");
		model.connection.rotateAngleX = !arm.inverted?3.14159f:0;

		model.connection.rotateAngleY = (float)Math.toRadians( arm.facing==2?270: arm.facing==3?90: arm.facing==4?0: 180 );
		model.render(null, 0, 0, 0, 0, 0, .0625f);
		GL11.glPopMatrix();
	}

}