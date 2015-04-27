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
		GL11.glTranslated(x+.5, y+.5, z+.5);

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
				armLeft = true;
			if(tileX1 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileX1).type==7)
				armRight = true;
			if(tileZ0 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileZ0).type==4)
			{
				armLeft = true;
				rotate = true;
			}
			if(tileZ1 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileZ1).type==5)
			{
				armRight = true;
				rotate = true;
			}
		}
		else
			armRight=true;
		
		ClientUtils.bindTexture("immersiveengineering:textures/models/post.png");
		model.Arm_left.isHidden=!armLeft;
		model.Arm_left.rotateAngleY=rotate?1.57079f:0;
		model.Arm_right.isHidden=!armRight;
		model.Arm_right.rotateAngleY=rotate?1.57079f:0;
		model.render(null, 0, 0, 0, 0, 0, .0625f);
		GL11.glPopMatrix();
	}

}