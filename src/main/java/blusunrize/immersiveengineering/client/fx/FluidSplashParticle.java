/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * @author BluSunrize - 21.02.2017
 */
@OnlyIn(Dist.CLIENT)
public class FluidSplashParticle extends SpriteTexturedParticle
{
	public FluidSplashParticle(Fluid fluid, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
							   double xSpeedIn, double ySpeedIn, double zSpeedIn)
	{
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

		this.motionX *= 0.3D;
		this.motionY = Math.random()*0.2D+0.1D;
		this.motionZ *= 0.3D;
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.setSize(0.01F, 0.01F);
		this.particleGravity = 0.06F;
		this.maxAge = (int)(8.0D/(Math.random()*0.8D+0.2D));
		this.particleScale = .375f;
		this.setFluidTexture(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME));
	}

	@Override
	public void tick()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= (double)this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98;
		this.motionY *= 0.98;
		this.motionZ *= 0.98;

		if(this.maxAge-- <= 0)
			this.setExpired();

		if(this.onGround)
		{
			if(Math.random() < 0.5D)
				this.setExpired();
			this.motionX *= 0.7;
			this.motionZ *= 0.7;
		}

		BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
		BlockState iblockstate = this.world.getBlockState(blockpos);
		Material material = iblockstate.getMaterial();

		if(material.isLiquid()||material.isSolid())
		{
			double d0;
			/*TODO if(iblockstate.getBlock() instanceof BlockLiquid)
				d0 = (double)(1.0F-BlockLiquid.getLiquidHeightPercent(iblockstate.getValue(BlockLiquid.LEVEL).intValue()));
			else*/
			d0 = iblockstate.getShape(this.world, blockpos).getEnd(Axis.Y);
			double d1 = (double)MathHelper.floor(this.posY)+d0;
			if(this.posY < d1)
				this.setExpired();
		}
	}

	public void setFluidTexture(FluidStack fluid)
	{
		TextureAtlasSprite sprite = ClientUtils.getSprite(fluid.getFluid().getAttributes().getStillTexture(fluid));
		if(sprite==null)
			sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(MissingTextureSprite.getLocation());
		setSprite(sprite);
		int argb = fluid.getFluid().getAttributes().getColor(fluid);
		this.particleAlpha = ((argb >> 24)&255)/255f;
		this.particleRed = ((argb >> 16)&255)/255f;
		this.particleRed = ((argb >> 8&255))/255f;
		this.particleRed = (argb&255)/255f;
	}

	@Override
	public IParticleRenderType getRenderType()
	{
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<Data>
	{
		@Nullable
		@Override
		public Particle makeParticle(Data typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new FluidSplashParticle(typeIn.fluid, worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}

	public static class Data implements IParticleData
	{

		private final Fluid fluid;

		public Data(Fluid fluid)
		{
			this.fluid = fluid;
		}

		@Override
		public ParticleType<?> getType()
		{
			return IEParticles.FLUID_SPLASH;
		}

		@Override
		public void write(PacketBuffer buffer)
		{
			buffer.writeString(fluid.getRegistryName().toString());
		}

		@Override
		public String getParameters()
		{
			return fluid.getRegistryName().toString();
		}
	}

	public static class DataDeserializer implements IDeserializer<Data>
	{

		@Override
		public Data deserialize(ParticleType<Data> particleTypeIn, StringReader reader) throws CommandSyntaxException
		{
			String name = reader.getString();
			Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(name));
			return new Data(f);
		}

		@Override
		public Data read(ParticleType<Data> particleTypeIn, PacketBuffer buffer)
		{
			String name = buffer.readString();
			Fluid f = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(name));
			return new Data(f);

		}
	}
}