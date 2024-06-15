/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

public class ShaderBannerBlockEntity extends IEBaseBlockEntity
{
	public ShaderWrapper_Direct shader = new ShaderWrapper_Direct(IEApi.ieLoc("banner"));

	public ShaderBannerBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.SHADER_BANNER.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		if(nbt.contains("shader", Tag.TAG_COMPOUND))
			shader = ShaderWrapper_Direct.SERIALIZER.read(this, nbt.getCompound("shader"), provider);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.put("shader", ShaderWrapper_Direct.SERIALIZER.write(shader, provider));
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

	public static void registerCapabilities(BECapabilityRegistrar<ShaderBannerBlockEntity> registrar)
	{
		registrar.registerAllContexts(CapabilityShader.BLOCK, be -> be.shader);
	}
}
