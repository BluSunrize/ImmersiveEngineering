package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityChargingStation;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderChargingStation extends TileRenderIE
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/charger.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_chargingStation);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, 0, .5);
		TileEntityChargingStation charger = (TileEntityChargingStation)tile;
		switch(charger.facing)
		{
		case 2:
			break;
		case 3:
			rotationMatrix.rotate(Math.toRadians(180), 0,1,0);
			break;
		case 4:
			rotationMatrix.rotate(Math.toRadians(90), 0,1,0);
			break;
		case 5:
			rotationMatrix.rotate(Math.toRadians(-90), 0,1,0);
			break;
		}
		if(BlockRenderMetalDevices2.renderPass==1)
			model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "glass","tubes");
		else
			model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "base");
	}

	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
		if(tile.getWorldObj()!=null)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(x+.5, y+.3125, z+.5);
			GL11.glScalef(.75f,.75f,.75f);
			ClientUtils.bindAtlas(0);
			TileEntityChargingStation charger = (TileEntityChargingStation)tile;
			switch(charger.facing)
			{
			case 2:
				GL11.glRotated(180, 0,1,0);
				break;
			case 3:
				break;
			case 4:
				GL11.glRotated(-90, 0,1,0);
				break;
			case 5:
				GL11.glRotated(90, 0,1,0);
				break;
			}
			if(charger.inventory!=null)
			{
				if(!RenderManager.instance.options.fancyGraphics && MinecraftForgeClient.getItemRenderer(charger.inventory, ItemRenderType.ENTITY)==null)
				{
					float rot = charger.facing==3?180: charger.facing==4?-90: charger.facing==5?90: 0;
					GL11.glRotatef(rot - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
				}
				EntityItem entityitem = new EntityItem(tile.getWorldObj(), 0.0D, 0.0D, 0.0D, charger.inventory);
				entityitem.hoverStart = 0.0F;
				RenderItem.renderInFrame = true;
				RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
				RenderItem.renderInFrame = false;
			}
			GL11.glColor3f(1,0,0);
			GL11.glPopMatrix();
		}
	}

}