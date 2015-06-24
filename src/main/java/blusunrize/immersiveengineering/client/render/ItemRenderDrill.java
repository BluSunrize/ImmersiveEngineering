package blusunrize.immersiveengineering.client.render;

import java.util.HashMap;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IDrillHead;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class ItemRenderDrill implements IItemRenderer
{
	static WavefrontObject model = ClientUtils.getModel("immersiveengineering:models/drill_diesel.obj");

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
//		if(GuiScreen.isShiftKeyDown())
//			try{
//				model = ClientUtils.getModel("immersiveengineering:models/drill_diesel.obj");
//			}catch(Exception e)
//			{
//				e.printStackTrace();
//			}
		if(model==null)
			return;
		GL11.glPushMatrix();
		if(type!=ItemRenderType.EQUIPPED_FIRST_PERSON)
			GL11.glRotatef(-45, 0, 1, 0);

		float ticks = 60;
		float angle = ClientUtils.mc().thePlayer.ticksExisted%ticks/ticks;
		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glRotatef(65, 0, 1, 0);
			GL11.glTranslatef(-.35f,1,.2f);
			EntityLivingBase user = null;
			try{
				user = (EntityLivingBase) data[1];
			}catch(Exception e){}
			if(user!=null)
			{
				if(ItemDrill.animationTimer==null)
					ItemDrill.animationTimer = new HashMap<String,Integer>();
				try{
					if(ItemDrill.animationTimer.containsKey(user.getCommandSenderName()))
					{
						Integer timer = ItemDrill.animationTimer.get(user.getCommandSenderName());

						float push = (20-(timer-20))/20f;
						GL11.glTranslatef(push*.25f,0,0);

						timer--;
						if(timer<=0)
							ItemDrill.animationTimer.remove(user.getCommandSenderName());
						else
							ItemDrill.animationTimer.put(user.getCommandSenderName(), timer);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(type==ItemRenderType.EQUIPPED)
		{
			GL11.glRotatef(-110, 0, 0, 1);
			GL11.glRotatef(180, 1, 0, 0);
			GL11.glRotatef(5, 0, 1, 0);
			GL11.glTranslatef(-.125f,-1f,-.3f);
			GL11.glScalef(1.5f,1.5f,1.5f);
		}
		else if(type==ItemRenderType.ENTITY)
		{
			if(RenderItem.renderInFrame)
			{
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glRotatef(45, 0, 1, 0);
				GL11.glRotatef(45, 0, 0, 1);
				GL11.glTranslatef(-.0625f,.0625f,-.0625f);
				GL11.glScalef(.625f,.625f,.625f);
				angle = 0;
			}
			else
			{
				GL11.glRotatef(45, 0, 1, 0);
				GL11.glTranslatef(0,.25f,0f);
				GL11.glScalef(1.25f,1.25f,1.25f);
			}
		}
		else if(type==ItemRenderType.INVENTORY)
		{
			GL11.glRotatef(-40, 0, 1, 0);
			GL11.glTranslatef(0,.2f,-.2f);
			GL11.glScalef(1.2f,1.2f,1.2f);
		}

		ItemStack head = ((ItemDrill)item.getItem()).getHead(item);
		NBTTagCompound upgrades = ((ItemDrill)item.getItem()).getUpgrades(item);
		String texture = "immersiveengineering:textures/models/drill_diesel.png";
		ClientUtils.bindTexture(texture);
		GL11.glColor4f(1, 1, 1, 1);
		Tessellator tes = ClientUtils.tes();
		//		model.renderPart("drill_frame");
		tes.startDrawing(GL11.GL_QUADS);
		ClientUtils.renderWavefrontModelWithModifications(model, tes, new Matrix4(),new Matrix4(), false, "drill_frame");
		if(upgrades.getBoolean("waterproof"))
			ClientUtils.renderWavefrontModelWithModifications(model, tes, new Matrix4(),new Matrix4(), false, "upgrade_waterproof");
		if(upgrades.getInteger("speed")>0)
			ClientUtils.renderWavefrontModelWithModifications(model, tes, new Matrix4(),new Matrix4(), false, "upgrade_speed");
		//		model.renderPart("upgrade_waterproof");
		
		tes.draw();

		if(head!=null)
		{
			if(!((ItemDrill)item.getItem()).canDrillBeUsed(item, ClientUtils.mc().thePlayer))
				angle = 0;

			texture = ((IDrillHead)head.getItem()).getDrillTexture(item, head);
			ClientUtils.bindTexture(texture);

			int col = head.getItem().getColorFromItemStack(head, 0);
			GL11.glColor3f((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f);

			GL11.glRotatef(-360*angle, 1,0,0);
			tes.startDrawing(GL11.GL_TRIANGLES);
			ClientUtils.renderWavefrontModelWithModifications(model, tes, new Matrix4().translate(upgrades.getInteger("damage")<=0?-.25:0,0,0),new Matrix4(), false, "drill_head");
			if(upgrades.getInteger("damage")>0)
				ClientUtils.renderWavefrontModelWithModifications(model, tes, new Matrix4(),new Matrix4(), false, "upgrade_damage0");
			tes.draw();
			GL11.glRotatef(360*angle, 1,0,0);

			if(upgrades.getInteger("damage")>0)
			{
				GL11.glTranslatef(.441f,0,0);
				GL11.glRotatef(-720*angle, 0,1,0);
				model.renderPart("upgrade_damage1");
				GL11.glRotatef(1440*angle, 0,1,0);
				model.renderPart("upgrade_damage2");
				GL11.glRotatef(-720*angle, 0,1,0);

				GL11.glRotatef(-720*angle, 0,0,1);
				model.renderPart("upgrade_damage3");
				GL11.glRotatef(1440*angle, 0,0,1);
				model.renderPart("upgrade_damage4");
				GL11.glRotatef(-720*angle, 0,0,1);
				GL11.glTranslatef(-.441f,0,0);
			}

			//			ClientUtils.renderWavefrontModelWithModifications(model, tes, new Matrix4(),new Matrix4(), "upgrade_damage3");

			GL11.glColor3f(1,1,1);

			//			model.renderPart("upgrade_damage1");
			//			model.renderPart("upgrade_damage2");
			//			model.renderPart("upgrade_damage3");
			//			model.renderPart("upgrade_damage4");
		}

		if(type==ItemRenderType.INVENTORY)
			GL11.glDisable(3042);
		GL11.glPopMatrix();
	}

}
