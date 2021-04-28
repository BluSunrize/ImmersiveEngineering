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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.stream.Collectors;

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
		ListNBT dimensionList = nbt.getList("mineralVeins", NBT.TAG_COMPOUND);
		synchronized(ExcavatorHandler.getMineralVeinList())
		{
			ExcavatorHandler.getMineralVeinList().clear();
			for(int i = 0; i < dimensionList.size(); i++)
			{
				CompoundNBT dimTag = dimensionList.getCompound(i);
				ResourceLocation rl = new ResourceLocation(dimTag.getString("dimension"));
				RegistryKey<World> dimensionType = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, rl);
			ListNBT mineralList = dimTag.getList("veins", NBT.TAG_COMPOUND);

				ExcavatorHandler.getMineralVeinList().
						putAll(dimensionType, mineralList.stream()
								.map(inbt -> MineralVein.readFromNBT((CompoundNBT)inbt))
								.collect(Collectors.toList()));
			}
			ExcavatorHandler.resetCache();
		}
		// Legacy, using mineralDepletion key
		if(nbt.contains("mineralDepletion", NBT.TAG_LIST))
		{
			ListNBT oldList = nbt.getList("mineralDepletion", NBT.TAG_COMPOUND);
			for(int i = 0; i < oldList.size(); i++)
			{
				CompoundNBT tag = oldList.getCompound(i);
				CompoundNBT mineralInfo = tag.getCompound("info");
				if(mineralInfo.contains("mineral", NBT.TAG_STRING))
				{
					ResourceLocation mineral = new ResourceLocation(mineralInfo.getString("mineral"));
					DimensionChunkCoords oldCoords = DimensionChunkCoords.readFromNBT(tag);
					int depletion = mineralInfo.getInt("depletion");
					ColumnPos convertedPos = new ColumnPos(oldCoords.getXStart()+8, oldCoords.getZStart()+8);
					MineralVein convertedVein = new MineralVein(convertedPos, mineral, 8);
					convertedVein.setDepletion(depletion);
					ExcavatorHandler.getMineralVeinList().put(oldCoords.dimension, convertedVein);
				}
			}
		}

		ListNBT receivedShaderList = nbt.getList("receivedShaderList", NBT.TAG_COMPOUND);
		for(int i = 0; i < receivedShaderList.size(); i++)
		{
			CompoundNBT tag = receivedShaderList.getCompound(i);
			UUID player = tag.getUniqueId("player");
			ShaderRegistry.receivedShaders.get(player).clear();

			ListNBT playerReceived = tag.getList("received", NBT.TAG_STRING);
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
		ListNBT dimensionList = new ListNBT();
		synchronized(ExcavatorHandler.getMineralVeinList())
		{
			for(RegistryKey<World> dimension : ExcavatorHandler.getMineralVeinList().keySet())
			{
				CompoundNBT dimTag = new CompoundNBT();
				dimTag.putString("dimension", dimension.getLocation().toString());
				ListNBT mineralList = new ListNBT();
				for(MineralVein mineralVein : ExcavatorHandler.getMineralVeinList().get(dimension))
					mineralList.add(mineralVein.writeToNBT());
				dimTag.put("veins", mineralList);
				dimensionList.add(dimTag);
			}
		}
		nbt.put("mineralVeins", dimensionList);


		ListNBT receivedShaderList = new ListNBT();
		for(UUID player : ShaderRegistry.receivedShaders.keySet())
		{
			CompoundNBT tag = new CompoundNBT();
			tag.putUniqueId("player", player);
			ListNBT playerReceived = new ListNBT();
			for(ResourceLocation shader : ShaderRegistry.receivedShaders.get(player))
				if(shader!=null)
					playerReceived.add(StringNBT.valueOf(shader.toString()));
			tag.put("received", playerReceived);
			receivedShaderList.add(tag);
		}
		nbt.put("receivedShaderList", receivedShaderList);

		return nbt;
	}


	public static void markInstanceDirty()
	{
		if(INSTANCE!=null)
			INSTANCE.markDirty();
	}

	public static void setInstance(IESaveData in)
	{
		INSTANCE = in;
	}

}
