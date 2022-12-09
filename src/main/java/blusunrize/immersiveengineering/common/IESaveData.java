/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.stream.Collectors;

public class IESaveData extends SavedData
{
	private static IESaveData INSTANCE;
	public static final String dataName = "ImmersiveEngineering-SaveData";

	public IESaveData()
	{
		super();
	}

	public IESaveData(CompoundTag nbt)
	{
		this();
		ListTag dimensionList = nbt.getList("mineralVeins", Tag.TAG_COMPOUND);
		synchronized(ExcavatorHandler.getMineralVeinList())
		{
			ExcavatorHandler.getMineralVeinList().clear();
			for(int i = 0; i < dimensionList.size(); i++)
			{
				CompoundTag dimTag = dimensionList.getCompound(i);
				ResourceLocation rl = new ResourceLocation(dimTag.getString("dimension"));
				ResourceKey<Level> dimensionType = ResourceKey.create(Registries.DIMENSION, rl);
				ListTag mineralList = dimTag.getList("veins", Tag.TAG_COMPOUND);

				ExcavatorHandler.getMineralVeinList().
						putAll(dimensionType, mineralList.stream()
								.map(inbt -> MineralVein.readFromNBT((CompoundTag)inbt))
								.collect(Collectors.toList()));
			}
			ExcavatorHandler.resetCache();
		}

		ListTag receivedShaderList = nbt.getList("receivedShaderList", Tag.TAG_COMPOUND);
		for(int i = 0; i < receivedShaderList.size(); i++)
		{
			CompoundTag tag = receivedShaderList.getCompound(i);
			UUID player = tag.getUUID("player");
			ShaderRegistry.receivedShaders.get(player).clear();

			ListTag playerReceived = tag.getList("received", Tag.TAG_STRING);
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
	public CompoundTag save(@Nonnull CompoundTag nbt)
	{
		ListTag dimensionList = new ListTag();
		synchronized(ExcavatorHandler.getMineralVeinList())
		{
			for(ResourceKey<Level> dimension : ExcavatorHandler.getMineralVeinList().keySet())
			{
				CompoundTag dimTag = new CompoundTag();
				dimTag.putString("dimension", dimension.location().toString());
				ListTag mineralList = new ListTag();
				for(MineralVein mineralVein : ExcavatorHandler.getMineralVeinList().get(dimension))
					mineralList.add(mineralVein.writeToNBT());
				dimTag.put("veins", mineralList);
				dimensionList.add(dimTag);
			}
		}
		nbt.put("mineralVeins", dimensionList);


		ListTag receivedShaderList = new ListTag();
		for(UUID player : ShaderRegistry.receivedShaders.keySet())
		{
			CompoundTag tag = new CompoundTag();
			tag.putUUID("player", player);
			ListTag playerReceived = new ListTag();
			for(ResourceLocation shader : ShaderRegistry.receivedShaders.get(player))
				if(shader!=null)
					playerReceived.add(StringTag.valueOf(shader.toString()));
			tag.put("received", playerReceived);
			receivedShaderList.add(tag);
		}
		nbt.put("receivedShaderList", receivedShaderList);

		return nbt;
	}


	public static void markInstanceDirty()
	{
		if(INSTANCE!=null)
			INSTANCE.setDirty();
	}

	public static void setInstance(IESaveData in)
	{
		INSTANCE = in;
	}

}
