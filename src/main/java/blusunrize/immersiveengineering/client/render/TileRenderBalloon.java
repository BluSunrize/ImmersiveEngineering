package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCaseBalloon;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.cloth.TileEntityBalloon;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.Vertex;
import net.minecraftforge.client.model.obj.WavefrontObject;

public class TileRenderBalloon extends TileRenderImmersiveConnectable
{
	//	ModelIEObj model = new ModelIEObj("immersiveengineering:models/balloon.obj")
	//	{
	//		@Override
	//		public IIcon getBlockIcon(String groupName)
	//		{
	//			return IEContent.blockClothDevice.getIcon(0, 0);
	//		}
	//	};
	WavefrontObject model = ClientUtils.getModel("immersiveengineering:models/balloon.obj");

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, .65625, .5);
		TileEntityBalloon balloon = (TileEntityBalloon)tile;
		if(BlockRenderClothDevices.renderPass==0||!tile.hasWorldObj())
			ClientUtils.renderStaticWavefrontModelWithIcon(balloon, model, IEContent.blockClothDevice.getIcon(0, 0), tes, translationMatrix, rotationMatrix, 0,false, 1,1,1, "base");
//		if(BlockRenderClothDevices.renderPass==1||!tile.hasWorldObj())
		{
			ShaderCase sCase = null;
			if(balloon.shader!=null && balloon.shader.getItem() instanceof IShaderItem)
				sCase = ((IShaderItem)balloon.shader.getItem()).getShaderCase(balloon.shader,null,"balloon");
			if(sCase!=null && sCase instanceof ShaderCaseBalloon)
			{
				String[] parts = {"balloon0_0","balloon1_0","balloon0_1","balloon1_1"};
				int maxPasses;
				IIcon icon;
				maxPasses = sCase.getPasses(balloon.shader,null,"");
				for(int pass=0; pass<maxPasses; pass++)
				if(tile.hasWorldObj() || (pass==0&&BlockRenderClothDevices.renderPass==0) || BlockRenderClothDevices.renderPass==1)
				{
					float scale = 1+pass*.001f;
					translationMatrix.scale(new Vertex(scale,scale,scale));
					for(String part : parts)
					{
						icon = sCase.getReplacementIcon(balloon.shader,null, part, pass);
						int[] colour = sCase.getRGBAColourModifier(balloon.shader,null, part, pass);
						ClientUtils.renderStaticWavefrontModelWithIcon(balloon, model, icon, tes, translationMatrix, rotationMatrix, -1,false, colour[0]/255f,colour[1]/255f,colour[2]/255f, part);
					}
					translationMatrix.scale(new Vertex(1/scale,1/scale,1/scale));
				}
			}
			else
			{
				float[] col0 = EntitySheep.fleeceColorTable[15-balloon.colour0];
				float[] col1 = EntitySheep.fleeceColorTable[15-balloon.colour1];
				if(balloon.style==0)
				{
					ClientUtils.renderStaticWavefrontModelWithIcon(balloon, model, IEContent.blockClothDevice.getIcon(0, 0), tes, translationMatrix, rotationMatrix, -1,false, col0[0],col0[1],col0[2], "balloon0_0","balloon0_1");
					ClientUtils.renderStaticWavefrontModelWithIcon(balloon, model, IEContent.blockClothDevice.getIcon(0, 0), tes, translationMatrix, rotationMatrix, -1,false, col1[0],col1[1],col1[2], "balloon1_0","balloon1_1");
					//			ClientUtils.renderStaticWavefrontModel(tile, model.model, tes, translationMatrix, rotationMatrix, -1,false, col0[0],col0[1],col0[2], "balloon0_0","balloon0_1");
					//			ClientUtils.renderStaticWavefrontModel(tile, model.model, tes, translationMatrix, rotationMatrix, -1,false, col1[0],col1[1],col1[2], "balloon1_0","balloon1_1");
				}
				else
				{
					ClientUtils.renderStaticWavefrontModelWithIcon(balloon, model, IEContent.blockClothDevice.getIcon(0, 0), tes, translationMatrix, rotationMatrix, -1,false, col0[0],col0[1],col0[2], "balloon0_0","balloon1_0");
					ClientUtils.renderStaticWavefrontModelWithIcon(balloon, model, IEContent.blockClothDevice.getIcon(0, 0), tes, translationMatrix, rotationMatrix, -1,false, col1[0],col1[1],col1[2], "balloon0_1","balloon1_1");
					//			ClientUtils.renderStaticWavefrontModel(tile, model.model, tes, translationMatrix, rotationMatrix, -1,false, col0[0],col0[1],col0[2], "balloon0_0","balloon1_0");
					//			ClientUtils.renderStaticWavefrontModel(tile, model.model, tes, translationMatrix, rotationMatrix, -1,false, col1[0],col1[1],col1[2], "balloon0_1","balloon1_1");
				}
			}
		}
	}

}