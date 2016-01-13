package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMetalPress;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public class TileRenderMetalPress extends TileRenderIE
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/metalPress.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			if(groupName.equalsIgnoreCase("conveyors"))
				return IEContent.blockMetalDevice.getIcon(0, BlockMetalDevices.META_conveyorBelt);
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_metalPress);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityMetalPress press = (TileEntityMetalPress)tile;
		translationMatrix.translate(.5, -1, .5);
		rotationMatrix.rotate(Math.toRadians(press.facing==2?180: press.facing==4?-90: press.facing==5?90: 0), 0,1,0);
		model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "base","conveyors");
	}
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityMetalPress press = (TileEntityMetalPress)tile;
		if(press.pos!=4)
			return;
		GL11.glPushMatrix();
		GL11.glTranslated(x+.5, y-1, z+.5);
		GL11.glRotatef(press.facing==3?180: press.facing==4?90: press.facing==5?-90: 0, 0,1,0);
		float piston = 0;
		float shift[] = new float[press.inventory.length];
		for(int i=0; i<press.inventory.length; i++)
		{
			float process = press.process[i]/120f;
			if(process<.4375f)
				shift[i] = process/.4375f*.5f;
			else if(process<.5625f)
				shift[i] = .5f;
			else
				shift[i] = .5f+ (process-.5625f)/.4375f*.5f;

			if(press.mold!=null)
				if(process>=.4375f&&process<.5625f)
					if(process<.46875f)
						piston = (process-.4375f)/.03125f;
					else if(process<.53125f)
						piston = 1;
					else
						piston = 1 - (process-.53125f)/.03125f;
		}

		ClientUtils.bindAtlas(0);
		GL11.glTranslated(0,-piston*.6875f,0);
		ClientUtils.tes().startDrawingQuads();
		model.render(tile, ClientUtils.tes(), new Matrix4(),new Matrix4(), 0, false, "piston");
		ClientUtils.tes().draw();
		if(press.mold!=null)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(0,1.875,.3125);
			GL11.glRotatef(-90, 1,0,0);
			ClientUtils.bindAtlas(1);
			for(int pass=0; pass<press.mold.getItem().getRenderPasses(press.mold.getItemDamage()); pass++)
			{
				IIcon icon = press.mold.getItem().getIcon(press.mold, pass);
				if(icon!=null)
				{
					float scale = .625f;
					GL11.glScalef(scale,scale,1);
					int col = press.mold.getItem().getColorFromItemStack(press.mold, pass);
					GL11.glColor3f((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f);
					ClientUtils.renderItemIn2D(icon, new double[]{0,1,0,1}, icon.getIconWidth(),icon.getIconHeight(), .0625f);
					GL11.glColor3f(1,1,1);
					GL11.glScalef(1/scale,1/scale,1);
				}
			}
			GL11.glPopMatrix();
		}
		GL11.glTranslated(0,piston*.6875f,0);

		GL11.glTranslated(0,1.15,1.46);
		for(int i=0; i<press.inventory.length; i++)
			if(press.inventory[i]!=null && press.process[i]<119 && press.process[i]>=0)
			{
				GL11.glTranslated(0,0,-2.5*shift[i]);
				ItemStack stack = press.inventory[i];
				GL11.glRotatef(-90, 1,0,0);
				EntityItem entityitem = new EntityItem(tile.getWorldObj(), 0.0D, 0.0D, 0.0D, stack);
				entityitem.getEntityItem().stackSize = 1;
				entityitem.hoverStart = 0.0F;
				RenderItem.renderInFrame = true;
				RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
				RenderItem.renderInFrame = false;
				GL11.glRotatef(90, 1,0,0);
				GL11.glTranslated(0,0,2.5*shift[i]);
			}
		GL11.glPopMatrix();
	}

}