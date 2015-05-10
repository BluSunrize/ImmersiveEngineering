package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelRevolverVariable;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;

public class ItemRenderRevolver implements IItemRenderer
{
	static ModelRevolverVariable model = new ModelRevolverVariable();

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return item.getItemDamage()!=2;
	}
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return item.getItemDamage()!=2;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();
		GL11.glRotatef(180, 0, 0, 1);
		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
			GL11.glRotatef(45, 0, 1, 0);
		else
			GL11.glRotatef(-45, 0, 1, 0);

		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glTranslatef(-1.25f,-.75f,-.75f);
			GL11.glScalef(1, 1, .75f);
		}
		else if(type==ItemRenderType.EQUIPPED)
		{
			GL11.glRotatef(65, 1, 0, 0);
			GL11.glTranslatef(0,.5625f,.7f);
			GL11.glScalef(.5f,.5f,.5f);
		}
		else if(type==ItemRenderType.ENTITY)
		{
			if(!RenderItem.renderInFrame)
				GL11.glTranslatef(0,-.2f,0);
			GL11.glRotatef(45, 0, 1, 0);
			GL11.glRotatef(-45, 1, 0, 0);
			GL11.glTranslatef(0,-.1f,0.2f);
			GL11.glScalef(.25f,.25f,.25f);
		}
		else if(type==ItemRenderType.INVENTORY)
		{
			GL11.glRotatef(-90, 0, 1, 0);
			GL11.glRotatef(-45, 1, 0, 0);
			GL11.glTranslatef(0,-.1f,.3f);
			GL11.glScalef(.4375f,.4375f,.4375f);
		}
		
		ClientUtils.bindTexture(((ItemRevolver)item.getItem()).getRevolverTexture(item));
		GL11.glEnable(3042);
		boolean b = (ItemNBTHelper.getInt(item, "upgrade")&1)==1;
		model.Cylinder.isHidden = b;
		model.CylinderAuto.isHidden = !b;
		model.CylinderAuto_1.isHidden = !b;
		model.CylinderAuto_2.isHidden = !b;
		
		model.render(null, 0, 0, 0, 0, 0, .0625f);
		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}

}
