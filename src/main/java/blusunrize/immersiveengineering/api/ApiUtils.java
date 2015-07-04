package blusunrize.immersiveengineering.api;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.WireType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.registry.GameData;

public class ApiUtils
{
	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		for(int oid : OreDictionary.getOreIDs(stack))
			if(OreDictionary.getOreName(oid).equals(oreName))
				return true;
		return false;
	}
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		if(o instanceof String)
			return compareToOreName(stack, (String)o);
		if(o instanceof ItemStack)
			return OreDictionary.itemMatches((ItemStack)o, stack, false);
		return false;
	}

	public static String nameFromStack(ItemStack stack)
	{
		if(stack==null)
			return "";
		try
		{
			return GameData.getItemRegistry().getNameForObject(stack.getItem());
		}
		catch (NullPointerException e) {}
		return "";
	}

	public static ChunkCoordinates toCC(Object object)
	{
		if(object instanceof ChunkCoordinates)
			return (ChunkCoordinates)object;
		if(object instanceof TileEntity)
			return new ChunkCoordinates(((TileEntity)object).xCoord,((TileEntity)object).yCoord,((TileEntity)object).zCoord);
		return null;
	}
	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		if(object instanceof IImmersiveConnectable)
			return (IImmersiveConnectable)object;
		else if(object instanceof ChunkCoordinates && world!=null && world.getTileEntity( ((ChunkCoordinates)object).posX, ((ChunkCoordinates)object).posY, ((ChunkCoordinates)object).posZ) instanceof IImmersiveConnectable)
		{
			return (IImmersiveConnectable)world.getTileEntity( ((ChunkCoordinates)object).posX, ((ChunkCoordinates)object).posY, ((ChunkCoordinates)object).posZ);
		}
		return null;
	}

	public static WireType getWireTypeFromNBT(NBTTagCompound tag, String key)
	{
		//Legacy code for old save data, where types used to be integers
		if(tag.getTag(key) instanceof NBTTagInt)
		{
			int i = tag.getInteger(key);
			return i==1?WireType.ELECTRUM: i==2?WireType.STEEL: i==3?WireType.STRUCTURE_ROPE: i==4?WireType.STRUCTURE_STEEL: WireType.COPPER;
		}
		else
			return WireType.getValue(tag.getString(key));
	}

	public static Map<String, Integer> sortMap(Map<String, Integer> map, boolean inverse)
	{
		TreeMap<String,Integer> sortedMap = new TreeMap<String,Integer>(new ValueComparator(map, inverse));
		sortedMap.putAll(map);
		return sortedMap;
	}
	static class ValueComparator implements Comparator<String>
	{
		Map<String, Integer> base;
		boolean inverse;
		public ValueComparator(Map<String, Integer> base, boolean inverse)
		{
			this.base = base;
			this.inverse = inverse;
		}
		@Override
		//Cant return equal to keys separate
		public int compare(String s0, String s1)
		{
			if(inverse)
			{
				if (base.get(s0) <= base.get(s1))
					return -1;
				else
					return 1;
			}
			else
			{
				if (base.get(s0) >= base.get(s1))
					return -1;
				else
					return 1;
			}
		}
	}
}