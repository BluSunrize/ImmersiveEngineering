/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ShaderBannerTileEntity extends IEBaseTileEntity implements IAdvancedCollisionBounds, ITileDrop
{
	public boolean wall = false;
	public byte orientation = 0;
	public ShaderWrapper_Direct shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "banner"));

	public static TileEntityType<BannerTileEntity> TYPE;

	public ShaderBannerTileEntity()
	{
		super(TYPE);
		reInitCapability();
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		this.wall = nbt.getBoolean("wall");
		this.orientation = nbt.getByte("orientation");
		if(nbt.contains("shader", NBT.TAG_COMPOUND))
		{
			shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "banner"));
			shader.deserializeNBT(nbt.getCompound("shader"));
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putBoolean("wall", this.wall);
		nbt.putByte("orientation", this.orientation);
		nbt.put("shader", shader.serializeNBT());
	}

	@Override
	public float[] getBlockBounds()
	{
		if(this.wall)
			switch(this.orientation)
			{
				default:
				case 2:
					return new float[]{0, 0, .875f, 1, .78125f, 1};
				case 3:
					return new float[]{0, 0, 0, 1, .78125f, .125f};
				case 4:
					return new float[]{.875f, 0, 0, 1, .78125f, 1};
				case 5:
					return new float[]{0, 0, 0, .125f, .78125f, 1};
			}
		return new float[]{.25f, 0, .25f, .75f, 1, .75f};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedCollisionBounds()
	{
		return ImmutableList.of();
	}

	@Override
	public List<ItemStack> getTileDrops(Builder context)
	{
		return ImmutableList.of(new ItemStack(Items.WHITE_BANNER, 1), this.shader.getShaderItem());
	}

	@Override
	public void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack)
	{
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

	private LazyOptional<ShaderWrapper> shaderCap;

	private void reInitCapability()
	{
		if(shaderCap!=null)
			unregisterCap(shaderCap);
		shaderCap = registerConstantCap(shader);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityShader.SHADER_CAPABILITY)
			return shaderCap.cast();
		return super.getCapability(capability, facing);
	}
}