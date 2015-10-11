package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityWallmountMetal;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallmount;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderWallmount extends TileRenderIE
{
	static ModelIEObj model = new ModelIEObj("immersiveengineering:models/wallmount.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockWoodenDecoration.getIcon(0, 6);
		}
	};
	static ModelIEObj modelMetal = new ModelIEObj("immersiveengineering:models/wallmount.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockMetalDecoration.getIcon(0, BlockMetalDecoration.META_wallMount);
		}
	};

	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
	}

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, .5, .5);

		TileEntityWallmount arm = (TileEntityWallmount)tile;
		
		rotationMatrix.rotate(Math.toRadians(arm.facing==2?270: arm.facing==3?90: arm.facing==4?0: 180 ), 0,1,0);
		rotationMatrix.rotate(arm.inverted?3.14159f:0, 1,0,0);
		if(arm.sideAttached>0)
			rotationMatrix.rotate(Math.toRadians(-90), 0,0,1);

		if(arm instanceof TileEntityWallmountMetal)
			modelMetal.render(tile, tes, translationMatrix, rotationMatrix, 1, false);
		else
			model.render(tile, tes, translationMatrix, rotationMatrix, 1, false);
	}
	//	static ModelWallMount model = new ModelWallMount();
	//
	//	@Override
	//	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	//	{
	//		if(GuiScreen.isShiftKeyDown())
	//			model = new ModelWallMount();
	//		TileEntityWallMount arm = (TileEntityWallMount)tile;
	//
	//		GL11.glPushMatrix();
	//		GL11.glTranslated(x, y, z);
	//
	//		String s = arm instanceof TileEntityWallMountSteel?"steel":"wood";
	//		ClientUtils.bindTexture("immersiveengineering:textures/models/wallmount_"+s+".png");
	//		model.connection.rotateAngleX = !arm.inverted?3.14159f:0;
	//
	//		model.connection.rotateAngleY = (float)Math.toRadians( arm.facing==2?270: arm.facing==3?90: arm.facing==4?0: 180 );
	//		model.render(null, 0, 0, 0, 0, 0, .0625f);
	//		GL11.glPopMatrix();
	//	}

}
