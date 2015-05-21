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

		translationMatrix.translate(.5, 1.5, .5);
		if(rotate)
			rotationMatrix.rotate(Math.toRadians(-90), 0.0, 1.0, 0.0);

		String[] parts = armRight&&armLeft?new String[]{"Base","Arm_right","Arm_left"}: armRight?new String[]{"Base","Arm_right"}: armLeft?new String[]{"Base","Arm_left"}: new String[]{"Base"};
		
		model.render(tile, tes, translationMatrix, rotationMatrix, false, parts);
		
//		model = (WavefrontObject) AdvancedModelLoader.loadModel(new ResourceLocation("immersiveengineering:models/post.obj"));
//		IIcon icon = IEContent.blockWoodenDevice.getIcon(0, 0);
//		float minU = icon.getInterpolatedU(0);
//		float sizeU = icon.getInterpolatedU(16) - minU;
//		float minV = icon.getInterpolatedV(0);
//		float sizeV = icon.getInterpolatedV(16) - minV;
//
//		for(GroupObject groupObject : model.groupObjects)
//		{
//			for(Face face : groupObject.faces)
//			{
//				for (int i = 0; i < face.vertices.length; ++i)
//				{
//					TextureCoordinate textureCoordinate = face.textureCoordinates[i];
//					float newU = minU + sizeU * textureCoordinate.u;
//					float newV = minV + sizeV * textureCoordinate.v;
//					face.textureCoordinates[i] = new TextureCoordinate(
//							newU,
//							newV
//							);
//				}
//			}
//		}
//
//
//		String[] exempt = armRight&&armLeft?new String[0]: armLeft?new String[]{"Arm_right"}: armRight?new String[]{"Arm_left"}: new String[]{"Arm_right","Arm_left"};
//
//		GL11.glEnable(GL11.GL_LIGHTING);
//		tes.setColorRGBA_F(1F, 1F, 1F, 1F);
//
//		if(tile.getWorldObj()!=null)
//		{
//			int lb = tile.getWorldObj().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord, tile.zCoord, 0);
//			int lb_j = lb % 65536;
//			int lb_k = lb / 65536;
//			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lb_j / 1.0F, (float)lb_k / 1.0F);
//		} 
//		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
//		Vertex vertexCopy = new Vertex(0,0,0);
//		for (GroupObject groupObject : model.groupObjects)
//		{
//			if(groupObject.name.equalsIgnoreCase("Base") || (groupObject.name.equalsIgnoreCase("Arm_right")&&armRight) || (groupObject.name.equalsIgnoreCase("Arm_left")&&armLeft))
//				for (Face face : groupObject.faces)
//				{
//					if (face.faceNormal == null)
//						face.faceNormal = face.calculateFaceNormal();
//					tes.setNormal(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z);
//					for (int i = 0; i < face.vertices.length; ++i)
//					{
//						Vertex vertex = face.vertices[i];
//						vertexCopy.x = vertex.x;
//						vertexCopy.y = vertex.y;
//						vertexCopy.z = vertex.z;
//						matrix.apply(vertexCopy);
//						if ((face.textureCoordinates != null) && (face.textureCoordinates.length > 0))
//						{
//							TextureCoordinate textureCoordinate = face.textureCoordinates[i];
//							tes.addVertexWithUV(vertexCopy.x, vertexCopy.y, vertexCopy.z, textureCoordinate.u, textureCoordinate.v);
//						}
//						else
//						{
//							tes.addVertex(vertexCopy.x, vertexCopy.y, vertexCopy.z);
//						}
//					}
//				}
//		}
//		GL11.glDisable(GL11.GL_LIGHTING);
	}
}