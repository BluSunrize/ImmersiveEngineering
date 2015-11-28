package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.obj.WavefrontObject;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.client.ClientUtils;
import cofh.api.energy.IEnergyReceiver;

public class ItemRenderVoltmeter implements IItemRenderer
{
	WavefrontObject model = ClientUtils.getModel("immersiveengineering:models/voltmeter.obj");

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return item.getItemDamage()==2;
	}
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return item.getItemDamage()==2;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glPushMatrix();
		ClientUtils.bindAtlas(1);
		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
			GL11.glRotatef(-45, 0, 1, 0);
		else
			GL11.glRotatef(45, 0, 1, 0);

		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glTranslatef(-.6875f,1.2f,.7f);
			GL11.glScalef(1.5f,1.5f,1.5f);
		}
		else if(type==ItemRenderType.EQUIPPED)
		{
			GL11.glRotatef(-90, 0, 1, 0);
			GL11.glRotatef(65, 0, 0, 1);
			GL11.glRotatef(45, 0, 1, 0);
			GL11.glTranslatef(.1f,-1.2f,.6f);
			float scale = 1.5f;
			GL11.glScalef(scale,scale,scale);
		}
		else if(type==ItemRenderType.ENTITY)
		{
			float scale = 1;//.25f;
			GL11.glRotatef(45, 0, 1, 0);
			GL11.glScalef(scale,scale, scale);
		}
		else if(type==ItemRenderType.INVENTORY)
		{
			float scale = 1.75f;
			GL11.glScalef(scale,scale,scale);
			GL11.glTranslatef(0,-.1f,0);
		}


		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(3042);
		OpenGlHelper.glBlendFunc(770, 771, 0, 1);
		ClientUtils.renderWavefrontWithIconUVs(model, item.getIconIndex(), "base");

		float angle = 0;
		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			MovingObjectPosition mop = ClientUtils.mc().objectMouseOver;
			TileEntity tileEntity = ClientUtils.mc().thePlayer.worldObj.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
			if(tileEntity instanceof IEnergyReceiver)
			{
				ForgeDirection fd = ForgeDirection.getOrientation(mop.sideHit);
				int maxStorage = ((IEnergyReceiver)tileEntity).getMaxEnergyStored(fd);
				int storage = ((IEnergyReceiver)tileEntity).getEnergyStored(fd);
				if(maxStorage>0)
					angle = storage/(float)maxStorage*60;
			}
		}
		GL11.glRotatef(-angle, 0.0F, 0.0F, 1.0F);
		ClientUtils.renderWavefrontWithIconUVs(model, item.getIconIndex(), "pointer");
		GL11.glRotatef(angle, 0.0F, 0.0F, 1.0F);

		if(type==ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glScalef(.75f,.75f,.75f);
			float f12 = 0.8F;
			EntityClientPlayerMP player = ClientUtils.mc().thePlayer;
			Render render;
			RenderPlayer renderplayer;

			GL11.glTranslatef(0.0F, 0.0F * f12 - (1.0F - 1.0f) * 1.2F + 0.04F, -0.9F * f12);
			GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			ClientUtils.bindTexture(player.getLocationSkin().getResourceDomain()+":"+player.getLocationSkin().getResourcePath());

			for(int arm=0; arm<2; ++arm)
			{
				int l = arm * 2 - 1;
				GL11.glPushMatrix();
				GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(1.4F, .01F, 0.25F * l);
				GL11.glRotatef(-7 * l, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(100.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(90 * l, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-15, 0.0F, 0.0F, 1.0F);
				render = RenderManager.instance.getEntityRenderObject(player);
				renderplayer = (RenderPlayer)render;
				renderplayer.renderFirstPersonArm(player);
				GL11.glPopMatrix();
			}
		}


		GL11.glDisable(3042);
		GL11.glPopMatrix();

	}

}
