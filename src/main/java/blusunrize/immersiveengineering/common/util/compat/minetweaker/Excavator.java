package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;

@ZenClass("mods.immersiveengineering.Excavator")
public class Excavator
{
	@ZenMethod
	public static void addMineral(String name, int mineralWeight, double failChance, String[] ores, double[] chances,  @Optional int[] dimensionWhitelist,  @Optional boolean blacklist)
	{
		float[] fChances = new float[chances.length];
		for(int i=0; i<chances.length; i++)
			fChances[i] = (float)chances[i];
		MineTweakerAPI.apply(new AddMineral(name,mineralWeight,(float)failChance,ores,fChances, dimensionWhitelist,blacklist));
	}

	private static class AddMineral implements IUndoableAction
	{
		private final String name;
		private final int mineralWeight;
		private final float failChance;
		private final String[] ores;
		private final float[] chances;
		private final int[] dimensions;
		private final boolean blacklist;

		MineralMix mineral;

		public AddMineral(String name, int mineralWeight, float failChance, String[] ores, float[] chances, int[] dimensions, boolean blacklist)
		{
			this.name=name;
			this.mineralWeight=mineralWeight;
			this.failChance=failChance;
			this.ores=ores;
			this.chances=chances;
			this.dimensions = dimensions;
			this.blacklist = blacklist;
		}
		@Override
		public void apply()
		{
			this.mineral = ExcavatorHandler.addMineral(name, mineralWeight, failChance, ores, chances);
			if(dimensions!=null)
				if(blacklist)
					this.mineral.dimensionBlacklist = dimensions;
				else
					this.mineral.dimensionWhitelist = dimensions;
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
		@Override
		public void undo()
		{
			Iterator<Map.Entry<MineralMix,Integer>> it = ExcavatorHandler.mineralList.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry<MineralMix,Integer> e = it.next();
				if(e.getKey().name.equalsIgnoreCase(name))
					it.remove();
			}
		}
		@Override
		public String describe()
		{
			return "Adding MineralMix: " + name +" with weight "+mineralWeight;
		}
		@Override
		public String describeUndo()
		{
			return "Removing MineralMix: " + name;
		}
		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}

	@ZenMethod
	public static void removeMineral(String name)
	{
		MineTweakerAPI.apply(new RemoveMineral(name));
	}
	private static class RemoveMineral implements IUndoableAction
	{
		private final String name;

		ArrayList<MineralMix> mix;
		ArrayList<Integer> weight;
		public RemoveMineral(String name)
		{
			this.name = name;
			mix = new ArrayList<MineralMix>();
			weight = new ArrayList<Integer>();
		}
		@Override
		public void apply()
		{
			mix.clear();
			weight.clear();
			Iterator<Map.Entry<MineralMix,Integer>> it = ExcavatorHandler.mineralList.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry<MineralMix,Integer> e = it.next();
				if(e.getKey().name.equalsIgnoreCase(name) && !mix.contains(e.getKey()))
				{
					mix.add(e.getKey());
					weight.add(e.getValue());
					it.remove();
				}
			}
		}
		@Override
		public void undo()
		{
			if(mix!=null&&weight!=null)
				for(int i=0; i<mix.size(); i++)
				{
					boolean exists = false;
					for(MineralMix mm : ExcavatorHandler.mineralList.keySet())
						if(mm!=null && mix.get(i).name.equalsIgnoreCase(mm.name))
						{
							exists=true;
							break;
						}
					if(!exists)
						ExcavatorHandler.mineralList.put(mix.get(i), weight.get(i));
				}
		}
		@Override
		public String describe()
		{
			return "Removing MineralMix: " + name;
		}
		@Override
		public String describeUndo()
		{
			return "Re-Adding MineralMix: " + name;
		}
		@Override
		public Object getOverrideKey()
		{
			return null;
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
	}

	@ZenMethod
	public static MTMineralMix getMineral(String name)
	{
		for(MineralMix mix : ExcavatorHandler.mineralList.keySet())
			if(mix.name.equalsIgnoreCase(name))
				return new MTMineralMix(mix,ExcavatorHandler.mineralList.get(mix));
		return null;
	}

	@ZenClass("mods.immersiveengineering.MineralMix")
	public static class MTMineralMix
	{
		MineralMix mix;
		int weight;

		public MTMineralMix(MineralMix mix, int weight)
		{
			this.mix = mix;
			this.weight = weight;
		}
		@ZenGetter("failChance")
		public double getFailChance()
		{
			return mix.failChance;
		}
		@ZenSetter("failChance")
		public void setFailChance(double chance)
		{
			mix.failChance = (float)chance;
		}

		@ZenMethod
		public void addOre(String ore, double chance)
		{
			String[] newOres = new String[mix.ores.length+1];
			float[] newChances = new float[newOres.length];
			System.arraycopy(mix.ores,0, newOres,0, mix.ores.length);
			System.arraycopy(mix.chances,0, newChances,0, mix.chances.length);
			newOres[mix.ores.length] = ore;
			newChances[mix.ores.length] = (float)chance;
			mix.ores = newOres;
			mix.chances = newChances;
		}

		@ZenMethod
		public void removeOre(String ore)
		{
			HashMap<String, Float> map = new HashMap<String, Float>();
			for(int i=0; i<mix.ores.length; i++)
				map.put(mix.ores[i], mix.chances[i]);
			map.remove(ore);
			mix.ores = new String[map.size()];
			mix.chances = new float[map.size()];
			int i=0;
			for(Entry<String,Float> e: map.entrySet())
			{
				mix.ores[i] = e.getKey();
				mix.chances[i] = e.getValue();
				i++;
			}
		}

	}
}