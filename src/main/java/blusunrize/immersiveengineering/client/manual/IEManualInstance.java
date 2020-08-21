/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.network.MessageShaderManual;
import blusunrize.immersiveengineering.common.network.MessageShaderManual.MessageType;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree;
import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class IEManualInstance extends ManualInstance
{
	private final Set<ResourceLocation> hiddenEntries = new HashSet<>();
	public final List<Function<String, Object>> configGetters = new ArrayList<>();

	public IEManualInstance()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "textures/gui/manual.png"),
				120, 148, new ResourceLocation(ImmersiveEngineering.MODID, "manual"));
		configGetters.add(s -> {
			//TODO forge PR or wait for Lex to fix this
			Config actualCfg = IEConfig.getRawConfig();
			if(!actualCfg.contains(s))
				return null;
			else
				return actualCfg.get(s);
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

		s = s.replaceAll("<br>", "\n");
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
			ResourceLocation dimKey = new ResourceLocation(segment[1]);
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
			for(KeyBinding kb : ClientUtils.mc().gameSettings.keyBindings)
				if(segment[1].equalsIgnoreCase(kb.getKeyDescription()))
				{
					result = kb.func_238171_j_().getString();
					if(result.length() > 1)
						result = Utils.toCamelCase(result);
					break;
				}
			s = s.replaceFirst(rep, result);
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
	public FontRenderer fontRenderer()
	{
		return /*TODO new IEItemFontRender()*/ClientUtils.unicodeFontRender();
	}

	@Override
	public String getManualName()
	{
		return I18n.format("item.immersiveengineering.manual");
	}

	@Override
	public String formatCategoryName(ResourceLocation s)
	{
		return (improveReadability()?TextFormatting.BOLD: "")+I18n.format("manual."
				+s.toString().replace(':', '.'));
	}

	@Override
	public String formatEntryName(String s)
	{
		return (improveReadability()?TextFormatting.BOLD: "")+s;
	}

	@Override
	public String formatEntrySubtext(String s)
	{
		return s;
	}

	//TODO this was changed to snake_case. Where else do I need to change it
	private static final ResourceLocation SHADER_ENTRY = new ResourceLocation(ImmersiveEngineering.MODID, "shader_list");

	public void hideEntry(ResourceLocation name)
	{
		this.hiddenEntries.add(name);
	}

	@Override
	public boolean showNodeInList(Tree.AbstractNode<ResourceLocation, ManualEntry> node)
	{
		ResourceLocation nodeLoc = node.isLeaf()?node.getLeafData().getLocation(): node.getNodeData();
		if(ImmersiveEngineering.MODID.equals(nodeLoc.getNamespace())&&
				nodeLoc.getPath().startsWith(ManualHelper.CAT_UPDATE))
			return IEConfig.GENERAL.showUpdateNews.get();
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
		return TextFormatting.GOLD+"  -> "+link.getKey().getTitle()+", "+
				(link.getPage()+1);
	}

	@Override
	public void openEntry(ManualEntry entry)
	{
		if(SHADER_ENTRY.equals(entry.getLocation()))
			ImmersiveEngineering.packetHandler.sendToServer(new MessageShaderManual(MessageType.SYNC));
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
	public boolean allowGuiRescale()
	{
		return IEConfig.GENERAL.adjustManualScale.get();
	}

	@Override
	public boolean improveReadability()
	{
		return IEConfig.GENERAL.badEyesight.get();
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
