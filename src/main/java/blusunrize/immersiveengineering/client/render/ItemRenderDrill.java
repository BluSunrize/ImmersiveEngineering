package blusunrize.immersiveengineering.client.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

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
		if(model==null)
			return;
		GL11.glPushMatrix();
		GL11.glEnable(3042);
		OpenGlHelper.glBlendFunc(770, 771, 0, 1);
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
				GL11.glRotatef(-135, 0, 0, 1);
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

		GL11.glColor4f(1, 1, 1, 1);
		String[] parts = {"drill_frame","drill_grip",null,null};
		if(upgrades.getBoolean("waterproof"))
			parts[2] = "upgrade_waterproof";
		if(upgrades.getInteger("speed")>0)
			parts[3] = "upgrade_speed";


		IIcon icon = item.getIconIndex();
		ItemStack shader = ((ItemDrill)item.getItem()).getShaderItem(item);
		ShaderCase sCase = (shader!=null && shader.getItem() instanceof IShaderItem)?((IShaderItem)shader.getItem()).getShaderCase(shader, item, "drill"):null;

		if(sCase==null)
			ClientUtils.renderWavefrontWithIconUVs(model, icon, parts);
		else
		{
			boolean inventory = type==ItemRenderType.INVENTORY;
			List<String> renderParts = Arrays.asList(parts);
			for(GroupObject obj : model.groupObjects)
				if(renderParts.contains(obj.name))
				{
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
				}
		}



		if(head!=null)
		{
			if(type==ItemRenderType.INVENTORY && !((ItemDrill)item.getItem()).canDrillBeUsed(item, ClientUtils.mc().thePlayer))
				angle = 0;

			IIcon headIcon = ((IDrillHead)head.getItem()).getDrillTexture(item, head);

			int col = head.getItem().getColorFromItemStack(head, 0);
			GL11.glColor3f((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f);

			GL11.glRotatef(-360*angle, 1,0,0);
			GL11.glTranslatef(upgrades.getInteger("damage")<=0?-.25f:0, 0, 0);
			ClientUtils.renderWavefrontWithIconUVs(model, headIcon, "drill_head");
			if(upgrades.getInteger("damage")>0)
				ClientUtils.renderWavefrontWithIconUVs(model, headIcon, "upgrade_damage0");
			GL11.glRotatef(360*angle, 1,0,0);

			if(upgrades.getInteger("damage")>1)
			{
				GL11.glTranslatef(.441f,0,0);
				GL11.glRotatef(-720*angle, 0,1,0);
				ClientUtils.renderWavefrontWithIconUVs(model, headIcon, "upgrade_damage1");
				GL11.glRotatef(1440*angle, 0,1,0);
				ClientUtils.renderWavefrontWithIconUVs(model, headIcon, "upgrade_damage2");
				GL11.glRotatef(-720*angle, 0,1,0);

				GL11.glRotatef(-720*angle, 0,0,1);
				if(upgrades.getInteger("damage")>2)
					ClientUtils.renderWavefrontWithIconUVs(model, headIcon, "upgrade_damage3");
				GL11.glRotatef(1440*angle, 0,0,1);
				if(upgrades.getInteger("damage")>2)
					ClientUtils.renderWavefrontWithIconUVs(model, headIcon, "upgrade_damage4");
				GL11.glRotatef(-720*angle, 0,0,1);
				GL11.glTranslatef(-.441f,0,0);
			}

			GL11.glColor3f(1,1,1);
		}

			GL11.glDisable(3042);
		GL11.glPopMatrix();
	}

}
