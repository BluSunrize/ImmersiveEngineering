package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.ItemRailgun;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

public class ItemRenderRailgun implements IItemRenderer
{
	static WavefrontObject modelobj = ClientUtils.getModel("immersiveengineering:models/railgun.obj");

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();

		EntityLivingBase user = null;
		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			float scale = .375f;
			GL11.glRotatef(42, 0, 1, 0);
			GL11.glTranslatef(-.5f,1f,.25f);
			user = (EntityLivingBase) data[1];
			if(user instanceof EntityPlayer && ((EntityPlayer)user).getItemInUseCount()>0)
				GL11.glTranslatef(0,0,-.25f);
			GL11.glScalef(.3125f,scale,scale);
		}
		else if(type==ItemRenderType.EQUIPPED)
		{
			float scale = .4375f;
			GL11.glRotatef(135, 0, 1, 0);
			GL11.glRotatef(-35, 0, 0, 1);
			GL11.glTranslatef(.1f,-.4f,-0f);
			if(ClientUtils.mc().thePlayer.getItemInUseCount()>0)
			{
				GL11.glRotatef(35, 0, 0, 1);
				GL11.glTranslatef(.4f,.8f,-0f);
			}
			GL11.glScalef(scale,scale,scale);
		}
		else if(type==ItemRenderType.ENTITY)
		{
			float scale = .5f;
//			GL11.glRotatef(-90, 0, 0, 1);
			GL11.glRotatef(45, 0, 1, 0);
			if(RenderItem.renderInFrame)
			{
				GL11.glRotatef(-135, 0, 1, 0);
				GL11.glRotatef(40, 0, 0, 1);
				scale = .125f;
				GL11.glTranslatef(.125f,.1f,-.01f);
			}
			else
				GL11.glRotatef(45, 0, 1, 0);
			GL11.glScalef(scale,scale, scale);
		}
		else if(type==ItemRenderType.INVENTORY)
		{
			GL11.glRotatef(-190, 0, 1, 0);
			GL11.glTranslatef(.5f,-.2f,.2f);
			GL11.glScalef(.25f,.375f,.375f);
		}

		//		ClientUtils.mc().renderEngine.bindTexture(ClientProxy.revolverTextureResource);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(3042);
		OpenGlHelper.glBlendFunc(770, 771, 0, 1);

		IIcon icon = item.getIconIndex();
		ItemStack shader = ((ItemRailgun)item.getItem()).getShaderItem(item);
		ShaderCase sCase = (shader!=null && shader.getItem() instanceof IShaderItem)?((IShaderItem)shader.getItem()).getShaderCase(shader, item, "railgun"):null;

		if(sCase==null)
		{
			ClientUtils.renderWavefrontWithIconUVs(modelobj, icon, "grip","frame","barrel","capacitors","sled","wires");
			//			ClientUtils.renderWavefrontWithIconUVs(modelobj, icon, "upgrade_speed","upgrade_scope");

			//			ClientUtils.renderWavefrontWithIconUVs(modelobj, icon, "base","grip","tank","tanks");
			//			GL11.glDisable(GL11.GL_CULL_FACE);
			//			ClientUtils.renderWavefrontWithIconUVs(modelobj, icon, "cage");
			//			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		else
		{
			boolean inventory = type==ItemRenderType.INVENTORY;
			for(GroupObject obj : modelobj.groupObjects)
			{
				if(obj.name=="cage")
					GL11.glDisable(GL11.GL_CULL_FACE);
				for(int pass=0; pass<sCase.getPasses(shader, item, obj.name); pass++)
				{
					IIcon ic = sCase.getReplacementIcon(shader, item, obj.name, pass);
					if(ic==null)
						ic=icon;
					int[] col = sCase.getRGBAColourModifier(shader, item, obj.name, pass);
					if(col==null||col.length<4)
						col= new int[]{255,255,255,255};

					sCase.modifyRender(shader, item, obj.name, pass, true, inventory);
					ClientUtils.tes().startDrawing(obj.glDrawingMode);
					ClientUtils.tes().setColorRGBA(col[0], col[1], col[2], col[3]);
					ClientUtils.tessellateWavefrontGroupObjectWithIconUVs(obj, ic);
					ClientUtils.tes().draw();
					sCase.modifyRender(shader, item, obj.name, pass, false, inventory);
				}
				if(obj.name=="cage")
					GL11.glEnable(GL11.GL_CULL_FACE);
			}
		}

		if(user instanceof EntityPlayer && ((EntityPlayer)user).getItemInUseCount()>0)
		{
			ItemStack ammo = ItemRailgun.findAmmo((EntityPlayer)user);

			int[][] colourMap = {{0x777777,0xa4a4a4}};
			if(ammo!=null)
			{
				RailgunHandler.RailgunProjectileProperties prop = RailgunHandler.getProjectileProperties(ammo);
				colourMap = prop!=null?prop.colourMap:colourMap;
			}
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);

			if(colourMap.length==1)
			{
				colourMap = new int[][]{colourMap[0],colourMap[0]};
			}
			float height = .1875f;
			float halfWidth = height/2;
			float length = 2;
			Tessellator tes = ClientUtils.tes();
			tes.startDrawingQuads();
			int colWidth = colourMap[0].length;
			for(int i=0; i<colourMap.length; i++)
				colWidth = Math.min(colWidth, colourMap[i].length);
			int colLength = colourMap.length;
			float widthStep = height/colWidth;
			float lengthStep = length/colLength;

			GL11.glTranslatef(.5f,.1f,0f);

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

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}

}
