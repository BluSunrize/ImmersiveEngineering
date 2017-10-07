/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author BluSunrize - 21.02.2017
 */
@SideOnly(Side.CLIENT)
public class ParticleIEBubble extends Particle
{
	public ParticleIEBubble(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
	{
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.setParticleTextureIndex(32);
		this.setSize(0.02F, 0.02F);
		this.particleScale *= this.rand.nextFloat()*0.6F+0.2F;
		this.motionX = xSpeedIn*0.20000000298023224D+(Math.random()*2.0D-1.0D)*0.019999999552965164D;
		this.motionY = ySpeedIn*0.20000000298023224D+(Math.random()*2.0D-1.0D)*0.019999999552965164D;
		this.motionZ = zSpeedIn*0.20000000298023224D+(Math.random()*2.0D-1.0D)*0.019999999552965164D;
		this.particleMaxAge = (int)(8.0D/(Math.random()*0.8D+0.2D));
	}

	@Override
	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY += 0.002D;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.8500000238418579D;
		this.motionY *= 0.8500000238418579D;
		this.motionZ *= 0.8500000238418579D;

		if(this.particleMaxAge-- <= 0)
			this.setExpired();
	}
}