package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderEnergyMeter extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/eMeter.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_energyMeter);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityEnergyMeter meter = (TileEntityEnergyMeter)tile;
		int f = meter.facing;
		rotationMatrix.rotate(Math.toRadians(f==2?180: f==4?-90: f==5?90: 0), 0,1,0);
		translationMatrix.translate(.5,0,.5);
		
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false);
	}

}