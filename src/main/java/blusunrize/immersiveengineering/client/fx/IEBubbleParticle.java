/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

//A version of the vanilla bubble particle that can exist outside of water blocks
@OnlyIn(Dist.CLIENT)
public class IEBubbleParticle extends SpriteTexturedParticle
{
	public IEBubbleParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
	{
		super(worldIn, xCoordIn, yCoordIn, zCoordIn);
		this.setSize(0.02F, 0.02F);
		this.particleScale *= this.rand.nextFloat()*0.6F+0.2F;
		this.motionX = xSpeedIn*(double)0.2F+(Math.random()*2.0D-1.0D)*(double)0.02F;
		this.motionY = ySpeedIn*(double)0.2F+(Math.random()*2.0D-1.0D)*(double)0.02F;
		this.motionZ = zSpeedIn*(double)0.2F+(Math.random()*2.0D-1.0D)*(double)0.02F;
		this.maxAge = (int)(8.0D/(Math.random()*0.8D+0.2D));
	}

	public void tick()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if(this.maxAge-- <= 0)
		{
			this.setExpired();
		}
		else
		{
			this.motionY += 0.002D;
			this.move(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.85F;
			this.motionY *= 0.85F;
			this.motionZ *= 0.85F;
		}
	}

	@Nonnull
	public IParticleRenderType getRenderType()
	{
		return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<BasicParticleType>
	{
		private final IAnimatedSprite texture;

		public Factory(IAnimatedSprite p_i50227_1_)
		{
			this.texture = p_i50227_1_;
		}

		public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			IEBubbleParticle bubbleparticle = new IEBubbleParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
			bubbleparticle.selectSpriteRandomly(this.texture);
			return bubbleparticle;
		}
	}
}