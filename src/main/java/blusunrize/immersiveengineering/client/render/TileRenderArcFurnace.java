package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;

public class TileRenderArcFurnace extends TileEntitySpecialRenderer
{
	static IModelCustom model = ClientUtils.getModel("immersiveengineering:models/arcFurnace.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityArcFurnace arc = (TileEntityArcFurnace)tile;
		if(!arc.formed || arc.pos!=62)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x+.5, y+.5, z+.5);
		GL11.glRotatef(arc.facing==2?180: arc.facing==4?-90: arc.facing==5?90: 0, 0,1,0);

		if(arc.mirrored)
		{
			GL11.glScalef(-1,1,1);
			GL11.glDisable(GL11.GL_CULL_FACE);
		}

		ClientUtils.bindTexture("immersiveengineering:textures/models/arcFurnace_"+(arc.active?"active":"inactive")+".png");

		String[] electrodes = new String[3];
		for(int i=0; i<3; i++)
			electrodes[i] = !arc.electrodes[i]?"electrode"+(i+1):"";
		model.renderAllExcept(electrodes);
		
		if(arc.mirrored)
		{
			GL11.glScalef(-1,1,1);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		GL11.glPopMatrix();
	}

}