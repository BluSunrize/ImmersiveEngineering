/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.*;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.ITextureOverride;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ItemShader extends ItemIEBase implements IShaderItem, ITextureOverride
{
	public ItemShader()
	{
		super("shader", 1);
		//DEFAULT CUTOUTS
		//whitestripe
		setDefaultTextureBounds(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_whitestripe"), 0, 0, .25, .25);
		setDefaultTextureBounds(new ResourceLocation("immersiveengineering:items/shaders/drill_diesel_whitestripe"), 0, 22/64d, .5, 54/64d);

		//REGISTER SHADERS
		addShader("Rosequartz", 0, EnumRarity.COMMON, 0xff412323, 0xffe6b4b4, 0xfff0cdcd, 0xffe6b4b4).setInfo(null, null, "rosequartz");
		addShader("Argo", 2, EnumRarity.COMMON, 0xff2d2d2d, 0xffdcdcdc, 0xffdc7823, 0xffc8c8c8).setInfo(null, null, "argo");
		addShader("Sunstrike", 5, EnumRarity.RARE, 0xff737373, 0xffcd6900, 0xb9d73a00, 0xb9d73a00).setInfo(null, null, "sunstrike");
		addShader("Locus", 2, EnumRarity.COMMON, 0xff0a0a0a, 0xff4a4a4a, 0xff84964c, 0xff4a4a4a).setInfo("Mercenaries", "Red vs Blue", "locus");
		addShader("Felix", 1, EnumRarity.COMMON, 0xff0a0a0a, 0xff4a4a4a, 0xfff08803, 0xff4a4a4a).setInfo("Mercenaries", "Red vs Blue", "felix");
		addShader("Sharkface", 2, EnumRarity.UNCOMMON, 0xff0a0a0a, 0xff4a4a4a, 0xff910008, 0xff4a4a4a, "shark", true, 0xffffffff).setInfo("Mercenaries", "Red vs Blue", "sharkface");
		addShader("Dragon's Breath", 1, EnumRarity.UNCOMMON, 0xff191919, 0xff333f2b, 0xff8a8a8a, 0xff8a8a8a, "shark", true, 0xffffffff).setInfo(null, "Destiny", "sharkface");
		addShader("Hawk", 3, EnumRarity.COMMON, 0xff67636b, 0xfff4eeeb, 0xff2d2d2d, 0xfff4eeeb).setInfo(null, "Destiny", "hawk");
		addShader("Eyas", 3, EnumRarity.COMMON, 0xff52534e, 0xff7a2a08, 0xff2d2d2d, 0xfff4eeeb).setInfo(null, "Destiny", "eyas");
		addShader("Magnum", 1, EnumRarity.COMMON, 0xff56382c, 0xffdcdcdc, 0xffa0a0a0, 0xffdcdcdc).setInfo(null, null, "magnum");
		addShader("Fox", 2, EnumRarity.UNCOMMON, 0xff2d2d2d, 0xffd47e31, 0xffeaeaea, 0xffeaeaea).setInfo(null, null, "fox");
		addShader("Vault-Tec", 0, EnumRarity.COMMON, 0xff56382c, 0xff1a4785, 0xffc0aa50, 0xffaaaaaa).setInfo(null, "Fallout", "vaulttec");

		ShaderRegistryEntry entry = addShader("Sponsor", 0, EnumRarity.EPIC, 0xff191919, 0xfff71b24, 0xffffffff, 0xffaaaaaa, "sponsor", false, 0xffffffff).setInfo(null, "Fallout", "sponsor");
		((ShaderCaseMinecart)entry.getCase("immersiveengineering:minecart")).mirrorSideForPass[2] = false;
		entry.getCase("immersiveengineering:revolver").getLayers()[4].setTextureBounds(0, 0, .25, .25);
		entry.getCase("immersiveengineering:drill").getLayers()[3].setTextureBounds(10/64d, 34/64d, 26/64d, 50/64d);
		entry.getCase("immersiveengineering:chemthrower").getLayers()[3].setTextureBounds(6/64d, 16/64d, 22/64d, 32/64d);
		entry.getCase("immersiveengineering:shield").getLayers()[2].setTextureBounds(0/32d, 9/32d, 14/32d, 25/32d).setCutoutBounds(.0625, 0, .9375, 1);

		addShader("Mass Fusion", 3, EnumRarity.RARE, 0xff6e5a37, 0xff394730, 0xff545454, 0xffaaaaaa, "fusion", true, 0xffffffff).setInfo(null, "Fallout", "massfusion");

		addShader("StormFlower", 1, EnumRarity.COMMON, 0xff273427, 0xff286f30, 0xff4b9255, 0xff286f30).setInfo(null, "RWBY", "stormflower");
		addShader("Mil\u00F3", 2, EnumRarity.UNCOMMON, 0xff3b1b10, 0xff670004, 0xffce7e10, 0xff670004).setInfo(null, "RWBY", "milo");
		addShader("Trident", 2, EnumRarity.UNCOMMON, 0xff515151, 0xffa8a8a8, 0xff29d3ff, 0xffafafaf).setInfo(null, "RWBY", "trident");
		addShader("Chloris", 4, EnumRarity.RARE, 0xff38322a, 0xff38322a, 0xff88fabe, 0xffc8c8c8).setInfo(null, "RWBY", "chloris");
		addShader("Crescent Rose", 2, EnumRarity.COMMON, 0xff141414, 0xff910008, 0xff080808, 0xffa4a4a4).setInfo(null, "RWBY", "crescentrose");
		addShader("Qrow", 2, EnumRarity.UNCOMMON, 0xff6d1c11, 0xffd8d7d0, 0xff313640, 0xff730008).setInfo(null, "RWBY", "qrow");
		entry = addShader("Lusus Naturae", 0, EnumRarity.EPIC, 0xff141919, 0xff141919, 0xffadb4bf, 0xffadb4bf).setInfo(null, "RWBY", "lususnaturae");
		addBlockScaledLayer(entry, "minecraft:blocks/destroy_stage_8", 0xbb940c0c);
		addLayer(entry, "1_4", 0xffadb4bf);

		addShader("Vanguard", 3, EnumRarity.UNCOMMON, 0xff373737, 0xff131b42, 0xffb86c14, 0xffdcdcdc).setInfo(null, "Destiny", "vanguard");
		addShader("Regal", 4, EnumRarity.UNCOMMON, 0xffd8d4d1, 0xff431c1d, 0xffd8d4d1, 0xffd8d4d1).setInfo(null, "Destiny", "regal");
		addShader("Harrowed", 4, EnumRarity.RARE, 0xff161321, 0xff431c1d, 0xff161321, 0xff161321).setInfo(null, "Destiny", "harrowed");
		addShader("Taken", 5, EnumRarity.EPIC, 0xff111c26, 0xff111c26, 0xffbad7dd, 0xff111c26, null, false, 0xffffffff).setInfo(null, "Destiny", "taken");

		addShader("Angel's Thesis", 2, EnumRarity.EPIC, 0xff1e1e1e, 0xff754697, 0xff77b93d, 0xff505050, null, false, 0xffffffff).setInfo("Mecha", "Neon Genesis Evangelion", "angelsthesis");
		addShader("Sutherland", 0, EnumRarity.RARE, 0xff44404f, 0xff6b5eae, 0xff702739, 0xff44404f, "whitestripe", true, 0xff702034).setInfo("Mecha", "Code Geass", "sutherland");
		addShader("Exia", 8, EnumRarity.RARE, 0xffb2220c, 0xff5571d2, 0xffece7e1, 0xffc0fdc7, "whitestripe", true, 0xffc09032).setInfo("Mecha", "Gundam 00", "exia");
		addShader("Crimson Lotus", 3, EnumRarity.EPIC, 0xffd83239, 0xffd83239, 0xff4e4f53, 0xff2ff177, "whitestripe", true, 0xfff4b951).setInfo("Mecha", "Gurren Lagann", "gurrenlagann");
		addShader("Dominator", 1, EnumRarity.UNCOMMON, 0xff4c311f, 0xff2a2c36, 0xff5bfffb, 0xff2a2c36, "1_3", true, 0xff2a2c36).setInfo(null, "Psycho-Pass", "dominator");

		addShader("Warbird", 7, EnumRarity.UNCOMMON, 0xff313640, 0xffd8d7d0, 0xffebac00, 0xffd8d7d0).setInfo(null, null, "warbird");
		addShader("Matrix", 7, EnumRarity.RARE, 0xff053f3c, 0xffe1e1ff, 0xffd4ffff, 0xffffffff, "pipes", true, 0xff84ddd8).setInfo(null, null, "matrix");
		addShader("Twili", 5, EnumRarity.EPIC, 0xff555d70, 0xff1a1e2b, 0xff222739, 0xff1db58e, "circuit", false, 0xff1db58e).setInfo(null, "The Legend of Zelda: Twilight Princess", "twili");
		addShader("Usurper", 3, EnumRarity.EPIC, 0xff3e1e1e, 0xff5c6156, 0xff111010, 0xff737a6c, "circuit", false, 0xffca2f38).setInfo(null, "The Legend of Zelda: Twilight Princess", "usurper");
		entry = addShader("Ancient", 6, EnumRarity.EPIC, 0xff9c3a2d, 0xff514848, 0xfff6ae4a, 0xff80fcf2, "circuit", false, 0x99bc9377).setInfo(null, "The Legend of Zelda: Breath of the Wild", "ancient");
		((ShaderCaseDrill)entry.getCase("immersiveengineering:drill")).addHeadLayers(new ShaderLayer(new ResourceLocation("immersiveengineering", "items/drill_iron"), 0xff80fcf2));

		addShader("Glacis", 6, EnumRarity.RARE, 0xff499bc2, 0x3376d0f9, 0x33bdfffd, 0x33bdfffd).setInfo(null, null, "glacis");
		addShader("Phoenix", 5, EnumRarity.RARE, 0xff750000, 0xffd00000, 0xffff7f00, 0xffff7f00).setInfo(null, null, "phoenix");

		addShader("Radiant", 3, EnumRarity.UNCOMMON, 0xffa36eab, 0xfff8dbbc, 0xffc79ab1, 0xffd8d8e3, "pipes", true, 0xfff1c91e).setInfo(null, "Kingdom Hearts", "radiant");
		addShader("Hollow", 4, EnumRarity.UNCOMMON, 0xff542d1c, 0xffeec5e5, 0xffcc8980, 0xffc4a1aa, "pipes", true, 0xffc49838).setInfo(null, "Kingdom Hearts", "hollow");

		addShader("Microshark", 8, EnumRarity.RARE, 0xff775054, 0xfff7f6cf, 0xff936267, 0xff936267, "shark", true, 0xffffffff).setInfo(null, "Terraria", "microshark");

		addShader("N7", 2, EnumRarity.EPIC, 0xff13171b, 0xff524d4a, 0xffe01919, 0xff8a8684, "whitestripe", false, 0xffffffff).setInfo(null, "Mass Effect", "n7");
		addShader("Normandy", 8, EnumRarity.RARE, 0xffffffff, 0xff1a1a1a, 0xffffffff, 0xffffffff, "whitestripe", true, 0xff35447e).setInfo(null, "Mass Effect", "normandy");
		addShader("OmniTool", 2, EnumRarity.RARE, 0x40ff952c, 0x30ff952c, 0x40ff952c, 0x20ff952c).setInfo(null, "Mass Effect", "omnitool");

		entry = addShader("The Kindled", 5, EnumRarity.EPIC, 0xff2b160b, 0xff3a3a3a, 0x80bf541f, 0xff4f4f4f).setInfo(null, "Dark Souls", "kindled");
		addBlockScaledLayer(entry, "minecraft:blocks/fire_layer_0", 0x80ffffff);

		entry = addShader("Dark Fire", 5, EnumRarity.EPIC, 0xff1e131b, 0xff211633, 0xff330812, 0xff412965).setInfo(null, "Kingdom Hearts", "darkfire");
		addBlockScaledLayer(entry, "immersiveengineering:blocks/shaders/greyscale_fire", 0xff9e83eb);

		entry = addShader("Erruption", 5, EnumRarity.RARE, 0xff2b160b, 0xff58432f, 0x80bf301f, 0xff58432f).setInfo(null, null, "erruption");
		addBlockScaledLayer(entry, "minecraft:blocks/destroy_stage_8", 0xffff6314);

		addShader("WAAAGH!", 5, EnumRarity.RARE, 0xff0f0f0f, 0xffdea712, 0xffc15b09, 0xff2f2f2f, "1_7", true, 0xff2f2f2f).setInfo(null, "Warhammer 40k", "waaagh");
	}

	@Override
	public ShaderCase getShaderCase(ItemStack shader, ItemStack item, String shaderType)
	{
		String name = getShaderName(shader);
		return ShaderRegistry.getShader(name, shaderType);
	}

	public static ShaderRegistryEntry addShader(String name, int overlayType, EnumRarity rarity, int colourBackground, int colourPrimary, int colourSecondary, int colourBlade)
	{
		return addShader(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, colourBlade, null, true, 0xffffffff);
	}

	public static ShaderRegistryEntry addShader(String name, int overlayType, EnumRarity rarity, int colourBackground, int colourPrimary, int colourSecondary, int colourBlade, String additionalTexture, boolean loot, int colourOverlay)
	{
		return ShaderRegistry.registerShader(name, Integer.toString(overlayType), rarity, colourPrimary, colourSecondary, colourBackground, colourBlade, additionalTexture, colourOverlay, loot, true);
		//		ShaderCaseRevolver revolver = IEApi.registerShader_Revolver(name, overlayType, colour0, colour1, colour2, colour3, additionalTexture);
		//		revolver.glowLayer = revolver_glow;
		//		IEApi.registerShader_Chemthrower(name, overlayType, colour0, colour1, colour2, true,false, additionalTexture);
		//		IEApi.registerShader_Minecart(name, overlayType, colour1, colour2, additionalTexture);
		//		IEApi.registerShader_Balloon(name, overlayType, colour1, colour2, additionalTexture);
	}

	private static void addBlockScaledLayer(ShaderRegistryEntry entry, String texture, int colour)
	{
		entry.getCase("immersiveengineering:revolver").addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0, 0, .25, .1875));
		entry.getCase("immersiveengineering:drill").addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(10/64d, 34/64d, 26/64d, 50/64d).setCutoutBounds(.1875f, 0, .8125, .75f));
		entry.getCase("immersiveengineering:chemthrower").addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(6/64d, 16/64d, 22/64d, 24/64d).setCutoutBounds(0, 0, 1, .5));
		entry.getCase("immersiveengineering:railgun").addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(55/64d, 48/64d, 1, 58/64d).setCutoutBounds(.25, .125, .75, .6875));
		entry.getCase("immersiveengineering:shield").addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0/32f, 9/32f, 14/32f, 26/32f).setCutoutBounds(.0625, 0, .9375, 1));
		entry.getCase("immersiveengineering:balloon").addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0, .375, .75, .875).setCutoutBounds(.125, 0, .875, .5));
	}

	private static void addLayer(ShaderRegistryEntry entry, String texture, int colour)
	{
		entry.getCase("immersiveengineering:revolver").addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_"+texture), colour));
		entry.getCase("immersiveengineering:drill").addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/drill_diesel_"+texture), colour));
		entry.getCase("immersiveengineering:chemthrower").addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/chemthrower_"+texture), colour));
		entry.getCase("immersiveengineering:railgun").addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/railgun_"+texture), colour));
		entry.getCase("immersiveengineering:shield").addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/shield_"+texture), colour));
		entry.getCase("immersiveengineering:minecart").addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:textures/models/shaders/minecart_"+texture+".png"), colour));
		entry.getCase("immersiveengineering:balloon").addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:blocks/shaders/balloon_"+texture), colour));
	}

	public void setDefaultTextureBounds(ResourceLocation rl, double... bounds)
	{
		ShaderRegistry.defaultLayerBounds.put(rl, bounds);
	}

	@Override
	public String getShaderName(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
		{
			String name = ItemNBTHelper.getString(stack, "shader_name");
			if(ShaderRegistry.shaderRegistry.containsKey(ItemNBTHelper.getString(stack, "shader_name")))
				return name;
			else
			{
				Set<String> keys = ShaderRegistry.shaderRegistry.keySet();
				List<String> corrected = ManualUtils.getPrimitiveSpellingCorrections(name,
						keys, 4, s -> s);
				if(!corrected.isEmpty())
				{
					IELogger.info("SHADER UPDATE: Fixing "+name+" to "+corrected.get(0));
					IELogger.info("Others: "+corrected);
					ItemNBTHelper.setString(stack, "shader_name", corrected.get(0));
					return corrected.get(0);
				}
			}
		}
		return "";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		list.add(I18n.format("Level: "+this.getRarity(stack).rarityColor+this.getRarity(stack).rarityName));
		if(!GuiScreen.isShiftKeyDown())
			list.add(I18n.format(Lib.DESC_INFO+"shader.applyTo")+" "+I18n.format(Lib.DESC_INFO+"holdShift"));
		else
		{
			list.add(I18n.format(Lib.DESC_INFO+"shader.applyTo"));
			String name = getShaderName(stack);
			if(name!=null&&!name.isEmpty())
			{
				List<ShaderCase> array = ShaderRegistry.shaderRegistry.get(name).getCases();
				for(ShaderCase sCase : array)
					if(!(sCase instanceof ShaderCaseItem))
						list.add(TextFormatting.DARK_GRAY+" "+I18n.format(Lib.DESC_INFO+"shader."+sCase.getShaderType()));
			}
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String s = getShaderName(stack);
		return super.getItemStackDisplayName(stack)+(s!=null&&!s.isEmpty()?(": "+s): "");
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		String s = getShaderName(stack);
		return ShaderRegistry.shaderRegistry.containsKey(s)?ShaderRegistry.shaderRegistry.get(s).getRarity(): EnumRarity.COMMON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		if(this.isInCreativeTab(tab))
			for(String key : ShaderRegistry.shaderRegistry.keySet())
			{
				ItemStack s = new ItemStack(this);
				ItemNBTHelper.setString(s, "shader_name", key);
				list.add(s);
			}
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		String name = getShaderName(stack);
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase("immersiveengineering:item");
			if(sCase!=null)
			{
				ShaderLayer[] layers = sCase.getLayers();
				if(pass < layers.length&&layers[pass]!=null)
					return layers[pass].getColour();
				return 0xffffffff;
			}
		}
		return super.getColourForIEItem(stack, pass);
	}

	@Override
	public String getModelCacheKey(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
			return ItemNBTHelper.getString(stack, "shader_name");
		return null;
	}

	@Override
	public List<ResourceLocation> getTextures(ItemStack stack, String key)
	{
		String name = getShaderName(stack);
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase("immersiveengineering:item");
			if(sCase!=null)
			{
				ShaderLayer[] layers = sCase.getLayers();
				ArrayList<ResourceLocation> list = new ArrayList<>(layers.length);
				for(ShaderLayer layer : layers)
					list.add(layer.getTexture());
				return list;
			}
		}
//		return Arrays.asList(new ResourceLocation("immersiveengineering:items/shader_0"));
		return Arrays.asList(new ResourceLocation("immersiveengineering:items/shader_0"), new ResourceLocation("immersiveengineering:items/shader_1"), new ResourceLocation("immersiveengineering:items/shader_2"));
	}
}