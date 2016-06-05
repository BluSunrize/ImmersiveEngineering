package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class TileRenderWorkbench extends TileEntitySpecialRenderer<TileEntityModWorkbench>
{
	//	static ModelIEObj model = new ModelIEObj("immersiveengineering:models/workbench.obj")
	//	{
	//		@Override
	//		public IIcon getBlockIcon(String groupName)
	//		{
	//			return IEContent.blockWoodenDevices.getIcon(0, 5);
	//		}
	//	};

	//	public void renderDynamic(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	@Override
	public void renderTileEntityAt(TileEntityModWorkbench te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(te.dummy||!te.getWorld().isBlockLoaded(te.getPos(), false))
			return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x,y,z);

		int facing = te.getFacing().ordinal();
		int off = te.dummyOffset;

		float angle = facing==2?180:facing==4?-90:facing==5?90:0;

//		GL11.glPushMatrix();
//		GL11.glTranslated(facing>3?.5: facing==2&&mirror?1: facing==3&&!mirror?1:0, 0, facing<4?.5: facing==5&&mirror?1: facing==4&&!mirror?1:0);
//		if(mirror)
//		{
//			GL11.glScaled(-1, 1, 1);
//			GL11.glDisable(GL11.GL_CULL_FACE);
//		}
//		//		ClientUtils.bindAtlas(0);
//		//		model.model.renderAll();
//		if(mirror)
//		{
//			GL11.glScaled(-1, 1, 1);
//			GL11.glEnable(GL11.GL_CULL_FACE);
//		}
//		GL11.glPopMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.translate(.5,1.0625,.5);
//		float offset = off * (mirror?1f:.825f);
//		GlStateManager.translate(facing<4?offset:0,0,facing>3?offset:0);
		angle = facing==2?90:facing==4?180:facing==5?0:-90;
		GlStateManager.rotate(180, 0,0,1);
		GlStateManager.rotate(-angle, 0,1,0);
		GlStateManager.rotate(-90, 1,0,0);
		GlStateManager.translate(0,-.875,0);
		GlStateManager.scale(.75f,.75f,.75f);
		if(te.getInventory()[0]!=null)
		{
			try{
				ItemStack is = te.getInventory()[0].copy();
				is.stackSize = 1;
				ClientUtils.mc().getRenderItem().renderItem(is, ItemCameraTransforms.TransformType.FIXED);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();

	}
	//	@Override
	//	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	//	{
	//		int f = ((TileEntityModWorkbench)tile).facing;
	//		int off = ((TileEntityModWorkbench)tile).dummyOffset;
	//		float angle = f==2?180: f==4?-90: f==5?90: 0;
	//		boolean mirror = f==2?off>0: f==3?off<0: f==4?off<0: f==5?off>0 : false;
	//
	//		switch(f)
	//		{
	//		case 2:
	//			angle = 180;
	//			translationMatrix.translate(mirror?1:0,0,.5);
	//			break;
	//		case 3:
	//			translationMatrix.translate(!mirror?1:0,0,.5);
	//			break;
	//		case 4:
	//			angle = -90;
	//			translationMatrix.translate(.5,0,!mirror?1:0);
	//			break;
	//		case 5:
	//			angle = 90;
	//			translationMatrix.translate(.5,0,mirror?1:0);
	//			break;
	//		}
	//
	//		if(mirror)
	//			translationMatrix.scale(new Vertex(f<4?-1:1,1,f>3?-1:1));
	//		rotationMatrix.rotate(Math.toRadians(angle), 0.0, 1.0, 0.0);
	//		model.render(tile, tes, translationMatrix, rotationMatrix, 0, mirror);
	//	}
}