/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SparksParticle extends SpriteTexturedParticle
{
	public SparksParticle(World world, double x, double y, double z, double mx, double my, double mz, IAnimatedSprite sprite)
	{
		super(world, x, y, z, mx, my, mz);
		this.setMaxAge(16);
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		selectSpriteRandomly(sprite);
		//TODO this.setParticleTextureIndex(Utils.RAND.nextInt(3));
	}

	@Override
	public int getBrightnessForRender(float p_70070_1_)
	{
		return 240<<16|240;
	}

	@Override
	public void renderParticle(BufferBuilder bufferBuilder, ActiveRenderInfo activeRenderInfo, float v, float v1, float v2, float v3, float v4, float v5)
	{
		int particleAge = age;
		this.setColor(1, .2f+(16-particleAge)/16f, particleAge > 4?0: (4-particleAge)/4f);
		super.renderParticle(bufferBuilder, activeRenderInfo, v, v1, v2, v3, v4, v5);
	}

	@Nonnull
	@Override
	public IParticleRenderType getRenderType()
	{
		return IParticleRenderType.PARTICLE_SHEET_LIT;
	}

	public static class Factory implements IParticleFactory<BasicParticleType>
	{
		private final IAnimatedSprite sprite;

		public Factory(IAnimatedSprite sprite)
		{
			this.sprite = sprite;
		}

		@Nullable
		@Override
		public Particle makeParticle(BasicParticleType typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new SparksParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
		}
	}
}