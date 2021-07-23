/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.stream.Collectors;

public class IESaveData extends SavedData
{
	private static IESaveData INSTANCE;
	public static final String dataName = "ImmersiveEngineering-SaveData";

	public IESaveData()
	{
		super(dataName);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		ListTag dimensionList = nbt.getList("mineralVeins", NBT.TAG_COMPOUND);
		synchronized(ExcavatorHandler.getMineralVeinList())
		{
			ExcavatorHandler.getMineralVeinList().clear();
			for(int i = 0; i < dimensionList.size(); i++)
			{
				CompoundTag dimTag = dimensionList.getCompound(i);
				ResourceLocation rl = new ResourceLocation(dimTag.getString("dimension"));
				ResourceKey<Level> dimensionType = ResourceKey.create(Registry.DIMENSION_REGISTRY, rl);
				ListTag mineralList = dimTag.getList("veins", NBT.TAG_COMPOUND);

				ExcavatorHandler.getMineralVeinList().
						putAll(dimensionType, mineralList.stream()
								.map(inbt -> MineralVein.readFromNBT((CompoundTag)inbt))
								.collect(Collectors.toList()));
			}
			ExcavatorHandler.resetCache();
		}
		// Legacy, using mineralDepletion key
		if(nbt.contains("mineralDepletion", NBT.TAG_LIST))
		{
			ListTag oldList = nbt.getList("mineralDepletion", NBT.TAG_COMPOUND);
			for(int i = 0; i < oldList.size(); i++)
			{
				CompoundTag tag = oldList.getCompound(i);
				CompoundTag mineralInfo = tag.getCompound("info");
				if(mineralInfo.contains("mineral", NBT.TAG_STRING))
				{
					ResourceLocation mineral = new ResourceLocation(mineralInfo.getString("mineral"));
					DimensionChunkCoords oldCoords = DimensionChunkCoords.readFromNBT(tag);
					int depletion = mineralInfo.getInt("depletion");
					ColumnPos convertedPos = new ColumnPos(oldCoords.getMinBlockX()+8, oldCoords.getMinBlockZ()+8);
					MineralVein convertedVein = new MineralVein(convertedPos, mineral, 8);
					convertedVein.setDepletion(depletion);
					ExcavatorHandler.getMineralVeinList().put(oldCoords.dimension, convertedVein);
				}
			}
		}

		ListTag receivedShaderList = nbt.getList("receivedShaderList", NBT.TAG_COMPOUND);
		for(int i = 0; i < receivedShaderList.size(); i++)
		{
			CompoundTag tag = receivedShaderList.getCompound(i);
			UUID player = tag.getUUID("player");
			ShaderRegistry.receivedShaders.get(player).clear();

			ListTag playerReceived = tag.getList("received", NBT.TAG_STRING);
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
