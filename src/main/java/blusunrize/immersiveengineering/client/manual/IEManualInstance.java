/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageShaderManual;
import blusunrize.immersiveengineering.common.network.MessageShaderManual.MessageType;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.wires.IEWireTypes.IEWireType;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;

public class IEManualInstance extends ManualInstance
{
	private final Set<ResourceLocation> hiddenEntries = new HashSet<>();
	public final List<Function<String, Object>> configGetters = new ArrayList<>();

	public IEManualInstance()
	{
		super(IEApi.ieLoc("textures/gui/manual.png"),
				120, 148, IEApi.ieLoc("manual"));
		configGetters.add(s -> {
			//TODO change in manual?
			if("wires.wireTransferRate".equals(s))
				return ImmutableList.of(
						IEServerConfig.WIRES.energyWireConfigs.get(IEWireType.COPPER).transferRate.get(),
						IEServerConfig.WIRES.energyWireConfigs.get(IEWireType.ELECTRUM).transferRate.get(),
						IEServerConfig.WIRES.energyWireConfigs.get(IEWireType.STEEL).transferRate.get()
				);
			else if("machines.wireConnectorInput".equals(s))
				return ImmutableList.of(
						IEServerConfig.WIRES.energyWireConfigs.get(IEWireType.COPPER).connectorRate.get(),
						IEServerConfig.WIRES.energyWireConfigs.get(IEWireType.ELECTRUM).connectorRate.get(),
						IEServerConfig.WIRES.energyWireConfigs.get(IEWireType.STEEL).connectorRate.get()
				);
			// TODO is this right?
			UnmodifiableConfig actualCfg = IEServerConfig.CONFIG_SPEC.getValues();
			if(actualCfg.get(s) instanceof ConfigValue<?> value)
				return value.get();
			else
				return null;
		});
		/*
		TODO no longer easily possible?
		this.fontRenderer.colorCode[0+6] = Lib.COLOUR_I_ImmersiveOrange;
		this.fontRenderer.colorCode[16+6] = Lib.COLOUR_I_ImmersiveOrangeShadow;
		((IEItemFontRender)this.fontRenderer).createColourBackup();
		 */
	}

	@Override
	public String getDefaultResourceDomain()
	{
		return ImmersiveEngineering.MODID;
	}

	@Override
	public String formatText(String s)
	{
		String splitKey = ";";

		s = s.replace("<br>", "\n");
		int start;
		int overflow = 0;
		while((start = s.indexOf("<config")) >= 0&&overflow < 50)
		{
			overflow++;
			int end = s.indexOf(">", start);
			String rep = s.substring(start, end+1);
			String result = formatConfigEntry(rep, splitKey);

			s = s.replaceFirst(rep, result);
		}
		overflow = 0;
		while((start = s.indexOf("<dim")) >= 0&&overflow < 50)
		{
			overflow++;
			int end = s.indexOf(">", start);
			String rep = s.substring(start, end+1);
			String[] segment = rep.substring(0, rep.length()-1).split(splitKey);
			if(segment.length < 2)
				break;
			ResourceLocation dimKey = ResourceLocation.parse(segment[1]);
			StringBuilder dimName = new StringBuilder();
			for(String ss : dimKey.getPath().split("_"))
				if(!"the".equalsIgnoreCase(ss))
					dimName.append(Utils.toCamelCase(ss)+" ");
			s = s.replaceFirst(rep, dimName.toString().trim());
		}

		overflow = 0;
		while((start = s.indexOf("<keybind")) >= 0&&overflow < 50)
		{
			overflow++;
			int end = s.indexOf(">", start);
			String rep = s.substring(start, end+1);
			String[] segment = rep.substring(0, rep.length()-1).split(splitKey);
			if(segment.length < 2)
				break;
			String result = "";
			for(KeyMapping kb : ClientUtils.mc().options.keyMappings)
				if(segment[1].equalsIgnoreCase(kb.getName()))
				{
					result = kb.getTranslatedKeyMessage().getString();
					if(result.length() > 1)
						result = Utils.toCamelCase(result);
					break;
				}
			s = s.replaceFirst(rep, Matcher.quoteReplacement(result));
		}

		return s;
	}

	/*TODO readd
	@Override
	public void openManual()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = -.5f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 1f;
		}
	}

	@Override
	public void titleRenderPre()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = .5f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 4f;
		}
	}

	@Override
	public void titleRenderPost()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = -.5f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 1f;
		}
	}

	@Override
	public void entryRenderPre()
	{
		if(improveReadability())
			((IEItemFontRender)this.fontRenderer).verticalBoldness = true;
	}

	@Override
	public void entryRenderPost()
	{
		if(improveReadability())
			((IEItemFontRender)this.fontRenderer).verticalBoldness = false;
	}

	@Override
	public void tooltipRenderPre()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = 0f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 4f;
			((IEItemFontRender)this.fontRenderer).verticalBoldness = false;
		}
	}

	@Override
	public void tooltipRenderPost()
	{
		if(improveReadability())
		{
			((IEItemFontRender)this.fontRenderer).spacingModifier = -.5f;
			((IEItemFontRender)this.fontRenderer).customSpaceWidth = 1f;
			((IEItemFontRender)this.fontRenderer).verticalBoldness = true;
		}
	}*/

	@Override
	public Font fontRenderer()
	{
		return /*TODO new IEItemFontRender()*/ClientUtils.unicodeFontRender();
	}

	@Override
	public String getManualName()
	{
		return I18n.get("item.immersiveengineering.manual");
	}

