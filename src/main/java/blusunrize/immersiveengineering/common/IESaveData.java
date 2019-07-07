/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.energy.wires.IICProxy;
import blusunrize.immersiveengineering.api.energy.wires.old.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

public class IESaveData extends WorldSavedData
{
	//	private static HashMap<Integer, IESaveData> INSTANCE = new HashMap<Integer, IESaveData>();
	private static IESaveData INSTANCE;
	public static final String dataName = "ImmersiveEngineering-SaveData";

	public IESaveData(String s)
	{
		super(s);
	}

	@Override
	public void readFromNBT(CompoundNBT nbt)
	{
		//Load new info from NBT
		int[] savedDimensions = nbt.getIntArray("savedDimensions");
		for(int dim : savedDimensions)
		{
			ListNBT connectionList = nbt.getList("connectionList"+dim, 10);
			for(int i = 0; i < connectionList.size(); i++)
			{
				CompoundNBT conTag = connectionList.getCompound(i);
				ImmersiveNetHandler.Connection con = ImmersiveNetHandler.Connection.readFromNBT(conTag);
				if(con!=null)
				{
					ImmersiveNetHandler.INSTANCE.addConnection(dim, con.start, con);
				}
			}
		}

		ListNBT proxies = nbt.getList("iicProxies", 10);
		for(int i = 0; i < proxies.size(); i++)
			ImmersiveNetHandler.INSTANCE.addProxy(IICProxy.readFromNBT(proxies.getCompound(i)));

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
			String player = tag.getString("player");
			ShaderRegistry.receivedShaders.get(player).clear();

			ListNBT playerReceived = tag.getList("received", 8);
			for(int j = 0; j < playerReceived.size(); j++)
			{
				String s = playerReceived.getStringTagAt(j);
				if(s!=null&&!s.isEmpty())
					ShaderRegistry.receivedShaders.put(player, s);
			}
		}
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt)
	{
		Integer[] relDim = ImmersiveNetHandler.INSTANCE.getRelevantDimensions().toArray(new Integer[0]);
		int[] savedDimensions = new int[relDim.length];
		for(int ii = 0; ii < relDim.length; ii++)
			savedDimensions[ii] = relDim[ii];

		nbt.setIntArray("savedDimensions", savedDimensions);
		for(int dim : savedDimensions)
		{
			ListNBT connectionList = new ListNBT();
			for(ImmersiveNetHandler.Connection con : ImmersiveNetHandler.INSTANCE.getAllConnections(dim))
			{
				connectionList.add(con.writeToNBT());
			}
			nbt.setTag("connectionList"+dim, connectionList);
		}

		ListNBT proxies = new ListNBT();
		for(IICProxy iic : ImmersiveNetHandler.INSTANCE.proxies.values())
			proxies.add(iic.writeToNBT());
		nbt.setTag("iicProxies", proxies);

		ListNBT mineralList = new ListNBT();
		for(Map.Entry<DimensionChunkCoords, MineralWorldInfo> e : ExcavatorHandler.mineralCache.entrySet())
			if(e.getKey()!=null&&e.getValue()!=null)
			{
				CompoundNBT tag = e.getKey().writeToNBT();
				tag.setTag("info", e.getValue().writeToNBT());
				mineralList.add(tag);
			}
		nbt.setTag("mineralDepletion", mineralList);


		ListNBT receivedShaderList = new ListNBT();
		for(String player : ShaderRegistry.receivedShaders.keySet())
		{
			CompoundNBT tag = new CompoundNBT();
			tag.setString("player", player);
			ListNBT playerReceived = new ListNBT();
			for(String shader : ShaderRegistry.receivedShaders.get(player))
				if(shader!=null&&!shader.isEmpty())
					playerReceived.add(new StringNBT(shader));
			tag.setTag("received", playerReceived);
			receivedShaderList.add(tag);
		}
		nbt.setTag("receivedShaderList", receivedShaderList);

		return nbt;
	}


	public static void setDirty(DimensionType dimension)
	{
		//		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER && INSTANCE.get(dimension)!=null)
		//		{
		//			INSTANCE.get(dimension).markDirty();
		//		}
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER&&INSTANCE!=null)
			INSTANCE.markDirty();
	}

	public static void setInstance(DimensionType dimension, IESaveData in)
	{
		//		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		//			INSTANCE.put(dimension, in);
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
			INSTANCE = in;
	}

}