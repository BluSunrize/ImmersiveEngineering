package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;

public class TileRenderArcFurnace extends TileEntitySpecialRenderer
{
	static IModelCustom model = ClientUtils.getModel("immersiveengineering:models/arcFurnace.obj");
	public static IIcon hotMetal_flow;
	public static IIcon hotMetal_still;

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
			electrodes[i] = (!arc.electrodes[i]?"electrode"+(i+1):"");
		model.renderAllExcept(electrodes);


		if(arc.pouringMetal>0)
		{
			int process= 40;
			float speed = 5f;
			int pour = process-arc.pouringMetal;
			GL11.glDisable(GL11.GL_LIGHTING);
			ClientUtils.bindAtlas(0);
			Tessellator tes = Tessellator.instance;
			float h = (pour>(process-speed)?((process-pour)/speed*27): pour>speed?27: (pour/speed*27))/16f;
			tes.addTranslation(-.5f,-.6875f,1.5f);
			tes.startDrawingQuads();
			tes.setBrightness(0xf000f0);
			if(pour>(process-speed))
				tes.addTranslation(0,-1.6875f+h,0);
			if(h>1)
			{
				tes.addTranslation(0,-h,0);
				ClientUtils.tessellateBox(.375,0,.375, .625,1,.625, hotMetal_flow);
				tes.addTranslation(0,1,0);
				ClientUtils.tessellateBox(.375,0,.375, .625,h-1,.625, hotMetal_flow);
				tes.addTranslation(0,-1,0);
				tes.addTranslation(0,h,0);
			}
			else
			{
				tes.addTranslation(0,-h,0);
				ClientUtils.tessellateBox(.375,0,.375, .625,h,.625, hotMetal_flow);
				tes.addTranslation(0,h,0);
			}
			if(pour>(process-speed))
				tes.addTranslation(0,1.6875f-h,0);
			if(pour>speed)
			{
				float h2 = (pour>(process-speed)?.625f: pour/(process-speed)*.625f);
				tes.addTranslation(0,-1.6875f,0);
				ClientUtils.tessellateBox(.125,0,.125, .875,h2,.875, hotMetal_still);
				tes.addTranslation(0,1.6875f,0);
			}
			tes.draw();
			tes.addTranslation(.5f,.6875f,-1.5f);
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		if(arc.mirrored)
		{
			GL11.glScalef(-1,1,1);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		GL11.glPopMatrix();
	}

}