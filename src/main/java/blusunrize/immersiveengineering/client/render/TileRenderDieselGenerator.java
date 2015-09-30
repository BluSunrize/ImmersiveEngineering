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
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderDieselGenerator extends TileRenderIE
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/dieselGenerator.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_dieselGenerator);
		}
	};
	
	
	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityDieselGenerator gen = (TileEntityDieselGenerator)tile;
		translationMatrix.translate(.5, .5, .5);
		rotationMatrix.rotate(Math.toRadians(gen.facing==3?180: gen.facing==4?90: gen.facing==5?-90: 0), 0,1,0);
		if(gen.mirrored)
			translationMatrix.scale(new Vertex(1,1,-1));
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, gen.mirrored, "base");
	}
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityDieselGenerator gen = (TileEntityDieselGenerator)tile;
		if(!gen.formed || gen.pos!=31)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glTranslated(+.5, +.5, +.5);
		GL11.glRotatef(gen.facing==3?180: gen.facing==4?90: gen.facing==5?-90: 0, 0,1,0);
		
		if(gen.mirrored)
		{
			GL11.glScalef(-1,1,1);
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
		
		ClientUtils.bindAtlas(0);
		GL11.glTranslated(0, .1875, 2.96875);
		GL11.glRotatef(gen.fanRotation+(gen.fanRotationStep*f), 0,0,1);
		model.model.renderOnly("fan");
		
		if(gen.mirrored)
		{
			GL11.glScalef(-1,1,1);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		GL11.glPopMatrix();
	}
}