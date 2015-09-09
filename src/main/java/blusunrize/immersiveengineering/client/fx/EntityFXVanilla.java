package blusunrize.immersiveengineering.client.fx;

import net.minecraft.world.World;

public class EntityFXVanilla extends EntityFXIEBase
{
	String tag = "";

	public EntityFXVanilla(World world, String tag, double x, double y, double z, double mx, double my, double mz)
	{
		super(world, x, y, z, mx, my, mz);
		this.tag = tag;

		switch(tag)
		{
		case "smoke":
			this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
			break;    
		case "largeSmoke":
			this.particleMaxAge = (int)((8.0D / (Math.random() * 0.8D + 0.2D))*2.5f);
			break;    
		case "explode":
			this.particleMaxAge = (int)(16.0D / ((double)this.rand.nextFloat() * 0.8D + 0.2D)) + 2;
			break;
		case "heart":
			this.particleMaxAge = 16;
			this.setParticleTextureIndex(80);
		case "angryVillager":
			this.particleMaxAge = 16;
			this.setParticleTextureIndex(81);
			break;
		default:
			break;
		}
	}

	@Override
	public void onUpdate()
	{
		switch(tag)
		{
		case "smoke":
		case "largeSmoke":
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			if (this.particleAge++ >= this.particleMaxAge)
				this.setDead();
			this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
			this.motionY += 0.004D;
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			if (this.posY == this.prevPosY)
			{
				this.motionX *= 1.1D;
				this.motionZ *= 1.1D;
			}
			this.motionX *= 0.9599999785423279D;
			this.motionY *= 0.9599999785423279D;
			this.motionZ *= 0.9599999785423279D;
			if (this.onGround)
			{
				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
			}
			return;
		case "explode":
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			if (this.particleAge++ >= this.particleMaxAge)
				this.setDead();
			this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
			this.motionY += 0.004D;
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.8999999761581421D;
			this.motionY *= 0.8999999761581421D;
			this.motionZ *= 0.8999999761581421D;
			if (this.onGround)
			{
				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
			}
			return;
		case "heart":
		case "angryVillager":
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			if (this.particleAge++ >= this.particleMaxAge)
				this.setDead();
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			if (this.posY == this.prevPosY)
			{
				this.motionX *= 1.1D;
				this.motionZ *= 1.1D;
			}
			this.motionX *= 0.8600000143051147D;
			this.motionY *= 0.8600000143051147D;
			this.motionZ *= 0.8600000143051147D;
			if (this.onGround)
			{
				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
			}
			return;
		default:
			super.onUpdate();
			return;
		}
	}

	@Override
	public String getParticleName()
	{
		return tag;
	}

}