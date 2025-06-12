/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.items.ShaderItem;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns.BannerEntry;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IEShaders;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class Language extends LanguageProvider
{
	final ExistingFileHelper existingFileHelper;
	private final ResourceLocation baseLangFile;

	public Language(PackOutput output, ExistingFileHelper existingFileHelper, String locale)
	{
		super(output, Lib.MODID, locale);
		this.existingFileHelper = existingFileHelper;
		this.baseLangFile = ieLoc(locale);
	}

	@Override
	protected void addTranslations()
	{
		// Load our existing resource file
		try
		{
			final Gson gson = new Gson();
			final Resource resource = existingFileHelper.getResource(baseLangFile, PackType.CLIENT_RESOURCES, ".json", "lang_base");
			final BufferedReader reader = resource.openAsReader();
			JsonObject object = GsonHelper.fromJson(gson, reader, JsonObject.class);
			for(String key : object.keySet())
				add(key, object.get(key).getAsString());
		} catch(IOException e)
		{
			throw new RuntimeException("Failure to read base language file", e);
		}
		addBanners();
		addItems();
		addShaders();
		addSigns(WoodenDecoration.SIGN);
		addSigns(MetalDecoration.STEEL_SIGN);
		addSigns(MetalDecoration.ALU_SIGN);

		verify();
	}

	private void addItems()
	{
		addItem(Misc.SHADER_BAG.get(Rarity.COMMON), "Common Shader Grabbag");
		addItem(Misc.SHADER_BAG.get(Rarity.UNCOMMON), "Uncommon Shader Grabbag");
		addItem(Misc.SHADER_BAG.get(Rarity.RARE), "Rare Shader Grabbag");
		addItem(Misc.SHADER_BAG.get(Rarity.EPIC), "Epic Shader Grabbag");
		addItem(Misc.SHADER_BAG.get(Lib.RARITY_MASTERWORK.getValue()), "Masterwork Shader Grabbag");

		addBullets();
	}

	private void addShaders()
	{
		addShader(IEShaders.ROSEQUARTZ, "Rosequartz");
		addShader(IEShaders.ARGO, "Argo");
		addShader(IEShaders.SUNSTRIKE, "Sunstrike");
		addShader(IEShaders.LOCUS, "Locus");
		addShader(IEShaders.FELIX, "Felix");
		addShader(IEShaders.SHARKFACE, "Sharkface");
		addShader(IEShaders.DRAGONSBREATH, "Dragon's Breath");
		addShader(IEShaders.HAWK, "Hawk");
		addShader(IEShaders.EYAS, "Eyas");
		addShader(IEShaders.MAGNUM, "Magnum");
		addShader(IEShaders.FOX, "Fox");
		addShader(IEShaders.VAULTTEC, "Vault-Tec");
		addShader(IEShaders.SPONSOR, "Sponsor");
		addShader(IEShaders.MASSFUSION, "Mass Fusion");
		addShader(IEShaders.STORMFLOWER, "StormFlower");
		addShader(IEShaders.MILO, "Miló");
		addShader(IEShaders.TRIDENT, "Trident");
		addShader(IEShaders.CHLORIS, "Chloris");
		addShader(IEShaders.CRESCENTROSE, "Crescent Rose");
		addShader(IEShaders.QROW, "Qrow");
		addShader(IEShaders.LUSUSNATURAE, "Lusus Naturae");
		addShader(IEShaders.VANGUARD, "Vanguard");
		addShader(IEShaders.REGAL, "Regal");
		addShader(IEShaders.HARROWED, "Harrowed");
		addShader(IEShaders.TAKEN, "Taken");
		addShader(IEShaders.IKELOS, "IKELOS");
		addShader(IEShaders.ANGELSTHESIS, "Angel's Thesis");
		addShader(IEShaders.SUTHERLAND, "Sutherland");
		addShader(IEShaders.EXIA, "Exia");
		addShader(IEShaders.CRIMSONLOTUS, "Crimson Lotus");
		addShader(IEShaders.DOMINATOR, "Dominator");
		addShader(IEShaders.WARBIRD, "Warbird");
		addShader(IEShaders.MATRIX, "Matrix");
		addShader(IEShaders.TWILI, "Twili");
		addShader(IEShaders.USURPER, "Usurper");
		addShader(IEShaders.ANCIENT, "Ancient");
		addShader(IEShaders.GLACIS, "Glacis");
		addShader(IEShaders.PHOENIX, "Phoenix");
		addShader(IEShaders.RADIANT, "Radiant");
		addShader(IEShaders.HOLLOW, "Hollow");
		addShader(IEShaders.MICROSHARK, "Microshark");
		addShader(IEShaders.N7, "N7");
		addShader(IEShaders.NORMANDY, "Normandy");
		addShader(IEShaders.OMNITOOL, "OmniTool");
		addShader(IEShaders.KINDLED, "The Kindled");
		addShader(IEShaders.DARKFIRE, "Dark Fire");
		addShader(IEShaders.ERRUPTION, "Erruption");
		addShader(IEShaders.WAAAGH, "WAAAGH!");
		addShader(IEShaders.ASTARTES, "Astartes");
		addShader(IEShaders.NETHERFORGED, "Netherforged");
		addShader(IEShaders.TRANSPRIDE, "Break the Cis-tem");
		addShader(IEShaders.ENBYPRIDE, "NB");
		addShader(IEShaders.GAYPRIDE, "Boys love Boys");
		addShader(IEShaders.LESBIANPRIDE, "Girls love Girls");
		addShader(IEShaders.BIPRIDE, "Love them both");
		addShader(IEShaders.ACEPRIDE, "The Ace");
	}

	private void addShader(ShaderRegistryEntry shader, String translation)
	{
		add(ShaderItem.getShaderNameKey(shader.name), translation);
	}

	private void addBanners()
	{
		// Add banners with their coloration
		for(BannerEntry banner : IEBannerPatterns.ALL_BANNERS)
			for(var pattern : banner.patterns())
			{
				final String key = pattern.location().getPath();
				final String[] keyParts = key.split("_");
				String bannerName = Utils.toCamelCase(keyParts[0]);
				if(keyParts.length > 1)
					bannerName += " ("+Utils.toCamelCase(keyParts[1])+")";
				for(DyeColor dye : DyeColor.values())
				{
					String color = dye.getSerializedName();
					add("block.minecraft.banner.immersiveengineering."+key+"."+color, I18n.get("color.minecraft."+color)+" "+bannerName);
				}
			}
	}

	private void addSigns(IEBlocks.SignHolder holder)
	{
		String key = holder.baseName();
		String name = Pattern.compile("(?:^|_)([a-z])")
				.matcher(key)
				.replaceAll(m -> " "+m.group(1).toUpperCase()).trim();
		add("block.immersiveengineering."+key+"_sign", name+" Sign");
		add("block.immersiveengineering."+key+"_wall_sign", name+" Sign");
		add("block.immersiveengineering."+key+"_hanging_sign", name+" Hanging Sign");
		add("block.immersiveengineering."+key+"_wall_hanging_sign", name+" Hanging Sign");
	}

	private void addBullets()
	{
		addBullet(IEBullets.ARMOR_PIERCING, "Armor-Piercing Cartridge");
		addBullet(IEBullets.BUCKSHOT, "Buckshot Cartridge");
		addBullet(IEBullets.CASULL, "Casull Cartridge");
		addBullet(IEBullets.DRAGONS_BREATH, "Dragon's Breath Cartridge");
		addBullet(IEBullets.FIREWORK, "Firework Cartridge");
		addBullet(IEBullets.FLARE, "Flare Cartridge");
		addBullet(IEBullets.HIGH_EXPLOSIVE, "High-Explosive Cartridge");
		addBullet(IEBullets.HOMING, "Homing Cartridge");
		addBullet(IEBullets.POTION, "Phial Cartridge");
		addBullet(IEBullets.SILVER, "Silver Cartridge");
		addBullet(IEBullets.WOLFPACK, "Wolfpack Cartridge");
	}

	private void addBullet(ResourceLocation bullet, String name)
	{
		addItem(Weapons.BULLETS.get(BulletHandler.getBullet(bullet)), name);
	}

	private void verify()
	{
		final var mapData = Preconditions.checkNotNull(
				(Map<String, String>)ObfuscationReflectionHelper.getPrivateValue(LanguageProvider.class, this, "data")
		);
		final var failed = new MutableBoolean();
		for(var item : IEItems.REGISTER.getEntries())
			if(!item.getKey().location().getPath().startsWith("fake_icon"))
				assertMapped(mapData, failed, item.get().getDescriptionId(item.get().getDefaultInstance()));
		for(var shader : ShaderRegistry.shaderRegistry.keySet())
			assertMapped(mapData, failed, ShaderItem.getShaderNameKey(shader));
		if(failed.booleanValue())
			throw new RuntimeException("Missing expected translation keys!");
	}

	private void assertMapped(Map<String, String> mappedValues, MutableBoolean failed, String key)
	{
		if(!mappedValues.containsKey(key))
		{
			IELogger.logger.error("No mapping for {} found!", key);
			failed.setTrue();
		}
	}
}