package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.ItemChemthrower;

public class ItemRenderChemthrower implements IItemRenderer
{
	static WavefrontObject modelobj = ClientUtils.getModel("immersiveengineering:models/chemthrower.obj");

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

		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			if(ClientUtils.mc().thePlayer.getItemInUseCount()>0)
			{
				GL11.glRotatef(65, 0, 1, 0);
				GL11.glRotatef(15, 0, 0, 1);
				GL11.glRotatef(-10, 1, 0, 0);
				GL11.glTranslatef(-.25f,.625f,1f);
				GL11.glScalef(.5f,.5f,.625f);
			}
			else
			{
				GL11.glRotatef(52, 0, 1, 0);
				GL11.glTranslatef(-.5f,1.125f,.25f);
				GL11.glScalef(.5f,.5f,.625f);
			}
		}
		else if(type==ItemRenderType.EQUIPPED)
		{
			if(ClientUtils.mc().thePlayer.getItemInUseCount()>0)
			{
				GL11.glRotatef(135, 0, 1, 0);
				GL11.glRotatef(-45, 0, 0, 1);
				GL11.glRotatef(20, 0, 1, 0);
				GL11.glTranslatef(-.2f,-.5f,-.35f);
			}
			else
			{
				GL11.glRotatef(195, 0, 1, 0);
				GL11.glTranslatef(.5f,.5f,-1f);
			}
		}
		else if(type==ItemRenderType.ENTITY)
		{
			float scale = .5f;
			GL11.glRotatef(45, 0, 1, 0);
			if(RenderItem.renderInFrame)
			{
				GL11.glRotatef(-135, 0, 1, 0);
				GL11.glRotatef(20, 0, 0, 1);
				scale = .25f;
				GL11.glTranslatef(-.15f,.1f,-.05f);
			}
			else
				GL11.glRotatef(45, 0, 1, 0);
			GL11.glScalef(scale,scale, scale);
		}
		else if(type==ItemRenderType.INVENTORY)
		{
			GL11.glRotatef(-80, 0, 1, 0);
			GL11.glTranslatef(-.625f,-.1f,.125f);
			GL11.glScalef(.625f,.625f,.625f);
		}

		//		ClientUtils.mc().renderEngine.bindTexture(ClientProxy.revolverTextureResource);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(3042);
		OpenGlHelper.glBlendFunc(770, 771, 0, 1);

		IIcon icon = item.getIconIndex();
		ItemStack shader = ((ItemChemthrower)item.getItem()).getShaderItem(item);
		ShaderCase sCase = (shader!=null && shader.getItem() instanceof IShaderItem)?((IShaderItem)shader.getItem()).getShaderCase(shader, item, "chemthrower"):null;

		if(sCase==null)
		{
			ClientUtils.renderWavefrontWithIconUVs(modelobj, icon, "base","grip","tank","tanks");
			GL11.glDisable(GL11.GL_CULL_FACE);
			ClientUtils.renderWavefrontWithIconUVs(modelobj, icon, "cage");
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		else
		{
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

					sCase.modifyRender(shader, item, obj.name, pass, true);
					ClientUtils.tes().startDrawing(obj.glDrawingMode);
					ClientUtils.tes().setColorRGBA(col[0], col[1], col[2], col[3]);
					ClientUtils.tessellateWavefrontGroupObjectWithIconUVs(obj, ic);
					ClientUtils.tes().draw();
					sCase.modifyRender(shader, item, obj.name, pass, false);
				}
				if(obj.name=="cage")
					GL11.glEnable(GL11.GL_CULL_FACE);
			}
		}

		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}

}
