/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SparksParticle extends TextureSheetParticle
{
	public SparksParticle(ClientLevel world, double x, double y, double z, double mx, double my, double mz, SpriteSet sprite)
	{
		super(world, x, y, z, mx, my, mz);
		this.setLifetime(16);
		this.x = x;
		this.y = y;
		this.z = z;
		this.xd = mx;
		this.yd = my;
		this.zd = mz;
		pickSprite(sprite);
		//TODO this.setParticleTextureIndex(ApiUtils.RANDOM.nextInt(3));
	}

	@Override
	public int getLightColor(float p_70070_1_)
	{
		return 240<<16|240;
	}

	@Override
	public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks)
	{
		int particleAge = age;
		this.setColor(1, .2f+(16-particleAge)/16f, particleAge > 4?0: (4-particleAge)/4f);
		super.render(buffer, renderInfo, partialTicks);
	}

	@Nonnull
	@Override
	public ParticleRenderType getRenderType()
	{
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}

	public static class Factory implements ParticleProvider<SimpleParticleType>
	{
		private final SpriteSet sprite;

		public Factory(SpriteSet sprite)
		{
			this.sprite = sprite;
		}

		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new SparksParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
		}
	}
}