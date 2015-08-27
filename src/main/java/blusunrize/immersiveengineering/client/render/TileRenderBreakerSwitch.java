package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderBreakerSwitch extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/breakerSwitch.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_breakerSwitch);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{

		TileEntityBreakerSwitch breaker = (TileEntityBreakerSwitch)tile;
		int f = breaker.facing;
		translationMatrix.translate(.5,.3125,.5);

		if(breaker.sideAttached==0)
		{
			rotationMatrix.rotate(Math.toRadians(180), 0,0,1);
			rotationMatrix.rotate(Math.toRadians(f==3?180: f==4?-90: f==5?90: 0), 0,1,0);
			rotationMatrix.translate(0, 0,-.125);
		}
		else
		{
			rotationMatrix.rotate(Math.toRadians(f==3?180: f==4?90: f==5?-90: 0), 0,breaker.sideAttached==2?-1:1,0);
			rotationMatrix.rotate(Math.toRadians(breaker.sideAttached==2?90:-90), 1,0,0);
			rotationMatrix.translate(0, .1875, breaker.sideAttached==2?-.3125:.0625);
		}

		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "base");
		if(!breaker.active)
			rotationMatrix.rotate(Math.toRadians(-76), 1,0,0);
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "lever");
	}

}