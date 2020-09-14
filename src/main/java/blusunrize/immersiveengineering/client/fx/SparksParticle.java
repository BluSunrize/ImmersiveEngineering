/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SparksParticle extends SpriteTexturedParticle
{
	public SparksParticle(ClientWorld world, double x, double y, double z, double mx, double my, double mz, IAnimatedSprite sprite)
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
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks)
	{
		int particleAge = age;
		this.setColor(1, .2f+(16-particleAge)/16f, particleAge > 4?0: (4-particleAge)/4f);
		super.renderParticle(buffer, renderInfo, partialTicks);
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
		public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new SparksParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
		}
	}
}