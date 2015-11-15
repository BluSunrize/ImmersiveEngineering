package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderRedstoneBreaker extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/redstoneBreaker.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_redstoneBreaker);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityBreakerSwitch breaker = (TileEntityBreakerSwitch)tile;
		int f = breaker.facing;
		translationMatrix.translate(.5,.5,.5);

		if(breaker.sideAttached==0 && breaker.getWorldObj()!=null)
		{
			rotationMatrix.rotate(Math.toRadians(f==3?180: f==4?-90:f==5?90: 0), 0,1,0);
			rotationMatrix.rotate(Math.toRadians(f<4?90:-90), 1,0,0);
		}
		else
		{
			rotationMatrix.rotate(Math.toRadians(f==3?180: f==4?90: f==5?-90: 0), 0,breaker.sideAttached==2?-1:1,0);
			rotationMatrix.rotate(Math.toRadians(breaker.sideAttached==2?180:0), 1,0,0);
		}
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false);
	}

}