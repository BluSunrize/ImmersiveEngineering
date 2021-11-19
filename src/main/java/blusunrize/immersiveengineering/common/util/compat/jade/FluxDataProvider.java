/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.common.immersiveflux.IFluxReceiver;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FluxDataProvider implements IComponentProvider, IServerDataProvider<BlockEntity>
{
	private static final String DATA_KEY = ImmersiveEngineering.MODID+":flux";
	private static final String STORED_KEY = "stored";
	private static final String MAX_STORED_KEY = "maxStored";

	@Override
	public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig)
	{
		Tag dataTag = blockAccessor.getServerData().get(DATA_KEY);
		if(dataTag instanceof CompoundTag data)
		{
			int cur = data.getInt(STORED_KEY);
			int max = data.getInt(MAX_STORED_KEY);
			iTooltip.add(new TextComponent(String.format("%d / %d IF", cur, max)));
		}
	}

	@Override
	public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b)
	{
		CompoundTag fluxData = null;
		if(blockEntity instanceof IFluxReceiver receiver)
		{
			fluxData = new CompoundTag();
			fluxData.putInt(STORED_KEY, receiver.getEnergyStored(null));
			fluxData.putInt(MAX_STORED_KEY, receiver.getMaxEnergyStored(null));
		}
		else if(blockEntity instanceof IFluxProvider provider)
		{
			fluxData = new CompoundTag();
			fluxData.putInt(STORED_KEY, provider.getEnergyStored(null));
			fluxData.putInt(MAX_STORED_KEY, provider.getMaxEnergyStored(null));
		}
		if(fluxData!=null)
			compoundTag.put(DATA_KEY, fluxData);
	}
}
