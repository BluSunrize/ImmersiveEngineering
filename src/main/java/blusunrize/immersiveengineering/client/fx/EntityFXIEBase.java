package blusunrize.immersiveengineering.client.fx;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.google.common.collect.ArrayListMultimap;

public abstract class EntityFXIEBase extends EntityFX
{
	public static ArrayListMultimap<String, EntityFXIEBase> queuedRenders = ArrayListMultimap.create();
	public static ArrayListMultimap<String, EntityFXIEBase> queuedDepthIgnoringRenders = ArrayListMultimap.create();

	protected float f2;
	protected float f3;
	protected float f4;
	protected float f5;
	protected float f6;
	protected float f7;
	protected static ResourceLocation vanillaParticleTextures = new ResourceLocation("textures/particle/particles.png");
	
	public EntityFXIEBase(World world, double x,double y,double z, double mx,double my,double mz)
	{
		super(world, x,y,z, mx,my,mz);
		this.particleMaxAge = 16;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		this.setParticleTextureIndex(world.rand.nextInt(3));
	}

	@Override
	public void renderParticle(Tessellator tes, float f2, float f3, float f4, float f5, float f6, float f7)
	{
		this.f2=f2;
		this.f3=f3;
		this.f4=f4;
		this.f5=f5;
		this.f6=f6;
		this.f7=f7;
		this.addToQueue(false);
	}

	public void addToQueue(boolean ignoreDepth)
	{
		if(!ignoreDepth)
			queuedRenders.put(getParticleName(), this);
		else
			queuedDepthIgnoringRenders.put(getParticleName(), this);
	}

	public abstract String getParticleName();
	public ResourceLocation getParticleTexture()
	{
		return vanillaParticleTextures;
	}
	public void tessellateFromQueue(Tessellator tessellator)
	{
		float f6 = (float)this.particleTextureIndexX / 16.0F;
        float f7 = f6 + 0.0624375F;
        float f8 = (float)this.particleTextureIndexY / 16.0F;
        float f9 = f8 + 0.0624375F;
        float f10 = 0.1F * this.particleScale;

        if (this.particleIcon != null)
        {
            f6 = this.particleIcon.getMinU();
            f7 = this.particleIcon.getMaxU();
            f8 = this.particleIcon.getMinV();
            f9 = this.particleIcon.getMaxV();
        }

        float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f2 - interpPosX);
        float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f2 - interpPosY);
        float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f2 - interpPosZ);
        tessellator.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
        tessellator.addVertexWithUV((double)(f11 - f3 * f10 - f6 * f10), (double)(f12 - f4 * f10), (double)(f13 - f5 * f10 - f7 * f10), (double)f7, (double)f9);
        tessellator.addVertexWithUV((double)(f11 - f3 * f10 + f6 * f10), (double)(f12 + f4 * f10), (double)(f13 - f5 * f10 + f7 * f10), (double)f7, (double)f8);
        tessellator.addVertexWithUV((double)(f11 + f3 * f10 + f6 * f10), (double)(f12 + f4 * f10), (double)(f13 + f5 * f10 + f7 * f10), (double)f6, (double)f8);
        tessellator.addVertexWithUV((double)(f11 + f3 * f10 - f6 * f10), (double)(f12 - f4 * f10), (double)(f13 + f5 * f10 - f7 * f10), (double)f6, (double)f9);
   
	}
}