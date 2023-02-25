/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.DynamicShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.shader.impl.ShaderCaseDrill;
import blusunrize.immersiveengineering.api.shader.impl.ShaderCaseMinecart;
import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.joml.Vector4f;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class IEShaders
{
	public static void commonConstruction()
	{
		//DEFAULT CUTOUTS
		//whitestripe
		setDefaultTextureBounds(ieLoc("item/revolvers/shaders/revolver_whitestripe"), 0, .75, .25, 1);
		setDefaultTextureBounds(ieLoc("item/shaders/drill_diesel_whitestripe"), 0, 17/64d, .5, 42/64d);

		//REGISTER SHADERS
		addShader("rosequartz", 0, Rarity.COMMON, 0xff412323, 0xffe6b4b4, 0xfff0cdcd, 0xffe6b4b4).setInfo(null, null, "rosequartz");
		addShader("argo", 2, Rarity.COMMON, 0xff2d2d2d, 0xffdcdcdc, 0xffdc7823, 0xffc8c8c8).setInfo(null, null, "argo");
		addShader("sunstrike", 5, Rarity.RARE, 0xff737373, 0xffcd6900, 0xb9d73a00, 0xb9d73a00).setInfo(null, null, "sunstrike");
		addShader("locus", 2, Rarity.COMMON, 0xff0a0a0a, 0xff4a4a4a, 0xff84964c, 0xff4a4a4a).setInfo("Mercenaries", "Red vs Blue", "locus");
		addShader("felix", 1, Rarity.COMMON, 0xff0a0a0a, 0xff4a4a4a, 0xfff08803, 0xff4a4a4a).setInfo("Mercenaries", "Red vs Blue", "felix");
		addShader("sharkface", 2, Rarity.UNCOMMON, 0xff0a0a0a, 0xff4a4a4a, 0xff910008, 0xff4a4a4a, "shark", true, 0xffffffff).setInfo("Mercenaries", "Red vs Blue", "sharkface");
		addShader("dragonsbreath", 1, Rarity.UNCOMMON, 0xff191919, 0xff333f2b, 0xff8a8a8a, 0xff8a8a8a, "shark", true, 0xffffffff).setInfo(null, "Destiny", "dragonsbreath");
		addShader("hawk", 3, Rarity.COMMON, 0xff67636b, 0xfff4eeeb, 0xff2d2d2d, 0xfff4eeeb).setInfo(null, "Destiny", "hawk");
		addShader("eyas", 3, Rarity.COMMON, 0xff52534e, 0xff7a2a08, 0xff2d2d2d, 0xfff4eeeb).setInfo(null, "Destiny", "eyas");
		addShader("magnum", 1, Rarity.COMMON, 0xff56382c, 0xffdcdcdc, 0xffa0a0a0, 0xffdcdcdc).setInfo(null, null, "magnum");
		addShader("fox", 2, Rarity.UNCOMMON, 0xff2d2d2d, 0xffd47e31, 0xffeaeaea, 0xffeaeaea).setInfo(null, null, "fox");
		addShader("vaulttec", 0, Rarity.COMMON, 0xff56382c, 0xff1a4785, 0xffc0aa50, 0xffaaaaaa).setInfo(null, "Fallout", "vaulttec");

		final ShaderRegistryEntry sponsor = addShader("sponsor", 0, Rarity.EPIC, 0xff191919, 0xfff71b24, 0xffffffff, 0xffaaaaaa, "sponsor", false, 0xffffffff).setInfo(null, "Fallout", "sponsor");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			((ShaderCaseMinecart)sponsor.getCase(ieLoc("minecart"))).mirrorSideForPass[2] = false;
			sponsor.getCase(ieLoc("revolver")).getLayers()[4].setTextureBounds(0, .75, .25, 1);
			sponsor.getCase(ieLoc("drill")).getLayers()[3].setTextureBounds(10/64d, 14/64d, 26/64d, 30/64d);
			sponsor.getCase(ieLoc("buzzsaw")).getLayers()[3].setTextureBounds(2/64d, 33/64d, 23/64d, 54/64d);
			sponsor.getCase(ieLoc("chemthrower")).getLayers()[3].setTextureBounds(6/64d, 32/64d, 22/64d, 48/64d);
			sponsor.getCase(ieLoc("shield")).getLayers()[2].setTextureBounds(0/32d, 7/32d, 14/32d, 23/32d).setCutoutBounds(.0625, 0, .9375, 1);
		});

		addShader("massfusion", 3, Rarity.RARE, 0xff6e5a37, 0xff394730, 0xff545454, 0xffaaaaaa, "fusion", true, 0xffffffff).setInfo(null, "Fallout", "massfusion");

		addShader("stormflower", 1, Rarity.COMMON, 0xff273427, 0xff286f30, 0xff4b9255, 0xff286f30).setInfo(null, "RWBY", "stormflower");
		addShader("milo", 2, Rarity.UNCOMMON, 0xff3b1b10, 0xff670004, 0xffce7e10, 0xff670004).setInfo(null, "RWBY", "milo");
		addShader("trident", 2, Rarity.UNCOMMON, 0xff515151, 0xffa8a8a8, 0xff29d3ff, 0xffafafaf).setInfo(null, "RWBY", "trident");
		addShader("chloris", 4, Rarity.RARE, 0xff38322a, 0xff38322a, 0xff88fabe, 0xffc8c8c8).setInfo(null, "RWBY", "chloris");
		addShader("crescentrose", 2, Rarity.COMMON, 0xff141414, 0xff910008, 0xff080808, 0xffa4a4a4).setInfo(null, "RWBY", "crescentrose");
		addShader("qrow", 2, Rarity.UNCOMMON, 0xff6d1c11, 0xffd8d7d0, 0xff313640, 0xff730008).setInfo(null, "RWBY", "qrow");
		final ShaderRegistryEntry lususnaturae = addShader("lususnaturae", 0, Rarity.EPIC, 0xff141919, 0xff141919, 0xffadb4bf, 0xffadb4bf).setInfo(null, "RWBY", "lususnaturae");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			addBlockScaledLayer(lususnaturae, "minecraft:block/destroy_stage_8", 0xbb940c0c);
			addLayer(lususnaturae, "1_4", 0xffadb4bf);
		});

		addShader("vanguard", 3, Rarity.UNCOMMON, 0xff373737, 0xff131b42, 0xffb86c14, 0xffdcdcdc).setInfo(null, "Destiny", "vanguard");
		addShader("regal", 4, Rarity.UNCOMMON, 0xffd8d4d1, 0xff431c1d, 0xffd8d4d1, 0xffd8d4d1).setInfo(null, "Destiny", "regal");
		addShader("harrowed", 4, Rarity.RARE, 0xff161321, 0xff431c1d, 0xff161321, 0xff161321).setInfo(null, "Destiny", "harrowed");
		addShader("taken", 5, Rarity.EPIC, 0xff111c26, 0xff111c26, 0xffbad7dd, 0xff111c26, null, false, 0xffffffff).setInfo(null, "Destiny", "taken");
		final ShaderRegistryEntry ikelos = addShader("ikelos", 2, Lib.RARITY_MASTERWORK, 0xff74665d, 0xff424348, 0xff424348, 0xff313131).setInfo(null, "Destiny", "ikelos");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			addDynamicLayer(ikelos, "circuit", 0xffefa117,
					(layer, superColour) -> ClientUtils.pulseRGBAlpha(superColour, 40, .15f, 1f),
					pre -> {
					},
					true
			);
			addLayer(ikelos, "1_4", 0xff5f646a);
			ikelos.setEffectFunction((world, shader, item, shaderType, pos, dir, scale) -> {
				ImmersiveEngineering.proxy.spawnFractalFX(world, pos.x, pos.y, pos.z, dir!=null?dir: new Vec3(0, 1, 0), scale, 2, null);
				ImmersiveEngineering.proxy.spawnFractalFX(world, pos.x, pos.y, pos.z, dir!=null?dir: new Vec3(0, 0, 1), scale, 2, null);
				ImmersiveEngineering.proxy.spawnFractalFX(world, pos.x, pos.y, pos.z, dir!=null?dir: new Vec3(1, 0, 0), scale, 2, null);
			});
		});

		addShader("angelsthesis", 2, Rarity.EPIC, 0xff1e1e1e, 0xff754697, 0xff77b93d, 0xff505050, null, false, 0xffffffff).setInfo("Mecha", "Neon Genesis Evangelion", "angelsthesis");
		addShader("sutherland", 0, Rarity.RARE, 0xff44404f, 0xff6b5eae, 0xff702739, 0xff44404f, "whitestripe", true, 0xff702034).setInfo("Mecha", "Code Geass", "sutherland");
		addShader("exia", 8, Rarity.RARE, 0xffb2220c, 0xff5571d2, 0xffece7e1, 0xffc0fdc7, "whitestripe", true, 0xffc09032).setInfo("Mecha", "Gundam 00", "exia");
		addShader("crimsonlotus", 3, Lib.RARITY_MASTERWORK, 0xffd83239, 0xffd83239, 0xff4e4f53, 0xff2ff177, "whitestripe", true, 0xfff4b951).setInfo("Mecha", "Gurren Lagann", "crimsonlotus");
		addShader("dominator", 1, Rarity.UNCOMMON, 0xff4c311f, 0xff2a2c36, 0xff5bfffb, 0xff2a2c36, "1_3", true, 0xff2a2c36).setInfo(null, "Psycho-Pass", "dominator");

		addShader("warbird", 7, Rarity.UNCOMMON, 0xff313640, 0xffd8d7d0, 0xffebac00, 0xffd8d7d0).setInfo(null, null, "warbird");
		addShader("matrix", 7, Rarity.RARE, 0xff053f3c, 0xffe1e1ff, 0xffd4ffff, 0xffffffff, "pipes", true, 0xff84ddd8).setInfo(null, null, "matrix");
		addShader("twili", 5, Rarity.EPIC, 0xff555d70, 0xff1a1e2b, 0xff222739, 0xff1db58e, "circuit", false, 0xff1db58e).setInfo(null, "The Legend of Zelda: Twilight Princess", "twili");
		addShader("usurper", 3, Rarity.EPIC, 0xff3e1e1e, 0xff5c6156, 0xff111010, 0xff737a6c, "circuit", false, 0xffca2f38).setInfo(null, "The Legend of Zelda: Twilight Princess", "usurper");
		final ShaderRegistryEntry ancient = addShader("ancient", 6, Lib.RARITY_MASTERWORK, 0xff9c3a2d, 0xff514848, 0xfff6ae4a, 0xff80fcf2).setInfo(null, "The Legend of Zelda: Breath of the Wild", "ancient");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			addDynamicLayer(ancient, "1_6", 0xff80fcf2,//0xaafaf307,
					(layer, superColour) -> ClientUtils.pulseRGBAlpha(superColour, 60, .05f, .5f),
					pre -> {
					},
					true
			);
			addLayer(ancient, "circuit", 0x99bc9377);
			((ShaderCaseDrill)ancient.getCase(ieLoc("drill"))).addHeadLayers(new ShaderLayer(ieLoc("item/drill_iron"), 0xff80fcf2));
		});

		addShader("glacis", 6, Rarity.RARE, 0xff499bc2, 0x3376d0f9, 0x33bdfffd, 0x33bdfffd).setInfo(null, null, "glacis");
		addShader("phoenix", 5, Rarity.RARE, 0xff750000, 0xffd00000, 0xffff7f00, 0xffff7f00).setInfo(null, null, "phoenix");

		addShader("radiant", 3, Rarity.UNCOMMON, 0xffa36eab, 0xfff8dbbc, 0xffc79ab1, 0xffd8d8e3, "pipes", true, 0xfff1c91e).setInfo(null, "Kingdom Hearts", "radiant");
		addShader("hollow", 4, Rarity.UNCOMMON, 0xff542d1c, 0xffeec5e5, 0xffcc8980, 0xffc4a1aa, "pipes", true, 0xffc49838).setInfo(null, "Kingdom Hearts", "hollow");

		addShader("microshark", 8, Rarity.RARE, 0xff775054, 0xfff7f6cf, 0xff936267, 0xff936267, "shark", true, 0xffffffff).setInfo(null, "Terraria", "microshark");

		addShader("n7", 2, Rarity.EPIC, 0xff13171b, 0xff524d4a, 0xffe01919, 0xff8a8684, "whitestripe", false, 0xffffffff).setInfo(null, "Mass Effect", "n7");
		addShader("normandy", 8, Rarity.RARE, 0xffffffff, 0xff1a1a1a, 0xffffffff, 0xffffffff, "whitestripe", true, 0xff35447e).setInfo(null, "Mass Effect", "normandy");
		addShader("omnitool", 2, Rarity.RARE, 0x40ff952c, 0x30ff952c, 0x40ff952c, 0x20ff952c).setInfo(null, "Mass Effect", "omnitool");

		final ShaderRegistryEntry kindled = addShader("kindled", 5, Rarity.EPIC, 0xff2b160b, 0xff3a3a3a, 0x80bf541f, 0xff4f4f4f).setInfo(null, "Dark Souls", "kindled");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addBlockScaledLayer(kindled, "minecraft:block/fire_0", 0x80ffffff));

		final ShaderRegistryEntry darkfire = addShader("darkfire", 5, Rarity.EPIC, 0xff1e131b, 0xff211633, 0xff330812, 0xff412965).setInfo(null, "Kingdom Hearts", "darkfire");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addBlockScaledLayer(darkfire, "immersiveengineering:block/shaders/greyscale_fire", 0xff9e83eb));

		final ShaderRegistryEntry erruption = addShader("erruption", 5, Rarity.RARE, 0xff2b160b, 0xff58432f, 0x80bf301f, 0xff58432f).setInfo(null, null, "erruption");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addBlockScaledLayer(erruption, "minecraft:block/destroy_stage_8", 0xffff6314));

		addShader("waaagh", 5, Rarity.RARE, 0xff0f0f0f, 0xffdea712, 0xffc15b09, 0xff2f2f2f, "1_7", true, 0xff2f2f2f).setInfo(null, "Warhammer 40k", "waaagh");

		final ShaderRegistryEntry astartes = addShader("astartes", 3, Rarity.RARE, 0xff212429, 0xff8e3334, 0xff7b7e87, 0xff7b7e87).setInfo(null, "Warhammer 40k", "astartes");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addLayer(astartes, "1_8", 0xff8e3334));

		final ShaderRegistryEntry netherforged = addShader("netherforged", 0, Rarity.EPIC, 0xff575046, 0xff323c2c, 0x80bf541f, 0xff4f4f4f).setInfo(null, null, "netherforged");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			addBlockScaledLayer(netherforged, "minecraft:block/magma", 0xffffffff);
			netherforged.getCase(ieLoc("revolver")).getLayers()[4].setTextureBounds(0, .6875, .3125, 1);
			addLayer(netherforged, "1_8", 0xff323c2c);
			netherforged.getCase(ieLoc("revolver")).getLayers()[5].setTextureBounds(0, .8125, 1, 1).setCutoutBounds(0, 0, 1, .1875);
			addLayer(netherforged, "1_0", 0xff323c2c);
		});

		addShader("transpride", 2, Rarity.EPIC, 0xfff5abb9, 0xff5bcffa, 0xfff5abb9, 0xffffffff, "whitestripe", true, 0xffffffff).setInfo("Pride", "Pride Flags", "transpride");
		final ShaderRegistryEntry enbypride = addShader("enbypride", 8, Rarity.EPIC, 0xff282828, 0xff282828, 0xff9d59d2, 0xffffffff, "1_2", true, 0xfffcf431).setInfo("Pride", "Pride Flags", "enbypride");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addLayer(enbypride, "whitestripe", 0xffffffff));
		final ShaderRegistryEntry gaypride = addShader("gaypride", 8, Rarity.EPIC, 0xff5049cc, 0xff26ceaa, 0xff7bade2, 0xffffff, "whitestripe", true, 0xffffffff).setInfo("Pride", "Pride Flags", "gaypride");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addLayer(gaypride, "1_3", 0xff98e8e6));
		final ShaderRegistryEntry lesbianpride = addShader("lesbianpride", 8, Rarity.EPIC, 0xffa50062, 0xffd462a5, 0xffff9b55, 0xffa50062, "1_2", true, 0xffd62900).setInfo("Pride", "Pride Flags", "lesbianpride");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addLayer(lesbianpride, "whitestripe", 0xffffffff));
		addShader("bipride", 8, Rarity.EPIC, 0xff0035a9, 0xff9c4e97, 0xffd70071, 0xff0035a9).setInfo("Pride", "Pride Flags", "bipride");
		final ShaderRegistryEntry acepride = addShader("acepride", 8, Rarity.EPIC, 0xff510053, 0xffffffff, 0xff6c6c6c, 0xff6c6c6c, "1_2", true, 0xff000000).setInfo("Pride", "Pride Flags", "acepride");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> addLayer(acepride, "whitestripe", 0xff510053));
	}

	public static ShaderRegistryEntry addShader(String name, int overlayType, Rarity rarity, int colourBackground, int colourPrimary, int colourSecondary, int colourBlade)
	{
		return addShader(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, colourBlade, null, true, 0xffffffff);
	}

	public static ShaderRegistryEntry addShader(String name, int overlayType, Rarity rarity, int colourBackground, int colourPrimary, int colourSecondary, int colourBlade, String additionalTexture, boolean loot, int colourOverlay)
	{
		return ShaderRegistry.registerShader(ieLoc(name), Integer.toString(overlayType), rarity, colourPrimary, colourSecondary, colourBackground, colourBlade, additionalTexture, colourOverlay, loot, true);
	}

	private static void addBlockScaledLayer(ShaderRegistryEntry entry, String texture, int colour)
	{
		entry.getCase(ieLoc("revolver")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0, .8125, .25, 1));
		entry.getCase(ieLoc("drill")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(10/64d, 14/64d, 26/64d, 30/64d).setCutoutBounds(.1875f, 0, .8125, .75f));
		entry.getCase(ieLoc("buzzsaw")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0/64f, 44/64f, 26/64f, 54/64f).setCutoutBounds(0, .615, 1, 1));
		entry.getCase(ieLoc("chemthrower")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(6/64d, 40/64d, 22/64d, 48/64d).setCutoutBounds(0, 0, 1, .5));
		entry.getCase(ieLoc("railgun")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(55/64d, 6/64d, 1, 16/64d).setCutoutBounds(.25, .125, .75, .6875));
		entry.getCase(ieLoc("shield")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0/32f, 6/32f, 14/32f, 23/32f).setCutoutBounds(.0625, 0, .9375, 1));
		entry.getCase(ieLoc("balloon")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0, .125, .75, .625).setCutoutBounds(.125, 0, .875, .5));
		entry.getCase(ieLoc("banner")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(1/64d, 23/64d, 21/64d, 43/64d));
	}

	private static void addLayer(ShaderRegistryEntry entry, String texture, int colour)
	{
		entry.getCase(ieLoc("revolver")).addLayers(new ShaderLayer(ieLoc("item/revolvers/shaders/revolver_"+texture), colour));
		entry.getCase(ieLoc("drill")).addLayers(new ShaderLayer(ieLoc("item/shaders/drill_diesel_"+texture), colour));
		entry.getCase(ieLoc("buzzsaw")).addLayers(new ShaderLayer(ieLoc("item/shaders/buzzsaw_diesel_"+texture), colour));
		entry.getCase(ieLoc("chemthrower")).addLayers(new ShaderLayer(ieLoc("item/shaders/chemthrower_"+texture), colour));
		entry.getCase(ieLoc("railgun")).addLayers(new ShaderLayer(ieLoc("item/shaders/railgun_"+texture), colour));
		entry.getCase(ieLoc("shield")).addLayers(new ShaderLayer(ieLoc("item/shaders/shield_"+texture), colour));
		entry.getCase(ieLoc("minecart")).addLayers(new ShaderLayer(ieLoc("textures/models/shaders/minecart_"+texture+".png"), colour));
		entry.getCase(ieLoc("balloon")).addLayers(new ShaderLayer(ieLoc("block/shaders/balloon_"+texture), colour));
		entry.getCase(ieLoc("banner")).addLayers(new ShaderLayer(ieLoc("block/shaders/banner_"+texture), colour));
	}

	private static void addDynamicLayer(ShaderRegistryEntry entry, String texture, int colour, final BiFunction<ShaderLayer, Vector4f, Vector4f> func_getColour, final Consumer<Boolean> func_modifyRender)
	{
		addDynamicLayer(entry, texture, colour, func_getColour, func_modifyRender, false);
	}

	private static void addDynamicLayer(ShaderRegistryEntry entry, String texture, int colour, final BiFunction<ShaderLayer, Vector4f, Vector4f> func_getColour, final Consumer<Boolean> func_modifyRender, boolean translucent)
	{
		entry.getCase(ieLoc("revolver")).addLayers(new InternalDynamicShaderLayer(ieLoc("item/revolvers/shaders/revolver_"+texture), colour, func_getColour, func_modifyRender, translucent));
		entry.getCase(ieLoc("drill")).addLayers(new InternalDynamicShaderLayer(ieLoc("item/shaders/drill_diesel_"+texture), colour, func_getColour, func_modifyRender, translucent));
		entry.getCase(ieLoc("buzzsaw")).addLayers(new InternalDynamicShaderLayer(ieLoc("item/shaders/buzzsaw_diesel_"+texture), colour, func_getColour, func_modifyRender, translucent));
		entry.getCase(ieLoc("chemthrower")).addLayers(new InternalDynamicShaderLayer(ieLoc("item/shaders/chemthrower_"+texture), colour, func_getColour, func_modifyRender, translucent));
		entry.getCase(ieLoc("railgun")).addLayers(new InternalDynamicShaderLayer(ieLoc("item/shaders/railgun_"+texture), colour, func_getColour, func_modifyRender, translucent));
		entry.getCase(ieLoc("shield")).addLayers(new InternalDynamicShaderLayer(ieLoc("item/shaders/shield_"+texture), colour, func_getColour, func_modifyRender, translucent));
		entry.getCase(ieLoc("minecart")).addLayers(new InternalDynamicShaderLayer(ieLoc("textures/models/shaders/minecart_"+texture+".png"), colour, func_getColour, func_modifyRender, translucent));
		entry.getCase(ieLoc("balloon")).addLayers(new InternalDynamicShaderLayer(ieLoc("block/shaders/balloon_"+texture), colour, func_getColour, func_modifyRender, translucent));
		entry.getCase(ieLoc("banner")).addLayers(new InternalDynamicShaderLayer(ieLoc("block/shaders/banner_"+texture), colour, func_getColour, func_modifyRender, translucent));
	}

	public static void setDefaultTextureBounds(ResourceLocation rl, double... bounds)
	{
		ShaderRegistry.defaultLayerBounds.put(rl, bounds);
	}

	private static class InternalDynamicShaderLayer extends DynamicShaderLayer
	{
		private final BiFunction<ShaderLayer, Vector4f, Vector4f> func_getColour;
		// TODO direct render layer?
		private final Consumer<Boolean> func_modifyRender;
		private final boolean translucent;

		public InternalDynamicShaderLayer(ResourceLocation texture, int colour, BiFunction<ShaderLayer, Vector4f, Vector4f> func_getColour, Consumer<Boolean> func_modifyRender, boolean translucent)
		{
			super(texture, colour);
			this.func_getColour = func_getColour;
			this.func_modifyRender = func_modifyRender;
			this.translucent = translucent;
		}

		@Override
		public Vector4f getColor()
		{
			if(func_getColour!=null)
				return func_getColour.apply(this, super.getColor());
			return super.getColor();
		}

		@Override
		public RenderType getRenderType(RenderType base)
		{
			if(func_modifyRender==null)
				return base;
			else
				return new RenderType(
						//TODO better name?
						"shader_"+base.toString()+func_modifyRender,
						base.format(),
						base.mode(),
						256,
						base.affectsCrumbling(),
						true,
						() -> {
							base.setupRenderState();
							func_modifyRender.accept(true);
						},
						() -> {
							func_modifyRender.accept(false);
							base.clearRenderState();
						}
				)
				{
				};
		}

		@Override
		public boolean isTranslucent()
		{
			return translucent;
		}
	}
}