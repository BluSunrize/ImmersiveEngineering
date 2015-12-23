package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.ItemRailgun;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
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

		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			float scale = .375f;
			GL11.glRotatef(42, 0, 1, 0);
			GL11.glTranslatef(-.5f,1.25f,.25f);
			if(ClientUtils.mc().thePlayer.getItemInUseCount()>0)
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
		ShaderCase sCase = (shader!=null && shader.getItem() instanceof IShaderItem)?((IShaderItem)shader.getItem()).getShaderCase(shader, item, "chemthrower"):null;

		if(sCase==null)
		{
			ClientUtils.renderWavefrontWithIconUVs(modelobj, icon, "grip","frame","barrel","capacitors","sled","wires");
			ClientUtils.renderWavefrontWithIconUVs(modelobj, icon, "upgrade_speed","upgrade_scope");
			
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

		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}

}
