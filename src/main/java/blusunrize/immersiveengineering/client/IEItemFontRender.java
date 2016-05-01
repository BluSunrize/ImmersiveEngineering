package blusunrize.immersiveengineering.client;

import java.util.Arrays;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class IEItemFontRender extends FontRenderer
{
	int[] backupColours;
	String colourFormattingKeys = "0123456789abcdef";
	public IEItemFontRender()
	{
		super(ClientUtils.mc().gameSettings, new ResourceLocation("textures/font/ascii.png"), ClientUtils.mc().renderEngine, false);
		if (Minecraft.getMinecraft().gameSettings.language != null)
		{
			this.setUnicodeFlag(ClientUtils.mc().getLanguageManager().isCurrentLocaleUnicode());
			this.setBidiFlag(ClientUtils.mc().getLanguageManager().isCurrentLanguageBidirectional());
		}
		((IReloadableResourceManager)ClientUtils.mc().getResourceManager()).registerReloadListener(this);
		this.backupColours = Arrays.copyOf(this.colorCode, 32);
	}

	@Override
	public void renderStringAtPos(String text, boolean shadow)
	{
		int idx = -1;
		int loop = 0;
		HashMap<Integer, Integer> formattingReplacements = new HashMap<Integer, Integer>();
		while((idx=text.indexOf("<hexcol="))>=0 && loop++<20)
		{
			int end = text.indexOf(">",idx);
			if(end>=0)
			{
				String rep = "ERROR";
				String s = text.substring(idx, end+1);
				int formatEnd = s.indexOf(":");
				if(formatEnd>=0)
				{
					rep = s.substring(formatEnd+1, s.length()-1);
					String hex = s.substring("<hexcol=".length(), formatEnd);
					try{
						int hexColour = Integer.parseInt(hex,16);
						int formatting = 0;
						if(formattingReplacements.containsKey(hexColour))
							formatting = formattingReplacements.get(hexColour);
						else
							while(formatting<16 && text.contains("\u00A7"+colourFormattingKeys.charAt(formatting)))
								formatting++;
						if(formatting<16)
						{
							rep = "\u00A7"+colourFormattingKeys.charAt(formatting)+ rep + "\u00A7r";
							this.colorCode[formatting] = hexColour;
							this.colorCode[16+formatting] = ClientUtils.getDarkenedTextColour(hexColour);
						}
						formattingReplacements.put(hexColour, formatting);
					}catch(Exception e){}
				}
				text = text.replace(s, rep);
			}
		}
		super.renderStringAtPos(text, shadow);
		this.colorCode = Arrays.copyOf(backupColours, 32);
	}
}
