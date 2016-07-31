package blusunrize.immersiveengineering.client.fx;

import com.google.common.collect.ArrayListMultimap;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class EntityFXIEBase extends Particle
{
	public static ArrayListMultimap<String, EntityFXIEBase> queuedRenders = ArrayListMultimap.create();
	public static ArrayListMultimap<String, EntityFXIEBase> queuedDepthIgnoringRenders = ArrayListMultimap.create();

	protected float partialTicks;
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
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float f3, float f4, float f5, float f6, float f7)
	{
		this.partialTicks=partialTicks;
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
	public void tessellateFromQueue(VertexBuffer worldRendererIn)
	{
		float f6 = (float)this.particleTextureIndexX / 16.0F;
		float f7 = f6 + 0.0624375F;
		float f8 = (float)this.particleTextureIndexY / 16.0F;
		float f9 = f8 + 0.0624375F;
		float f10 = 0.1F * this.particleScale;

		if (this.particleTexture != null)
		{
			f6 = this.particleTexture.getMinU();
			f7 = this.particleTexture.getMaxU();
			f8 = this.particleTexture.getMinV();
			f9 = this.particleTexture.getMaxV();
		}

		float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

		int i = this.getBrightnessForRender(partialTicks);
		int j = i >> 16 & 65535;
		int k = i & 65535;
		worldRendererIn.pos((f11 - f3 * f10 - f6 * f10), (f12 - f4 * f10), (f13 - f5 * f10 - f7 * f10)).tex(f7,f9).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		worldRendererIn.pos((f11 - f3 * f10 + f6 * f10), (f12 + f4 * f10), (f13 - f5 * f10 + f7 * f10)).tex(f7,f8).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		worldRendererIn.pos((f11 + f3 * f10 + f6 * f10), (f12 + f4 * f10), (f13 + f5 * f10 + f7 * f10)).tex(f6,f8).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		worldRendererIn.pos((f11 + f3 * f10 - f6 * f10), (f12 - f4 * f10), (f13 + f5 * f10 - f7 * f10)).tex(f6,f9).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
	}
}