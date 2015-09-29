package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.Vertex;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderArcFurnace extends TileRenderIE
{
	ModelIEObj model0 = new ModelIEObj("immersiveengineering:models/arcFurnace.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_arcFurnace);
		}
	};
	ModelIEObj model1 = new ModelIEObj("immersiveengineering:models/arcFurnace.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalMultiblocks.getIcon(1, BlockMetalMultiblocks.META_arcFurnace);
		}
	};
	public static IIcon hotMetal_flow;
	public static IIcon hotMetal_still;

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityArcFurnace arc = (TileEntityArcFurnace)tile;

		translationMatrix.translate(.5, .5, .5);
		rotationMatrix.rotate(Math.toRadians(arc.facing==2?180: arc.facing==4?-90: arc.facing==5?90: 0), 0,1,0);
		if(arc.mirrored)
			translationMatrix.scale(new Vertex(1,1,-1));

		String[] render = new String[5];
		render[0]="base";
		render[1]="furnace";
		for(int i=0; i<3; i++)
			render[2+i] = (arc.electrodes[i]?"electrode"+(i+1):"");
		if(arc.active)
			model1.render(tile, tes, translationMatrix, rotationMatrix, 0, arc.mirrored, render);
		else
			model0.render(tile, tes, translationMatrix, rotationMatrix, 0, arc.mirrored, render);
	}
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
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