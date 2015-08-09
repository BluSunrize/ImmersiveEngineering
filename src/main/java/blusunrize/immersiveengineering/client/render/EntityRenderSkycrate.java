package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;

public class EntityRenderSkycrate extends Render
{
	static WavefrontObject model = ClientUtils.getModel("immersiveengineering:models/skycrate.obj");

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1)
	{
		if(entity.isDead)
			return;
//		this.bindEntityTexture(entity);
//		Tessellator tes = Tessellator.instance;
//		Block b = IEContent.blockWoodenDevice;
//		IIcon ic = b.getIcon(0, 4);
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
//		
//		RenderBlocks.getInstance().setRenderBounds(0,0,0, 1,1,1);
//		tes.startDrawingQuads();
//		RenderBlocks.getInstance().renderFaceYNeg(b, 0,0,0, ic);
//		tes.draw();
//		tes.startDrawingQuads();
//		RenderBlocks.getInstance().renderFaceYPos(b, 0,0,0, ic);
//		tes.draw();
//		tes.startDrawingQuads();
//		RenderBlocks.getInstance().renderFaceZNeg(b, 0,0,0, ic);
//		tes.draw();
//		tes.startDrawingQuads();
//		RenderBlocks.getInstance().renderFaceZPos(b, 0,0,0, ic);
//		tes.draw();
//		tes.startDrawingQuads();
//		RenderBlocks.getInstance().renderFaceXNeg(b, 0,0,0, ic);
//		tes.draw();
//		tes.startDrawingQuads();
//		RenderBlocks.getInstance().renderFaceXPos(b, 0,0,0, ic);
//		tes.draw();
		GL11.glRotatef(entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*f1, 0.0F, -1.0F, 0.0F);
				
		ClientUtils.bindTexture("immersiveengineering:textures/models/skycrate.png");
		model.renderPart("Box");
		
		GL11.glRotatef(entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*f1, -1.0F, 0.0F, 0.0F);
		model.renderPart("Hook");
		
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return null;
//		return new ResourceLocation("immersiveengineering:textures/blocks/woodenCrate.png");
	}

}
