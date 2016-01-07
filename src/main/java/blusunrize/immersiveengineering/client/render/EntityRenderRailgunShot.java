package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntityRailgunShot;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EntityRenderRailgunShot extends Render
{
	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1)
	{
		double yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f1 - 90.0F;
		double pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f1;

		ItemStack ammo = ((EntityRailgunShot)entity).getAmmo();
		int[][] colourMap = {{0x777777,0xa4a4a4}};
		if(ammo!=null)
		{
			RailgunHandler.RailgunProjectileProperties prop = RailgunHandler.getProjectileProperties(ammo);
			colourMap = prop!=null?prop.colourMap:colourMap;
		}
		
		renderRailgunProjectile(x,y,z, yaw, pitch, colourMap);
	}

	public static void renderRailgunProjectile(double x, double y, double z, double yaw, double pitch, int[][] colourMap)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		Tessellator tes = ClientUtils.tes();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glRotated(yaw, 0.0F, 1.0F, 0.0F);
		GL11.glRotated(pitch, 0.0F, 0.0F, 1.0F);

		GL11.glScalef(.25f, .25f, .25f);

		if(colourMap.length==1)
		{
			colourMap = new int[][]{colourMap[0],colourMap[0]};
		}

		float height = .1875f;
		float halfWidth = height/2;
		float length = 2;
		int colWidth = colourMap[0].length;
		for(int i=0; i<colourMap.length; i++)
			colWidth = Math.min(colWidth, colourMap[i].length);
		int colLength = colourMap.length;
		float widthStep = height/colWidth;
		float lengthStep = length/colLength;

		GL11.glTranslatef(-length*.85f,0,0);
		tes.startDrawingQuads();
		//Front&Back
		for(int i=0; i<colWidth; i++)
		{
			tes.setNormal(-1,0,0);
			tes.setColorOpaque_I(colourMap[0][i]);
			tes.addVertex(0,height,-halfWidth+widthStep*i);
			tes.addVertex(0,0     ,-halfWidth+widthStep*i);
			tes.addVertex(0,0     ,-halfWidth+widthStep*(i+1));
			tes.addVertex(0,height,-halfWidth+widthStep*(i+1));

			tes.setNormal(1,0,0);
			tes.setColorOpaque_I(colourMap[colLength-1][i]);
			tes.addVertex(length,0     ,-halfWidth+widthStep*i);
			tes.addVertex(length,height,-halfWidth+widthStep*i);
			tes.addVertex(length,height,-halfWidth+widthStep*(i+1));
			tes.addVertex(length,0     ,-halfWidth+widthStep*(i+1));
		}
		//Sides
		for(int i=0; i<colLength; i++)
		{
			tes.setNormal(0,0,-1);
			tes.setColorOpaque_I(colourMap[i][0]);
			tes.addVertex(lengthStep*i    ,0     ,-halfWidth);
			tes.addVertex(lengthStep*i    ,height,-halfWidth);
			tes.addVertex(lengthStep*(i+1),height,-halfWidth);
			tes.addVertex(lengthStep*(i+1),0     ,-halfWidth);

			tes.setNormal(0,0,1);
			tes.setColorOpaque_I(colourMap[i][colWidth-1]);
			tes.addVertex(lengthStep*i    ,height,halfWidth);
			tes.addVertex(lengthStep*i    ,0     ,halfWidth);
			tes.addVertex(lengthStep*(i+1),0     ,halfWidth);
			tes.addVertex(lengthStep*(i+1),height,halfWidth);
		}
		//Top&Bottom
		for(int i=0; i<colLength; i++)
			for(int j=0; j<colWidth; j++)
			{
				tes.setNormal(0,1,0);
				tes.setColorOpaque_I(colourMap[i][j]);
				tes.addVertex(lengthStep*(i+1),height,-halfWidth+widthStep*j);
				tes.addVertex(lengthStep*i    ,height,-halfWidth+widthStep*j);
				tes.addVertex(lengthStep*i    ,height,-halfWidth+widthStep*(j+1));
				tes.addVertex(lengthStep*(i+1),height,-halfWidth+widthStep*(j+1));

				tes.setNormal(0,-1,0);
				tes.addVertex(lengthStep*i    ,0     ,-halfWidth+widthStep*j);
				tes.addVertex(lengthStep*(i+1),0     ,-halfWidth+widthStep*j);
				tes.addVertex(lengthStep*(i+1),0     ,-halfWidth+widthStep*(j+1));
				tes.addVertex(lengthStep*i    ,0     ,-halfWidth+widthStep*(j+1));
			}
		tes.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}


	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
