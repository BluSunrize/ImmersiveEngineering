/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class IESaveData extends WorldSavedData
{
	private static IESaveData INSTANCE;
	public static final String dataName = "ImmersiveEngineering-SaveData";

	public IESaveData()
	{
		super(dataName);
	}

	@Override
	public void read(CompoundNBT nbt)
	{
		EventHandler.validateConnsNextTick = true;

		ListNBT mineralList = nbt.getList("mineralDepletion", 10);
		ExcavatorHandler.mineralCache.clear();
		for(int i = 0; i < mineralList.size(); i++)
		{
			CompoundNBT tag = mineralList.getCompound(i);
			DimensionChunkCoords coords = DimensionChunkCoords.readFromNBT(tag);
			if(coords!=null)
			{
				MineralWorldInfo info = MineralWorldInfo.readFromNBT(tag.getCompound("info"));
				ExcavatorHandler.mineralCache.put(coords, info);
			}
		}


		ListNBT receivedShaderList = nbt.getList("receivedShaderList", 10);
		for(int i = 0; i < receivedShaderList.size(); i++)
		{
			CompoundNBT tag = receivedShaderList.getCompound(i);
			UUID player = tag.getUniqueId("player");
			ShaderRegistry.receivedShaders.get(player).clear();

			ListNBT playerReceived = tag.getList("received", 8);
			for(int j = 0; j < playerReceived.size(); j++)
			{
				String s = playerReceived.getString(j);
				if(!s.isEmpty())
					ShaderRegistry.receivedShaders.put(player, new ResourceLocation(s));
			}
		}
	}

	@Nonnull
	@Override
	public CompoundNBT write(@Nonnull CompoundNBT nbt)
	{
		ListNBT mineralList = new ListNBT();
		for(Map.Entry<DimensionChunkCoords, MineralWorldInfo> e : ExcavatorHandler.mineralCache.entrySet())
			if(e.getKey()!=null&&e.getValue()!=null)
			{
				CompoundNBT tag = e.getKey().writeToNBT();
				tag.put("info", e.getValue().writeToNBT());
				mineralList.add(tag);
			}
		nbt.put("mineralDepletion", mineralList);


		ListNBT receivedShaderList = new ListNBT();
		for(UUID player : ShaderRegistry.receivedShaders.keySet())
		{
			CompoundNBT tag = new CompoundNBT();
			tag.putUniqueId("player", player);
			ListNBT playerReceived = new ListNBT();
			for(ResourceLocation shader : ShaderRegistry.receivedShaders.get(player))
				if(shader!=null)
					playerReceived.add(new StringNBT(shader.toString()));
			tag.put("received", playerReceived);
			receivedShaderList.add(tag);
		}
		nbt.put("receivedShaderList", receivedShaderList);

		return nbt;
	}


	public static void setDirty()
	{
		INSTANCE.markDirty();
	}

	public static void setInstance(IESaveData in)
	{
		INSTANCE = in;
	}

}