package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.Vertex;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderWorkbench extends TileRenderIE
{
	static ModelIEObj model = new ModelIEObj("immersiveengineering:models/workbench.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockWoodenDevice.getIcon(0, 5);
		}
	};
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityModWorkbench bench = (TileEntityModWorkbench)tile;
		if(bench.dummy)
			return;
		
		GL11.glPushMatrix();
		GL11.glTranslated(x,y,z);

		int facing = bench.facing;
		int off = bench.dummyOffset;

		boolean mirror = facing==2?off>0: facing==3?off<0: facing==4?off<0: facing==5?off>0 : false;
		float angle = facing==2?180:facing==4?-90:facing==5?90:0;

		GL11.glPushMatrix();
		GL11.glTranslated(facing>3?.5: facing==2&&mirror?1: facing==3&&!mirror?1:0, 0, facing<4?.5: facing==5&&mirror?1: facing==4&&!mirror?1:0);
		GL11.glRotatef(angle, 0,1,0);
		if(mirror)
		{
			GL11.glScaled(-1, 1, 1);
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
		ClientUtils.bindAtlas(0);
		model.model.renderAll();
		if(mirror)
		{
			GL11.glScaled(-1, 1, 1);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		GL11.glPopMatrix();

		GL11.glTranslated(.5,1.05,.5);
		float offset = off * (mirror?1f:.825f);
		GL11.glTranslated(facing<4?offset:0,0,facing>3?offset:0);
		angle = facing==2?90:facing==4?180:facing==5?0:-90;
		GL11.glRotatef(angle, 0,1,0);
		GL11.glRotatef(-90, 1,0,0);
		GL11.glScalef(1.5f, 1.5f, 1.5f);
		if(bench.getStackInSlot(0)!=null)
		{
			try{

				ItemStack is = bench.getStackInSlot(0).copy();
				is.stackSize = 1;
				EntityItem entityitem = new EntityItem(tile.getWorldObj(), 0.0D, 0.0D, 0.0D, is);
				entityitem.hoverStart = 0.0F;
				RenderItem.renderInFrame=true;
				RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
				RenderItem.renderInFrame=false;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		GL11.glPopMatrix();
	}
	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		int f = ((TileEntityModWorkbench)tile).facing;
		int off = ((TileEntityModWorkbench)tile).dummyOffset;
		float angle = f==2?180: f==4?-90: f==5?90: 0;
		boolean mirror = f==2?off>0: f==3?off<0: f==4?off<0: f==5?off>0 : false;

		switch(f)
		{
		case 2:
			angle = 180;
			translationMatrix.translate(mirror?1:0,0,.5);
			break;
		case 3:
			translationMatrix.translate(!mirror?1:0,0,.5);
			break;
		case 4:
			angle = -90;
			translationMatrix.translate(.5,0,!mirror?1:0);
			break;
		case 5:
			angle = 90;
			translationMatrix.translate(.5,0,mirror?1:0);
			break;
		}

		if(mirror)
			translationMatrix.scale(new Vertex(f<4?-1:1,1,f>3?-1:1));
		rotationMatrix.rotate(Math.toRadians(angle), 0.0, 1.0, 0.0);

		//		model.render(tile, tes, translationMatrix, rotationMatrix, true, mirror);
	}
}