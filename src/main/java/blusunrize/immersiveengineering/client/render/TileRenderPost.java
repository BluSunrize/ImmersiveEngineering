package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderPost extends TileRenderIE
{
	static ModelIEObj model = new ModelIEObj("immersiveengineering:models/post.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockWoodenDevice.getIcon(0, 0);
		}
	};
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
	}
	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		boolean armLeft = false;
		boolean armRight = false;
		boolean rotate = false;
		if(tile.getWorldObj()!=null)
		{
			TileEntity tileX0 = tile.getWorldObj().getTileEntity(tile.xCoord+1,tile.yCoord+3,tile.zCoord);
			TileEntity tileX1 = tile.getWorldObj().getTileEntity(tile.xCoord-1,tile.yCoord+3,tile.zCoord);
			TileEntity tileZ0 = tile.getWorldObj().getTileEntity(tile.xCoord,tile.yCoord+3,tile.zCoord+1);
			TileEntity tileZ1 = tile.getWorldObj().getTileEntity(tile.xCoord,tile.yCoord+3,tile.zCoord-1);
			if(tileX0 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileX0).type==7)
				armLeft = true;
			if(tileX1 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileX1).type==6)
				armRight = true;
			if(tileZ0 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileZ0).type==5)
			{
				armLeft = true;
				rotate = true;
			}
			if(tileZ1 instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)tileZ1).type==4)
			{
				armRight = true;
				rotate = true;
			}
		}
		else
			armRight=true;

		translationMatrix.translate(.5, 0, .5);
		if(rotate)
			rotationMatrix.rotate(Math.toRadians(-90), 0.0, 1.0, 0.0);

		String[] parts = armRight&&armLeft?new String[]{"Base","Arm_right","Arm_left"}: armRight?new String[]{"Base","Arm_right"}: armLeft?new String[]{"Base","Arm_left"}: new String[]{"Base"};
		
		model.render(tile, tes, translationMatrix, rotationMatrix, true, false, parts);
	}
}