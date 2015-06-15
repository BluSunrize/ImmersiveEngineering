package blusunrize.immersiveengineering.client.fx;

import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityFXItemParts extends EntityFX
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
			if(item.getItem() instanceof ItemBlock)
				this.particleIcon = Block.getBlockFromItem(item.getItem()).getIcon(0, item.getItemDamage());
			else
				this.particleIcon = item.getItem().getIcon(item, 0);
		}
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
	}
	@Override
	public int getFXLayer()
	{
		if(item!=null && item.getItem()!=null)
			return item.getItemSpriteNumber()+1;
		return 0;
	}

	@Override
	public void renderParticle(Tessellator p_70539_1_, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_)
	{
		if(item!=null && item.getItem()!=null && this.particleIcon!=null)
		{
			float f10 = 0.025F * this.particleScale;

			float f6 = this.particleIcon.getInterpolatedU((part%4)*4);
			float f7 = this.particleIcon.getInterpolatedU((part%4+1)*4);
			float f8 = this.particleIcon.getInterpolatedV((part/4)*4);
			float f9 = this.particleIcon.getInterpolatedV((part/4+1)*4);

			float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)p_70539_2_ - interpPosX);
			float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)p_70539_2_ - interpPosY);
			float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)p_70539_2_ - interpPosZ);
			p_70539_1_.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
			p_70539_1_.addVertexWithUV((double)(f11 - p_70539_3_ * f10 - p_70539_6_ * f10), (double)(f12 - p_70539_4_ * f10), (double)(f13 - p_70539_5_ * f10 - p_70539_7_ * f10), (double)f7, (double)f9);
			p_70539_1_.addVertexWithUV((double)(f11 - p_70539_3_ * f10 + p_70539_6_ * f10), (double)(f12 + p_70539_4_ * f10), (double)(f13 - p_70539_5_ * f10 + p_70539_7_ * f10), (double)f7, (double)f8);
			p_70539_1_.addVertexWithUV((double)(f11 + p_70539_3_ * f10 + p_70539_6_ * f10), (double)(f12 + p_70539_4_ * f10), (double)(f13 + p_70539_5_ * f10 + p_70539_7_ * f10), (double)f6, (double)f8);
			p_70539_1_.addVertexWithUV((double)(f11 + p_70539_3_ * f10 - p_70539_6_ * f10), (double)(f12 - p_70539_4_ * f10), (double)(f13 + p_70539_5_ * f10 - p_70539_7_ * f10), (double)f6, (double)f9);
		}
	}

}