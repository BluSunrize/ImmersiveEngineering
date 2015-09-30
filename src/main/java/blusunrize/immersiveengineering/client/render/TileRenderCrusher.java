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
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderCrusher extends TileRenderIE
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/crusher.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_crusher);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityCrusher crusher = (TileEntityCrusher)tile;
		
		translationMatrix.translate(.5, 1.5, .5);
		rotationMatrix.rotate(Math.toRadians(crusher.facing==2?180: crusher.facing==4?-90: crusher.facing==5?90: 0), 0,1,0);
		if(crusher.mirrored)
			translationMatrix.scale(new Vertex(1,1,-1));

		model.render(tile, tes, translationMatrix, rotationMatrix, 0, crusher.mirrored, "base");
	}
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityCrusher crusher = (TileEntityCrusher)tile;
		if(!crusher.formed || crusher.pos!=17)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x+.5, y+1.5, z+.5);
		GL11.glRotatef(crusher.facing==2?180: crusher.facing==4?-90: crusher.facing==5?90: 0, 0,1,0);

		if(crusher.mirrored)
		{
			GL11.glScalef(-1,1,1);
			GL11.glDisable(GL11.GL_CULL_FACE);
		}

		ClientUtils.bindAtlas(0);
		boolean b = crusher.hasPower&&((crusher.active&&crusher.process>0)||crusher.mobGrinding||crusher.grindingTimer>0);
		float angle = crusher.barrelRotation+(b?18*f:0);
		
		GL11.glTranslated(17/16f,14/16f,-8.5/16f);
		GL11.glRotatef(angle, 1,0,0);
		model.model.renderOnly("drum1");
		GL11.glRotatef(-angle, 1,0,0);
		GL11.glTranslated(0,0,17/16f);
		GL11.glRotatef(-angle, 1,0,0);
		model.model.renderOnly("drum0");
		
		if(crusher.mirrored)
		{
			GL11.glScalef(-1,1,1);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		GL11.glPopMatrix();
	}

}