	@Override
	public String formatCategoryName(ResourceLocation s)
	{
		return (improveReadability()?ChatFormatting.BOLD: "")+I18n.get("manual."
				+s.toString().replace(':', '.'));
	}

	@Override
	public String formatEntryName(String s)
	{
		return (improveReadability()?ChatFormatting.BOLD: "")+s;
	}

	@Override
	public String formatEntrySubtext(String s)
	{
		return s;
	}

	//TODO this was changed to snake_case. Where else do I need to change it
	private static final ResourceLocation SHADER_ENTRY = IEApi.ieLoc("shader_list");

	public void hideEntry(ResourceLocation name)
	{
		this.hiddenEntries.add(name);
	}

	@Override
	public boolean showNodeInList(Tree.AbstractNode<ResourceLocation, ManualEntry> node)
	{
		if(!super.showNodeInList(node))
			return false;
		ResourceLocation nodeLoc = node.isLeaf()?node.getLeafData().getLocation(): node.getNodeData();
		if(ImmersiveEngineering.MODID.equals(nodeLoc.getNamespace())&&
				nodeLoc.getPath().startsWith(ManualHelper.CAT_UPDATE))
			return IEClientConfig.showUpdateNews.get();
		return !nodeLoc.equals(SHADER_ENTRY)&&!hiddenEntries.contains(nodeLoc);
	}

	@Override
	public boolean showCategoryInList(String category)
	{
		return true;
	}

	@Override
	public String formatLink(ManualLink link)
	{
		return ChatFormatting.GOLD+"  -> "+link.getKey().getTitle()+", "+
				(link.getPage()+1);
	}

	@Override
	public void openEntry(ManualEntry entry)
	{
		if(SHADER_ENTRY.equals(entry.getLocation()))
			PacketDistributor.sendToServer(new MessageShaderManual(MessageType.SYNC));
	}

	@Override
	public int getTitleColour()
	{
		return 0xf78034;
	}

	@Override
	public int getSubTitleColour()
	{
		return 0xf78034;
	}

	@Override
	public int getTextColour()
	{
		return improveReadability()?0: 0x555555;
	}

	@Override
	public int getHighlightColour()
	{
		return 0xd4804a;
	}

	@Override
	public int getPagenumberColour()
	{
		return 0x9c917c;
	}

	@Override
	public int getGuiRescale()
	{
		return IEClientConfig.manualGuiScale.get();
	}

	@Override
	public boolean improveReadability()
	{
		return IEClientConfig.badEyesight.get();
	}

	public String formatConfigEntry(String rep, String splitKey)
	{
		String[] segment = rep.substring(0, rep.length()-1).split(splitKey);
		Preconditions.checkState(
				segment.length >= 3,
				"%s is not a valid config entry",
				rep
		);
		Object configValueObj = null;
		for(Function<String, Object> f : configGetters)
		{
			configValueObj = f.apply(segment[2]);
			if(configValueObj!=null)
				break;
		}
		Preconditions.checkState(
				configValueObj!=null,
				"Config key %s does not exist",
				segment[2]
		);
		if(segment[1].equalsIgnoreCase("b"))
		{
			Preconditions.checkState(
					configValueObj instanceof Boolean,
					"Expected boolean value for %s, got %s",
					segment[2], configValueObj
			);
			boolean configValue = (boolean)configValueObj;
			if(segment.length > 3)
				return (configValue?segment[3]: segment.length > 4?segment[4]: "");
			else
				return Boolean.toString(configValue);
		}
		else if(segment[1].equalsIgnoreCase("i"))
		{
			Preconditions.checkState(
					configValueObj instanceof Number,
					"Expected number value for %s, got %s",
					segment[2], configValueObj
			);
			return Integer.toString(((Number)configValueObj).intValue());
		}
		else if(segment[1].equalsIgnoreCase("iA"))
		{
			Preconditions.checkState(
					configValueObj instanceof List<?>&&((List<?>)configValueObj).stream()
							.allMatch(n -> n instanceof Number),
					"Expected list of integer for %s, got %s",
					segment[2], configValueObj
			);
			List<?> configList = (List<?>)configValueObj;
			if(segment.length > 3)
				try
				{
					if(segment[3].startsWith("l"))
					{
						int limiter = Integer.parseInt(segment[3].substring(1));
						StringBuilder result = new StringBuilder();
						for(int i = 0; i < limiter; i++)
							result.append(i > 0?", ": "")
									.append(configList.get(i));
						return result.toString();
					}
					else
					{
						int idx = Integer.parseInt(segment[3]);
						return ""+configList.get(idx);
					}
				} catch(Exception ex)
				{
					throw new RuntimeException("Failed to parse "+segment[3]+" as integer");
				}
			else
			{
				StringBuilder result = new StringBuilder();
				for(int i = 0; i < configList.size(); i++)
					result.append(i > 0?", ": "").append(configList.get(i));
				return result.toString();
			}
		}
		else if(segment[1].equalsIgnoreCase("d"))
		{
			Preconditions.checkState(
					configValueObj instanceof Number,
					"Expected double for %s, got %s",
					segment[2], configValueObj
			);
			return Double.toString(((Number)configValueObj).doubleValue());
		}
		else if(segment[1].equalsIgnoreCase("str"))
		{
			Preconditions.checkState(
					configValueObj instanceof String,
					"Expected string for %s, got %s",
					segment[2], configValueObj
			);
			return (String)configValueObj;
		}
		throw new RuntimeException("Unknown config type: "+segment[1]+" (part of "+rep+")");
	}
}
