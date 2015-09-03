package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodLight;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderFloodLight extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/searchlight.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_floodLight);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, .5625, .5);
		TileEntityFloodLight light = (TileEntityFloodLight)tile;
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "base");

		float angle = light.facing==2?180: light.facing==4?-90: light.facing==5?90: 0;
		rotationMatrix.rotate(Math.toRadians(angle+light.rotY), 0,1,0);
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "gear0");
		
		rotationMatrix.rotate(Math.toRadians(light.rotX), 1,0,0);
		String[] s = {"light","off"};
		if(((TileEntityFloodLight)tile).active)
			s[1]="on";
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, s);
	}

}