package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelPost;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;

public class TileRenderPost extends TileEntitySpecialRenderer
{
	static ModelPost model = new ModelPost();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityWoodenPost post = (TileEntityWoodenPost)tile;
		if(post.type!=0)
			return;

		GL11.glPushMatrix();
		GL11.glTranslated(x+.5, y+1.5, z+.5);
		//		switch(relay.facing)
		//		{
		//		case 0:
		//			GL11.glTranslated(0,-.4375,0);
		GL11.glRotatef(180, 1, 0, 0);
		//			break;
		//		case 1:
		//			GL11.glTranslated(0,.4375,0);
		//			break;
		//		case 2:
		//			GL11.glTranslated(0,0,-.4375);
		//			GL11.glRotatef(-90, 1, 0, 0);
		//			break;
		//		case 3:
		//			GL11.glTranslated(0,0,.4375);
		//			GL11.glRotatef(90, 1, 0, 0);
		//			break;
		//		case 4:
		//			GL11.glTranslated(-.4375,0,0);
		//			GL11.glRotatef(90, 0, 0, 1);
		//			break;
		//		case 5:
		//			GL11.glTranslated(.4375,0,0);
		//			GL11.glRotatef(-90, 0, 0, 1);
		//			break;
		//		}

		boolean armLeft = false;
		boolean armRight = false;
		boolean rotate = false;
		if(tile.getWorldObj()!=null)
		{
			TileEntity tileX0 = tile.getWorldObj().getTileEntity(tile.xCoord-1,tile.yCoord+3,tile.zCoord);
			TileEntity tileX1 = tile.getWorldObj().getTileEntity(tile.xCoord+1,tile.yCoord+3,tile.zCoord);
			TileEntity tileZ0 = tile.getWorldObj().getTileEntity(tile.xCoord,tile.yCoord+3,tile.zCoord-1);
			TileEntity tileZ1 = tile.getWorldObj().getTileEntity(tile.xCoord,tile.yCoord+3,tile.zCoord+1);
			if(tileX0 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileX0).type==6)
				armRight = true;
			if(tileX1 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileX1).type==7)
				armLeft = true;
			if(tileZ0 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileZ0).type==4)
			{
				armRight = true;
				rotate = true;
			}
			if(tileZ1 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileZ1).type==5)
			{
				armLeft = true;
				rotate = true;
			}
		}
		else
			armLeft=true;
		ClientUtils.bindTexture("immersiveengineering:textures/models/post.png");
		if(rotate)
			GL11.glRotatef(90, 0, 1, 0);
		model.render(armLeft, armRight);
		GL11.glPopMatrix();
	}

}