package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public class TileRenderBlastFurnaceAdvanced extends TileRenderIE
{
	public static ModelIEObj model = new ModelIEObj("immersiveengineering:models/blastfurnace_advanced.obj") {
		@Override
		public IIcon getBlockIcon(String groupName) {
			if(groupName.equals("furnace"))
				return IEContent.blockStoneDevice.getIcon(0, 5);
			return IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_blastFurnacePreheater);
		}
	};

	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
	}

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityBlastFurnace furnace = (TileEntityBlastFurnace)tile;
		translationMatrix.translate(furnace.facing==5?-.5:furnace.facing==4?1.5:.5, -1, furnace.facing==3?-.5:furnace.facing==2?1.5:.5);
		rotationMatrix.rotate(Math.toRadians(furnace.facing==2?180: furnace.facing==4?-90: furnace.facing==5?90 :0), 0,1,0);
		model.render(tile, tes, translationMatrix, rotationMatrix, tile.getWorldObj()==null?-1:0, false, "furnace");
	}
}
