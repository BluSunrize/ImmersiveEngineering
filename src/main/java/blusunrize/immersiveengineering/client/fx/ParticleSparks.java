/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleSparks extends Particle
{
	public ParticleSparks(World world, double x, double y, double z, double mx, double my, double mz)
	{
		super(world, x, y, z, mx, my, mz);
		this.particleMaxAge = 16;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		this.setParticleTextureIndex(Utils.RAND.nextInt(3));
	}

	@Override
	public int getBrightnessForRender(float p_70070_1_)
	{
		return 240<<16|240;
	}


	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		this.setRBGColorF(1, .2f+(16-particleAge)/16f, this.particleAge > 4?0: (4-particleAge)/4f);
		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}
}