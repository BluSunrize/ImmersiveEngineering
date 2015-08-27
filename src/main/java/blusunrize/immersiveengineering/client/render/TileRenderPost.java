package blusunrize.immersiveengineering.client.render;

import java.util.ArrayList;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
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
		ArrayList<String> parts = new ArrayList<String>();
		parts.add("Base");
		boolean rotate=false;
		float fr = 0;
		if(tile.getWorldObj()!=null)
			for(int i=0; i<4; i++)
				rotate |= handleArms(tile.getWorldObj().getTileEntity(tile.xCoord+(i==2?-1:i==3?1:0),tile.yCoord+3,tile.zCoord+(i==0?-1:i==1?1:0)), 4+i, fr, parts);
		else
			parts.add("Arm_right_u");

		translationMatrix.translate(.5, 0, .5);
		if(rotate)
			rotationMatrix.rotate(Math.toRadians(-90), 0.0, 1.0, 0.0);

		model.render(tile, tes, translationMatrix, rotationMatrix, 1, false, parts.toArray(new String[parts.size()]));
	}


	boolean handleArms(TileEntity arm, int checkType, float rotate, ArrayList<String> parts)
	{
		if(arm instanceof TileEntityWoodenPost && ((TileEntityWoodenPost)arm).type==checkType)
		{
			String dir = checkType%2==1?"left":"right";
			if(canArmConnectToBlock(arm.getWorldObj(), arm.xCoord,arm.yCoord-1,arm.zCoord, true))
			{
				parts.add("Arm_"+dir+"_d");
				if(canArmConnectToBlock(arm.getWorldObj(), arm.xCoord,arm.yCoord+1,arm.zCoord, false))
					parts.add("Arm_"+dir+"_u");
			}
			else
				parts.add("Arm_"+dir+"_u");
			return checkType<6;
		}
		return false;
	}

	public static boolean canArmConnectToBlock(World world, int x, int y, int z, boolean down)
	{
		if(world.isAirBlock(x,y,z))
			return false;
		world.getBlock(x,y,z).setBlockBoundsBasedOnState(world, x, y, z);
		return down?world.getBlock(x,y,z).getBlockBoundsMaxY()>=1: world.getBlock(x,y,z).getBlockBoundsMinY()<=0;
	}
}