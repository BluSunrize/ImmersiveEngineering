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
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ShaderBannerBlockEntity extends IEBaseBlockEntity implements IBlockEntityDrop
{
	public ShaderWrapper_Direct shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "banner"));

	public ShaderBannerBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.SHADER_BANNER.get(), pos, state);
		reInitCapability();
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(nbt.contains("shader", NBT.TAG_COMPOUND))
		{
			shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "banner"));
			shader.deserializeNBT(nbt.getCompound("shader"));
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.put("shader", shader.serializeNBT());
	}

	@Override
	public List<ItemStack> getBlockEntityDrop(LootContext context)
	{
		return ImmutableList.of(new ItemStack(Items.WHITE_BANNER, 1));
	}

	@Override
	public void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack)
	{
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return super.triggerEvent(id, arg);
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