package blusunrize.immersiveengineering.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.Face;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;

public class TileRenderBucketWheel extends TileEntitySpecialRenderer
{
	static WavefrontObject model = ClientUtils.getModel("immersiveengineering:models/bucketWheel.obj");

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityBucketWheel wheel = (TileEntityBucketWheel)tile;
		if(!wheel.formed || wheel.pos!=24)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x+.5, y+.5, z+.5);
		GL11.glRotatef(wheel.facing==3?180: wheel.facing==5?-90: wheel.facing==4?90: 0, 0,1,0);

		if(wheel.mirrored)
		{
			GL11.glScalef(1,1,-1);
			GL11.glDisable(GL11.GL_CULL_FACE);
		}

		float rot =  wheel.rotation+(float)(wheel.active?Config.getDouble("excavator_speed")*f:0);
		GL11.glRotatef(rot, 0,0,-1);
		ClientUtils.bindTexture("immersiveengineering:textures/models/bucketWheel.png");
		model.renderOnly("bucketWheel");

		for(int i=0; i<8; i++)
		{
			ItemStack stack = wheel.digStacks[i];
			//			String ss = ClientUtils.getResourceNameForItemStack(stack);
			//			if(!ss.isEmpty())
			if(stack==null || stack.getItem()==null)
				continue;
			IIcon ic = null;
			Block b = Block.getBlockFromItem(stack.getItem());
			if(b!=null&&b!=Blocks.air)
				ic = b.getIcon(2, stack.getItemDamage());
			else
				ic = stack.getIconIndex();
			if(ic!=null)
			{
				ClientUtils.bindAtlas(stack.getItemSpriteNumber());
				ClientUtils.tes().startDrawingQuads();
				for(GroupObject go : model.groupObjects)
				{
					if(go.name.equals("dig"+i))
					{
						for(Face face : go.faces)
						{
							float minU = ic.getMinU();
							float sizeU = ic.getMaxU() - minU;
							float minV = ic.getMinV();
							float sizeV = ic.getMaxV() - minV;

							TextureCoordinate[] oldUVs = new TextureCoordinate[face.textureCoordinates.length];
							for(int v=0; v<face.vertices.length; ++v)
							{
								oldUVs[v] = face.textureCoordinates[v]; 
								TextureCoordinate textureCoordinate = face.textureCoordinates[v];
								face.textureCoordinates[v] = new TextureCoordinate(
										minU + sizeU * textureCoordinate.u,
										minV + sizeV * textureCoordinate.v
										);
							}
							face.addFaceForRender(ClientUtils.tes(),0);
							for(int v=0; v<face.vertices.length; ++v)
								face.textureCoordinates[v] = new TextureCoordinate(oldUVs[v].u,oldUVs[v].v);
//							face.textureCoordinates = oldUVs;
						}
//						go.render();
					}
				}
				ClientUtils.tes().draw();
			}
		}

		if(wheel.mirrored)
		{
			GL11.glScalef(1,1,-1);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		GL11.glPopMatrix();
	}

}