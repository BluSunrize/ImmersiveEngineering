package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodlight;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderFloodlight extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/floodlight.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockMetalDevice2.getIcon(groupName.equals("glass")?1:0, BlockMetalDevices2.META_floodlight);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityFloodlight light = (TileEntityFloodlight)tile;
		translationMatrix.translate(.5+(light.side==4?-.125:light.side==5?.125:0), .5+(light.side==0?-.125:light.side==1?.125:0), .5+(light.side==2?-.125:light.side==3?.125:0));
		if(light.side==0)
			rotationMatrix.rotate(Math.PI, light.facing<4?0:1,0,light.facing<4?1:0);
		else if(light.side!=1)
			rotationMatrix.rotate(Math.PI/2, light.side==2?-1:light.side==3?1:0,0,light.side==5?-1:light.side==4?1:0);

		if(BlockRenderMetalDevices2.renderPass==0)
			model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "base");

		float angle = light.facing==2?180: light.facing==4?-90: light.facing==5?90: 0;
		rotationMatrix.rotate(Math.toRadians(angle+light.rotY), 0,1,0);
		if(BlockRenderMetalDevices2.renderPass==0)
			model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "axis");

		rotationMatrix.rotate(Math.toRadians(light.rotX), 1,0,0);
		String[] s = {"light","off"};
		if(BlockRenderMetalDevices2.renderPass==1)
			s = new String[]{"glass"};
		else if(((TileEntityFloodlight)tile).active)
			s[1]="on";
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, s);
	}

}