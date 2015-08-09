package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelRevolverVariable;
import blusunrize.immersiveengineering.common.items.ItemRevolver;

public class ItemRenderRevolver implements IItemRenderer
{
	static ModelRevolverVariable model = new ModelRevolverVariable();
	static WavefrontObject modelobj = ClientUtils.getModel("immersiveengineering:models/revolver.obj");

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return item.getItemDamage()!=1;
	}
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return item.getItemDamage()!=1;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();
		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
			GL11.glRotatef(-135, 0, 1, 0);
		else
			GL11.glRotatef(45, 0, 1, 0);

		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glTranslatef(-1.15f,.5f,-1f);
			GL11.glScalef(.625f, .75f, .75f);
		}
		else if(type==ItemRenderType.EQUIPPED)
		{
			GL11.glRotatef(-90, 0, 1, 0);
			GL11.glRotatef(65, 0, 0, 1);
			GL11.glTranslatef(.3f,-.7f,0f);
			GL11.glScalef(.375f,.375f,.375f);
		}
		else if(type==ItemRenderType.ENTITY)
		{
			float scale = .25f;
			GL11.glRotatef(45, 0, 1, 0);
			if(RenderItem.renderInFrame)
			{
				GL11.glRotatef(20, 0, 0, 1);
				scale = .16f;
				GL11.glTranslatef(.05f,.1f,0);
			}
			GL11.glScalef(scale,scale, scale);
		}
		else if(type==ItemRenderType.INVENTORY)
		{
			GL11.glRotatef(0, 0, 1, 0);
			GL11.glRotatef(-40, 0, 0, 1);
			GL11.glTranslatef(0,.2f,.4f);
			GL11.glScalef(.3f,.3f,.3f);
		}

		ClientUtils.mc().renderEngine.bindTexture(ClientProxy.revolverTextureResource);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(3042);
		OpenGlHelper.glBlendFunc(770, 771, 0, 1);

		IIcon ic = ((ItemRevolver)item.getItem()).getRevolverIcon(item);
		String[] parts = ((ItemRevolver)item.getItem()).compileRender(item);
		ClientUtils.renderWavefrontWithIconUVs(modelobj, ic, parts);

		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}

}
