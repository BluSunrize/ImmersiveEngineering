/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ILightValue;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.init.Particles;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityBalloon extends TileEntityConnectorStructural implements ILightValue, IPlayerInteraction, IHammerInteraction
{
	public int style = 0;
	public int colour0 = 0xffffff;
	public int colour1 = 0xffffff;
	public ShaderWrapper_Direct shader = new ShaderWrapper_Direct("immersiveengineering:balloon");

	@Override
	public int getLightValue()
	{
		return 13;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		//to prevent old ballons from going black
		int nbtVersion = nbt.getInt("nbtVersion");
		if(nbtVersion >= 1)
		{
			style = nbt.getInt("style");
			colour0 = nbt.getInt("colour0");
			colour1 = nbt.getInt("colour1");
		}
		if(nbt.hasKey("shader"))
		{
			shader = new ShaderWrapper_Direct("immersiveengineering:balloon");
			shader.deserializeNBT(nbt.getCompound("shader"));
		}
	}

	@Override
	public void writeCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInt("nbtVersion", 1);
		nbt.setInt("style", style);
		nbt.setInt("colour0", colour0);
		nbt.setInt("colour1", colour1);
		nbt.setTag("shader", shader.serializeNBT());
	}

	@Nonnull
	@Override
	public float[] getBlockBounds()
	{
		return new float[]{.125f, 0, .125f, .875f, .9375f, .875f};
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	LazyOptional<ShaderWrapper> shaderCap = registerConstantCap(shader);//TODO deal with NBT reads

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityShader.SHADER_CAPABILITY)
			return ApiUtils.constantOptional(shaderCap, shader);
		return super.getCapability(capability, facing);
	}

	@Nonnull
	@Override
	public String getCacheKey(@Nonnull BlockState object)
	{
		if(shader!=null&&!shader.getShaderItem().isEmpty()&&shader.getShaderItem().getItem() instanceof IShaderItem)
			return ((IShaderItem)shader.getShaderItem().getItem()).getShaderName(shader.getShaderItem());
		return colour0+":"+colour1+":"+style;
	}

	@Override
	public int getRenderColour(BlockState object, String group)
	{
		if(shader!=null&&!shader.getShaderItem().isEmpty()&&shader.getShaderItem().getItem() instanceof IShaderItem)
			return 0xffffffff;
		if(style==0)
		{
			if(group.startsWith("balloon1_"))
				return 0xff000000|colour1;
			if(group.startsWith("balloon0_"))
				return 0xff000000|colour0;
		}
		else
		{
			if(group.endsWith("_1"))
				return 0xff000000|colour1;
			if(group.endsWith("_0"))
				return 0xff000000|colour0;
		}
		return 0xffffffff;
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos end = con.getOtherEnd(here).getPosition();
		int xDif = end.getX()-getPos().getX();
		int zDif = end.getZ()-getPos().getZ();
		int yDif = end.getY()-getPos().getY();
		if(yDif < 0)
		{
			double dist = Math.sqrt(xDif*xDif+zDif*zDif);
			if(dist/Math.abs(yDif) < 2.5)
				return new Vec3d(.5, .09375, .5);
		}
		if(Math.abs(zDif) > Math.abs(xDif))
			return new Vec3d(.5, .09375, zDif > 0?.78125: .21875);
		else
			return new Vec3d(xDif > 0?.78125: .21875, .09375, .5);
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!heldItem.isEmpty()&&heldItem.getItem() instanceof IShaderItem)
		{
			if(this.shader==null)
				this.shader = new ShaderWrapper_Direct("immersiveengineering:balloon");
			this.shader.setShaderItem(Utils.copyStackWithAmount(heldItem, 1));
			markContainingBlockForUpdate(null);
			return true;
		}
		int target = 0;
		if(side.getAxis()==Axis.Y&&style==0)
			target = (hitX < .375||hitX > .625)&&(hitZ < .375||hitZ > .625)?1: 0;
		else if(side.getAxis()==Axis.Z)
		{
			if(style==0)
				target = (hitX < .375||hitX > .625)?1: 0;
			else
				target = (hitY > .5625&&hitY < .75)?1: 0;
		}
		else if(side.getAxis()==Axis.X)
		{
			if(style==0)
				target = (hitZ < .375||hitZ > .625)?1: 0;
			else
				target = (hitY > .5625&&hitY < .75)?1: 0;
		}
		DyeColor heldDye = Utils.getDye(heldItem);
		if(heldDye==null)
			return false;
		int color = ObfuscationReflectionHelper.getPrivateValue(DyeColor.class, heldDye,
				"field_193351_w");
		if(target==0)
		{
			if(colour0==color)
				return false;
			colour0 = color;
		}
		else
		{
			if(colour1==color)
				return false;
			colour1 = color;
		}
		markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, float hitX, float hitY, float hitZ)
	{
		style = 1-style;
		markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(entity instanceof AbstractArrowEntity||entity instanceof EntityRevolvershot)
		{
			Vec3d pos = new Vec3d(getPos()).add(.5, .5, .5);
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
					SoundCategory.BLOCKS, 1.5f, 0.7f);
			world.removeBlock(getPos());
			world.spawnParticle(Particles.EXPLOSION, pos.x, pos.y, pos.z, 0, .05, 0);
			Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(this.shader);
			if(shader!=null)
				shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), null, shader.getRight().getShaderType(), pos, null, .375f);

		}
	}
}