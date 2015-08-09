package blusunrize.immersiveengineering.common;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class IESaveData extends WorldSavedData
{
	//	private static HashMap<Integer, IESaveData> INSTANCE = new HashMap<Integer, IESaveData>();
	private static IESaveData INSTANCE;
	public static final String dataName = "ImmersiveEngineering-SaveData";
	public static boolean loaded = false;

	public IESaveData(String s)
	{
		super(s);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		int[] savedDimensions = nbt.getIntArray("savedDimensions");
		for(int dim: savedDimensions)
		{
			NBTTagList connectionList = nbt.getTagList("connectionList"+dim, 10);
			World world = MinecraftServer.getServer().worldServerForDimension(dim);
			if(world!=null)
			{
				ImmersiveNetHandler.INSTANCE.clearAllConnections(world);
				for(int i=0; i<connectionList.tagCount(); i++)
				{
					NBTTagCompound conTag = connectionList.getCompoundTagAt(i);
					Connection con = Connection.readFromNBT(conTag);
					if(con!=null)
						ImmersiveNetHandler.INSTANCE.addConnection(world, con.start, con);
				}
			}
		}

		NBTTagList mineralList = nbt.getTagList("mineralDepletion", 10);
		ExcavatorHandler.mineralDepletion.clear();		
		for(int i=0; i<mineralList.tagCount(); i++)
		{
			NBTTagCompound tag = mineralList.getCompoundTagAt(i);
			DimensionChunkCoords coords = DimensionChunkCoords.readFromNBT(tag);
			if(coords!=null)
				ExcavatorHandler.mineralDepletion.put(coords, tag.getInteger("depletion"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		Integer[] relDim = ImmersiveNetHandler.INSTANCE.getRelevantDimensions().toArray(new Integer[0]);
		int[] savedDimensions = new int[relDim.length];
		for(int ii=0; ii<relDim.length; ii++)
			savedDimensions[ii] = relDim[ii];

		nbt.setIntArray("savedDimensions", savedDimensions);
		for(int dim: savedDimensions)
		{
			World world = MinecraftServer.getServer().worldServerForDimension(dim);
			if(world!=null)
			{
				NBTTagList connectionList = new NBTTagList();
				for(Connection con : ImmersiveNetHandler.INSTANCE.getAllConnections(world))
				{
					connectionList.appendTag(con.writeToNBT());
				}
				nbt.setTag("connectionList"+dim, connectionList);
			}
		}

		NBTTagList mineralList = new NBTTagList();
		for(Map.Entry<DimensionChunkCoords,Integer> e: ExcavatorHandler.mineralDepletion.entrySet())
			if(e.getKey()!=null && e.getValue()!=null)
			{
				NBTTagCompound tag = e.getKey().writeToNBT();
				tag.setInteger("depletion", e.getValue());
				mineralList.appendTag(tag);
			}
		nbt.setTag("mineralDepletion", mineralList);
	}


	public static void setDirty(int dimension)
	{
		//		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER && INSTANCE.get(dimension)!=null)
		//		{
		//			INSTANCE.get(dimension).markDirty();
		//		}
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER && INSTANCE!=null)
			INSTANCE.markDirty();
	}
	public static void setInstance(int dimension, IESaveData in)
	{
		//		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		//			INSTANCE.put(dimension, in);
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
			INSTANCE=in;
	}

}