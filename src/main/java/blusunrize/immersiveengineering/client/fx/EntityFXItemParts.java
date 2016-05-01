package blusunrize.immersiveengineering.client.fx;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFXItemParts extends EntityFXIEBase
{
	ItemStack item;
	int part = 0;
	public EntityFXItemParts(World world, ItemStack item, int part, double x,double y,double z, double mx,double my,double mz)
	{
		super(world, x,y,z, mx,my,mz);
		this.item = item;
		this.part = part;
		this.particleMaxAge = 16;
		if(item!=null && item.getItem()!=null)
		{
//			if(item.getItem() instanceof ItemBlock)
//				this.particleIcon = Block.getBlockFromItem(item.getItem()).getIcon(0, item.getItemDamage());
//			else
//				this.particleIcon = item.getItem().getIcon(item, 0);
		}
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
	}

	@Override
	public ResourceLocation getParticleTexture()
	{
		return null;//return TextureMap.locationItemsTexture;
	}
	@Override
	public String getParticleName()
	{
		return "itemParts";
	}
	@Override
	public void tessellateFromQueue(WorldRenderer worldRendererIn)
	{
		if(item!=null && item.getItem()!=null && this.particleIcon!=null)
		{
			float f10 = 0.025F * this.particleScale;

			float uMin = this.particleIcon.getInterpolatedU((part%4)*4);
			float uMax = this.particleIcon.getInterpolatedU((part%4+1)*4);
			float vMin = this.particleIcon.getInterpolatedV((part/4)*4);
			float vMax = this.particleIcon.getInterpolatedV((part/4+1)*4);

			float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
			float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
			float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
			
			
			int i = this.getBrightnessForRender(partialTicks);
			int j = i >> 16 & 65535;
			int k = i & 65535;
			worldRendererIn.pos((f11 - f3 * f10 - f6 * f10), (f12 - f4 * f10), (f13 - f5 * f10 - f7 * f10)).tex(uMax, vMax).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			worldRendererIn.pos((f11 - f3 * f10 + f6 * f10), (f12 + f4 * f10), (f13 - f5 * f10 + f7 * f10)).tex(uMax, vMin).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			worldRendererIn.pos((f11 + f3 * f10 + f6 * f10), (f12 + f4 * f10), (f13 + f5 * f10 + f7 * f10)).tex(uMin, vMin).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			worldRendererIn.pos((f11 + f3 * f10 - f6 * f10), (f12 - f4 * f10), (f13 + f5 * f10 - f7 * f10)).tex(uMin, vMax).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		}
	}
}