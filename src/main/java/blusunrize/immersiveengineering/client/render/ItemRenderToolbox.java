package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.WavefrontObject;

public class ItemRenderToolbox implements IItemRenderer
{
	static WavefrontObject modelobj = ClientUtils.getModel("immersiveengineering:models/toolbox.obj");

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
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glTranslatef(-.5f,1f,-.5f);
		}
		else if(type==ItemRenderType.EQUIPPED)
		{
			GL11.glScalef(1.25f,1.25f,1.25f);
			GL11.glRotatef(45, 0, 1, 0);
			GL11.glRotatef(20, 1, 0, 0);
			GL11.glTranslatef(0f,.5f,1f);
		}
		else if(type==ItemRenderType.ENTITY)
		{
			float scale = .75f;
			if(RenderItem.renderInFrame)
			{
				GL11.glRotatef(90, 1, 0, 0);
				GL11.glTranslatef(0f,.25f,0f);
				scale = .5f;
			}
			else
				GL11.glTranslatef(0,.5f,0);
			GL11.glScalef(scale,scale, scale);
		}
		else if(type==ItemRenderType.INVENTORY)
		{
			GL11.glRotatef(-80, 0, 1, 0);
			GL11.glTranslatef(-.5f,0,.375f);
		}

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(3042);
		OpenGlHelper.glBlendFunc(770, 771, 0, 1);

		IIcon icon = item.getIconIndex();
		ClientUtils.renderWavefrontWithIconUVs(modelobj, icon);

		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}

}
